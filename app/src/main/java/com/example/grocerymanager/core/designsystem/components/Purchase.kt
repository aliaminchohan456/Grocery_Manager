package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.R
import com.example.grocerymanager.core.common.MinorUnits
import com.example.grocerymanager.core.common.MoneyUtils
import com.example.grocerymanager.core.common.QuantityFormat
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.icons.CategoryIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.domain.model.Category
import com.example.grocerymanager.domain.model.PurchaseItem
import com.example.grocerymanager.domain.model.PurchaseWithItems

@Composable
fun PurchaseItemRow(
    itemName: String,
    quantity: Double,
    unit: String,
    totalPrice: MinorUnits,
    currencyCode: String,
    modifier: Modifier = Modifier,
    categoryName: String? = null,
    pricePerUnit: MinorUnits? = null,
    onClick: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
) {
    // Phase 6: rows are tappable when [onClick] is provided so callers
    // (e.g. AddPurchaseScreen) can open an inline editor on the row's
    // qty / unit / price / total. The delete IconButton still wins taps
    // because Compose's clickable consumes them only on the row body,
    // not on the trailing button.
    val rowModifier = if (onClick != null) {
        modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp)
    } else {
        modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 2.dp)
    }
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
            Text(
                text = itemName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.size(2.dp))
            val meta = buildMeta(categoryName, quantity, unit, pricePerUnit, currencyCode)
            Text(
                text = meta,
                style = MaterialTheme.typography.bodySmall,
                color = AppTheme.colors.onSurfaceMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = MoneyUtils.format(totalPrice, currencyCode),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
        )
        if (onDelete != null) {
            IconButton(onClick = onDelete) {
                Icon(
                    AppIcons.Delete,
                    contentDescription = "Remove $itemName",
                    tint = AppTheme.colors.onSurfaceMuted,
                )
            }
        }
    }
}

private fun buildMeta(
    categoryName: String?,
    quantity: Double,
    unit: String,
    pricePerUnit: MinorUnits?,
    currencyCode: String,
): String {
    val parts = mutableListOf<String>()
    if (!categoryName.isNullOrBlank()) parts += categoryName
    val qtyText = QuantityFormat.format(quantity, unit)
    if (qtyText.isNotBlank()) parts += qtyText
    if (pricePerUnit != null && pricePerUnit > 0L) {
        val per = if (unit.isBlank()) "" else "/$unit"
        parts += "${MoneyUtils.format(pricePerUnit, currencyCode)}$per"
    }
    return parts.joinToString(" · ")
}

@Composable
fun PurchaseCard(
    shopName: String?,
    totalAmount: MinorUnits,
    dateLabel: String,
    itemCount: Int,
    currencyCode: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    GroceryCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AppTheme.colors.accentSurface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = AppIcons.Bag,
                    contentDescription = null,
                    tint = AppTheme.colors.brand,
                    modifier = Modifier.size(20.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shopName ?: "—",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = AppTheme.colors.onSurfaceMuted,
                )
                Text(
                    text = "$itemCount items",
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.onSurfaceMuted,
                )
            }
            Text(
                text = MoneyUtils.format(totalAmount, currencyCode),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

// ---------------------------------------------------------------------------
// RecentPurchaseCard
// ---------------------------------------------------------------------------
//
// Premium replacement for the previous "Tap to view" placeholder row on the
// Home screen. The card renders:
//   1. A dynamic category icon (per-purchase) — derived from the dominant
//      category of the items list, with a category-tinted surface so the
//      icon "speaks" the kind of purchase at a glance.
//   2. The shop name (most prominent left-aligned text) and the total
//      (most prominent right-aligned text), so the eye anchors on price.
//   3. An inline preview of the first 2–3 line items, each prefixed with
//      a color-coded category dot. If the purchase has more items, a
//      muted `+ N more items` overflow line is appended.
//
// Design goals:
//   - Smart-truncation: at most `MAX_INLINE_ITEMS` rows before the
//     overflow line is shown — keeps every card the same height band
//     regardless of how many items the user actually bought.
//   - No "Tap to view" placeholder text — the items themselves
//     communicate the purchase.
//   - Perfectly balanced 8dp micro-rhythm between the title row, the
//     first item, and the overflow line.

/** Maximum number of item rows rendered inline before the "+ N more" line. */
private const val MAX_INLINE_ITEMS = 3

/**
 * Premium "recent purchase" card for the Home and Records screens.
 *
 * @param purchase the purchase + items to render.
 * @param currencyCode ISO 4217 code for formatting the per-item prices and
 *   the prominent total. The total uses [MoneyUtils.formatCompact] so a
 *   whole-major-unit total like `1,200.00` is shown as `USD 1,200`.
 * @param categories the full category list, used to resolve the per-item
 *   category color and icon. Unrecognised category ids fall back to the
 *   shared category fallback color.
 * @param dateLabel formatted date string (e.g. "Mar 14" or "Today") shown
 *   as the secondary line under the shop name. Pass whatever format the
 *   host screen wants — the card is intentionally format-agnostic.
 * @param onClick tap callback — the card routes to the purchase detail.
 */
@Composable
fun RecentPurchaseCard(
    purchase: PurchaseWithItems,
    currencyCode: String,
    categories: List<Category>,
    dateLabel: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    // Pre-compute the "dominant" category (the category with the highest
    // totalPrice across the items). This drives the leading icon and the
    // accent surface — the card "speaks" the kind of bill at a glance.
    val dominant = remember(purchase, categories) {
        resolveDominantCategory(purchase.items, categories)
    }
    val categoryColor = dominant?.second ?: Color(0xFF94A3B8)
    val categoryName = dominant?.first?.name

    GroceryCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // ---- Title row: dynamic category icon | shop name | total ----
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                CategoryIcon(
                    iconName = dominant?.first?.iconName,
                    colorHex = hexStringFor(categoryColor),
                    size = AppSizing.IconBadgeLarge,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = purchase.purchase.shopName
                            ?: stringResource(R.string.shop_name_unknown),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    // Date is the secondary line — matches the Records
                    // list-row convention so the eye reads the same
                    // hierarchy on both screens. Hosts pass whatever
                    // format they want (e.g. "Today" / "Mar 14" / "2026-03-14").
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = AppTheme.colors.onSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = MoneyUtils.formatCompact(
                        purchase.purchase.totalAmount,
                        currencyCode,
                    ),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // ---- Inline item preview (smart-truncated) ----
            if (purchase.items.isNotEmpty()) {
                RecentPurchaseItemPreview(
                    items = purchase.items,
                    categories = categories,
                    currencyCode = currencyCode,
                    dominantCategoryName = categoryName,
                )
            }
        }
    }
}

