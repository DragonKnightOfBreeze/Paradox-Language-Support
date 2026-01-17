package icu.windea.pls.lang.ui.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.lang.actions.HandlePathActionBase
import icu.windea.pls.lang.actions.HandleUrlActionBase
import icu.windea.pls.lang.tools.PlsUrlService
import java.nio.file.Path

interface ParadoxModDependenciesPopupActions {
    /**
     * 打开模组所在路径。
     */
    class OpenModPathAction(private val table: ParadoxModDependenciesTable) : HandlePathActionBase(
        PlsIcons.General.ModDirectory,
        PlsBundle.message("mod.dependencies.popup.action.OpenModPath.text"),
        PlsBundle.message("mod.dependencies.popup.action.OpenModPath.description"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val selectedItem = table.getSelectedItem() ?: return null
            return selectedItem.modDirectory?.orNull()?.toPathOrNull()
        }
    }

    /**
     * 打开模组的Steam创意工坊页面。（直接在Steam应用中打开）
     */
    class OpenModPageInSteamAction(private val table: ParadoxModDependenciesTable) : HandleUrlActionBase(
        PlsIcons.General.Steam,
        PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteam.text"),
        PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteam.description"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val selectedItem = table.getSelectedItem() ?: return null
            val steamId = selectedItem.remoteId?.orNull() ?: return null
            return PlsUrlService.getInstance().getSteamWorkshopUrlInSteam(steamId)
        }
    }

    /**
     * 打开模组的Steam创意工坊页面。
     */
    class OpenModPageInSteamWebsiteAction(private val table: ParadoxModDependenciesTable) : HandleUrlActionBase(
        PlsIcons.General.Steam,
        PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteamWebsite.text"),
        PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteamWebsite.description"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val selectedItem = table.getSelectedItem() ?: return null
            val steamId = selectedItem.remoteId?.orNull() ?: return null
            return PlsUrlService.getInstance().getSteamWorkshopUrl(steamId)
        }
    }

    /**
     * 复制模组所在路径。
     */
    class CopyModPathAction(private val table: ParadoxModDependenciesTable) : HandlePathActionBase(
        AllIcons.Actions.Copy,
        PlsBundle.message("mod.dependencies.popup.action.CopyModPath.text"),
        PlsBundle.message("mod.dependencies.popup.action.CopyModPath.description"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val selectedItem = table.getSelectedItem() ?: return null
            return selectedItem.modDirectory?.orNull()?.toPathOrNull()
        }
    }

    /**
     * 复制模组的Steam创意工坊页面的URL。
     */
    class CopyModPageUrlAction(private val table: ParadoxModDependenciesTable) : HandleUrlActionBase(
        AllIcons.Actions.Copy,
        PlsBundle.message("mod.dependencies.popup.action.CopyModPageUrl.text"),
        PlsBundle.message("mod.dependencies.popup.action.CopyModPageUrl.description")
    ) {
        override fun actionPerformed(e: AnActionEvent) = copyUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val selectedItem = table.getSelectedItem() ?: return null
            val steamId = selectedItem.remoteId?.orNull() ?: return null
            return PlsUrlService.getInstance().getSteamWorkshopUrlInSteam(steamId)
        }
    }
}
