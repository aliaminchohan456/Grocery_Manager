package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme

@Composable
private fun PremiumFieldColors(
    borderColor: Color? = null,
): androidx.compose.material3.TextFieldColors {
    val brand = borderColor ?: AppTheme.colors.brand
    val fill = AppTheme.colors.inputSurface
    val transparent = Color.Transparent
    return TextFieldDefaults.colors(
        focusedContainerColor = fill,
        unfocusedContainerColor = fill,
        disabledContainerColor = AppTheme.colors.surfaceVariant,
        errorContainerColor = fill,
        focusedIndicatorColor = transparent,
        unfocusedIndicatorColor = transparent,
        disabledIndicatorColor = transparent,
        errorIndicatorColor = transparent,
        focusedTextColor = AppTheme.colors.onBackground,
        unfocusedTextColor = AppTheme.colors.onBackground,
        disabledTextColor = AppTheme.colors.onSurfaceDisabled,
        cursorColor = brand,
        focusedLabelColor = brand,
        unfocusedLabelColor = AppTheme.colors.onSurfaceMuted,
        focusedLeadingIconColor = brand,
        unfocusedLeadingIconColor = AppTheme.colors.onSurfaceMuted,
        focusedTrailingIconColor = AppTheme.colors.onSurfaceMuted,
        unfocusedTrailingIconColor = AppTheme.colors.onSurfaceMuted,
    )
}

@Composable
private fun Modifier.fieldBorder(
    isError: Boolean,
    borderColor: Color?,
): Modifier {
    val color = when {
        isError -> AppTheme.colors.overBudget
        borderColor != null -> borderColor
        else -> AppTheme.colors.outline.copy(alpha = 0.6f)
    }
    return this.border(
        width = 1.dp,
        color = color,
        shape = AppShapes.Input,
    )
}

@Composable
fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Amount",
    currencySymbol: String? = null,
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
    borderColor: Color? = null,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(AppSizing.InputHeight)
            .fieldBorder(isError, borderColor),
        label = { Text(label) },
        leadingIcon = currencySymbol?.let { { Text(it, style = MaterialTheme.typography.titleMedium) } },
        singleLine = true,
        enabled = enabled,
        isError = isError,
        keyboardOptions = keyboardOptions,
        shape = AppShapes.Input,
        textStyle = TextStyle(fontFeatureSettings = "tnum"),
        colors = PremiumFieldColors(borderColor),
    )
}

@Composable
fun QuantityInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Quantity",
    enabled: Boolean = true,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .height(AppSizing.InputHeight)
            .fieldBorder(isError = false, borderColor = null),
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = AppShapes.Input,
        textStyle = TextStyle(fontFeatureSettings = "tnum"),
        colors = PremiumFieldColors(),
    )
}

@Composable
fun TextInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = keyboardType),
    focusRequester: FocusRequester? = null,
    onFocusChanged: ((Boolean) -> Unit)? = null,
) {
    val baseModifier = modifier
        .fillMaxWidth()
        .fieldBorder(isError, null)
        .let {
            it
                .let { m -> if (focusRequester != null) m.focusRequester(focusRequester) else m }
                .let { m -> if (onFocusChanged != null) m.onFocusChanged { focusState -> onFocusChanged(focusState.isFocused) } else m }
        }
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = baseModifier,
        label = if (label.isNotEmpty()) { { Text(label) } } else null,
        placeholder = placeholder?.let { { Text(it) } },
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        enabled = enabled,
        isError = isError,
        keyboardOptions = keyboardOptions,
        shape = AppShapes.Input,
        colors = PremiumFieldColors(),
    )
}

/**
 * Shop-name input with a one-tap autocomplete list of previously used shop
 * names shown below the field while it has focus. Suggestions come from
 * `PurchaseRepository.observeShopNames()` and are filtered case-insensitively
 * against the current value (an exact-case match is hidden so the user is
 * never offered what they already typed verbatim).
 */
