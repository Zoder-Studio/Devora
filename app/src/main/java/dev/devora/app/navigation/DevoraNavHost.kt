package dev.devora.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.devora.feature.apkinspector.presentation.AabInspectorScreen
import dev.devora.feature.apkinspector.presentation.ApkInspectorScreen
import dev.devora.feature.artifactmanager.presentation.ArtifactManagerScreen
import dev.devora.feature.buildsystem.presentation.BuildScreen
import dev.devora.feature.editor.domain.event.NanoLaunchEvent
import dev.devora.feature.editor.domain.event.NanoLaunchEventBus
import dev.devora.feature.editor.presentation.NanoInstallScreen
import dev.devora.feature.filemanager.presentation.FileManagerScreen
import dev.devora.feature.projectmanager.presentation.ProjectListScreen
import dev.devora.feature.signing.presentation.CreateKeystoreScreen
import dev.devora.feature.signing.presentation.SignApkScreen
import dev.devora.feature.terminal.domain.event.TerminalLaunchEvent
import dev.devora.feature.terminal.domain.event.TerminalLaunchEventBus
import dev.devora.feature.terminal.presentation.EmbeddedTerminalScreen
import dev.devora.feature.workflowengine.presentation.WorkflowRunScreen
import java.net.URLDecoder
import java.net.URLEncoder
import kotlinx.coroutines.flow.collectLatest

object DevoraDestinations {
    const val PROJECT_LIST = "project_list"
    const val FILE_MANAGER = "file_manager/{rootPath}"
    const val EMBEDDED_TERMINAL = "embedded_terminal/{workingDirectory}"
    const val EMBEDDED_NANO = "embedded_nano/{filePath}"
    const val NANO_INSTALL = "nano_install/{filePath}"
    const val BUILD_SCREEN = "build_screen/{rootPath}"
    const val WORKFLOW_RUN = "workflow_run/{rootPath}/{workflowFilePath}/{jobId}"
    const val ARTIFACT_MANAGER = "artifact_manager/{rootPath}"
    const val APK_INSPECTOR = "apk_inspector/{apkFilePath}"
    const val AAB_INSPECTOR = "aab_inspector/{aabFilePath}"
    const val CREATE_KEYSTORE = "create_keystore/{destinationDir}"
    const val SIGN_APK = "sign_apk/{apkFilePath}"

    fun fileManagerRoute(rootPath: String) =
        "file_manager/${URLEncoder.encode(rootPath, "UTF-8")}"

    fun embeddedTerminalRoute(dir: String) =
        "embedded_terminal/${URLEncoder.encode(dir, "UTF-8")}"

    fun embeddedNanoRoute(path: String) =
        "embedded_nano/${URLEncoder.encode(path, "UTF-8")}"

    fun nanoInstallRoute(path: String) =
        "nano_install/${URLEncoder.encode(path, "UTF-8")}"

    fun buildScreenRoute(rootPath: String) =
        "build_screen/${URLEncoder.encode(rootPath, "UTF-8")}"

    fun workflowRunRoute(rootPath: String, workflowFilePath: String, jobId: String) =
        "workflow_run/${URLEncoder.encode(rootPath, "UTF-8")}/" +
            "${URLEncoder.encode(workflowFilePath, "UTF-8")}/" +
            URLEncoder.encode(jobId, "UTF-8")

    fun artifactManagerRoute(rootPath: String) =
        "artifact_manager/${URLEncoder.encode(rootPath, "UTF-8")}"

    fun apkInspectorRoute(apkFilePath: String) =
        "apk_inspector/${URLEncoder.encode(apkFilePath, "UTF-8")}"

    fun aabInspectorRoute(aabFilePath: String) =
        "aab_inspector/${URLEncoder.encode(aabFilePath, "UTF-8")}"

    fun createKeystoreRoute(destinationDir: String) =
        "create_keystore/${URLEncoder.encode(destinationDir, "UTF-8")}"

    fun signApkRoute(apkFilePath: String) =
        "sign_apk/${URLEncoder.encode(apkFilePath, "UTF-8")}"
}

