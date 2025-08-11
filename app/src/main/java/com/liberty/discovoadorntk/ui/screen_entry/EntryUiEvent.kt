package com.liberty.discovoadorntk.ui.screen_entry

import com.liberty.discovoadorntk.core.data.GroupInfo

sealed class EntryUiEvent {

    // -- Dialog requests ------------------------------------------------------
    object ShowInsertNameDialog : EntryUiEvent()

    object ShowJoinGroupDialog : EntryUiEvent()

    object ShowCreateGroupDialog : EntryUiEvent()

    data class ShowLeaveGroupDialog(val groupInfo: GroupInfo) : EntryUiEvent()


    // -- Dialog confirmations ------------------------------------------------
    data class ConfirmInsertName(val userName: String) : EntryUiEvent()

    data class ConfirmJoinGroup(val groupName: String, val groupPassword: String) : EntryUiEvent()

    data class ConfirmCreateGroup(val groupName: String, val groupPassword: String) : EntryUiEvent()

    object ConfirmLeaveGroup : EntryUiEvent()

    object CancelAndClearDialog : EntryUiEvent()


    // -- Core actions ---------------------------------------------------------
    object StopAlarm: EntryUiEvent()

    data class SelectGroup(val groupInfo: GroupInfo) : EntryUiEvent()
}