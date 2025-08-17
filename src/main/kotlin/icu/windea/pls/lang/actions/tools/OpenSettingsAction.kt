package icu.windea.pls.lang.actions.tools

import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.listeners.*
import icu.windea.pls.model.*

abstract class OpenSettingsAction : DumbAwareAction() {
    override fun getActionUpdateThread() = ActionUpdateThread.BGT

    override fun update(e: AnActionEvent) {
        //基于插件设置判断是否需要显示在编辑器悬浮工具栏中
        if (e.place == ActionPlaces.CONTEXT_TOOLBAR && !PlsFacade.getSettings().others.showEditorContextToolbar) {
            e.presentation.isEnabledAndVisible = false
            return
        }

        val presentation = e.presentation
        presentation.isEnabledAndVisible = false
        //这里需要兼容直接从项目根目录右键打开菜单的情况
        val file = getFile(e) ?: return
        val rootInfo = file.fileInfo?.rootInfo ?: return
        if (!isAvailable(rootInfo)) return
        //必须位于当前项目中
        val project = e.project ?: return
        val isInProject = ProjectFileIndex.getInstance(project).isInContent(file)
        if (!isInProject) return
        presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(e: AnActionEvent) {
        //这里需要兼容直接从项目根目录右键打开菜单的情况
        val file = getFile(e) ?: return
        val rootInfo = file.fileInfo?.rootInfo ?: return
        if (!isAvailable(rootInfo)) return
        //必须位于当前项目中
        val project = e.project ?: return
        val isInProject = ProjectFileIndex.getInstance(project).isInContent(file)
        if (!isInProject) return
        //打开配置前确保已有配置数据
        application.messageBus.syncPublisher(ParadoxRootInfoListener.TOPIC).onAdd(rootInfo)

        showSettingsDialog(rootInfo, project)
    }

    private fun getFile(e: AnActionEvent): VirtualFile? {
        return e.getData(CommonDataKeys.VIRTUAL_FILE)
            ?: e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY)?.singleOrNull()
            ?: e.getData(PlatformCoreDataKeys.SELECTED_ITEM)?.castOrNull<PsiDirectoryNode>()?.virtualFile
    }

    protected abstract fun isAvailable(rootInfo: ParadoxRootInfo): Boolean

    protected abstract fun showSettingsDialog(rootInfo: ParadoxRootInfo, project: Project)
}