@Composable
fun ShopNameField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String>,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    noSuggestionsLabel: String? = null,
) {
    var isFocused by remember { mutableStateOf(false) }
    val filtered = remember(value, suggestions) {
        val query = value.trim()
        if (query.isEmpty()) {
            suggestions
        } else {
            suggestions.filter { candidate ->
                candidate.contains(query, ignoreCase = true) &&
                    !candidate.equals(query, ignoreCase = true)
            }
        }
    }
    val showDropdown = isFocused && suggestions.isNotEmpty()

    Column(modifier = modifier.fillMaxWidth()) {
        TextInputField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            leadingIcon = leadingIcon,
            keyboardOptions = keyboardOptions,
            onFocusChanged = { focused -> isFocused = focused },
        )
        if (showDropdown) {
            Spacer(Modifier.height(6.dp))
            Surface(
                shape = AppShapes.Input,
                color = AppTheme.colors.elevatedCard,
                tonalElevation = 2.dp,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 220.dp)
                        .fillMaxWidth(),
                ) {
                    if (filtered.isEmpty()) {
                        // Field has text but no matches; offer a quiet hint
                        // so the user knows the dropdown is empty because
                        // nothing matched, not because the data is missing.
                        if (noSuggestionsLabel != null) {
                            Text(
                                text = noSuggestionsLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = AppTheme.colors.onSurfaceMuted,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            )
                        }
                    } else {
                        filtered.forEach { suggestion ->
                            ShopSuggestionRow(
                                name = suggestion,
                                onClick = {
                                    onValueChange(suggestion)
                                    isFocused = false
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopSuggestionRow(
    name: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = AppIcons.Store,
            contentDescription = null,
            tint = AppTheme.colors.onSurfaceMuted,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.size(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = AppTheme.colors.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun PremiumSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(AppSizing.SearchBarHeight)
            .fieldBorder(isError = false, null),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(AppIcons.Search, contentDescription = null) },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = AppIcons.Cancel,
                        contentDescription = null,
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = AppShapes.Input,
        colors = PremiumFieldColors(),
    )
}

@Composable
fun DatePickerField(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date",
) {
    Box(modifier = modifier.fillMaxWidth()) {
        TextField(
            value = value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .height(AppSizing.InputHeight)
                .fieldBorder(isError = false, null),
            label = { Text(label) },
            leadingIcon = { Icon(AppIcons.Calendar, contentDescription = null) },
            enabled = false,
            shape = AppShapes.Input,
            colors = PremiumFieldColors(),
        )
        Box(modifier = Modifier.matchParentSize()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick),
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    // Premium selected state: instead of a flat tinted pill, the selected
    // chip reads as frosted glass — a 15% white fill with a soft inner
    // brand glow + hairline border, so the active filter looks "lit".
    val container = if (selected) {
        Color.White.copy(alpha = 0.15f)
    } else {
        AppTheme.colors.elevatedCard
    }
    val borderColor = if (selected) {
        AppTheme.colors.brand.copy(alpha = 0.5f)
    } else {
        AppTheme.colors.outline
    }
    val content = if (selected) AppTheme.colors.brand else AppTheme.colors.onSurfaceMuted
    Row(
        modifier = modifier
            .then(
                // Selected chip gets a soft brand-tinted glow so the frosted
                // glass looks like it is emitting light.
                if (selected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = AppShapes.Chip,
                        clip = false,
                        ambientColor = AppTheme.colors.brand.copy(alpha = 0.25f),
                        spotColor = AppTheme.colors.brand.copy(alpha = 0.35f),
                    )
                } else {
                    Modifier
                },
            )
            .background(container, AppShapes.Chip)
            .border(1.dp, borderColor, AppShapes.Chip)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.size(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun FilterChipRow(
    options: List<Pair<String, Boolean>>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            count = options.size,
            key = { idx -> options[idx].first },
        ) { idx ->
            val (label, selected) = options[idx]
            CategoryChip(label = label, selected = selected, onClick = { onToggle(label) })
        }
    }
}

@Composable
fun AssistChipPill(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        modifier = modifier,
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(16.dp)) } },
        shape = AppShapes.Chip,
        colors = AssistChipDefaults.assistChipColors(
            containerColor = AppTheme.colors.surfaceVariant,
            labelColor = AppTheme.colors.mutedText,
        ),
    )
}
