package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.CineVaultApp
import com.example.ui.VaultViewModel
import com.example.ui.defaultCineVaultViewModelFactory
import com.example.ui.theme.CineVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        setContent {
            val vaultViewModel: VaultViewModel = viewModel(
                factory = defaultCineVaultViewModelFactory()
            )
            val preferencesState by vaultViewModel.preferencesState.collectAsStateWithLifecycle()
            val isDarkTheme = preferencesState.isDarkTheme

            LaunchedEffect(isDarkTheme) {
                val style = if (isDarkTheme) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT
                    )
                }
                enableEdgeToEdge(
                    statusBarStyle = style,
                    navigationBarStyle = style
                )
            }

            CineVaultTheme(darkTheme = isDarkTheme) {
                CineVaultApp(vaultViewModel = vaultViewModel)
            }
        }
    }
}
