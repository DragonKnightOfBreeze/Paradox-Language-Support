package icu.windea.pls.lang.tools.actions

import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.lang.tools.*
import icu.windea.pls.model.*

/**
 * 打开模组配置。
 *
 * * 当前文件是项目中的模组文件或目录时启用。
 *
 * @see icu.windea.pls.lang.settings.ParadoxModSettingsState
 * @see icu.windea.pls.lang.tools.ParadoxModSettingsDialog
 */
class OpenModSettingsAction : DumbAwareAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(e: AnActionEvent) {
        //基于插件设置判断是否需要显示在编辑器悬浮工具栏中
        if (e.place == ActionPlaces.CONTEXT_TOOLBAR && !getSettings().others.showEditorContextToolbar) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        //这里需要兼容直接从项目根目录右键打开菜单的情况
        val file = getFile(e) ?: return
        if (file.fileInfo?.rootInfo !is ParadoxModRootInfo) return
        //必须位于当前项目中
        val project = e.project ?: return
        val isInProject = ProjectFileIndex.getInstance(project).isInContent(file)
        if (!isInProject) return
        presentation.isEnabledAndVisible = true
    }

    private fun getFile(e: AnActionEvent): VirtualFile? {
        return (e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
            ?: e.getData(PlatformCoreDataKeys.SELECTED_ITEM)?.castOrNull<PsiDirectoryNode>()?.virtualFile)
    }

    override fun actionPerformed(e: AnActionEvent) {
        //这里需要兼容直接从项目根目录右键打开菜单的情况
        val file = getFile(e)
        val fileInfo = file?.fileInfo ?: return
        val rootInfo = fileInfo.rootInfo
        if (rootInfo !is ParadoxModRootInfo) return
        //必须位于当前项目中
        val project = e.project ?: return
        val isInProject = ProjectFileIndex.getInstance(project).isInContent(file)
        if (!isInProject) return
        //打开配置前确保已有配置数据
        ApplicationManager.getApplication().messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onAdd(rootInfo)

        val modPath = rootInfo.rootFile.path
        val modSettings = getProfilesSettings().modSettings.get(modPath) ?: return
        val dialog = ParadoxModSettingsDialog(project, modSettings)
        dialog.show()
    }
}

