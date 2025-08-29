package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.ModuleGroup
import com.intellij.ide.projectView.impl.nodes.AbstractProjectNode
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleGroupNode
import com.intellij.ide.projectView.impl.nodes.ProjectViewModuleNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider
import icu.windea.pls.model.ParadoxGameType

class CwtConfigFilesViewProjectNode(
    project: Project,
    viewSettings: ViewSettings
) : AbstractProjectNode(project, project, viewSettings) {
    override fun canRepresent(element: Any?): Boolean {
        return when {
            element is PsiDirectory -> canRepresent(element.virtualFile)
            element is VirtualFile -> canRepresent(element)
            else -> false
        }
    }

    private fun canRepresent(file: VirtualFile): Boolean {
        if (!file.isDirectory) return false
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            if (file == rootDirectory) return true
        }
        return false
    }

    override fun contains(file: VirtualFile): Boolean {
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val relativePath = VfsUtil.getRelativePath(file, rootDirectory) ?: return@f
            val directoryName = relativePath.substringBefore('/')
            val gameTypeId = fileProvider.getGameTypeIdFromDirectoryName(project, directoryName) ?: return@f
            if (ParadoxGameType.canResolve(gameTypeId)) return true
        }
        return false
    }

    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        val result = mutableSetOf<AbstractTreeNode<*>>()
        val coreElement = CwtConfigGameElement(project, null)
        val coreElementNode = CwtConfigGameElementNode(project, coreElement, settings)
        result += coreElementNode
        ParadoxGameType.entries.forEach { gameType ->
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
