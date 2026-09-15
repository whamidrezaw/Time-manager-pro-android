package com.example.timemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.timemanager.ui.screens.MainScreen
import com.example.timemanager.ui.theme.TimeManagerTheme
import com.example.timemanager.ui.viewmodel.EventViewModel
import com.example.timemanager.ui.viewmodel.EventViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: EventViewModel by viewModels {
        EventViewModelFactory((application as TimeManagerApp).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeManagerTheme {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
