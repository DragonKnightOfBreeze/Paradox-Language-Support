package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.*
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*

class ParadoxFilesViewProjectNode(
    project: Project,
    viewSettings: ViewSettings
) : AbstractProjectNode(project, project, viewSettings) {
    override fun canRepresent(element: Any?): Boolean {
        return false
    }

    override fun contains(file: VirtualFile): Boolean {
        return file.fileInfo != null
    }

    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        //如果项目中存在游戏或模组目录，则仅使用这个游戏或模组目录对应的游戏类型作为子节点
        val rootPaths = mutableSetOf<String>()
        val profilesSettings = getProfilesSettings()
        rootPaths += profilesSettings.gameDescriptorSettings.keys
        rootPaths += profilesSettings.modDescriptorSettings.keys
        val projectFileIndex = ProjectFileIndex.getInstance(project)
        rootPaths.forEach f@{ rootPath ->
            val rootFile = VfsUtil.findFile(rootPath.toPath(), true) ?: return@f
            val gameType = selectGameType(rootFile) ?: return@f
            if (projectFileIndex.isInContent(rootFile)) {
                val element = ParadoxGameElement(project, gameType, rootFile)
                val elementNode = ParadoxGameElementNode(project, element, settings)
                return listOf(elementNode)
            }
        }
        return emptyList()
    }

    override fun createModuleGroup(module: Module): AbstractTreeNode<*> {
        return ProjectViewModuleNode(project, module, settings)
    }

    override fun createModuleGroupNode(moduleGroup: ModuleGroup): AbstractTreeNode<*> {
        return ProjectViewModuleGroupNode(project, moduleGroup, settings)
    }

    override fun someChildContainsFile(file: VirtualFile?): Boolean {
        return true
    }
}
