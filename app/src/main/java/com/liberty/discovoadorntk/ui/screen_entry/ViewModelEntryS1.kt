package com.liberty.discovoadorntk.ui.screen_entry

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.liberty.discovoadorntk.core.data.DataStoreKey
import com.liberty.discovoadorntk.core.data.GroupInfo
import com.liberty.discovoadorntk.core.utils.ConnectivityObserver
import com.liberty.discovoadorntk.core.utils.stopAlarmService
import com.liberty.discovoadorntk.data.local.entity.GroupEntity
import com.liberty.discovoadorntk.data.repository.DataStoreRepository
import com.liberty.discovoadorntk.data.repository.GroupLocalRepository
import com.liberty.discovoadorntk.notifications.fcm.FirebaseManager
import com.liberty.discovoadorntk.notifications.fcm.FirebaseManagerException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@HiltViewModel
class ViewModelEntryS1 @Inject constructor(
    private val dataStoreRepo: DataStoreRepository,
    private val firebaseManager: FirebaseManager,
    private val application: Application,
    private val groupLocalRepo: GroupLocalRepository,
    private val connectivityObserver: ConnectivityObserver,
) : AndroidViewModel(application) {

    private val _entryScreenState = MutableStateFlow(EntryUiState())
    val entryScreenState = _entryScreenState.asStateFlow()

    private val _effects = Channel<EntryEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private var userGroupsJob: Job? = null

    private var remoteFetchAttempted = false

    init {
        // 0) Observa conectividade e dispara fetch ao reconectar
        viewModelScope.launch {
            var prevNetStatus: Boolean? = null
            connectivityObserver.observe().distinctUntilChanged().collect { online ->
                _entryScreenState.update {
                    it.copy(isOnline = online)
                }

                prevNetStatus?.let { wasOnline ->
                    if (wasOnline && !online) _effects.send(
                        EntryEffect.Snackbar("No internet connection", SnackbarKind.INFO)
                    )
                }
                prevNetStatus = online

                // se conectou e ainda não buscou + sem nome local, tenta buscar
                val currentName = _entryScreenState.value.userName
                if (online && currentName.isBlank() && !remoteFetchAttempted) {
                    remoteFetchAttempted = true
                    attemptRemoteFetch()
                }
            }
        }

        // 1) carrega nome local de forma bloqueante
        val localName = runBlocking {
            dataStoreRepo
                .getStringDS(DataStoreKey.USER_NAME_STRING)
                .firstOrNull()
                .orEmpty()
        }

        if (localName.isNotBlank()) {
            // — caso 1 —
            onUserNameReady(localName)
            remoteFetchAttempted = true
        } else {

            observeLocalGroups()

            // sem nome local: checa online **sincronamente**
            viewModelScope.launch {
                val onlineNow = connectivityObserver.observe().first()
                if (onlineNow) {
                    // — caso 3 — aguardamos o callback de fetch remoto
                    _entryScreenState.update {
                        it.copy(isInitializing = true, userName = "")
                    }
                    remoteFetchAttempted = true
                    attemptRemoteFetch()
                } else {
                    // — caso 2 — sem net e sem nome → libera a tela sem Loading
                    _entryScreenState.update {
                        it.copy(isInitializing = false, userName = "")
                    }
                }
            }
        }
    }

    private fun attemptRemoteFetch() {
        viewModelScope.launch {
            try {
                val fetched = firebaseManager.UserName()
                    .fetchUserNameByDeviceId()  // agora suspending

                if (!fetched.isNullOrBlank()) {
                    // 1) salva e dispara fluxo normal
                    saveUserNameDS(fetched)
                    onUserNameReady(fetched)
                    _entryScreenState.update { it.copy(isNameLocked = false) }

                } else {
                    // 2) não havia nome salvo no servidor
                    _entryScreenState.update {
                        it.copy(isInitializing = false, isNameLocked = false)
                    }
                }
            } catch (e: FirebaseManagerException.FirestoreError) {
                // erro no servidor
                snack(e.message ?: "Server error", SnackbarKind.ERROR)
                _entryScreenState.update {
                    it.copy(isInitializing = false, isNameLocked = false)
                }
            }
        }
    }

    fun handleEvent(events: EntryUiEvent) {
        when (events) {
            is EntryUiEvent.ShowInsertNameDialog -> setDialog(DialogStateEntry.InsertName)

            is EntryUiEvent.ShowJoinGroupDialog -> setDialog(DialogStateEntry.JoinGroup)

            is EntryUiEvent.ShowCreateGroupDialog -> setDialog(DialogStateEntry.CreateGroup)

            is EntryUiEvent.ShowLeaveGroupDialog -> {
                _entryScreenState.update {
                    it.copy(
                        selectedGroup = events.groupInfo
                    )
                }
                setDialog(DialogStateEntry.LeaveGroup(events.groupInfo))
            }

            is EntryUiEvent.CancelAndClearDialog -> {
                _entryScreenState.update {
                    it.copy(
                        dialog = DialogStateEntry.None,
                        nameError = InputErrorEntry.None,
                        passwordError = InputErrorEntry.None
                    )
                }
            }

            is EntryUiEvent.StopAlarm -> {
                stopAlarmService(application)
            }

            is EntryUiEvent.ConfirmInsertName -> {
                val name = events.userName.trim()
                // valida campo vazio
                val error = validateInsertName(name)
                if (error != null) {
                    _entryScreenState.update { it.copy(nameError = error) }
                    return
                }
                viewModelScope.launch {
                    _entryScreenState.update { it.copy(isSyncingInsertName = true) }
                    try {
                        // Verifica disponibilidade e registra (faz checagem e gravação junta)
                        // - UserNameAlreadyTaken
                        // - NoNetwork
                        // - UserNameVerificationFailed
                        firebaseManager.UserName().registerUserName(name)

                        // sucesso → atualiza UI e persiste local
                        _entryScreenState.update {
                            it.copy(
                                userName = name,
                                dialog = DialogStateEntry.None
                            )
                        }
                        saveUserNameDS(name)


                    } catch (e: FirebaseManagerException.UserNameAlreadyTaken) {
                        _entryScreenState.update { it.copy(nameError = InputErrorEntry.NameAlreadyTaken) }
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.NoNetwork) {
                        _entryScreenState.update { it.copy(nameError = InputErrorEntry.NetworkUnavailable) }
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.UserNameRegistrationFailed) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException) {
                        // catch-all de exceções do FirebaseManager que não tratado acima
                        snack(e.message ?: "Unexpected error", SnackbarKind.ERROR)
                    }
                    finally {
                        _entryScreenState.update { it.copy(isSyncingInsertName = false) }

                    }
                }

            }

            is EntryUiEvent.ConfirmJoinGroup -> {
                val name = events.groupName.trim()
                val password = events.groupPassword.trim()

                // verifica a rede e os campos
                val (nameError, passwordError) = validateGroupFields(name, password)
                if (nameError != null || passwordError != null) {
                    _entryScreenState.update {
                        it.copy(
                            nameError = nameError ?: InputErrorEntry.None,
                            passwordError = passwordError ?: InputErrorEntry.None
                        )
                    }
                    return
                }

                viewModelScope.launch {
                    try {
                        val groupId = firebaseManager.Group()
                            .joinGroup(
                                groupName = events.groupName,
                                password = events.groupPassword,
                                userName = _entryScreenState.value.userName
                            )

                        groupLocalRepo.insert(GroupEntity(groupId, events.groupName))

                        navigateTo(GroupInfo(groupId, events.groupName))

                    } catch (e: FirebaseManagerException.GroupNotFoundOrInvalidPassword) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.AlreadyMember) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.NoNetwork) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.UserGroupsUpdateError) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.FirestoreError) {
                        snack(e.message ?: "Server error", SnackbarKind.ERROR)

                    } finally {
                        _entryScreenState.update { it.copy(dialog = DialogStateEntry.None) }
                    }
                }
            }


            is EntryUiEvent.ConfirmCreateGroup -> {
                val name = events.groupName.trim()
                val password = events.groupPassword.trim()

                // verifica a rede e os campos
                val (nameError, passwordError) = validateGroupFields(name, password)
                if (nameError != null || passwordError != null) {
                    _entryScreenState.update {
                        it.copy(
                            nameError = nameError ?: InputErrorEntry.None,
                            passwordError = passwordError ?: InputErrorEntry.None
                        )
                    }
                    return
                }

                viewModelScope.launch {
                    try {
                        val newGroupId = firebaseManager.Group()
                            .createGroupWithPassword(
                                userName = _entryScreenState.value.userName,
                                groupName = events.groupName,
                                password = events.groupPassword
                            )

                        groupLocalRepo.insert(GroupEntity(newGroupId, events.groupName))

                        navigateTo(GroupInfo(newGroupId, events.groupName))

                    } catch (e: FirebaseManagerException.GroupAlreadyExist) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.NoNetwork) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.UserGroupsUpdateError) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.FirestoreError) {
                        snack(e.message ?: "Server error", SnackbarKind.ERROR)

                    } finally {
                        // SEMPRE fecha o diálogo, seja sucesso ou erro
                        _entryScreenState.update { it.copy(dialog = DialogStateEntry.None) }
                    }
                }
            }

            is EntryUiEvent.SelectGroup -> {
                navigateTo(events.groupInfo)
            }

            is EntryUiEvent.ConfirmLeaveGroup -> {
                _entryScreenState.update {
                    it.copy(
                        dialog = DialogStateEntry.None,
                        isSyncing = true
                    )
                }

                viewModelScope.launch {
                    try {
                        // 1) Primeiro, executa a remoção no Firestore
                        firebaseManager.Group()
                            .leaveGroupCompletely(
                                groupId = _entryScreenState.value.selectedGroup.groupId,
                                groupName = _entryScreenState.value.selectedGroup.groupName
                            )
                        // 2) Se deu tudo certo, apaga localmente e mostra sucesso
                        groupLocalRepo.deleteById(_entryScreenState.value.selectedGroup.groupId)
                        snack(
                            "You have left the group '${_entryScreenState.value.selectedGroup.groupName}'",
                            SnackbarKind.SUCCESS
                        )

                    } catch (e: FirebaseManagerException.NoNetwork) {
                        snack(e.message!!, SnackbarKind.ERROR)

                    } catch (e: FirebaseManagerException.FirestoreError) {
                        snack(e.message ?: "Error leaving the group", SnackbarKind.ERROR)

                    } finally {
                        delay(1000)
                        _entryScreenState.update { it.copy(isSyncing = false) }
                    }
                }
            }


        }
    }

    fun updateFCMToken() {
        // Atualiza o token FCM quando o usuário entra no grupo
        viewModelScope.launch {
            try {
                firebaseManager.Token().updateFCMToken()
            } catch (e: FirebaseManagerException.FirestoreError) {
                Log.d("xxt", "Erro ao salvar token", e)
            }
        }
    }

    private suspend fun saveUserNameDS(userName: String) {
        dataStoreRepo.saveStringDS(DataStoreKey.USER_NAME_STRING, userName)
    }

    private fun setDialog(dialog: DialogStateEntry) {
        _entryScreenState.update {
            it.copy(
                dialog = dialog,
                nameError = InputErrorEntry.None,
                passwordError = InputErrorEntry.None
            )
        }
    }

    // 1) Helpers de validação — UMA função por regra
    private fun onlineError(): InputErrorEntry? =
        if (!_entryScreenState.value.isOnline) InputErrorEntry.NetworkUnavailable else null

    private fun validateNameField(name: String): InputErrorEntry? {
        onlineError()?.let { return it }
        if (name.isBlank()) return InputErrorEntry.BlankField
        return null
    }

    private fun validatePasswordField(password: String): InputErrorEntry? {
        onlineError()?.let { return it }
        if (password.isBlank()) return InputErrorEntry.BlankField
        return null
    }

    // Regras compostas (só delegam)
    private fun validateInsertName(name: String): InputErrorEntry? =
        validateNameField(name)

    private fun validateGroupFields(
        name: String,
        password: String
    ): Pair<InputErrorEntry?, InputErrorEntry?> =
        validateNameField(name) to validatePasswordField(password)

    private fun snack(msg: String, kind: SnackbarKind) {
        _effects.trySend(EntryEffect.Snackbar(msg, kind))
    }

    private fun navigateTo(group: GroupInfo) {
        _entryScreenState.update { it.copy(selectedGroup = group) }
        _effects.trySend(EntryEffect.NavigateToGroup(group))
    }

    private fun observeLocalGroups() {
        viewModelScope.launch {
            groupLocalRepo.observeAll().collect { local ->
                _entryScreenState.update {
                    it.copy(
                        groupsList = local.map { ge -> GroupInfo(ge.groupId, ge.groupName) }
                    )
                }
            }
        }
    }

    private fun syncRemoteGroups() {
        // Cancelamos qualquer coleta anterior
        userGroupsJob?.cancel()
        userGroupsJob = viewModelScope.launch {
            firebaseManager.UserGroups().observeUserGroups()
                .onStart {
                    _entryScreenState.update { it.copy(isSyncing = true) }
                }
                .catch { e ->
                    // captura erro de leitura do Firestore
                    snack(
                        (e as? FirebaseManagerException.FirestoreError)?.message
                            ?: "Error syncing groups", SnackbarKind.ERROR
                    )

                    // desliga spinner
                    _entryScreenState.update { it.copy(isSyncing = false) }
                }
                .collect { groupsList ->
                    _entryScreenState.update { it.copy(groupsList = groupsList) }
                    // atualiza Room
                    syncRoomWith(groupsList)

                    delay(1000)
                    _entryScreenState.update { it.copy(isSyncing = false) }
                }
        }

    }

    private suspend fun syncRoomWith(groupsList: List<GroupInfo>) {
        try {
            val localList = groupLocalRepo.observeAll().first()
            val remoteListGroupIds = groupsList.map { it.groupId }
            //filtra e transforma em GroupEntity os ids que estão no firestone e não estão localmente
            val toAdd = groupsList
                .filter { it.groupId !in localList.map { g -> g.groupId } }
                .map { GroupEntity(it.groupId, it.groupName) }
            //filtra os ids que estão localmente e não estão no firestone
            val toRemove = localList
                .filter { it.groupId !in remoteListGroupIds }
                .map { it.groupId }

            toAdd.forEach { groupLocalRepo.insert(it) }
            toRemove.forEach { groupLocalRepo.deleteById(it) }

        } catch (e: Exception) {
            Log.i("xxt", "syncRemoteGroups: ${e.message}")
        }
    }

    private fun onUserNameReady(name: String) {

        _entryScreenState.update {
            it.copy(isInitializing = false, userName = name)
        }
        // 2) observa o Room imediatamente
        observeLocalGroups()
        // 3) dispara a sincronização remota para preencher o Room e atualizar UI
        syncRemoteGroups()
    }

    override fun onCleared() {
        super.onCleared()
        userGroupsJob?.cancel()
    }
}