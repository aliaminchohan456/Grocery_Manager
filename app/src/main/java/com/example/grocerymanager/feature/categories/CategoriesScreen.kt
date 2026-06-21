package com.example.grocerymanager.feature.categories

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.components.AddPurchaseFab
import com.example.grocerymanager.core.designsystem.components.AppTopBar
import com.example.grocerymanager.core.designsystem.components.CategoryIcon
import com.example.grocerymanager.core.designsystem.components.ConfirmDialog
import com.example.grocerymanager.core.designsystem.components.EmptyState
import com.example.grocerymanager.core.designsystem.components.GroceryCard
import com.example.grocerymanager.core.designsystem.components.PremiumSnackbarHost
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppShapes
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.theme.CardPadding
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.feature.categorydetail.CategoryEditorSheet
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriesScreen(
    navController: NavHostController,
    onOpenCategory: (Long) -> Unit = {},
    viewModel: CategoriesViewModel = hiltViewModel(),
) {
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current
    // The "Add category" sheet is triggered from the FAB at the
    // bottom-right. It's uncontrolled (it owns its own draft) and
    // only commits via `viewModel.createCategory(name, iconName)`
    // when the user taps Save.
    var showAddCategorySheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CategoriesEvent.Error -> {
                    val res = when (event) {
                        CategoriesEvent.Error.InUse -> R.string.categories_error_in_use
                        CategoriesEvent.Error.DeleteFailed -> R.string.categories_error_delete_failed
                        CategoriesEvent.Error.ReorderFailed -> R.string.categories_error_reorder_failed
                        is CategoriesEvent.Error.Generic -> R.string.categories_error_create_failed
                    }
                    snackbar.showSnackbar(context.getString(res))
                }
                is CategoriesEvent.Deleted -> snackbar.showSnackbar(context.getString(R.string.categories_deleted, event.name))
            }
        }
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = stringResource(R.string.categories_title),
                onBack = null,
            )
        },
        snackbarHost = { PremiumSnackbarHost(snackbar) },
        floatingActionButton = {
            // Use the design-system AddPurchaseFab for the solid circular
            // FAB with gradient + glow so this matches the Home screen FAB.
            AddPurchaseFab(onClick = { showAddCategorySheet = true })
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .navigationBarsPadding(),
        ) {
            if (categories.isEmpty()) {
                EmptyState(
                    title = stringResource(R.string.categories_empty_title),
                    body = stringResource(R.string.categories_empty_body),
                    icon = AppIcons.Tag,
                    eyebrow = stringResource(R.string.categories_empty_eyebrow),
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                ReorderableCategoryList(
                    items = categories,
                    onReorder = { orderedIds -> viewModel.reorder(orderedIds) },
                    onOpenCategory = onOpenCategory,
                    onDelete = { viewModel.askDelete(it) },
                )
            }
        }
    }

    if (showAddCategorySheet) {
        CategoryEditorSheet(
            onSave = { name, iconName ->
                viewModel.createCategory(name, iconName)
                showAddCategorySheet = false
            },
            onDismiss = { showAddCategorySheet = false },
        )
    }

    val pending = uiState.pendingDelete
    if (pending != null) {
        ConfirmDialog(
            title = stringResource(R.string.categories_delete_confirm_title, pending.name),
            message = stringResource(R.string.categories_delete_confirm_message),
            confirmLabel = stringResource(R.string.action_delete),
            icon = AppIcons.Delete,
            onConfirm = viewModel::confirmDelete,
            onDismiss = viewModel::cancelDelete,
        )
    }
}

