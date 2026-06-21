package com.example.grocerymanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.grocerymanager.core.designsystem.components.AmbientBackdrop
import com.example.grocerymanager.core.designsystem.theme.GroceryManagerTheme
import com.example.grocerymanager.core.designsystem.theme.isLight
import com.example.grocerymanager.core.preferences.ThemeMode
import com.example.grocerymanager.core.preferences.UserPreferencesRepository
import com.example.grocerymanager.feature.shell.MainShell
import com.example.grocerymanager.navigation.GroceryManagerNavHost
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ThemeViewModel @Inject constructor(
    preferences: UserPreferencesRepository,
) : ViewModel() {
    val themeMode: StateFlow<ThemeMode> = preferences.preferences
        .map { it.themeMode }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ThemeMode.System)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        var ready = false
        splash.setKeepOnScreenCondition { !ready }

        setContent {
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val themeMode by themeViewModel.themeMode.collectAsStateWithLifecycle()
            GroceryManagerTheme(themeMode = themeMode) {
                // Mount AmbientBackdrop once at the root so every screen
                // inherits the orbs automatically. Suppressed in light mode
                // so the bright surface stays clean.
                val isLight = com.example.grocerymanager.core.designsystem.theme.AppTheme.colors.isLight()
                AmbientBackdrop(enabled = !isLight) {
                    MainShell { navController ->
                        GroceryManagerNavHost(
                            onAppReady = { ready = true },
                            navController = navController,
                        )
                    }
                }
            }
        }
    }
}
