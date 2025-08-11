package com.liberty.discovoadorntk.ui.screen_entry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.liberty.discovoadorntk.R
import com.liberty.discovoadorntk.core.BaseBox
import com.liberty.discovoadorntk.core.BaseButton
import com.liberty.discovoadorntk.core.BaseDialog
import com.liberty.discovoadorntk.core.BaseLazyColumn
import com.liberty.discovoadorntk.core.BaseOutlinedTextField
import com.liberty.discovoadorntk.core.BaseText
import com.liberty.discovoadorntk.core.LoadingScreen
import com.liberty.discovoadorntk.core.MainSurfaceNColumn
import com.liberty.discovoadorntk.core.MyGroupsListItem
import com.liberty.discovoadorntk.core.VerticalSpacer
import com.liberty.discovoadorntk.core.data.GroupInfo
import com.liberty.discovoadorntk.ui.components.snackbar.SnackbarManager


@Composable
fun ScreenEntry(
    navigateToGroupScreen: (groupId: String, groupName: String) -> Unit,
    paddingValues: PaddingValues,
) {

    val vm: ViewModelEntryS1 = hiltViewModel()
    val uiStateES1 by vm.entryScreenState.collectAsState()

    val textInputName = remember { mutableStateOf("") }
    val textInputPassword = remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val maxChars = 21

    LaunchedEffect(Unit) {
        vm.effects.collect { effect ->
            when (effect) {
                is EntryEffect.NavigateToGroup -> {
                    navigateToGroupScreen(effect.group.groupId, effect.group.groupName)
                    vm.updateFCMToken()
                }

                is EntryEffect.Snackbar -> {
                    when (effect.kind) {
                        SnackbarKind.INFO -> SnackbarManager.showInfo(effect.message)
                        SnackbarKind.SUCCESS -> SnackbarManager.showSuccess(effect.message)
                        SnackbarKind.ERROR -> SnackbarManager.showError(effect.message)
                    }
                }
            }
        }
    }

// 1) Enquanto inicializa, só mostra loading
    if (uiStateES1.isInitializing) {
        LoadingScreen()
        return
    }

    MainSurfaceNColumn(paddingValues = paddingValues) {
        VerticalSpacer(21)

        Image(
            painter = painterResource(id = R.drawable.entry_screen_flying_ufo_white),
            contentDescription = "ufo abducting a cellphone",
            modifier = Modifier.scale(.95f),
            contentScale = ContentScale.Crop
        )

//---BUTTON INSERT NAME ----------------------------------------------------------------------------
        VerticalSpacer(21)
        if (uiStateES1.userName.isBlank()) {
            Text(
                text = "Insert your name",
                fontSize = 20.sp,
                color = if (uiStateES1.isOnline && !uiStateES1.isNameLocked) MaterialTheme.colorScheme.secondaryContainer else colorScheme.onPrimaryContainer,

                modifier = Modifier.clickable {
                    if (uiStateES1.isOnline && !uiStateES1.isNameLocked) vm.handleEvent(EntryUiEvent.ShowInsertNameDialog)
                }
            )
        } else {
            Text(
                text = uiStateES1.userName,
                fontSize = 20.sp,
            )
        }
//---BUTTON JOIN GROUP -----------------------------------------------------------------------------
        VerticalSpacer(21)
        BaseButton(
            text = "Join a Group",
            enabled = uiStateES1.userName.isNotBlank() && uiStateES1.isOnline
        ) {
            vm.handleEvent(EntryUiEvent.ShowJoinGroupDialog)
        }
//---BUTTON CREATE GROUP ---------------------------------------------------------------------------
        VerticalSpacer()
        BaseButton(
            text = "Create a Group",
            enabled = uiStateES1.userName.isNotBlank() && uiStateES1.isOnline,
        ) {
            vm.handleEvent(EntryUiEvent.ShowCreateGroupDialog)
        }
//---BUTTON STOP THE ALARM ---------------------------------------------------------------------------
        VerticalSpacer()
        BaseButton(
            text = "Stop the alarm",
            textColor = colorScheme.secondaryContainer,
            backgroundColor = colorScheme.onSurface.copy(alpha = 0.12f)
        ) {
            vm.handleEvent(EntryUiEvent.StopAlarm)
        }
//---MYGROUPS LIST----------------------------------------------------------------------------------
        VerticalSpacer(21)
        BaseBox(
            textTag = "MY GROUPS",
            syncing = uiStateES1.isSyncing,
            paddingTop = 6,
            paddingBottom = 6
        ) {
            BaseLazyColumn(
                listItens = uiStateES1.groupsList,
                weight = 1f,
                content = { myGroupsList ->
                    MyGroupsListItem(
                        groupName = myGroupsList.groupName,
                        onClickGroupName = {
                            vm.handleEvent(
                                EntryUiEvent.SelectGroup(myGroupsList)
                            )
                        },
                        onClickIconExit = {
                            vm.handleEvent(
                                EntryUiEvent.ShowLeaveGroupDialog(
                                    GroupInfo(
                                        groupId = myGroupsList.groupId,
                                        groupName = myGroupsList.groupName
                                    )
                                )
                            )
                        }
                    )
                }
            )
        }
    }

//------------- DIALOG USERNAME --------------------------------------------------------------------
    when (val dialog = uiStateES1.dialog) {

        is DialogStateEntry.InsertName ->


            BaseDialog(
                title = "Insert your name",
                content = {
                    Box {
                        // --- conteúdo normal do diálogo ---
                        Column {
                            BaseOutlinedTextField(
                                textInput = textInputName,
                                labelText = "Name",
                                isError = uiStateES1.nameError !is InputErrorEntry.None,
                                isErrorMessage = uiStateES1.nameError.toErrorMessage() ?: "",
                                iconDrawable = null,
                                isEnabled = !uiStateES1.isSyncingInsertName,
                                maxLengthForOutlinedTextField = maxChars
                            )
                            VerticalSpacer(6)
                            Row(
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "${textInputName.value.length} / $maxChars",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }


                        // --- overlay bloqueando tudo + spinner central ---
                        if (uiStateES1.isSyncingInsertName) {
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .background(colorScheme.surface.copy(alpha = 0.12f))
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                )
                            }
                        }
                    }

                },
                onCancel = {
                    vm.handleEvent(EntryUiEvent.CancelAndClearDialog)
                    textInputName.value = ""
                },
                onConfirm = {
                    vm.handleEvent(EntryUiEvent.ConfirmInsertName(textInputName.value))
                    textInputName.value = ""
                }
            )


//------------- DIALOG JOIN GROUP ------------------------------------------------------------------
        is DialogStateEntry.JoinGroup ->
            BaseDialog(
                title = "Join existing group",
                content = {
                    BaseOutlinedTextField(
                        textInput = textInputName,
                        labelText = "Group name",
                        isError = uiStateES1.nameError !is InputErrorEntry.None,
                        isErrorMessage = uiStateES1.nameError.toErrorMessage() ?: "",
                        iconDrawable = null
                    )
                    VerticalSpacer(6)
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "${textInputName.value.length} / $maxChars",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    BaseOutlinedTextField(
                        textInput = textInputPassword,
                        labelText = "Password",
                        isError = uiStateES1.passwordError !is InputErrorEntry.None,
                        isErrorMessage = uiStateES1.passwordError.toErrorMessage() ?: "",
                        iconDrawable = null,
                        maxLengthForOutlinedTextField = maxChars,
                        passwordVisible = passwordVisible,
                        onVisibilityClick = { passwordVisible = !passwordVisible }
                    )
                    VerticalSpacer(6)
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "${textInputPassword.value.length} / $maxChars",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
                onCancel = {
                    vm.handleEvent(EntryUiEvent.CancelAndClearDialog)
                    textInputName.value = ""
                    textInputPassword.value = ""
                    passwordVisible = false
                },
                onConfirm = {
                    vm.handleEvent(
                        EntryUiEvent.ConfirmJoinGroup(
                            groupName = textInputName.value,
                            groupPassword = textInputPassword.value
                        )
                    )

                    passwordVisible = false

                    if (textInputName.value.isNotBlank() && textInputPassword.value.isNotBlank()) {
                        textInputName.value = ""
                        textInputPassword.value = ""
                    }
                }
            )

//------------- DIALOG CREATE GROUP ----------------------------------------------------------------
        is DialogStateEntry.CreateGroup ->
            BaseDialog(
                title = "Create your group",
                content = {
                    BaseOutlinedTextField(
                        textInput = textInputName,
                        labelText = "Name",
                        isError = uiStateES1.nameError !is InputErrorEntry.None,
                        isErrorMessage = uiStateES1.nameError.toErrorMessage() ?: "",
                        iconDrawable = null,
                        maxLengthForOutlinedTextField = maxChars,
                    )
                    VerticalSpacer(6)
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "${textInputName.value.length} / $maxChars",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    BaseOutlinedTextField(
                        textInput = textInputPassword,
                        labelText = "Password",
                        isError = uiStateES1.passwordError !is InputErrorEntry.None,
                        isErrorMessage = uiStateES1.passwordError.toErrorMessage() ?: "",
                        iconDrawable = null,
                        maxLengthForOutlinedTextField = maxChars,
                        passwordVisible = passwordVisible,
                        onVisibilityClick = { passwordVisible = !passwordVisible }
                    )
                    VerticalSpacer(6)
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "${textInputPassword.value.length} / $maxChars",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                },
                onCancel = {
                    vm.handleEvent(EntryUiEvent.CancelAndClearDialog)
                    textInputName.value = ""
                    textInputPassword.value = ""
                    passwordVisible = false
                },
                onConfirm = {
                    vm.handleEvent(
                        EntryUiEvent.ConfirmCreateGroup(
                            groupName = textInputName.value,
                            groupPassword = textInputPassword.value
                        )
                    )

                    passwordVisible = false

                    if (textInputName.value.isNotBlank() && textInputPassword.value.isNotBlank()) {
                        textInputName.value = ""
                        textInputPassword.value = ""
                    }
                }
            )

//------------- DIALOG LEAVE GROUP -----------------------------------------------------------------
        is DialogStateEntry.LeaveGroup ->

            BaseDialog(
                title = "Leave group",
                content = {
                    VerticalSpacer(9)
                    BaseText(
                        text = "Are you sure you want to leave the group '${dialog.groupInfo.groupName}' ?",
                        color = colorScheme.onSurfaceVariant,
                        maxLinesAndOverflow = false
                    )
                    VerticalSpacer(9)
                },
                onCancel = {
                    vm.handleEvent(EntryUiEvent.CancelAndClearDialog)
                },
                onConfirm = {
                    vm.handleEvent(EntryUiEvent.ConfirmLeaveGroup)
                }
            )

        is DialogStateEntry.None -> {}

    }
}
