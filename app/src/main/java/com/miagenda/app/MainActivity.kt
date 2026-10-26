package com.miagenda.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.miagenda.app.ui.MiAgendaApp
import com.miagenda.app.ui.theme.AppViewModel
import com.miagenda.app.ui.theme.GradientBackground
import com.miagenda.app.ui.theme.MiAgendaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val factory = (application as MiAgendaApplication).container.viewModelFactory()
        setContent {
            val appViewModel: AppViewModel = viewModel(factory = factory)
            val themeMode by appViewModel.themeMode.collectAsStateWithLifecycle()
            val accentColor by appViewModel.accentColor.collectAsStateWithLifecycle()
            MiAgendaTheme(themeMode = themeMode, accentColor = accentColor) {
                GradientBackground(modifier = Modifier.fillMaxSize()) {
                    MiAgendaApp(factory = factory)
                }
            }
        }
    }
}
