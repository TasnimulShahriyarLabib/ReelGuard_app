package com.reelguard.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.reelguard.app.ui.HomeScreen
import com.reelguard.app.ui.MainViewModel
import com.reelguard.app.ui.ReelGuardTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModel.factory((application as ReelGuardApp).container)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReelGuardTheme {
                HomeScreen(viewModel)
            }
        }
    }
}
