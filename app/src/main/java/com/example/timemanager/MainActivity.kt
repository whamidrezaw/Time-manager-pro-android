package com.example.timemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.timemanager.ui.screens.MainScreen
import com.example.timemanager.ui.screens.SplashScreen
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
                val navController = rememberNavController()
                
                NavHost(
                    navController = navController, 
                    startDestination = "splash",
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None }
                ) {
                    composable("splash") {
                        SplashScreen(
                            onTimeout = {
                                navController.navigate("main") {
                                    popUpTo("splash") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("main") {
                        MainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
