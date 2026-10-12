package com.example.hiddenarmy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.hiddenarmy.navigation.AppNavigation
import com.example.hiddenarmy.ui.theme.HiddenArmyTheme
import com.example.hiddenarmy.ui.viewmodel.HiddenArmyViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HiddenArmyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val theme by viewModel.preferences.theme.collectAsState()
            val dynamicColor by viewModel.preferences.dynamicColor.collectAsState()

            HiddenArmyTheme(
                appTheme = theme,
                dynamicColor = dynamicColor
            ) {
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}