@Composable
fun DevoraNavHost(
    terminalLaunchEventBus: TerminalLaunchEventBus,
    nanoLaunchEventBus: NanoLaunchEventBus
) {
    val navController = rememberNavController()

    LaunchedEffect(Unit) {
        terminalLaunchEventBus.events.collectLatest { event ->
            when (event) {
                is TerminalLaunchEvent.NavigateToEmbeddedTerminal ->
                    navController.navigate(DevoraDestinations.embeddedTerminalRoute(event.directoryPath))
                is TerminalLaunchEvent.Failed -> { }
            }
        }
    }
    LaunchedEffect(Unit) {
        nanoLaunchEventBus.events.collectLatest { event ->
            when (event) {
                is NanoLaunchEvent.NavigateToEmbeddedNano ->
                    navController.navigate(DevoraDestinations.embeddedNanoRoute(event.filePath))
                is NanoLaunchEvent.NavigateToInstallNano ->
                    navController.navigate(DevoraDestinations.nanoInstallRoute(event.filePath))
                is NanoLaunchEvent.Failed -> { }
            }
        }
    }

    NavHost(navController = navController, startDestination = DevoraDestinations.PROJECT_LIST) {

        composable(DevoraDestinations.PROJECT_LIST) {
            ProjectListScreen(
                onProjectSelected = { project ->
                    navController.navigate(DevoraDestinations.fileManagerRoute(project.rootPath))
                },
                onImportRequested = { }
            )
        }

        composable(
            route = DevoraDestinations.FILE_MANAGER,
            arguments = listOf(navArgument("rootPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val rootPath = URLDecoder.decode(
                backStackEntry.arguments?.getString("rootPath").orEmpty(), "UTF-8"
            )
            FileManagerScreen(rootPath = rootPath)
        }

        composable(
            route = DevoraDestinations.EMBEDDED_TERMINAL,
            arguments = listOf(navArgument("workingDirectory") { type = NavType.StringType })
        ) { backStackEntry ->
            val dir = URLDecoder.decode(
                backStackEntry.arguments?.getString("workingDirectory").orEmpty(), "UTF-8"
            )
            EmbeddedTerminalScreen(workingDirectory = dir)
        }

        composable(
            route = DevoraDestinations.EMBEDDED_NANO,
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = URLDecoder.decode(
                backStackEntry.arguments?.getString("filePath").orEmpty(), "UTF-8"
            )
            EmbeddedTerminalScreen(
                workingDirectory = java.io.File(path).parent ?: "/",
                initialCommand = "nano '$path'"
            )
        }

        composable(
            route = DevoraDestinations.NANO_INSTALL,
            arguments = listOf(navArgument("filePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val path = URLDecoder.decode(
                backStackEntry.arguments?.getString("filePath").orEmpty(), "UTF-8"
            )
            NanoInstallScreen(
                filePath = path,
                onInstalled = { installedPath ->
                    navController.navigate(DevoraDestinations.embeddedNanoRoute(installedPath))
                }
            )
        }

        composable(
            route = DevoraDestinations.BUILD_SCREEN,
            arguments = listOf(navArgument("rootPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val rootPath = URLDecoder.decode(
                backStackEntry.arguments?.getString("rootPath").orEmpty(), "UTF-8"
            )
            BuildScreen(projectRootPath = rootPath)
        }

        composable(
            route = DevoraDestinations.WORKFLOW_RUN,
            arguments = listOf(
                navArgument("rootPath") { type = NavType.StringType },
                navArgument("workflowFilePath") { type = NavType.StringType },
                navArgument("jobId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val rootPath = URLDecoder.decode(
                backStackEntry.arguments?.getString("rootPath").orEmpty(), "UTF-8"
            )
            val workflowFilePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("workflowFilePath").orEmpty(), "UTF-8"
            )
            val jobId = URLDecoder.decode(
                backStackEntry.arguments?.getString("jobId").orEmpty(), "UTF-8"
            )
            WorkflowRunScreen(
                projectRootPath = rootPath,
                workflowFilePath = workflowFilePath,
                jobId = jobId
            )
        }

        composable(
            route = DevoraDestinations.ARTIFACT_MANAGER,
            arguments = listOf(navArgument("rootPath") { type = NavType.StringType })
        ) { backStackEntry ->
            val rootPath = URLDecoder.decode(
                backStackEntry.arguments?.getString("rootPath").orEmpty(), "UTF-8"
            )
            ArtifactManagerScreen(projectRootPath = rootPath)
        }

        composable(
            route = DevoraDestinations.APK_INSPECTOR,
            arguments = listOf(navArgument("apkFilePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val apkFilePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("apkFilePath").orEmpty(), "UTF-8"
            )
            ApkInspectorScreen(apkFilePath = apkFilePath)
        }

        composable(
            route = DevoraDestinations.AAB_INSPECTOR,
            arguments = listOf(navArgument("aabFilePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val aabFilePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("aabFilePath").orEmpty(), "UTF-8"
            )
            AabInspectorScreen(aabFilePath = aabFilePath)
        }

        composable(
            route = DevoraDestinations.CREATE_KEYSTORE,
            arguments = listOf(navArgument("destinationDir") { type = NavType.StringType })
        ) { backStackEntry ->
            val destinationDir = URLDecoder.decode(
                backStackEntry.arguments?.getString("destinationDir").orEmpty(), "UTF-8"
            )
            CreateKeystoreScreen(
                destinationDir = destinationDir,
                onCreated = { navController.popBackStack() }
            )
        }

        composable(
            route = DevoraDestinations.SIGN_APK,
            arguments = listOf(navArgument("apkFilePath") { type = NavType.StringType })
        ) { backStackEntry ->
            val apkFilePath = URLDecoder.decode(
                backStackEntry.arguments?.getString("apkFilePath").orEmpty(), "UTF-8"
            )
            SignApkScreen(apkFilePath = apkFilePath)
        }
    }
}