package com.liberty.discovoadorntk.notifications.fcm


import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.Query
import com.liberty.discovoadorntk.core.data.AlarmMessageInfo
import com.liberty.discovoadorntk.core.data.GroupInfo
import com.liberty.discovoadorntk.core.data.UserInfo
import com.liberty.discovoadorntk.di.DeviceIdQualifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers


//Interação com o Firestore (grupos, membros, etc.)
//Gerenciamento de grupos no Firestore (criar, entrar, sair).
//Buscar e observar membros de grupos no Firestore.
//Enviar notificações para membros do grupo.
class FirebaseManager @Inject constructor(
    @DeviceIdQualifier private val deviceId: String
) {

    private val firestoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val devicesCollection = firestoreInstance.collection("devices")
    private val groupsCollection = firestoreInstance.collection("groups")
    private val userGroupsCollection = firestoreInstance.collection("userGroups")
    private val userNameCollection = firestoreInstance.collection("userNames")


    inner class Group {
        //--- criar grupo com senha ------------------------------------------------------------------------
        //throws GroupAlreadyExists se nome duplicado
        //throws NoNetwork se sem conexão
        //throws FirestoreError pra qualquer outro erro
        suspend fun createGroupWithPassword(
            userName: String,
            groupName: String,
            password: String
        ): String = withContext(Dispatchers.IO) {
            try {
                // 1) verifica duplicata
                val snapshot = groupsCollection
                    .whereEqualTo("groupName", groupName)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    throw FirebaseManagerException.GroupAlreadyExist(groupName)
                }

                // 2) gera ID e hash
                val newGroupRef = groupsCollection.document()
                val generatedId = newGroupRef.id
                val hash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(password.toByteArray())
                    .joinToString("") { "%02x".format(it) }

                // 3) grava no Firestore
                newGroupRef.set(
                    mapOf(
                        "groupName" to groupName,
                        "passwordHash" to hash,
                        "members" to mapOf(deviceId to userName)
                    )
                ).await()

                // 4) atualiza userGroups
                UserGroups().addUserToUserGroups(generatedId, groupName)

                // 5) sucesso
                generatedId

            } catch (e: FirebaseManagerException) {
                // repropaga
                throw e
            } catch (e: FirebaseFirestoreException) {
                // detecta falha de rede
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    throw FirebaseManagerException.NoNetwork
                } else {
                    throw FirebaseManagerException.FirestoreError(e)
                }
            } catch (e: Exception) {
                // pode ser sem internet ou qualquer coisa inesperada
                throw FirebaseManagerException.FirestoreError(e)
            }
        }

        //--- entrar no grupo ------------------------------------------------------------------------------
        //return docId do grupo caso sucesso.
        //throws GroupNotFoundOrInvalidPassword se não achar ou senha inválida.
        //throws AlreadyMember se já for membro.
        //throws NoNetwork se sem internet.
        //throws FirestoreError para qualquer outro erro do Firestore.
        suspend fun joinGroup(
            groupName: String,
            password: String,
            userName: String
        ): String = withContext(Dispatchers.IO) {
            try {
                // 1) Busca grupo pelo nome
                val query = groupsCollection
                    .whereEqualTo("groupName", groupName)
                    .get()
                    .await()

                if (query.isEmpty) {
                    throw FirebaseManagerException.GroupNotFoundOrInvalidPassword()
                }

                val doc = query.documents.first()
                val docId = doc.id

                // 2) Verifica hash da senha
                val storedHash = doc.getString("passwordHash") ?: ""
                val provideHash = MessageDigest
                    .getInstance("SHA-256")
                    .digest(password.toByteArray())
                    .joinToString("") { "%02x".format(it) }

                if (storedHash != provideHash) {
                    throw FirebaseManagerException.GroupNotFoundOrInvalidPassword()
                }

                // 3) Checa se já é membro
                val members = (doc.get("members") as? Map<String, String>) ?: emptyMap()
                if (members.containsKey(deviceId)) {
                    throw FirebaseManagerException.AlreadyMember(groupName)
                }

                // 4) Atualiza lista de membros
                val updated = members.toMutableMap().apply {
                    put(deviceId, userName)
                }
                groupsCollection
                    .document(docId)
                    .update("members", updated)
                    .await()

                // 5) Atualiza userGroups
                UserGroups().addUserToUserGroups(groupId = docId, groupName = groupName)

                docId

            } catch (e: FirebaseFirestoreException) {
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    throw FirebaseManagerException.NoNetwork
                } else {
                    throw FirebaseManagerException.FirestoreError(e)
                }
            } catch (e: FirebaseManagerException) {
                // repropaga nossas exceções tipadas
                throw e
            } catch (e: Exception) {
                throw FirebaseManagerException.FirestoreError(e)
            }
        }

        //--- Remove o usuário do mapa de membros em "groups/{groupId}" ------------------------------------
        //--- e também do array "groups" em "userGroups/{deviceId}" ----------------------------------------
        //throws NoNetwork se não houver internet.
        //throws FirestoreError para qualquer falha de batch.commit().
        //return Unit em caso de sucesso.
        suspend fun leaveGroupCompletely(
            groupId: String,
            groupName: String
        ): Unit = withContext(Dispatchers.IO) {
            try {
                // 1) Prepare batch
                val batch = firestoreInstance.batch()

                // remove do mapa "members"
                val groupRef = groupsCollection.document(groupId)
                batch.update(groupRef, "members.$deviceId", FieldValue.delete())

                // remove do array em userGroups
                val userGroupsRef = userGroupsCollection.document(deviceId)
                val entry = mapOf(
                    "groupId" to groupId,
                    "groupName" to groupName
                )
                batch.update(userGroupsRef, "groups", FieldValue.arrayRemove(entry))
                // 2) Commit e await()
                batch.commit().await()

            } catch (e: FirebaseFirestoreException) {
                // erro de rede ou Firestore
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    throw FirebaseManagerException.NoNetwork
                } else {
                    throw FirebaseManagerException.FirestoreError(e)
                }
            } catch (e: Exception) {
                // qualquer outra falha inesperada
                throw FirebaseManagerException.FirestoreError(e)
            }
        }

        //--- buscar e observa membros ---------------------------------------------------------------------
        //Observa o mapa `members` em `groups/{groupId}`.
        //Emite uma lista de UserInfo sempre que `members` mudar.
        //Fecha o fluxo com FirestoreError em caso de falha.
        fun observeGroupMembers(groupId: String): Flow<List<UserInfo>> =
            callbackFlow {
                // 1) Registra o listener imediatamente
                val registration = groupsCollection
                    .document(groupId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            // Fecha o flow com exceção
                            close(FirebaseManagerException.FirestoreError(error))
                            return@addSnapshotListener
                        }
                        val membersMap = snapshot
                            ?.get("members") as? Map<String, String>
                            ?: emptyMap()
                        val members = membersMap
                            .map { (id, name) -> UserInfo(id, name) }
                        trySend(members).isSuccess
                    }
                // 2) Quando o collector cancelar, remove o listener
                awaitClose { registration.remove() }
            }
                // 3) E só aqui fora você empurra tudo para IO
                .flowOn(Dispatchers.IO)


    }


    inner class UserGroups {
        //--- criar nova coleção 'userGroups' e add o usuário ----------------------------------------------
        suspend fun addUserToUserGroups(groupId: String, groupName: String) {
            val entry = mapOf(
                "groupId" to groupId,
                "groupName" to groupName
            )
            try {
                userGroupsCollection
                    .document(deviceId)
                    .set(mapOf("groups" to FieldValue.arrayUnion(entry)), SetOptions.merge())
                    .await()  // extensão kotlinx-coroutines-play-services
            } catch (e: FirebaseFirestoreException) {
                throw FirebaseManagerException.UserGroupsUpdateError(e)
            } catch (e: Exception) {
                throw FirebaseManagerException.UserGroupsUpdateError(e)
            }
        }

        //--- observa os grupos que o usuário faz parte ----------------------------------------------------
        fun observeUserGroups(): Flow<List<GroupInfo>> = callbackFlow {
            val registration = userGroupsCollection
                .document(deviceId)
                .addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
                    if (error != null) {
                        // Fecha o flow com exceção
                        close(FirebaseManagerException.FirestoreError(error))
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val raw = snapshot.get("groups") as? List<Map<String, Any>> ?: emptyList()
                        val groups = raw.mapNotNull { entry ->
                            val id = entry["groupId"] as? String
                            val name = entry["groupName"] as? String
                            if (id != null && name != null) GroupInfo(id, name) else null
                        }
                        // Se for cache (pendingWrites), podemos querer emitir diferente,
                        // mas aqui só encaminhamos a mesma lista — a ViewModel distingue abaixo.

                        // mesmo comportamento: se for cache, hasPendingWrites=true, mas a ViewModel
                        // não precisará distinguir; ela só recebe duas emissões, e a última é a do servidor
                        trySend(groups).isSuccess
                    }
                }
            awaitClose { registration.remove() }
        }.flowOn(Dispatchers.IO)
    }


    inner class Message {
        //--- Salva a mensagem em groups/{groupId}/messages/{message.id} -----------------------------------  ******
        //Salva a mensagem em groups/{groupId}/messages/{message.id}.
        //- lança FirebaseManagerException.SaveAlarmMessageError em caso de falha.
        suspend fun saveAlarmMessage(
            groupId: String,
            messageInfo: AlarmMessageInfo
        ): Unit = withContext(Dispatchers.IO) {
            try {
                groupsCollection
                    .document(groupId)
                    .collection("messages")
                    .document(messageInfo.id)
                    .set(messageInfo)
                    .await()  // extensão do kotlinx-coroutines-play-services
            } catch (e: Exception) {
                // qualquer erro de rede ou permissão cai aqui
                throw FirebaseManagerException.SaveAlarmMessageError(e)
            }
        }

        //--- Observa em tempo real todos os AlarmMessageInfo que estão ------------------------------------
//--- em groups/{groupId}/messages ordenados por timestamp decrescente -----------------------------
        //Fecha o flow com FirebaseManagerException.FirestoreError em caso de erro.
        fun observeAlarmMessages(groupId: String): Flow<List<AlarmMessageInfo>> =
            callbackFlow {
                // 1) registra o listener
                val registration = groupsCollection
                    .document(groupId)
                    .collection("messages")
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .addSnapshotListener { snaps, error ->
                        if (error != null) {
                            // fecha o flow com exceção
                            close(FirebaseManagerException.FirestoreError(error))
                            return@addSnapshotListener
                        }
                        // converte para modelo
                        val list = snaps
                            ?.documents
                            ?.mapNotNull { it.toObject(AlarmMessageInfo::class.java) }
                            .orEmpty()

                        trySend(list).isSuccess
                    }
                // 2) quando o collector cancelar, remove o listener
                awaitClose { registration.remove() }
            }
                // 3) o trabalho de callback roda em IO
                .flowOn(Dispatchers.IO)
    }


    inner class Token {
        //--- Atualiza o token FCM, recuperando-o do FirebaseMessaging e salvando no Firestore -------------
        //throws FirebaseManagerException.FirestoreError se falhar ao salvar.
        suspend fun updateFCMToken() = withContext(Dispatchers.IO) {
            try {
                // await() vem de kotlinx-coroutines-play-services
                val token = FirebaseMessaging.getInstance().token.await()
                saveTokenToFirestore(token)
            } catch (e: Exception) {
                // qualquer outro erro
                throw FirebaseManagerException.FirestoreError(e)
            }
        }

        //--- Persiste o token FCM no Firestore ------------------------------------------------------------
        //throws FirebaseManagerException.FirestoreError para qualquer outro erro.
        suspend fun saveTokenToFirestore(token: String): Unit = withContext(Dispatchers.IO) {
            try {
                //se manter a ordem de inserção não importa, usar hashMapOf é mais otimizado que o mutableMapOf
                val userTokenData = hashMapOf("fcm_token" to token)
                devicesCollection.document(deviceId)
                    .set(userTokenData, SetOptions.merge())
                    .await()  // do kotlinx-coroutines-play-services
            } catch (e: Exception) {
                throw FirebaseManagerException.FirestoreError(e)
            }
        }

        //--- Busca os tokens FCM de todos os membros (exceto o próprio deviceId) e os retorna -------------
        // throws FirebaseManagerException.FirestoreError para qualquer outro erro do Firestore.
        // throws FirebaseManagerException.FirestoreError("No tokens found") se não houver tokens.
        suspend fun fetchGroupMemberTokens(groupId: String): List<String> =
            withContext(Dispatchers.IO) {
                try {
                    // 1) Lê o documento do grupo
                    val document = groupsCollection
                        .document(groupId)
                        .get()
                        .await()  // exige kotlinx-coroutines-play-services

                    if (!document.exists()) {
                        throw FirebaseManagerException.FirestoreError(Exception("Group $groupId does not exist"))
                    }

                    // 2) Extrai o mapa deviceId → userName
                    val membersMap = (document.get("members") as? Map<String, String>) ?: emptyMap()

                    // 3) Para cada membro (exceto eu), busca o token FCM
                    val tokens = membersMap.keys
                        .filter { it != deviceId }
                        .mapNotNull { memberId ->
                            try {
                                devicesCollection
                                    .document(memberId)
                                    .get()
                                    .await()
                                    .getString("fcm_token")
                            } catch (e: FirebaseFirestoreException) {
                                null  // ignora falha pontual
                            }
                        }

                    // 4) Se não encontrou nenhum token, dispara erro
                    if (tokens.isEmpty()) {
                        throw FirebaseManagerException.FetchTokensError(
                            Exception("No tokens found for group $groupId")
                        )
                    }

                    tokens

                } catch (e: FirebaseManagerException) {
                    throw e
                } catch (e: Exception) {
                    throw FirebaseManagerException.FetchTokensError(e)
                }
            }

        //--- Retorna pares (memberId, token) em vez de apenas tokens
        suspend fun fetchGroupMemberTokensWithIds(groupId: String): List<Pair<String, String>> =
            withContext(Dispatchers.IO) {
                val document = groupsCollection.document(groupId).get().await()
                if (!document.exists()) {
                    throw FirebaseManagerException.FirestoreError(Exception("Group $groupId does not exist"))
                }
                val membersMap = (document.get("members") as? Map<String, String>) ?: emptyMap()

                membersMap.keys
                    .filter { it != deviceId } // ignora o próprio
                    .mapNotNull { memberId ->
                        try {
                            val tk = devicesCollection.document(memberId).get().await()
                                .getString("fcm_token")
                            if (tk.isNullOrBlank()) null else (memberId to tk)
                        } catch (_: Exception) {
                            null
                        }
                    }
                    .also { pairs ->
                        if (pairs.isEmpty()) {
                            throw FirebaseManagerException.FetchTokensError(Exception("No tokens found for group $groupId"))
                        }
                    }
            }

        //--- Limpa o campo fcm_token do device no Firestore
        suspend fun clearDeviceTokenById(memberId: String) = withContext(Dispatchers.IO) {
            try {
                devicesCollection.document(memberId)
                    .update("fcm_token", com.google.firebase.firestore.FieldValue.delete())
                    .await()
            } catch (_: Exception) {
                // silencioso: se falhar, não quebra o fluxo
            }
        }

    }


    inner class UserName {
        //--- Verifica e registra no Firestore, doc userNames/{userName}, o userName + deviceId ------------
        suspend fun registerUserName(userName: String): Unit = withContext(Dispatchers.IO) {
            try {
                val userNameDoc = userNameCollection.document(userName)
                firestoreInstance.runTransaction { transaction ->
                    if (transaction.get(userNameDoc).exists()) {
                        // já existe: aborta
                        throw FirebaseFirestoreException(
                            "Nome '$userName' já está em uso",
                            FirebaseFirestoreException.Code.ABORTED
                        )
                    }
                    // salva o deviceId
                    transaction.set(userNameDoc, mapOf("deviceId" to deviceId))
                }.await()
            } catch (e: FirebaseFirestoreException) {
                when (e.code) {
                    FirebaseFirestoreException.Code.ABORTED ->
                        throw FirebaseManagerException.UserNameAlreadyTaken()

                    FirebaseFirestoreException.Code.UNAVAILABLE ->
                        throw FirebaseManagerException.NoNetwork

                    else ->
                        throw FirebaseManagerException.UserNameRegistrationFailed(e)
                }
            } catch (e: Exception) {
                throw FirebaseManagerException.FirestoreError(e)
            }
        }


        //--- Busca no firestore em userNames/{userName} onde deviceId == this.deviceId. -------------------
        //return o userName, ou null se não existir.
        //throws FirebaseManagerException.NoNetwork se não houver conexão.
        //throws FirebaseManagerException.FirestoreError para qualquer outro erro.
        suspend fun fetchUserNameByDeviceId(): String? = withContext(Dispatchers.IO) {
            try {
                val snap = userNameCollection
                    .whereEqualTo("deviceId", deviceId)
                    .get()
                    .await()

                if (snap.isEmpty) {
                    null
                } else {
                    // o ID do documento é o próprio userName
                    snap.documents.first().id
                }

            } catch (e: FirebaseFirestoreException) {
                // falha de rede?
                if (e.code == FirebaseFirestoreException.Code.UNAVAILABLE) {
                    throw FirebaseManagerException.NoNetwork
                } else {
                    throw FirebaseManagerException.FirestoreError(e)
                }
            } catch (e: Exception) {
                throw FirebaseManagerException.FirestoreError(e)
            }
        }
    }

}