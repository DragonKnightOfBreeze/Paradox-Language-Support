package icu.windea.pls.lang.tools

import com.intellij.execution.CommandLineUtil
import com.intellij.execution.ExecutionException
import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.configurations.PathEnvironmentVariableUtil
import com.intellij.execution.process.CapturingProcessHandler
import com.intellij.execution.util.ExecUtil
import com.intellij.ide.BrowserUtil
import com.intellij.ide.IdeBundle
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.util.SystemInfo
import icu.windea.pls.PlsFacade
import kotlinx.coroutines.launch
import java.awt.datatransfer.StringSelection

class PlsUrlServiceImpl : PlsUrlService {
    private val logger = thisLogger()

    override fun getSteamGameStoreUrl(steamId: String): String {
        return "https://store.steampowered.com/app/$steamId/"
    }

    override fun getSteamGameWorkshopUrl(steamId: String): String {
        return "https://steamcommunity.com/app/$steamId/workshop/"
    }

    override fun getSteamWorkshopUrl(steamId: String): String {
        return "https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
    }

    override fun getSteamGameStoreUrlInSteam(steamId: String): String {
        return "steam://store/$steamId"
    }

    override fun getSteamGameWorkshopUrlInSteam(steamId: String): String {
        return "steam://openurl/https://steamcommunity.com/app/$steamId/workshop/"
    }

    override fun getSteamWorkshopUrlInSteam(steamId: String): String {
        return "steam://openurl/https://steamcommunity.com/sharedfiles/filedetails/?id=$steamId"
    }

    override fun getSteamGameLaunchUrl(steamId: String): String {
        return "steam://launch/$steamId"
    }

    override fun isSteamUrl(url: String): Boolean {
        return url.startsWith("steam://", ignoreCase = true)
    }

    override fun openUrl(url: String) {
        // NOTE 2.1.7 since IDEA 2026.1, cannot use `BrowserUtil.open(url)` directly to handle Steam hyperlinks as expected
        if (isCustomUrl(url)) return openCustomUrl(url)

        BrowserUtil.open(url)
    }

    override fun isCustomUrl(url: String): Boolean {
        // enough now
        return isSteamUrl(url)
    }

    override fun openCustomUrl(url: String) {
        // 1. Use Java native `java.awt.Desktop` API (recommended)
        //    -> Unexpected behaviour: will open a new tab in the default browser
        // 2. Use OS-dependent system command (fallback)

        // see: com.intellij.ide.BrowserUtil.browse
        // see: com.intellij.ide.browsers.BrowserLauncher.browse
        // see: com.intellij.ide.browsers.BrowserLauncherAppless.browse

        val coroutine = PlsFacade.getCoroutineScope()
        coroutine.launch {
            openUrlWithSystemCommand(url)

            // if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            //     openCustomUrlWithDesktopApi(url)
            // } else {
            //     openCustomUrlWithSystemCommand(url)
            // }
        }
    }

    // private fun openUrlWithDesktopApi(url: String) {
    //     // see: com.intellij.ide.browsers.BrowserLauncherAppless.openWithDesktopApi(java.net.URI, com.intellij.openapi.project.Project)
    //
    //     try {
    //         logger.debug { "Trying to open url with desktop api: $url" }
    //         val uri = URI(url) // to make things simple, do not use `VfsUtil.toUri(uri)` here
    //         Desktop.getDesktop().browse(uri)
    //     } catch (e: Exception) {
    //         logger.warn("Failed to open url: $url")
    //         logger.warn(e.message, e)
    //         if (SystemInfo.isMac && e.message!!.contains("Error code: -10814")) {
    //             // if "No application knows how to open" the URL, there is no sense in retrying with the 'open' command
    //             return
    //         }
    //         openUrlWithSystemCommand(url)
    //     }
    // }

    private fun openUrlWithSystemCommand(url: String) {
        // see: com.intellij.ide.browsers.BrowserLauncherAppless.openWithDefaultBrowserCommand

        val command = when {
            SystemInfo.isWindows -> listOf(CommandLineUtil.getWinShellName(), "/c", "start", GeneralCommandLine.inescapableQuote(""))
            SystemInfo.isMac -> listOf(ExecUtil.openCommandPath)
            // see: com.intellij.openapi.util.SystemInfo.hasXdgOpen (deprecated since IDEA 2026.1)
            PathEnvironmentVariableUtil.isOnPath("xdg-open") -> listOf("xdg-open")
            else -> null
        }
        if (command == null) {
            logger.warn(IdeBundle.message("browser.default.not.supported"))
            return
        }
        val commandLine = GeneralCommandLine(command).withParameters(url)
        logger.debug { "Trying to open url with system command: $url" }
        logger.debug { "Running system command: ${commandLine.commandLineString}" }
        try {
            val output = CapturingProcessHandler.Silent(commandLine).runProcess(10000, false)
            if (!output.checkSuccess(logger) && output.exitCode == 1) {
                val error = output.stderrLines.firstOrNull()
                logger.warn(error)
            }
        } catch (e: ExecutionException) {
            logger.warn("Failed to run system command: ${commandLine.commandLineString}")
            logger.warn(e.message, e)
        }
    }

    override fun copyUrl(url: String) {
        CopyPasteManager.getInstance().setContents(StringSelection(url))
    }
}
