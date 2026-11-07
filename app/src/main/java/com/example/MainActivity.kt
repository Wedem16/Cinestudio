package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.CineStudioScreen
import com.example.ui.theme.CineAiTheme
import com.example.util.configureAppCheck
import com.example.viewmodel.CineAiViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory App Check configuration before setContent or ViewModels per firebase-ai-logic-android
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            CineAiTheme {
                val viewModel: CineAiViewModel = viewModel()
                CineStudioScreen(viewModel = viewModel)
            }
        }
    }
}
