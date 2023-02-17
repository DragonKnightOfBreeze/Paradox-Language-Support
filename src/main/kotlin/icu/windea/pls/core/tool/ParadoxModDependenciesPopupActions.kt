package icu.windea.pls.core.tool

import com.intellij.ide.*
import com.intellij.ide.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.ide.*
import com.intellij.openapi.project.*
import com.intellij.ui.table.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.settings.*
import java.awt.datatransfer.*
import java.nio.file.*

interface ParadoxModDependenciesPopupActions {
    /**
     * 打开模组所在路径。
     */
    class OpenModPathAction(
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : DumbAwareAction(PlsIcons.Actions.ModDirectory) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.OpenModPath.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.OpenModPath.description")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val targetPath = getTargetPath() ?: return
            RevealFileAction.openDirectory(targetPath)
        }
        
        private fun getTargetPath(): Path? {
            val selectedRow = tableView.selectedRow
            val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
            val targetDirectory = item.modDirectory ?: return null
            return targetDirectory.toPathOrNull()
        }
    }
    
    /**
     * 复制模组所在路径。
     */
    class CopyModPathAction(
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : DumbAwareAction() {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.CopyModPath.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.CopyModPath.description")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val targetPath = getTargetPath() ?: return
            CopyPasteManager.getInstance().setContents(StringSelection(targetPath.toString()))
        }
        
        private fun getTargetPath(): Path? {
            val selectedRow = tableView.selectedRow
            val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
            val targetDirectory = item.modDirectory ?: return null
            return targetDirectory.toPathOrNull()
        }
    }
    
    /**
     * 打开模组的Steam创意工坊页面。
     */
    class OpenModPageOnSteamWebsiteAction(
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : DumbAwareAction(PlsIcons.Steam) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.OpenModPageOnSteamWebsite.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.OpenModPageOnSteamWebsite.description")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = getSteamId() != null
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val targetUrl = getTargetUrl() ?: return //ignore
            BrowserUtil.open(targetUrl)
        }
        
        private fun getTargetUrl(): String? {
            val steamId = getSteamId() ?: return null
            return getSteamWorkshopLinkOnSteam(steamId)
        }
        
        private fun getSteamId(): String? {
            val selectedRow = tableView.selectedRow
            val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
            return getProfilesSettings().modDescriptorSettings.getValue(item.modDirectory.orEmpty()).remoteFileId
        }
    }
    
    
    /**
     * 打开模组的Steam创意工坊页面。（直接在Steam应用中打开）
     */
    class OpenModPageOnSteamAction(
        private val tableView: TableView<ParadoxModDependencySettingsState>,
        private val tableModel: ParadoxModDependenciesTableModel
    ) : DumbAwareAction(PlsIcons.Steam) {
        init {
            templatePresentation.text = PlsBundle.message("mod.dependencies.popup.action.OpenModPageOnSteam.text")
            templatePresentation.description = PlsBundle.message("mod.dependencies.popup.action.OpenModPageOnSteam.description")
        }
        
        override fun getActionUpdateThread(): ActionUpdateThread {
            return ActionUpdateThread.EDT
        }
        
        override fun update(e: AnActionEvent) {
            e.presentation.isEnabled = getSteamId() != null
        }
        
        override fun actionPerformed(e: AnActionEvent) {
            val targetUrl = getTargetUrl() ?: return //ignore
            BrowserUtil.open(targetUrl)
        }
        
        private fun getTargetUrl(): String? {
            val steamId = getSteamId() ?: return null
            return getSteamWorkshopLinkOnSteam(steamId)
        }
        
        private fun getSteamId(): String? {
            val selectedRow = tableView.selectedRow
            val item = tableModel.getItem(tableView.convertRowIndexToModel(selectedRow))
            return getProfilesSettings().modDescriptorSettings.getValue(item.modDirectory.orEmpty()).remoteFileId
        }
    }
}