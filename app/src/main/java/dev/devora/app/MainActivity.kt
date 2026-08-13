package dev.devora.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dev.devora.core.ui.theme.DevoraTheme
import dev.devora.app.navigation.*
import dev.devora.feature.terminal.domain.event.TerminalLaunchEventBus
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var terminalLaunchEventBus: TerminalLaunchEventBus

    @Inject
    lateinit var nanoLaunchEventBus: dev.devora.feature.editor.domain.event.NanoLaunchEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DevoraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DevoraNavHost(
                        terminalLaunchEventBus = terminalLaunchEventBus,
                        nanoLaunchEventBus = nanoLaunchEventBus
                    )
                }
            }
        }
    }
}