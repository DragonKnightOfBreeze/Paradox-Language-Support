package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.projectView.ProjectViewNode
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.PlsIcons
import icu.windea.pls.core.process
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.PlsIndexKeys
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType

class ParadoxGameElementNode(
    project: Project,
    value: ParadoxGameElement,
    viewSettings: ViewSettings
) : ProjectViewNode<ParadoxGameElement>(project, value, viewSettings) {
    override fun canRepresent(element: Any?): Boolean {
        return when {
            element is PsiDirectory -> canRepresent(element.virtualFile)
            element is VirtualFile -> canRepresent(element)
            else -> false
        }
    }

    private fun canRepresent(file: VirtualFile): Boolean {
        if (!file.isDirectory) return false
        val fileInfo = file.fileInfo ?: return false
        if (value.gameType != fileInfo.rootInfo.gameType) return false
        if (fileInfo.path.isNotEmpty()) return false
        return true
    }

    override fun contains(file: VirtualFile): Boolean {
        if (value == null) return false
        val fileInfo = file.fileInfo ?: return false
        if (value.gameType != fileInfo.rootInfo.gameType) return false
        if (fileInfo.path.isEmpty()) return false
        return true
    }

    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        if (value == null) return emptySet()
        val selector = selector(project, value.preferredRootFile).file().withGameType(value.gameType)
        val children = mutableSetOf<AbstractTreeNode<*>>()
        val directoryNames = mutableSetOf<String>()
        ParadoxFilePathSearch.search(null, null, selector).process p@{ file ->
            val fileInfo = file.fileInfo ?: return@p true
            if (fileInfo.path.length != 1) return@p true // 必须直接位于入口目录中
            if(!fileInfo.inMainOrExtraEntry) return@p true // 必须位于合法的入口目录中
            if (!isIncluded(file)) return@p true // 必须未被排除

            // 必须是目录
            if (file.isDirectory) {
                if (!directoryNames.add(file.name)) return@p true
                val element = ParadoxDirectoryElement(project, fileInfo.path, fileInfo.rootInfo.gameType, value.preferredRootFile)
                val elementNode = ParadoxDirectoryElementNode(project, element, settings)
                children += elementNode
            } else {
                // val psiFile = file.toPsiFile(project) ?: return@p true
                // val elementNode = PsiFileNode(project, psiFile, settings)
                // children += elementNode
            }
            true
        }
        return children
    }

    private fun isIncluded(file: VirtualFile): Boolean {
        return FileBasedIndex.getInstance().getFileData(PlsIndexKeys.FilePath, file, project).values.single().included
    }

    override fun update(presentation: PresentationData) {
        if (value == null) return
        presentation.setIcon(PlsIcons.General.GameDirectory)
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
