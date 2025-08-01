package com.liberty.discovoadorntk.core.data

//--------------------------------------------------------------------------------------------------
//Informação básica do usuário.
data class UserInfo(
    val userId: String = "",
    val userName: String = ""
)

//--------------------------------------------------------------------------------------------------
//Representa um membro na UI, com estado de mute.
data class MemberUi(
    val memberId: String,
    val memberName: String,
    val isMuted: Boolean
)

//--------------------------------------------------------------------------------------------------
//Info básica de um grupo.
data class GroupInfo(
    val groupId: String = "",
    val groupName: String = ""
)

//--------------------------------------------------------------------------------------------------
//Dados de uma mensagem de alarme enviada.
data class AlarmMessageInfo(
    val id: String = "",             // identificador único da própria mensagem
    val senderId: String = "",       // deviceId de quem enviou
    val senderName: String = "",     // nome de quem enviou
    val messageBody: String = "",    // conteúdo da mensagem
    val timestamp: Long =            // carimba a hora do envio em milissegundos
        System.currentTimeMillis()
//         Clock.systemUTC().millis()
)

//--------------------------------------------------------------------------------------------------
