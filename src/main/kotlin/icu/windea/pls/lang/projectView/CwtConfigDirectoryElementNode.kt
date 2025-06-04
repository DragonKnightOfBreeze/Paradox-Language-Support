package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.configGroup.*

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
            val relativePath = "$directoryName/${value.path}"
            val nodeFile = VfsUtil.findRelativeFile(rootDirectory, relativePath) ?: return@f
            if(!nodeFile.isDirectory) return@f
            return nodeFile == file
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
            val relativePath = "$directoryName/${value.path}"
            val nodeFile = VfsUtil.findRelativeFile(rootDirectory, relativePath) ?: return@f
            if(!nodeFile.isDirectory) return@f
            return VfsUtil.isAncestor(nodeFile, file, false)
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
            val relativePath = "$directoryName/${value.path}"
            val nodeFile = VfsUtil.findRelativeFile(rootDirectory, relativePath) ?: return@f
            if(!nodeFile.isDirectory) return@f
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
            val relativePath = "$directoryName/${value.path}"
            val nodeFile = VfsUtil.findRelativeFile(rootDirectory, relativePath) ?: return@f
            if(!nodeFile.isDirectory) return@f
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
        } catch (e: Exception) {
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
