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
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.util.PlatformIcons
import com.intellij.util.indexing.FileBasedIndex
import icu.windea.pls.core.matchesPath
import icu.windea.pls.core.processQuery
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.index.ParadoxIndexKeys
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType

@Suppress("UnstableApiUsage")
class ParadoxDirectoryElementNode(
    project: Project,
    value: ParadoxDirectoryElement,
    viewSettings: ViewSettings
) : ProjectViewNode<ParadoxDirectoryElement>(project, value, viewSettings), ValidateableNode {
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
        if (value.path != fileInfo.path) return false
        return true
    }

    override fun contains(file: VirtualFile): Boolean {
        if (value == null) return false
        val fileInfo = file.fileInfo ?: return false
        if (value.gameType != fileInfo.rootInfo.gameType) return false
        if (!value.path.path.matchesPath(fileInfo.path.parent)) return false
        return true
    }

    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        if (value == null) return emptySet()
        val selector = selector(project, value.preferredRootFile).file().withGameType(value.gameType)
        val directoryNames = mutableSetOf<String>()
        val query = ParadoxFilePathSearch.search(null, selector)
        val files = sortedSetOf(query.getPriorityComparator()) //按照覆盖顺序进行排序
        query.processQuery p@{ file ->
            val fileInfo = file.fileInfo ?: return@p true
            if (fileInfo.path.parent != value.path.path) return@p true
            if (file.isDirectory) {
                //直接位于入口目录中，且未被排除
                if (!directoryNames.add(file.name)) return@p true
                val fileData = FileBasedIndex.getInstance().getFileData(ParadoxIndexKeys.FilePath, file, project)
                if (!fileData.values.single().included) return@p true
                files.add(file)
            } else {
                //直接位于入口目录中
                files.add(file)
            }
            true
        }
        return files.mapNotNull { file ->
            if (file.isDirectory) {
                val fileInfo = file.fileInfo ?: return@mapNotNull null
                val element = ParadoxDirectoryElement(project, fileInfo.path, fileInfo.rootInfo.gameType, value.preferredRootFile)
                ParadoxDirectoryElementNode(project, element, settings)
            } else {
                val psiFile = file.toPsiFile(project) ?: return@mapNotNull null
                PsiFileNode(project, psiFile, settings)
            }
        }
    }

    override fun isValid(): Boolean {
        if (value == null) return false
        val selector = selector(project, value.preferredRootFile).file().withGameType(value.gameType)
        return ParadoxFilePathSearch.search(value.path.path, null, selector).findFirst() != null
    }

    override fun update(presentation: PresentationData) {
        if (value == null) return
        try {
            if (isValid) {
                presentation.setIcon(PlatformIcons.FOLDER_ICON)
                presentation.presentableText = value.path.fileName
                return
            }
        } catch (e: Exception) {
            //ignored
        }
        value = null
    }

    override fun getTitle(): String? {
        if (value == null) return null
        return value.path.fileName
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
