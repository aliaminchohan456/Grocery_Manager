package com.example.grocerymanager.feature.shoppinglist

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.QuantityFormat
import com.example.grocerymanager.core.designsystem.color.BrandColors
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.BrandExtendedFab
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.ConfirmDialog
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.IconBadge
import com.example.grocerymanager.core.designsystem.components.IconBadgeSize
import com.example.grocerymanager.core.designsystem.components.ListItem
import com.example.grocerymanager.core.designsystem.components.MagneticButton
import com.example.grocerymanager.core.designsystem.components.PremiumBottomSheet
import com.example.grocerymanager.core.designsystem.components.PremiumSnackbarHost
import com.example.grocerymanager.core.designsystem.components.QuantityInputField
import com.example.grocerymanager.core.designsystem.components.SectionHeader
import com.example.grocerymanager.core.designsystem.components.TextInputField
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.isLight
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.ShoppingListItem
import com.example.grocerymanager.navigation.AddPurchaseRoute

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavHostController,
    viewModel: ShoppingListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }

    // Phase 1 P0: confirm before deleting a shopping list item. The user
    // previously could remove a list entry with a single tap — too easy
    // to mis-fire next to the mark-purchased toggle. The dialog renders
    // the item's name so the user is sure what they're deleting.
    var pendingDelete by remember { mutableStateOf<ShoppingListItem?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ShoppingListEvent.ConvertedToPurchase -> {
                    navController.navigate(AddPurchaseRoute(event.purchaseId))
                }
                is ShoppingListEvent.Error -> {
                    val res = when (event) {
                        ShoppingListEvent.Error.SelectOne -> R.string.shopping_list_error_select_one
                        ShoppingListEvent.Error.NoCategory -> R.string.shopping_list_error_no_category
                        ShoppingListEvent.Error.NoItems -> R.string.shopping_list_error_no_items
                        ShoppingListEvent.Error.ConvertFailed -> R.string.shopping_list_error_convert_failed
                    }
                    snackbarHostState.currentSnackbarData?.dismiss()
                    snackbarHostState.showSnackbar(context.getString(res))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.shopping_list_title),
                onBack = { navController.popBackStack() },
                actions = {
                    if (state.selected.isNotEmpty()) {
                        IconButton(onClick = { viewModel.convertSelectedToPurchase() }) {
                            Icon(
                                imageVector = Icons.Outlined.ShoppingCart,
                                contentDescription = stringResource(R.string.action_convert_to_purchase),
                                tint = AppTheme.colors.brand,
                            )
                        }
                    }
                },
            )
        },
        snackbarHost = { PremiumSnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Phase 3: hide the FAB when the list is empty so the empty
            // state's primary CTA isn't redundant. The FAB reappears once
            // at least one item exists.
            if (state.items.isNotEmpty()) {
                BrandExtendedFab(
                    text = stringResource(R.string.action_add_item),
                    onClick = { showAdd = true },
                    leadingIcon = Icons.Outlined.Add,
                )
            }
        },
    ) { padding ->
        if (state.items.isEmpty()) {
            ShoppingListEmptyState(
                title = stringResource(R.string.shopping_list_empty_title),
                body = stringResource(R.string.shopping_list_empty_body),
                eyebrow = stringResource(R.string.shopping_list_empty_eyebrow),
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                cta = {
                    GradientPillCta(
                        text = stringResource(R.string.action_add_first_item),
                        onClick = { showAdd = true },
                    )
                },
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                // Phase 3: standardise to ScreenEdgeHorizontal.
                contentPadding = PaddingValues(
                    start = AppSizing.ScreenEdgeHorizontal,
                    end = AppSizing.ScreenEdgeHorizontal,
                    top = AppSpacing.sm,
                    bottom = AppSizing.BottomNavHeight + AppSpacing.xl,
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                val pending = state.items.filterNot { it.isPurchased }
                val purchased = state.items.filter { it.isPurchased }

                if (pending.isNotEmpty()) {
                    item {
                        // Phase 3: section header now uses just
                        // SectionHeader — the active-list count lives in
                        // the title.
                        SectionHeader(
                            title = stringResource(
                                R.string.shopping_list_to_buy,
                                pending.size,
                            ),
                        )
                    }
                    items(pending, key = { it.id }) { item ->
                        val checked = item.id in state.selected
                        ShoppingRow(
                            name = item.name,
                            quantity = item.quantity,
                            unit = item.unit,
                            category = state.categories.firstOrNull { it.id == item.categoryId },
                            checked = checked,
                            showCheckbox = true,
                            onCheck = { viewModel.toggleSelected(item.id) },
                            onTogglePurchased = { viewModel.togglePurchased(item) },
                            onDelete = { pendingDelete = item },
                        )
                    }
                }

                if (purchased.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = stringResource(
                                R.string.shopping_list_purchased,
                                purchased.size,
                            ),
                        )
                    }
                    items(purchased, key = { it.id }) { item ->
                        ShoppingRow(
                            name = item.name,
                            quantity = item.quantity,
                            unit = item.unit,
                            category = state.categories.firstOrNull { it.id == item.categoryId },
                            checked = false,
                            showCheckbox = false,
                            onCheck = {},
                            onTogglePurchased = { viewModel.togglePurchased(item) },
                            onDelete = { pendingDelete = item },
                            purchased = true,
                        )
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddItemSheet(
            onDismiss = { showAdd = false },
            onAdd = { name, qty, unit ->
                viewModel.add(name, qty, unit, null)
                showAdd = false
            },
        )
    }

    // Phase 1 P0: confirm before deleting a shopping list item. The
    // item's name appears in the title so the user is sure what they're
    // removing.
    pendingDelete?.let { item ->
        ConfirmDialog(
            title = stringResource(R.string.shopping_list_remove_title, item.name),
            message = stringResource(R.string.shopping_list_remove_message),
            confirmLabel = stringResource(R.string.shopping_list_remove),
            dismissLabel = stringResource(R.string.action_cancel),
            destructive = true,
            icon = AppIcons.Delete,
            onConfirm = {
                viewModel.remove(item.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddItemSheet(
    onDismiss: () -> Unit,
    onAdd: (name: String, quantity: Double, unit: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("1") }
    var unit by remember { mutableStateOf("") }

    PremiumBottomSheet(
        onDismiss = onDismiss,
        title = stringResource(R.string.shopping_list_add_title),
        onClose = onDismiss,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            // Chip removed — the body text opens the sheet directly.
            Text(
                text = stringResource(R.string.shopping_list_add_body),
                style = MaterialTheme.typography.bodyMedium,
                color = AppTheme.colors.onSurfaceMuted,
            )
            Spacer(Modifier.height(AppSpacing.xxs))
            TextInputField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.shopping_list_item_name),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                Box(modifier = Modifier.weight(1f)) {
                    QuantityInputField(
                        value = qty,
                        onValueChange = { qty = it },
                        label = stringResource(R.string.add_item_qty_label),
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    TextInputField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = stringResource(R.string.shopping_list_unit_label),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    )
                }
            }
            Spacer(Modifier.height(AppSpacing.xs))
            MagneticButton(
                text = stringResource(R.string.action_add_to_list),
                onClick = {
                    val q = qty.toDoubleOrNull() ?: 1.0
                    onAdd(name, q, unit)
                },
                enabled = name.isNotBlank() && (qty.toDoubleOrNull() ?: 0.0) > 0.0,
                trailingIcon = AppIcons.Add,
            )
        }
    }
}

@Composable
private fun ShoppingRow(
    name: String,
    quantity: Double,
    unit: String,
    category: Category?,
    checked: Boolean,
    showCheckbox: Boolean,
    onCheck: () -> Unit,
    onTogglePurchased: () -> Unit,
    onDelete: () -> Unit,
    purchased: Boolean = false,
) {
    // Phase 3: row is now built on the shared `ListItem` primitive so
    // it shares card chrome, padding rhythm, and trailing-slot shape
    // with the rest of the app. The checkbox + IconBadge sit in the
    // leading slot (composed in a Row); the toggle/delete icons live
    // in the trailing slot.
    ListItem(
        title = name,
        subtitle = QuantityFormat.format(quantity, unit),
        leading = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showCheckbox) {
                    Checkbox(checked = checked, onCheckedChange = { onCheck() })
                    Spacer(Modifier.width(AppSpacing.xxs))
                }
                CategoryIcon(
                    iconName = category?.iconName,
                    colorHex = category?.colorHex ?: BrandColors.CategoryFallbackHex,
                    size = AppSizing.IconBadgeMedium,
                )
            }
        },
        trailing = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onTogglePurchased) {
                    Icon(
                        imageVector = if (purchased) AppIcons.Cart else AppIcons.Check,
                        contentDescription = if (purchased)
                            stringResource(R.string.shopping_list_mark_not_purchased)
                        else stringResource(R.string.shopping_list_mark_purchased),
                        tint = if (purchased) AppTheme.colors.brand else AppTheme.colors.onSurfaceMuted,
                    )
                }
                // Phase 1 P0: explicit Spacer between mark-purchased and delete
                // so the destructive action is not adjacent to the toggle.
                Spacer(Modifier.width(AppSpacing.xxs))
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.shopping_list_remove),
                        tint = AppTheme.colors.onSurfaceMuted,
                    )
                }
            }
        },
        titleDecoration = if (purchased) TextDecoration.LineThrough else null,
        titleColor = if (purchased) AppTheme.colors.onSurfaceMuted else MaterialTheme.colorScheme.onSurface,
    )
}

