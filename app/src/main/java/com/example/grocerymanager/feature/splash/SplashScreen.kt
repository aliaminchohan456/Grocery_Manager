package com.example.grocerymanager.feature.splash

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.grocerymanager.R
import com.example.grocerymanager.core.designsystem.icons.AppIcons
import com.example.grocerymanager.core.designsystem.components.HeroIconBox
import com.example.grocerymanager.core.designsystem.theme.AppTheme

@Composable
fun SplashScreen(
    @Suppress("UNUSED_PARAMETER") navController: NavHostController,
) {
    // The NavHost determines the start destination from preferences and only renders
    // this splash while StartDestination is still Loading. No navigation logic lives
    // here — the router owns the redirect.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HeroIconBox(icon = AppIcons.Receipt, sizeDp = 96)
        Spacer(Modifier.height(20.dp))
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            color = AppTheme.colors.brand,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.app_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceMuted,
        )
        Spacer(Modifier.height(24.dp))
        CircularProgressIndicator(
            color = AppTheme.colors.brand,
            strokeWidth = 2.dp,
            modifier = Modifier.size(28.dp),
        )
    }
}