/**
 * Category list with a 3-line drag handle on the right of each row.
 *
 * Drag-to-reorder is implemented without a third-party library:
 *  1. We mirror the ViewModel's list into a local `mutableStateListOf` so the
 *     UI can re-render instantly as the user drags.
 *  2. On long-press of the drag handle, the row is "picked up" — it gets a
 *     visual elevation and follows the user's finger.
 *  3. As soon as the dragged row's center crosses the center of an adjacent
 *     row, we swap the two entries in the local list. Compose's stable key
 *     (`category.id`) animates the rest of the list to fill the gap.
 *  4. On release, we commit the new ordering to the repository via
 *     `viewModel.reorder(...)` and clear the drag state.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ReorderableCategoryList(
    items: List<Category>,
    onReorder: (List<Long>) -> Unit,
    onOpenCategory: (Long) -> Unit,
    onDelete: (Category) -> Unit,
) {
    val density = LocalDensity.current
    val rowHeightDp = 72.dp
    val rowHeightPx = with(density) { rowHeightDp.toPx() }
    val swapThreshold = rowHeightPx * 0.5f
    val haptics = LocalHapticFeedback.current

    val local = remember { mutableStateListOf<Category>().apply { addAll(items) } }
    // Sync from the source of truth when the list changes (e.g. a new
    // category was added), but never overwrite while the user is dragging.
    var isDragging by remember { mutableStateOf(false) }
    LaunchedEffect(items) {
        if (!isDragging) {
            local.clear()
            local.addAll(items)
        }
    }

    val listState = rememberLazyListState()
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        state = listState,
        contentPadding = PaddingValues(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        itemsIndexed(local, key = { _, it -> it.id }) { index, item ->
            // Each row manages its own drag state; the parent's `isDragging`
            // flag is derived from "any row is currently picked up" and is
            // used to suppress syncs from the ViewModel.
            val isThisDragging = remember { mutableStateOf(false) }
            val dragOffsetY = remember { mutableStateOf(0f) }
            val scope = rememberCoroutineScope()
            val dragHandleDescription = stringResource(R.string.categories_drag_handle)

            androidx.compose.runtime.DisposableEffect(item.id) {
                onDispose {
                    isThisDragging.value = false
                    isDragging = false
                }
            }

            CategoryRow(
                item = item,
                isDragging = isThisDragging.value,
                dragOffsetY = dragOffsetY.value,
                // `animateItem()` makes position changes (when the row
                // swaps with a neighbour during drag, or when the list
                // re-syncs from the VM) animate smoothly instead of
                // snapping. This is the "premium feel" the user asked for
                // — combined with the animated scale/elevation in
                // `CategoryRow`, the entire interaction reads as one fluid
                // gesture.
                modifier = Modifier.animateItem(),
                onClick = { onOpenCategory(item.id) },
                onDelete = { onDelete(item) },
                onDragStart = {
                    isThisDragging.value = true
                    isDragging = true
                    // Tactile confirmation that the row has been picked up.
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                },
                onDrag = { delta ->
                    val currentIndex = local.indexOfFirst { it.id == item.id }
                    if (currentIndex < 0) return@CategoryRow
                    dragOffsetY.value += delta
                    val totalDelta = dragOffsetY.value
                    val moveCount = (totalDelta / rowHeightPx).toInt()
                    val effectiveIndex = (currentIndex + moveCount)
                        .coerceIn(0, local.size - 1)
                    if (effectiveIndex != currentIndex) {
                        val mutable = local.toMutableList()
                        val moved = mutable.removeAt(currentIndex)
                        mutable.add(effectiveIndex, moved)
                        local.clear()
                        local.addAll(mutable)
                        // Reset the offset by the number of slots we crossed
                        // so the dragged row stays under the user's finger.
                        val crossed = (effectiveIndex - currentIndex) * rowHeightPx
                        dragOffsetY.value = totalDelta - crossed
                    }
                },
                onDragEnd = {
                    dragOffsetY.value = 0f
                    isThisDragging.value = false
                    isDragging = false
                    onReorder(local.map { it.id })
                },
                onDragCancel = {
                    dragOffsetY.value = 0f
                    isThisDragging.value = false
                    isDragging = false
                    onReorder(local.map { it.id })
                },
                dragHandleDescription = dragHandleDescription,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryRow(
    item: Category,
    isDragging: Boolean,
    dragOffsetY: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    dragHandleDescription: String,
) {
    // Animated lift — the row scales up and gains shadow when picked up,
    // then settles back smoothly when released. A spring gives a subtle
    // bouncy settle that reads as "premium" rather than the previous
    // hard snap.
    val scale by animateFloatAsState(
        targetValue = if (isDragging) 1.04f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "drag-scale",
    )
// Phase 6 fix: drop the resting shadow from `2.dp` to `0.dp` so
    // adjacent category cards don't render faint "container"
    // shadows behind each other. The drag-time shadow stays at 12.dp.
    //
    // Glass upgrade: opt the row into the `glass = true` recipe so
    // category tiles read as floating, tactile glass plates (3% white
    // fill, 8% white inner border, 16dp 50%-black drop shadow) instead of
    // flat solid dark cards.
    val elevation by animateDpAsState(
        targetValue = if (isDragging) 12.dp else 0.dp,
        animationSpec = tween(durationMillis = 180),
        label = "drag-elevation",
    )
    GroceryCard(
        onClick = if (isDragging) { -> } else onClick,
        contentPadding = CardPadding.Tight.toPaddingValues(),
        glass = true,
        modifier = modifier.graphicsLayer {
            translationY = dragOffsetY
            shadowElevation = elevation.toPx()
            scaleX = scale
            scaleY = scale
        },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(AppSizing.CategoryRowHeight),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            CategoryIcon(
                iconName = item.iconName,
                colorHex = item.colorHex,
                size = AppSizing.IconBadgeMedium + AppSpacing.xxs, // ~36dp for category rows
            )
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = AppIcons.Delete,
                    contentDescription = stringResource(R.string.cd_delete_category, item.name),
                    tint = AppTheme.colors.onSurfaceMuted,
                )
            }
            // 3-line drag handle — long-press to pick up, then drag up/down
            // to move the row. The handle is always visible so the user
            // knows the row is draggable.
            //
            // Phase 1 P0: bump the touch-target wrapper from 40dp to the
            // Material minimum 48dp (`AppSizing.MinTouchTarget`). The
            // glyph padding shrinks so the visual handle stays the same
            // size — only the tappable region grows.
            Icon(
                imageVector = AppIcons.Grip,
                contentDescription = dragHandleDescription,
                tint = AppTheme.colors.onSurfaceMuted,
                modifier = Modifier
                    .size(AppSizing.MinTouchTarget)
                    .padding(AppSpacing.xs)
                    .pointerInput(item.id) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { onDragStart() },
                            onDragEnd = { onDragEnd() },
                            onDragCancel = { onDragCancel() },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                onDrag(dragAmount.y)
                            },
                        )
                    },
            )
        }
    }
}
