package com.liberty.discovoadorntk.core

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.liberty.discovoadorntk.R
import com.liberty.discovoadorntk.core.data.AlarmMessageInfo
import com.liberty.discovoadorntk.core.data.MemberUi
import com.liberty.discovoadorntk.core.utils.toFormatTimestamp


//--------------------------------------------------------------------------------------------------
@Composable
fun MainSurfaceNColumn(
    horizontalAlignmentCenter: Boolean = true,
    paddingValues: PaddingValues,
    focusManager: FocusManager? = null,
    content: @Composable() (ColumnScope.() -> Unit)
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManager?.clearFocus() })
                }
                .padding(9.dp)
                .semantics { contentDescription = "Main column" },
            horizontalAlignment = if (horizontalAlignmentCenter) Alignment.CenterHorizontally else Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun VerticalSpacer(height: Int = 15) {
    Spacer(modifier = Modifier.height(height.dp))
}

//--------------------------------------------------------------------------------------------------
@Composable
fun BaseText(
    text: String,
    fontSize: Int = 16,
    color: Color = colorScheme.onBackground,
    textAlign: TextAlign = TextAlign.Start,
    maxLinesAndOverflow: Boolean = true
) {
    Text(
        text = text,
        fontSize = fontSize.sp,
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .fillMaxWidth(),
        color = color,
        overflow = if (maxLinesAndOverflow) TextOverflow.Ellipsis else TextOverflow.Clip,
        maxLines = if (maxLinesAndOverflow) 1 else Int.MAX_VALUE,
        textAlign = textAlign
    )
}

