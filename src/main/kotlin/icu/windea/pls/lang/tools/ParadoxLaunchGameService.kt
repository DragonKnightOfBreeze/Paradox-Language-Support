package icu.windea.pls.lang.tools

import com.intellij.notification.NotificationType
import com.intellij.openapi.application.EDT
import com.intellij.openapi.project.Project
import com.intellij.util.application
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.model.ParadoxRootInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ParadoxLaunchGameService {
    fun launchGameInSteam(rootInfo: ParadoxRootInfo) {
        val gameType = rootInfo.gameType
        val steamId = gameType.steamId
        val url = PlsUrlService.getSteamGameLaunchUrl(steamId)
        PlsUrlService.openUrl(url)
    }

    fun showNotification(rootInfo: ParadoxRootInfo, project: Project) {
        val gameType = rootInfo.gameType
        val gameVersion = if (rootInfo is ParadoxRootInfo.MetadataBased) rootInfo.version else null
        val content = when {
            gameVersion == null -> PlsBundle.message("game.launcher.notification.content.1", gameType.title)
            else -> PlsBundle.message("game.launcher.notification.content.2", gameType.title, gameVersion)
        }
        val notification = PlsFacade.createNotification(NotificationType.INFORMATION, content)
        notification.notify(project)
    }

    fun exitIde() {
        // NOTE 2.0.7 可能需要添加一些额外的处理逻辑
        val coroutineScope = PlsFacade.getCoroutineScope()
        coroutineScope.launch {
            withContext(Dispatchers.EDT) {
                application.exit()
            }
        }
    }
}
