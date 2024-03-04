package icu.windea.pls.core.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.*
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.lang.configGroup.*
import icu.windea.pls.model.*

class CwtConfigFilesViewProjectNode(
    project: Project,
    viewSettings: ViewSettings
) : AbstractProjectNode(project, project, viewSettings) {
    override fun canRepresent(element: Any?): Boolean {
        return false
    }
    
    override fun contains(file: VirtualFile): Boolean {
        if(!file.isDirectory) return false
        val rootDir = file.parent ?: return false
        CwtConfigGroupFileProvider.EP_NAME.extensionList.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            if(rootDir == rootDirectory) return true
        }
        return false
    }
    
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        val result = mutableSetOf<AbstractTreeNode<*>>()
        val coreElement = CwtConfigGameElement(project, null)
        val coreElementNode = CwtConfigGameElementNode(project, coreElement, settings)
        result += coreElementNode
        ParadoxGameType.values.forEach { gameType ->
            val element = CwtConfigGameElement(project, gameType)
            val elementNode = CwtConfigGameElementNode(project, element, settings)
            result += elementNode
        }
        return result
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