//--------------------------------------------------------------------------------------------------
@Composable
fun BaseButton(
    text: String,
    textSize: Int = 18,
    textColor: Color = colorScheme.onSecondaryContainer,
    backgroundColor: Color = colorScheme.secondaryContainer,
    isVisible: Boolean = true,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = { onClick() },
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = textColor
        ),
        shape = RoundedCornerShape(6.dp),
        enabled = enabled,

        modifier = Modifier
            .then(
                if (!isVisible) Modifier.size(0.dp)
                else Modifier
            )
            .fillMaxWidth()
            .padding(horizontal = 9.dp)
    ) {
        Text(
            text = text,
            fontSize = textSize.sp,
//            color = textColor
        )
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun BaseIconButton(
    icon: Int,
    contentDescription: String,
    backgroundColor: Color = colorScheme.primaryContainer,
    iconColor: Color = colorScheme.tertiary,
    onClick: () -> Unit
) {
    IconButton(
        onClick = { onClick() }
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = backgroundColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(2.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp), // Define o tamanho do ícone
                tint = iconColor
            )
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun BaseOutlinedButton(
    text: String,
    fontSize: Int = 18,
    textColor: Color? = null,
    backgroundColor: Color = Color.Transparent,
    isMaxWidth: Boolean = true,
    elevation: Int = 0,
    border: BorderStroke? = null,
    onClickX: () -> Unit
) {
    OutlinedButton(
        onClick = { onClickX() },
        shape = RoundedCornerShape(6.dp),
        border = border,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation.dp),
        modifier = Modifier
            .then(
                if (isMaxWidth) Modifier.fillMaxWidth() else Modifier
            )
//            .padding(horizontal = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            color = textColor ?: Color.Unspecified
        )
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun BaseOutlinedTextField(
    textInput: MutableState<String>,
    labelText: String,
    maxLengthForOutlinedTextField: Int = 18,
    iconDrawable: Int?,
    isError: Boolean = false,
    isErrorMessage: String = "Blank field",
    focusRequester: FocusRequester? = null,
    passwordVisible: Boolean? = null,
    isEnabled: Boolean = true,
    onVisibilityClick: () -> Unit = {},
) {
    OutlinedTextField(
        value = textInput.value,
        onValueChange = { newValue ->
            if (newValue.length <= maxLengthForOutlinedTextField) {
                textInput.value = newValue
            }
        },
        enabled = isEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier),
        colors = OutlinedTextFieldDefaults.colors(
            //            unfocusedBorderColor = cor30Porcento600,
            focusedBorderColor = colorScheme.secondary,
//            focusedLabelColor = corDoTextoEscuroPadrao700,
//            containerColor = cor30Porcento50,
//            errorBorderColor = levelRed,
//            errorCursorColor = levelRed,
        ),
        label = {
            Text(
                text = labelText,
            )
        },
        isError = isError,
        singleLine = true,
        visualTransformation = if (passwordVisible == null || passwordVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        trailingIcon = {
            if (iconDrawable != null) {
                Icon(
                    painter = painterResource(id = iconDrawable),
                    contentDescription = "Icon $labelText"
                )
            } else if (passwordVisible != null) {
                IconButton(onClick = onVisibilityClick) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible)
                                R.drawable.eye_open
                            else
                                R.drawable.eye_closed
                        ),
                        contentDescription = if (passwordVisible) "Show password" else "Hide password"
                    )
                }
            }
        }
    )
    if (isError) {
        Text(
            text = isErrorMessage,
//            color = colorScheme.secondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
//--------------------------------------------------------------------------------------------------

@Composable
fun BaseDialog(
    title: String,
    textYesButton: String = "Confirm",
    content: @Composable () -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .shadow(3.dp, shape = MaterialTheme.shapes.medium),
            shape = MaterialTheme.shapes.medium, // Aplica a forma com cantos arredondados
        ) {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                // Título
                Text(text = title, fontWeight = FontWeight.SemiBold)
                VerticalSpacer()
                // Corpo
                content()
                VerticalSpacer()
                // Botões
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    BaseOutlinedButton(
                        text = "Cancel",
                        textColor = colorScheme.onPrimaryContainer,
                        isMaxWidth = false
                    ) {
                        onCancel()
                    }
                    BaseOutlinedButton(
                        text = textYesButton,
                        textColor = colorScheme.secondaryContainer,
                        isMaxWidth = false

                    ) {
                        onConfirm()
                    }
                }
            }
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun <T> ColumnScope.BaseLazyColumn(
    listItens: List<T>,
    weight: Float? = 0.85f,
    reverseLayout: Boolean = false,
    content: @Composable (item: T) -> Unit
) {
    LazyColumn(
        modifier = Modifier
                then (
                if (weight != null) Modifier
                    .fillMaxSize()
                    .weight(weight)
                else Modifier
                )
            .padding(vertical = 9.dp),
        reverseLayout = reverseLayout
    ) {
        items(listItens) { item ->
            content(item)
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun MemberListItem(
    member: MemberUi,
    textSize: Int = 18,
    deviceId: String,
    onClickIconMute: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(
            painter = painterResource(id = R.drawable.alien), contentDescription = "member icon",
            modifier = Modifier
                .weight(0.1f)
        )
//        Spacer(modifier = Modifier.width(9.dp))
        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = member.memberName,
            fontSize = textSize.sp,
            modifier = Modifier.weight(0.6f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(12.dp))

            IconButton(
                onClick = { onClickIconMute() },
                modifier = Modifier
                    .weight(0.1f),
                enabled = member.memberId != deviceId
            ) {
                if (member.memberId != deviceId) {
                Icon(
                    painter = painterResource(
                        id = if (member.isMuted) R.drawable.baseline_volume_off_24
                        else R.drawable.baseline_volume_up_24
                    ),
                    contentDescription = if (member.isMuted) "Unmute" else "Mute",
//                tint = Color.Red
                )
            }
            }
        }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun MyGroupsListItem(
    groupName: String,
    textSize: Int = 18,
    onClickGroupName: () -> Unit,
    onClickIconExit: () -> Unit
) {
    Row(
        modifier = Modifier
            .clickable { onClickGroupName() }
            .fillMaxWidth()
            .padding(horizontal = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
//        horizontalArrangement = Arrangement.SpaceBetween
    ) {
//        Row(
//            modifier = Modifier
//                .weight(1f)
////                .clickable { onClickGroupName() }
//        ) {

        Icon(
            painter = painterResource(id = R.drawable.earth),
            contentDescription = "groups icon",
            modifier = Modifier
                .weight(0.1f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = groupName,
            fontSize = textSize.sp,
            modifier = Modifier
                .weight(0.6f),
//                .clickable { onClickGroupName() },
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
//        }
        Spacer(modifier = Modifier.width(12.dp))
//        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = { onClickIconExit() }, modifier = Modifier
                .weight(0.1f)
        ) {
            Icon(
//                painter = painterResource(id = R.drawable.ufo_leaving_b),
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
//                tint = Color.Red
            )
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun HistoryListItem(
    alarmMessages: AlarmMessageInfo
) {
//    // cria e memoriza o formatador
//    val dateFormatter = remember {
//        SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
//    }
//    // converte só uma vez por mensagem
//    val formatted = remember(alarmMessages.timestamp) {
//        dateFormatter.format(Date(alarmMessages.timestamp))
//    }

//    val formattedDate = remember(alarmMessages.timestamp) {
//        val instant = Instant.ofEpochMilli(alarmMessages.timestamp)
//        val zoned = instant.atZone(ZoneId.systemDefault())
//        val fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm")
//        zoned.format(fmt)
//    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        horizontalAlignment = Alignment.Start  // Alinhamento específico a esquerda
    ) {
        //Exibe o remetente
        Text(
            text = buildAnnotatedString {
                // “From:” com cor secundária
                withStyle(SpanStyle(color = colorScheme.onBackground)) {
                    append("From: ")
                }
                // nome do remetente com cor primaria (ou outra que você prefira)
                withStyle(SpanStyle(color = colorScheme.secondaryContainer)) {
                    append(alarmMessages.senderName)
                }
            },
            fontSize = 15.sp,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .fillMaxWidth(),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            textAlign = TextAlign.Start
        )

        VerticalSpacer(12)
        //Exibe a mensagem do alerta
        BracketedText(message = alarmMessages.messageBody, fontSize = 18, lineHeight = 20)
//        BaseText(
//            text = " >>      ${alarmMessages.text}      <<",
//            fontSize = 18,
//            maxLinesAndOverflow = false
//        )

        VerticalSpacer(12)
        //Exibe data e hora
        BaseText(text = alarmMessages.timestamp.toFormatTimestamp(), fontSize = 12, textAlign = TextAlign.End)

        VerticalSpacer(15)
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
        )
    }
}
//--------------------------------------------------------------------------------------------------

@Composable
fun BaseBox(
    textTag: String?,
    syncing: Boolean? = null,
    paddingHorizontal: Int = 6,
    paddingTop: Int = 0,
    paddingBottom: Int = 0,
    content: @Composable () -> Unit
) {
    if (textTag != null && syncing != null) {
        Row(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = textTag.uppercase(),
                fontSize = 12.sp,
                color = colorScheme.tertiary
            )
            if (syncing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),   // controla o tamanho
                    strokeWidth = 2.dp                 // opcional: controla a espessura
                )
            }
        }
    } else if (textTag != null) {
        BaseText(
            text = textTag.uppercase(),
            fontSize = 12,
            color = colorScheme.tertiary,
        )
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = colorScheme.primaryContainer,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(horizontal = paddingHorizontal.dp)
            .padding(top = paddingTop.dp, bottom = paddingBottom.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        content()
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun LeaveIconButton(
    icon: Int,
    iconContentDescription: String,
    backgroundColor: Color = colorScheme.primaryContainer,
    iconColor: Color = colorScheme.tertiary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
//            .padding(2.dp)
            .clickable { onClick() }
    ) {
        Spacer(modifier = Modifier.width(1.dp))
        Text(
            text = "Leave group ",
            color = iconColor,
            textAlign = TextAlign.Center,
            fontSize = 12.sp
        )
        Icon(
            painter = painterResource(id = icon),
            contentDescription = iconContentDescription,
            modifier = Modifier.size(24.dp), // Define o tamanho do ícone
            tint = iconColor
        )
//        IconButton(
//            onClick = { onClick() }
//        ) {
//            Box(
//                modifier = Modifier
//                    .background(
//                        color = backgroundColor,
//                        shape = RoundedCornerShape(4.dp)
//                    )
//                    .padding(2.dp)
//            ) {

//            }
//        }
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun RowSettingTextAndIcon(
    text: String,
    fontSize: Int = 16,
    icon: Int,
    iconContentDescription: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            fontSize = fontSize.sp,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .weight(0.9f),
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
//        Spacer(modifier = Modifier.weight(0.1f))
        IconButton(onClick = { onClick() }) {
            Icon(
                modifier = Modifier
                    .size(24.dp)
                    .weight(0.1f),
                painter = painterResource(id = icon),
                contentDescription = iconContentDescription
            )
        }
    }
}

//--------------------------------------------------------------------------------------------------
@Composable
fun RowSettingTextAndSwitch(

) {
    Row(
        modifier = Modifier.padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = if (true) {
//                    val text = if (uiStateGS1.switchButtonOn) {
            buildAnnotatedString {
                append("Alarm sound ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("ON")
                }
            }
        } else {
            buildAnnotatedString {
                append("Alarm sound ")
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                    append("OFF")
                }
            }
        }

        Text(
            text = text,
            fontSize = 16.sp,
        )
        Spacer(modifier = Modifier.weight(0.1f))
        Switch(
            checked = true,
//                        checked = uiStateGS1.switchButtonOn,
            onCheckedChange = { isChecked ->
//                            isAlarmEnabled = isChecked
//                            // Lógica para ativar/desativar o alarme
//                            if (isChecked) {
//                                // Lógica para ativar o alarme
//                            } else {
//                                // Lógica para desativar o alarme
//                            }
//                            vm.handleEvent(GroupS1Events.BtnSwitchAlarmOnOff(isChecked))
            },
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
            ),
            enabled = false
        )
    }
}
//--------------------------------------------------------------------------------------------------

@Composable
fun BracketedText(
    message: String,
    textColor: Color = colorScheme.onBackground,
    fontSize: Int,
    lineHeight: Int
) {
    Box(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 6.dp)) {
        // Colchete esquerdo colado à esquerda, alinhado ao topo do texto
        Text(
            text = "[",
            color = textColor,
            fontSize = fontSize.sp,
            lineHeight = lineHeight.sp,
            modifier = Modifier.align(Alignment.TopStart)
        )
        // Texto multilinha centralizado horizontalmente
        Text(
            text = "$message",
            color = textColor,
            fontSize = fontSize.sp,
            textAlign = TextAlign.Center,
            lineHeight = lineHeight.sp,
            modifier = Modifier.align(Alignment.TopCenter)
                .padding(horizontal = 6.dp)

        )
        // Colchete direito colado à direita, alinhado ao fim (última linha) do texto
        Text(
            text = "]",
            color = textColor,
            fontSize = fontSize.sp,
            lineHeight = lineHeight.sp,
            modifier = Modifier.align(Alignment.BottomEnd)

        )
    }
}
//--------------------------------------------------------------------------------------------------
//--------------------------------------------------------------------------------------------------



