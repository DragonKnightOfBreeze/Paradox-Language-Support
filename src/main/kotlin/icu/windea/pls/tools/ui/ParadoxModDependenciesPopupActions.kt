package icu.windea.pls.tools.ui

import com.intellij.icons.*
import com.intellij.ide.*
import com.intellij.ide.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import java.awt.datatransfer.*
import java.nio.file.*

interface ParadoxModDependenciesPopupActions {
    /**
     * 打开模组所在路径。
     */
    class OpenModPathAction(
        private val table: ParadoxModDependenciesTable
    ) : DumbAwareAction(PlsIcons.General.ModDirectory) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.OpenModPath.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.OpenModPath.description")
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            val presentation = e.presentation
            if (presentation.isVisible) {
                val targetPath = getTargetPath()
                if (targetPath != null) {
                    presentation.description = templatePresentation.description + " (" + targetPath + ")"
                }
            }
        }

        override fun actionPerformed(e: AnActionEvent) {
            val targetPath = getTargetPath() ?: return
            RevealFileAction.openDirectory(targetPath)
        }

        private fun getTargetPath(): Path? {
            val selectedRow = table.selectedRow
            val item = table.model.getItem(table.convertRowIndexToModel(selectedRow))
            val targetDirectory = item.modDirectory ?: return null
            return targetDirectory.toPathOrNull()
        }
    }

    /**
     * 打开模组的Steam创意工坊页面。（直接在Steam应用中打开）
     */
    class OpenModPageInSteamAction(
        private val table: ParadoxModDependenciesTable
    ) : DumbAwareAction(PlsIcons.General.Steam) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteam.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteam.description")
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            val presentation = e.presentation
            presentation.isEnabled = getSteamId() != null
            if (presentation.isVisible) {
                val targetUrl = getTargetUrl()
                if (targetUrl != null) {
                    presentation.description = templatePresentation.description + " (" + targetUrl + ")"
                }
            }
        }

        override fun actionPerformed(e: AnActionEvent) {
            val targetUrl = getTargetUrl() ?: return //ignore
            BrowserUtil.open(targetUrl)
        }

        private fun getTargetUrl(): String? {
            val steamId = getSteamId() ?: return null
            return PlsFacade.getDataProvider().getSteamWorkshopUrlInSteam(steamId)
        }

        private fun getSteamId(): String? {
            val selectedRow = table.selectedRow
            val item = table.model.getItem(table.convertRowIndexToModel(selectedRow))
            return PlsFacade.getProfilesSettings().modDescriptorSettings.getValue(item.modDirectory.orEmpty()).remoteId
        }
    }

    /**
     * 打开模组的Steam创意工坊页面。
     */
    class OpenModPageInSteamWebsiteAction(
        private val table: ParadoxModDependenciesTable
    ) : DumbAwareAction(PlsIcons.General.Steam) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteamWebsite.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.OpenModPageInSteamWebsite.description")
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            val presentation = e.presentation
            presentation.isEnabled = getSteamId() != null
            if (presentation.isVisible) {
                val targetUrl = getTargetUrl()
                if (targetUrl != null) {
                    presentation.description = templatePresentation.description + " (" + targetUrl + ")"
                }
            }
        }

        override fun actionPerformed(e: AnActionEvent) {
            val targetUrl = getTargetUrl() ?: return //ignore
            BrowserUtil.open(targetUrl)
        }

        private fun getTargetUrl(): String? {
            val steamId = getSteamId() ?: return null
            return PlsFacade.getDataProvider().getSteamWorkshopUrl(steamId)
        }

        private fun getSteamId(): String? {
            val selectedRow = table.selectedRow
            val item = table.model.getItem(table.convertRowIndexToModel(selectedRow))
            return PlsFacade.getProfilesSettings().modDescriptorSettings.getValue(item.modDirectory.orEmpty()).remoteId
        }
    }

    /**
     * 复制模组所在路径。
     */
    class CopyModPathAction(
        private val table: ParadoxModDependenciesTable
    ) : DumbAwareAction(AllIcons.Actions.Copy) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.CopyModPath.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.CopyModPath.description")
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            val presentation = e.presentation
            if (presentation.isVisible) {
                val targetPath = getTargetPath()
                if (targetPath != null) {
                    presentation.description = templatePresentation.description + " (" + targetPath + ")"
                }
            }
        }

        override fun actionPerformed(e: AnActionEvent) {
            val targetPath = getTargetPath() ?: return
            CopyPasteManager.getInstance().setContents(StringSelection(targetPath.toString()))
        }

        private fun getTargetPath(): Path? {
            val selectedRow = table.selectedRow
            val item = table.model.getItem(table.convertRowIndexToModel(selectedRow))
            val targetDirectory = item.modDirectory ?: return null
            return targetDirectory.toPathOrNull()
        }
    }

    /**
     * 复制模组的Steam创意工坊页面的URL。
     */
    class CopyModPageUrlAction(
        private val table: ParadoxModDependenciesTable
    ) : DumbAwareAction(AllIcons.Actions.Copy) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.CopyModPageUrl.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.CopyModPageUrl.description")
        }

        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            val presentation = e.presentation
            if (presentation.isVisible) {
                val targetPath = getTargetUrl()
                if (targetPath != null) {
                    presentation.description = templatePresentation.description + " (" + targetPath + ")"
                }
            }
        }

        override fun actionPerformed(e: AnActionEvent) {
            val targetPath = getTargetUrl() ?: return
            CopyPasteManager.getInstance().setContents(StringSelection(targetPath))
        }

        private fun getTargetUrl(): String? {
            val steamId = getSteamId() ?: return null
            return PlsFacade.getDataProvider().getSteamWorkshopUrlInSteam(steamId)
        }

        private fun getSteamId(): String? {
            val selectedRow = table.selectedRow
            val item = table.model.getItem(table.convertRowIndexToModel(selectedRow))
            return PlsFacade.getProfilesSettings().modDescriptorSettings.getValue(item.modDirectory.orEmpty()).remoteId
        }
    }
}
