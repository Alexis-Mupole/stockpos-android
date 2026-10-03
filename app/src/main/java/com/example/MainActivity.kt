package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.MainAppNavigation
import com.example.ui.theme.StockPosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as StockPosApplication).container

        setContent {
            StockPosTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppNavigation(container = appContainer)
                }
            }
        }
    }
}
