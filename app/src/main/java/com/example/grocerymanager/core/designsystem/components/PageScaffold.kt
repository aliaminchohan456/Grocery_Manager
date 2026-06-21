package com.example.grocerymanager.core.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppSpacing

/**
 * Standard page scaffold used by every top-level screen. Wraps a
 * Material 3 `Scaffold` with the design-system's frosted `AppTopBar`,
 * a `PremiumSnackbarHost` slot, and the canonical screen-edge content
 * padding.
 *
 * Use this instead of hand-rolling `Scaffold { ... LazyColumn(contentPadding
 * = PaddingValues(h = 24, v = 8, ...)) }` at every screen. The bottom
 * padding defaults to `AppSizing.BottomNavHeight + 24dp` so content never
 * sits under the bottom nav bar.
 *
 * @param title the page title rendered in the top bar.
 * @param onBack optional back navigation callback. Pass null for root
 *   tabs.
 * @param actions optional top-bar trailing actions (icon buttons).
 * @param snackbarHostState optional snackbar host state for one-shot
 *   messages. When null, no snackbar host is mounted.
 * @param floatingActionButton optional FAB composable.
 * @param bottomBar optional bottom bar (e.g. sticky CTA).
 * @param contentPadding override the default screen-edge content padding.
 *   Defaults to `ScreenEdgeHorizontal` with bottom reserved for the nav bar.
 * @param content the screen content.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageScaffold(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
    snackbarHostState: SnackbarHostState? = null,
    floatingActionButton: @Composable (() -> Unit)? = null,
    bottomBar: @Composable (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(
        start = AppSizing.ScreenEdgeHorizontal,
        end = AppSizing.ScreenEdgeHorizontal,
        top = AppSpacing.xs,
        bottom = AppSizing.BottomNavHeight + AppSpacing.xl,
    ),
    content: @Composable (PaddingValues) -> Unit,
) {
    val resolvedSnackbarHostState = snackbarHostState ?: remember { SnackbarHostState() }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                title = title,
                onBack = onBack,
                actions = actions ?: {},
            )
        },
        snackbarHost = {
            PremiumSnackbarHost(resolvedSnackbarHostState)
        },
        floatingActionButton = { floatingActionButton?.invoke() },
        bottomBar = { bottomBar?.invoke() },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            content(contentPadding)
        }
    }
}

/**
 * Lightweight page-title slot for places that don't need the full
 * `AppTopBar` chrome (e.g. an empty-state page header). Uses the same
 * typography rhythm as the top bar.
 */
@Composable
fun PageTitle(
    title: String,
    modifier: Modifier = Modifier,
    eyebrow: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = AppSizing.ScreenEdgeHorizontal, vertical = AppSpacing.md),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
            if (eyebrow != null) {
                Text(
                    text = eyebrow.uppercase(),
                    style = com.example.grocerymanager.core.designsystem.typography.Eyebrow,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (trailing != null) trailing()
    }
}