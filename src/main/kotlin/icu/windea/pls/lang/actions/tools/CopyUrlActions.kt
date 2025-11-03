package icu.windea.pls.lang.actions.tools

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ide.CopyPasteManager
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.actions.HandleUrlActionBase
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.tools.PlsUrlService
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.model.steamId
import java.awt.datatransfer.StringSelection

interface CopyUrlActions {
    abstract class Base : HandleUrlActionBase() {
        override fun actionPerformed(e: AnActionEvent) {
            val targetUrl = getTargetUrl(e) ?: return
            CopyPasteManager.getInstance().setContents(StringSelection(targetUrl))
        }
    }

    class GameStorePage : Base() {
        override fun getTargetUrl(e: AnActionEvent): String? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameStoreUrl(steamId)
        }
    }

    class GameWorkshopPage : Base() {
        override fun getTargetUrl(e: AnActionEvent): String? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val gameType = selectGameType(file) ?: return null
            val steamId = gameType.steamId
            return PlsUrlService.getSteamGameWorkshopUrl(steamId)
        }
    }

    class ModPage : Base() {
        override fun isVisible(e: AnActionEvent): Boolean {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return false
            val fileInfo = file.fileInfo ?: return false
            return fileInfo.rootInfo is ParadoxRootInfo.Mod
        }

        override fun getTargetUrl(e: AnActionEvent): String? {
            val file = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: return null
            val fileInfo = file.fileInfo ?: return null
            if (fileInfo.rootInfo !is ParadoxRootInfo.MetadataBased) return null
            val steamId = fileInfo.rootInfo.steamId?.orNull() ?: return null
            return PlsUrlService.getSteamWorkshopUrl(steamId)
        }
    }
}