@Composable
private fun RecentPurchaseItemPreview(
    items: List<PurchaseItem>,
    categories: List<Category>,
    currencyCode: String,
    dominantCategoryName: String?,
) {
    val byId = remember(categories) { categories.associateBy { it.id } }
    val visible = items.take(MAX_INLINE_ITEMS)
    val overflow = items.size - visible.size

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        visible.forEach { item ->
            val cat = byId[item.categoryId]
            // The "shared" category gets a slightly more prominent dot (it's
            // the dominant category for the bill). Other items get their
            // own category color when known, otherwise the muted fallback.
            val dotColor = when {
                cat == null -> Color(0xFF94A3B8)
                cat.name == dominantCategoryName -> parseColor(cat.colorHex)
                else -> parseColor(cat.colorHex).copy(alpha = 0.65f)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                // Category color dot — 8dp circle, a quiet but unmistakable
                // color cue. We draw it as a Box so it never depends on
                // the icon registry for items without an iconName.
                Spacer(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(dotColor),
                )
                Text(
                    text = item.itemName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = MoneyUtils.formatCompact(item.totalPrice, currencyCode),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                    color = AppTheme.colors.onSurfaceMuted,
                    maxLines = 1,
                    softWrap = false,
                )
            }
        }
        if (overflow > 0) {
            // Subtle, muted overflow line. Right-aligned with a small
            // horizontal indent so the eye reads it as a continuation of
            // the list above rather than a separate stat.
            val moreText = if (overflow == 1) {
                stringResource(R.string.home_recent_more_item, overflow)
            } else {
                stringResource(R.string.home_recent_more_items, overflow)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp), // 8dp dot + 10dp gap
                horizontalArrangement = Arrangement.Start,
            ) {
                Text(
                    text = moreText,
                    style = MaterialTheme.typography.labelMedium,
                    color = AppTheme.colors.onSurfaceFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * Resolve the dominant category for a purchase:
 *   - The category with the highest `totalPrice` across the line items
 *     wins. This is the category that visually represents the bill.
 *   - Returns a `Pair<Category, Color>` so the caller can hand the Color
 *     straight to [CategoryIcon] without re-parsing the hex string.
 */
private fun resolveDominantCategory(
    items: List<PurchaseItem>,
    categories: List<Category>,
): Pair<Category, Color>? {
    if (items.isEmpty() || categories.isEmpty()) return null
    val byId = categories.associateBy { it.id }
    val totals = items.groupBy { it.categoryId }
        .mapValues { (_, list) -> list.sumOf { it.totalPrice } }
    val topEntry = totals.maxByOrNull { it.value } ?: return null
    val category = byId[topEntry.key] ?: return null
    val color = runCatching {
        Color(android.graphics.Color.parseColor(category.colorHex))
    }.getOrElse { Color(0xFF94A3B8) }
    return category to color
}

/**
 * Format a [Color] back to a `#RRGGBB` hex string for the [CategoryIcon]
 * primitive, which expects a hex string. Composing two renderers this way
 * keeps `CategoryIcon`'s signature stable and avoids a parallel API.
 */
private fun hexStringFor(color: Color): String {
    val argb = color.toArgb()
    return String.format("#%06X", 0xFFFFFF and argb)
}
