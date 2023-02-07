@file:Suppress("ComponentNotRegistered")

package icu.windea.pls.core.settings.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*
import icu.windea.pls.lang.model.*

/**
 * 打开模组配置。
 * 
 * * 当当前文件是项目中的模组文件或目录时启用。
 * 
 * @see icu.windea.pls.core.settings.ParadoxModSettingsState
 * @see icu.windea.pls.core.settings.ParadoxModSettingsDialog
 */
class OpenModSettingsAction: AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        presentation.isVisible = false
        presentation.isEnabled = false
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
            ?: return
        val fileInfo = file.fileInfo ?: return
        //不为游戏文件提供
        if(fileInfo.rootInfo.rootType != ParadoxRootType.Mod) return
        //必须位于当前项目中
        val project = e.project ?: return
        presentation.isVisible = true
        presentation.isEnabled = true
    }
    
    override fun actionPerformed(e: AnActionEvent) {
        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
            ?: return
        val fileInfo = file.fileInfo ?: return
        //不为游戏文件提供
        if(fileInfo.rootInfo.rootType != ParadoxRootType.Mod) return
        //必须位于当前项目中
        val project = e.project ?: return
        val rootPath = fileInfo.rootInfo.rootPath
    }
}