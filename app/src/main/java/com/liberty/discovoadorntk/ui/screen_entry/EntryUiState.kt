package com.liberty.discovoadorntk.ui.screen_entry

import com.liberty.discovoadorntk.core.data.GroupInfo

data class EntryUiState(
    val userName: String = "",
    val isNameLocked: Boolean = true,
    val selectedGroup: GroupInfo = GroupInfo(),
    val groupsList: List<GroupInfo> = emptyList(),

    val isSyncing: Boolean = false,

    val isSyncingInsertName: Boolean = false,

    val isOnline: Boolean = true,
    val isInitializing: Boolean = true,

    val dialog: DialogStateEntry = DialogStateEntry.None,
    val nameError: InputErrorEntry = InputErrorEntry.None,
    val passwordError: InputErrorEntry = InputErrorEntry.None
)

sealed class EntryEffect {
    data class NavigateToGroup(val group: GroupInfo) : EntryEffect()
    data class Snackbar(val message: String, val kind: SnackbarKind) : EntryEffect()
}

enum class SnackbarKind { INFO, ERROR, SUCCESS }

sealed class DialogStateEntry {
    object None : DialogStateEntry()
    object InsertName : DialogStateEntry()
    object JoinGroup : DialogStateEntry()
    object CreateGroup : DialogStateEntry()
    data class LeaveGroup(val groupInfo: GroupInfo) : DialogStateEntry()
}

sealed class InputErrorEntry(val message: String? = null) {
    object None : InputErrorEntry(null)
    object BlankField : InputErrorEntry("Blank field")
    object NameAlreadyTaken : InputErrorEntry("Name not available")
    object NetworkUnavailable : InputErrorEntry("Offline")

    fun toErrorMessage(): String? = when (this) {
        None -> ""
        BlankField -> "Blank field"
        NameAlreadyTaken -> "Name not available"
        NetworkUnavailable -> "Offline"
    }
}