/**
 * Shopping List empty state — premium Dribbble-grade layout:
 *   - A pulsing "radar" of three concentric rings emanating from the
 *     shopping bag icon (animated infinite-transition).
 *   - Eyebrow / title / body typography using the magazine register.
 *   - A pill-shaped, fully-rounded emerald-gradient CTA with a soft
 *     emerald glow shadow.
 *
 * Kept local to this screen so other empty states (Home, Categories,
 * Records) keep the simpler two-circle glow without inheriting the
 * radar animation.
 */
@Composable
private fun ShoppingListEmptyState(
    title: String,
    body: String,
    eyebrow: String?,
    modifier: Modifier = Modifier,
    cta: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        PulsingRadarIcon(icon = AppIcons.Bag)
        if (eyebrow != null) {
            Text(
                text = eyebrow.uppercase(),
                style = androidx.compose.material3.MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.5.sp,
                ),
                color = AppTheme.colors.brand,
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceMuted,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        if (cta != null) {
            androidx.compose.foundation.layout.Spacer(
                Modifier.height(8.dp),
            )
            cta()
        }
    }
}

/**
 * Pulsing radar icon — 3 concentric rings expand outward from the central
 * bag icon and fade in opacity, giving a "breathing" radar pulse that reads
 * as premium and inviting rather than a static empty-state badge.
 *
 * The rings use three phase-offset infinite transitions so each ring pulses
 * independently — the user never sees a single beat, only a continuous
 * breathing field.
 */
