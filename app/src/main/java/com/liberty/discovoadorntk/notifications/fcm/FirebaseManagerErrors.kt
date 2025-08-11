package com.liberty.discovoadorntk.notifications.fcm


sealed class FirebaseManagerException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {

    //Exceção genérica para erros do Firestore
    class FirestoreError(cause: Throwable) :
        FirebaseManagerException(cause.message, cause)

    //Exceção para já-existente
    class GroupAlreadyExist(groupName: String) :
        FirebaseManagerException("Group '$groupName' already exists")

    //Exceção para falta de rede
    object NoNetwork :
        FirebaseManagerException("No internet connection")

    class GroupNotFoundOrInvalidPassword :
        FirebaseManagerException("Invalid group or password")

    class AlreadyMember(groupName: String) :
        FirebaseManagerException("You are already a member in the group '$groupName'")

    class UserGroupsUpdateError(e: Throwable) : FirebaseManagerException("Failed to update userGroups", e)

    class SaveAlarmMessageError(e: Throwable) : FirebaseManagerException("Failed to save message", e)

    class UserNameAlreadyTaken : FirebaseManagerException("This username is not available")

    class UserNameRegistrationFailed(cause: Throwable)
        : FirebaseManagerException("Name register failed", cause)

    //Falha ao buscar tokens dos membros de um grupo
    class FetchTokensError(cause: Throwable) :
        FirebaseManagerException("Error fetching group member tokens", cause)
}