package icu.windea.pls.lang.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.tools.PlsUrlService
import icu.windea.pls.model.ParadoxRootInfo

interface CopyUrlActions {
    class GameStorePage : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val contextFile = getContextFile(e) ?: return null
            val fileInfo = contextFile.fileInfo ?: return null
            val gameType = fileInfo.rootInfo.gameType
            val steamId = gameType.steamId
            return PlsUrlService.getInstance().getSteamGameStoreUrl(steamId)
        }
    }

    class GameWorkshopPage : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val contextFile = getContextFile(e) ?: return null
            val fileInfo = contextFile.fileInfo ?: return null
            val gameType = fileInfo.rootInfo.gameType
            val steamId = gameType.steamId
            return PlsUrlService.getInstance().getSteamGameWorkshopUrl(steamId)
        }
    }

    class ModPage : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) = copyUrl(e)

        override fun isVisible(e: AnActionEvent): Boolean {
            val contextFile = getContextFile(e) ?: return false
            val fileInfo = contextFile.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetUrl(e: AnActionEvent): String? {
            val contextFile = getContextFile(e) ?: return null
            val fileInfo = contextFile.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            val steamId = fileInfo.rootInfo.steamId?.orNull() ?: return null
            return PlsUrlService.getInstance().getSteamWorkshopUrl(steamId)
        }
    }
}