@Composable
private fun PulsingRadarIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    val isLight = AppTheme.colors.isLight()
    val brand = AppTheme.colors.brand

    val phase1 by rememberInfiniteTransition(label = "radar-1").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2400,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
        ),
        label = "radar-1-phase",
    )
    val phase2 by rememberInfiniteTransition(label = "radar-2").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2400,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
        ),
        label = "radar-2-phase",
    )
    val phase3 by rememberInfiniteTransition(label = "radar-3").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2400,
                easing = androidx.compose.animation.core.LinearEasing,
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart,
        ),
        label = "radar-3-phase",
    )

    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center,
    ) {
        // Three expanding rings. Each ring uses Canvas for sharp strokes
        // and an ease-out style opacity decay via ((1 - phase)^2).
        @Composable
        fun RadarRing(phase: Float, baseSize: Float) {
            val s = 1f + phase * 0.7f       // 1.0 → 1.7
            val a = (1f - phase) * 0.35f    // 0.35 → 0
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        scaleX = s
                        scaleY = s
                        this.alpha = a
                    }
                    .border(
                        width = 1.5.dp,
                        color = brand.copy(alpha = if (isLight) 0.35f else 0.5f),
                        shape = CircleShape,
                    ),
            )
        }
        // Stagger phases so rings emanate at intervals.
        RadarRing(phase = (phase1 - 0f + 1f) % 1f, baseSize = 1f)
        RadarRing(phase = (phase2 - 0.33f + 1f) % 1f, baseSize = 1f)
        RadarRing(phase = (phase3 - 0.66f + 1f) % 1f, baseSize = 1f)

        // Central badge with the bag icon — sits on top of the rings.
        Box(
            modifier = Modifier
                .size(88.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    clip = false,
                    ambientColor = brand.copy(alpha = 0.35f),
                    spotColor = brand.copy(alpha = 0.45f),
                )
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            brand.copy(alpha = if (isLight) 0.18f else 0.22f),
                            brand.copy(alpha = if (isLight) 0.08f else 0.10f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = if (isLight) 0.4f else 0.18f),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = brand,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

/**
 * Pill-shaped emerald-gradient CTA. Fully rounded corners, emerald gradient
 * fill, and a brand-coloured drop shadow that gives the button a glowing
 * "sheen" on dark surfaces. Used by the Shopping List empty state where the
 * brand block CTA is the primary affordance.
 */
@Composable
private fun GradientPillCta(
    text: String,
    onClick: () -> Unit,
) {
    val isLight = AppTheme.colors.isLight()
    val brand = AppTheme.colors.brand

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                clip = false,
                ambientColor = brand.copy(alpha = 0.35f),
                spotColor = brand.copy(alpha = 0.55f),
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        if (isLight) BrandColors.HeroGradientStart else BrandColors.DarkHeroStart,
                        if (isLight) BrandColors.HeroGradientEnd else BrandColors.DarkHeroEnd,
                    ),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold,
            ),
            color = AppTheme.colors.onAccent,
        )
    }
}