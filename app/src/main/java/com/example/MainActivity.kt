package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.data.AuthManager
import com.example.data.SettingsManager
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MainScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {

    private lateinit var settingsManager: SettingsManager
    private lateinit var authManager: AuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        settingsManager = SettingsManager(applicationContext)
        authManager = AuthManager(applicationContext)

        setContent {
            val isDarkMode by settingsManager.isDarkMode.collectAsState()
            val isAuthenticated by authManager.isAuthenticated.collectAsState()

            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    if (isAuthenticated) {
                        MainScreen(
                            settingsManager = settingsManager,
                            authManager = authManager
                        )
                    } else {
                        LoginScreen(
                            authManager = authManager,
                            onLoginSuccess = {
                                // authManager state automatically triggers recomposition
                            }
                        )
                    }
                }
            }
        }
    }
}
