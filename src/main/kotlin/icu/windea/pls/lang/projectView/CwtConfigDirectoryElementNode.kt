package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.NodeSortOrder
import com.intellij.ide.projectView.NodeSortSettings
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.nodes.PsiFileNode
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.ide.util.treeView.ValidateableNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.util.PlatformIcons
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.ep.configGroup.CwtConfigGroupFileProvider

@Suppress("UnstableApiUsage")
class CwtConfigDirectoryElementNode(
    project: Project,
    value: CwtConfigDirectoryElement,
    viewSettings: ViewSettings
) : ProjectViewNode<CwtConfigDirectoryElement>(project, value, viewSettings), ValidateableNode {
    override fun canRepresent(element: Any?): Boolean {
        return when {
            element is PsiDirectory -> canRepresent(element.virtualFile)
            element is VirtualFile -> canRepresent(element)
            else -> false
        }
    }

    private fun canRepresent(file: VirtualFile): Boolean {
        if (!file.isDirectory) return false
        val gameType = value.gameType
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType)
            val relativePaths = "$directoryName/${value.path}".split('/').toTypedArray()
            val nodeFile = VfsUtil.findRelativeFile(rootDirectory, *relativePaths) ?: return@f
            if (!nodeFile.isDirectory) return@f
            if (nodeFile == file) return true
        }
        return false
    }

    override fun contains(file: VirtualFile): Boolean {
        val gameType = value.gameType
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType)
            val relativePaths = "$directoryName/${value.path}".split('/').toTypedArray()
            val nodeFile = VfsUtil.findRelativeFile(rootDirectory, *relativePaths) ?: return@f
            if (!nodeFile.isDirectory) return@f
            if (VfsUtil.isAncestor(nodeFile, file, false)) return true
        }
        return false
    }

    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        if (value == null) return emptySet()
        val gameType = value.gameType
        val children = mutableSetOf<AbstractTreeNode<*>>()
        val directoryNames = mutableSetOf<String>()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType)
            val relativePaths = "$directoryName/${value.path}".split('/').toTypedArray()
            val nodeFile = VfsUtil.findRelativeFile(rootDirectory, *relativePaths) ?: return@f
            if (!nodeFile.isDirectory) return@f
            nodeFile.children.forEach { file ->
                if (file.isDirectory) {
                    if (!directoryNames.add(file.name)) return@f
                    val element = CwtConfigDirectoryElement(project, "${value.path}/${file.name}", value.gameType)
                    val elementNode = CwtConfigDirectoryElementNode(project, element, settings)
                    children += elementNode
                } else {
                    val psiFile = file.toPsiFile(project) ?: return@f
                    val elementNode = PsiFileNode(project, psiFile, settings)
                    children += elementNode
                }
            }
        }
        return children
    }

    override fun isValid(): Boolean {
        val gameType = value.gameType
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            if (!fileProvider.isEnabled) return@f
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val directoryName = fileProvider.getDirectoryName(project, gameType)
            val relativePaths = "$directoryName/${value.path}".split('/').toTypedArray()
            val nodeFile = VfsUtil.findRelativeFile(rootDirectory, *relativePaths) ?: return@f
            if (!nodeFile.isDirectory) return@f
            return true
        }
        return false
    }

    override fun update(presentation: PresentationData) {
        if (value == null) return
        try {
            if (isValid) {
                presentation.setIcon(PlatformIcons.FOLDER_ICON)
                presentation.presentableText = value.path.substringAfterLast('/')
                return
            }
        } catch (_: Exception) {
            //ignored
        }
        value = null
    }

    override fun getTitle(): String? {
        if (value == null) return null
        return value.path.substringAfterLast('/')
    }

    override fun getSortOrder(settings: NodeSortSettings): NodeSortOrder {
        return if (settings.isFoldersAlwaysOnTop) NodeSortOrder.FOLDER else super.getSortOrder(settings)
    }

    override fun getTypeSortWeight(sortByType: Boolean): Int {
        return 4
    }

    override fun isAlwaysShowPlus(): Boolean {
        return true
    }
}
