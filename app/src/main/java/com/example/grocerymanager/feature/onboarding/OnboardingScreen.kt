package com.example.grocerymanager.feature.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.components.HeroIconBox
import com.example.grocerymanager.core.designsystem.components.PrimaryButton
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.theme.AppSizing
import com.example.grocerymanager.core.designsystem.theme.AppTheme
import com.example.grocerymanager.core.designsystem.typography.Greeting
import com.example.grocerymanager.navigation.OnboardingRoute
import com.example.grocerymanager.navigation.SetupRoute
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val body: String,
    val icon: ImageVector,
)

@Composable
private fun rememberOnboardingPages(): List<OnboardingPage> {
    val p1Title = stringResource(R.string.onboarding_page1_title)
    val p1Body = stringResource(R.string.onboarding_page1_body)
    val p2Title = stringResource(R.string.onboarding_page2_title)
    val p2Body = stringResource(R.string.onboarding_page2_body)
    val p3Title = stringResource(R.string.onboarding_page3_title)
    val p3Body = stringResource(R.string.onboarding_page3_body)
    return remember(p1Title, p1Body, p2Title, p2Body, p3Title, p3Body) {
        listOf(
            OnboardingPage(p1Title, p1Body, AppIcons.Receipt),
            OnboardingPage(p2Title, p2Body, AppIcons.WalletAlt),
            OnboardingPage(p3Title, p3Body, AppIcons.Insights),
        )
    }
}

@Composable
fun OnboardingScreen(
    navController: NavHostController,
    viewModel: OnboardingViewModel = hiltViewModel(),
) {
    val pages = rememberOnboardingPages()
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val isLastPage = pagerState.currentPage == pages.lastIndex

    // Double-tap guard for both Skip and Get Started: a rapid second tap
    // would otherwise run `navController.navigate(SetupRoute) { popUpTo(OnboardingRoute) ... }`
    // a second time on a destination that's already been popped, which
    // can throw. The flag resets on screen dispose.
    var inFlight by remember { mutableStateOf(false) }
    androidx.compose.runtime.DisposableEffect(Unit) {
        onDispose { inFlight = false }
    }

    // Subtle haptic on every settled page (including programmatic scroll).
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppTheme.colors.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(modifier = Modifier.weight(1f))
            if (!isLastPage) {
                TextButton(
                    onClick = {
                        if (inFlight) return@TextButton
                        inFlight = true
                        viewModel.markComplete()
                        navController.navigate(SetupRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    },
                ) {
                    Text(
                        text = stringResource(R.string.action_skip),
                        style = MaterialTheme.typography.labelLarge,
                        color = AppTheme.colors.onSurfaceMuted,
                    )
                }
            } else {
                // Reserve the same space on the last page so the layout doesn't shift.
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center,
        ) {
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                key = { idx -> pages[idx].title },
            ) { page ->
                OnboardingPageView(pages[page])
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(pages.size) { index ->
                    val selected = pagerState.currentPage == index
                    val color by animateColorAsState(
                        if (selected) AppTheme.colors.brand else AppTheme.colors.outline,
                        animationSpec = tween(220),
                        label = "indicator-color",
                    )
                    val width = if (selected) 28.dp else 8.dp
                    Box(
                        modifier = Modifier
                            .size(width = width, height = 8.dp)
                            .clip(CircleShape)
                            .background(color),
                    )
                    if (index != pages.size - 1) Spacer(Modifier.size(8.dp))
                }
            }

            PrimaryButton(
                text = if (isLastPage) stringResource(R.string.action_get_started)
                else stringResource(R.string.action_next),
                onClick = {
                    if (isLastPage) {
                        if (inFlight) return@PrimaryButton
                        inFlight = true
                        viewModel.markComplete()
                        navController.navigate(SetupRoute) {
                            popUpTo(OnboardingRoute) { inclusive = true }
                        }
                    } else {
                        val target = (pagerState.currentPage + 1).coerceAtMost(pages.lastIndex)
                        scope.launch { pagerState.animateScrollToPage(target) }
                    }
                },
            )
            Spacer(Modifier.size(12.dp))
        }
    }
}

@Composable
private fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(28.dp),
    ) {
        HeroIconBox(
            icon = page.icon,
            sizeDp = AppSizing.OnboardingHeroSize.value.toInt(),
            showGlow = true,
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                page.title,
                style = Greeting,
                color = AppTheme.colors.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                page.body,
                style = MaterialTheme.typography.bodyLarge,
                color = AppTheme.colors.onSurfaceMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}
