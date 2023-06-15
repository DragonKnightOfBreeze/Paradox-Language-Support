package icu.windea.pls.core.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.*
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.application.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.model.*

class ParadoxFilesViewProjectNode(
    project: Project,
    viewSettings: ViewSettings
) : AbstractProjectNode(project, project, viewSettings) {
    override fun canRepresent(element: Any?): Boolean {
        return false //nothing
    }
    
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        //如果项目中存在游戏或模组目录，则仅使用这个游戏或模组目录对应的游戏类型作为子节点
        //否则使用从游戏目录依赖和模组目录依赖中得到的所有游戏类型作为子节点
        val gameTypes = enumSetOf<ParadoxGameType>()
        val rootInfos = ParadoxRootInfo.values.values
        for(rootInfo in rootInfos) {
            val isInProject = runReadAction { ProjectFileIndex.getInstance(project).isInContent(rootInfo.rootFile) }
            if(isInProject) return listOf(ParadoxGameElementNode(project, rootInfo.gameType, settings))
            gameTypes.add(rootInfo.gameType)
        }
        return gameTypes.sortedBy { it.ordinal }.map { gameType -> ParadoxGameElementNode(project, gameType, settings) }
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