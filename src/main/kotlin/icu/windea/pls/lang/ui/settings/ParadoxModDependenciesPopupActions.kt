package icu.windea.pls.lang.ui.settings

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPathOrNull
import icu.windea.pls.lang.actions.HandlePathActionBase
import icu.windea.pls.lang.actions.HandleUrlActionBase
import icu.windea.pls.lang.tools.SpecialUrlService
import java.nio.file.Path

interface ParadoxModDependenciesPopupActions {
    class CopyModPathAction(private val table: ParadoxModDependenciesTable) : HandlePathActionBase(
        AllIcons.Actions.Copy,
        PlsBundle.message("mod.dependencies.popup.action.CopyModPath.text"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = copyPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val selectedItem = table.getSelectedItem() ?: return null
            return selectedItem.modDirectory?.orNull()?.toPathOrNull()
        }
    }

    class CopyModPageUrlAction(private val table: ParadoxModDependenciesTable) : HandleUrlActionBase(
        AllIcons.Actions.Copy,
        PlsBundle.message("mod.dependencies.popup.action.CopyModPageUrl.text"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = copyUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val selectedItem = table.getSelectedItem() ?: return null
            val steamId = selectedItem.remoteId?.orNull() ?: return null
            return SpecialUrlService.getInstance().getSteamWorkshopUrlInSteam(steamId)
        }
    }

    class OpenModPathAction(private val table: ParadoxModDependenciesTable) : HandlePathActionBase(
        PlsIcons.General.ModDirectory,
        PlsBundle.message("mod.dependencies.popup.action.OpenModPath.text"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = openPath(e)

        override fun getTargetPath(e: AnActionEvent): Path? {
            val selectedItem = table.getSelectedItem() ?: return null
            return selectedItem.modDirectory?.orNull()?.toPathOrNull()
        }
    }

    class OpenModPageInSteamAction(private val table: ParadoxModDependenciesTable) : HandleUrlActionBase(
        PlsIcons.General.Steam,
        PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteam.text"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val selectedItem = table.getSelectedItem() ?: return null
            val steamId = selectedItem.remoteId?.orNull() ?: return null
            return SpecialUrlService.getInstance().getSteamWorkshopUrlInSteam(steamId)
        }
    }

    class OpenModPageInSteamWebsiteAction(private val table: ParadoxModDependenciesTable) : HandleUrlActionBase(
        PlsIcons.General.Steam,
        PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteamWebsite.text"),
    ) {
        override fun actionPerformed(e: AnActionEvent) = openUrl(e)

        override fun getTargetUrl(e: AnActionEvent): String? {
            val selectedItem = table.getSelectedItem() ?: return null
            val steamId = selectedItem.remoteId?.orNull() ?: return null
            return SpecialUrlService.getInstance().getSteamWorkshopUrl(steamId)
        }
    }
}
