package dev.devora.app

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import dev.devora.core.ui.snackbar.DevoraSnackbarController
import dev.devora.core.ui.theme.DevoraTheme
import dev.devora.feature.editor.domain.event.NanoLaunchEventBus
import dev.devora.feature.notifications.data.DevoraNotificationChannels
import dev.devora.feature.terminal.domain.event.TerminalLaunchEventBus
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var terminalLaunchEventBus: TerminalLaunchEventBus

    @Inject
    lateinit var nanoLaunchEventBus: NanoLaunchEventBus

    @Inject
    lateinit var snackbarController: DevoraSnackbarController

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private val pendingDeepLinkPath = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        pendingDeepLinkPath.value = intent?.getStringExtra(DevoraNotificationChannels.EXTRA_DEEP_LINK_PATH)

        setContent {
            DevoraTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    DevoraRootScaffold(
                        snackbarController = snackbarController,
                        terminalLaunchEventBus = terminalLaunchEventBus,
                        nanoLaunchEventBus = nanoLaunchEventBus,
                        initialDeepLinkPath = pendingDeepLinkPath.value
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingDeepLinkPath.value = intent.getStringExtra(DevoraNotificationChannels.EXTRA_DEEP_LINK_PATH)
    }
}