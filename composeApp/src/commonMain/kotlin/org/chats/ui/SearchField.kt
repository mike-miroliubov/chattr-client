package org.chats.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.SearchBarDefaults.InputFieldHeight
import androidx.compose.material3.SearchBarDefaults.inputFieldColors
import androidx.compose.material3.SearchBarDefaults.inputFieldShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource? = null,
) {
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    val colors = inputFieldColors()
    val textColor = colors.textColor(true, isError = false, focused = focused)
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier =
            modifier
                .height(InputFieldHeight)
                .focusRequester(focusRequester)
                .onFocusChanged { if (it.isFocused) onExpandedChange(true) },
        singleLine = true,
        textStyle = LocalTextStyle.current.merge(TextStyle(color = textColor)),
        cursorBrush = SolidColor(inputFieldColors().cursorColor),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
        interactionSource = interactionSource,
        decorationBox =
            @Composable { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = query,
                    innerTextField = innerTextField,
                    singleLine = true,
                    visualTransformation = VisualTransformation.None,
                    placeholder = { Text("Search") },
                    leadingIcon = {
                        Box(Modifier.padding(start = 8.dp)) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                            )
                        }
                    },
                    shape = SearchBarDefaults.inputFieldShape,
                    colors = colors,
                    contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(),
                    enabled = true,
                    interactionSource = interactionSource,
                    container = {
                        val containerColor by animateColorAsState(
                                targetValue =
                                    inputFieldColors(
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer
                                    ).containerColor(
                                        enabled = true,
                                        isError = false,
                                        focused = focused,
                                    ),
                                //animationSpec = MotionSchemeKeyTokens.FastEffects.value(),
                            )
                        Box(Modifier.background(containerColor, inputFieldShape))
                    },
                )
            },
    )
}

private fun TextFieldColors.containerColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color = when {
    !enabled -> this.disabledContainerColor
    isError -> this.errorContainerColor
    focused -> this.focusedContainerColor
    else -> this.unfocusedContainerColor
}

private fun TextFieldColors.textColor(enabled: Boolean, isError: Boolean, focused: Boolean): Color =
    when {
        !enabled -> disabledTextColor
        isError -> errorTextColor
        focused -> focusedTextColor
        else -> unfocusedTextColor
    }
