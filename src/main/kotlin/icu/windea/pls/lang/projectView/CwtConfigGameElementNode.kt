package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.model.*

class CwtConfigGameElementNode(
    project: Project,
    value: CwtConfigGameElement,
    viewSettings: ViewSettings
) : ProjectViewNode<CwtConfigGameElement>(project, value, viewSettings) {
    override fun canRepresent(element: Any?): Boolean {
        return when {
            element is PsiDirectory -> canRepresent(element.virtualFile)
            element is VirtualFile -> canRepresent(element)
            else -> false
        }
    }

    private fun canRepresent(file: VirtualFile): Boolean {
        if (!file.isDirectory) return false
        if (file.name != value.gameType.id) return false
        val rootDir = file.parent ?: return false
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            if (rootDir == rootDirectory) return true
        }
        return false
    }

    override fun contains(file: VirtualFile): Boolean {
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val relativePath = VfsUtil.getRelativePath(file, rootDirectory) ?: return@f
            val gameId = relativePath.substringBefore('/')
            return ParadoxGameType.canResolve(gameId)
        }
        return false
    }

    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        if (value == null) return emptySet()
        val gameTypeId = value.gameType.id
        val children = mutableSetOf<AbstractTreeNode<*>>()
        val directoryNames = mutableSetOf<String>()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val dir = rootDirectory.findChild(gameTypeId) ?: return@f
            dir.children.forEach { file ->
                if (file.isDirectory) {
                    if (!directoryNames.add(file.name)) return@f
                    val element = CwtConfigDirectoryElement(project, file.name, value.gameType)
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

    override fun update(presentation: PresentationData) {
        if (value == null) return
        presentation.setIcon(PlsIcons.GameDirectory)
        presentation.presentableText = value.gameType.title
    }

    override fun getTitle(): String? {
        if (value == null) return null
        return value.gameType.title
    }

    override fun isAlwaysShowPlus(): Boolean {
        return true
    }
}
