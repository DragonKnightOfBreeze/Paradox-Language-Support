package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.ep.configGroup.*
import icu.windea.pls.model.*

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
        if(!file.isDirectory) return false
        val gameTypeId = value.gameType.id
        CwtConfigGroupFileProvider.EP_NAME.extensionList.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val dir = rootDirectory.findChild(gameTypeId) ?: return@f
            val relativePath = VfsUtil.getRelativePath(file, dir)
            if(relativePath.isNotNullOrEmpty()) return true
        }
        return false
    }
    
    override fun contains(file: VirtualFile): Boolean {
        CwtConfigGroupFileProvider.EP_NAME.extensionList.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val relativePath = VfsUtil.getRelativePath(file, rootDirectory) ?: return@f
            val gameId = relativePath.substringBefore('/')
            return ParadoxGameType.canResolve(gameId)
        }
        return false
    }
    
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        if(value == null) return emptySet()
        val gameTypeId = value.gameType.id
        val path = value.path
        val children = mutableSetOf<AbstractTreeNode<*>>()
        val directoryNames = mutableSetOf<String>()
        CwtConfigGroupFileProvider.EP_NAME.extensionList.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val dir = rootDirectory.findChild(gameTypeId)?.let { VfsUtil.findRelativeFile(it, path) } ?: return@f
            dir.children.forEach { file ->
                if(file.isDirectory) {
                    if(!directoryNames.add(file.name)) return@f
                    val element = CwtConfigDirectoryElement(project, "$path/${file.name}", value.gameType)
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
        val gameTypeId = value.gameType.id
        CwtConfigGroupFileProvider.EP_NAME.extensionList.forEach f@{ fileProvider ->
            val rootDirectory = fileProvider.getRootDirectory(project) ?: return@f
            val dir = rootDirectory.findChild(gameTypeId) ?: return@f
            val relativeFile = VfsUtil.findRelativeFile(dir, value.path)
            if(relativeFile != null) return true
        }
        return false
    }
    
    override fun update(presentation: PresentationData) {
        if(value == null) return
        try {
            if(isValid) {
                presentation.setIcon(PlatformIcons.FOLDER_ICON)
                presentation.presentableText = value.path.substringAfterLast('/')
                return
            }
        } catch(e: Exception) {
            //ignored
        }
        value = null
    }
    
    override fun getTitle(): String? {
        if(value == null) return null
        return value.path.substringAfterLast('/')
    }
    
    override fun getSortOrder(settings: NodeSortSettings): NodeSortOrder {
        return if(settings.isFoldersAlwaysOnTop) NodeSortOrder.FOLDER else super.getSortOrder(settings)
    }
    
    override fun getTypeSortWeight(sortByType: Boolean): Int {
        return 4
    }
    
    override fun isAlwaysShowPlus(): Boolean {
        return true
    }
}
