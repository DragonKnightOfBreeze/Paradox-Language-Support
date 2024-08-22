package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.nodes.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import com.intellij.util.indexing.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.index.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*

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
        if(!file.isDirectory) return false
        val fileInfo = file.fileInfo ?: return false
        if(value.gameType != fileInfo.rootInfo.gameType) return false
        if(value.path != fileInfo.pathToEntry) return false
        return true
    }
    
    override fun contains(file: VirtualFile): Boolean {
        if(value == null) return false
        val fileInfo = file.fileInfo ?: return false
        if(value.gameType != fileInfo.rootInfo.gameType) return false
        if(!value.path.path.matchesPath(fileInfo.pathToEntry.parent)) return false
        return true
    }
    
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        if(value == null) return emptySet()
        val selector = fileSelector(project, value.preferredRootFile).withGameType(value.gameType)
        val directoryNames = mutableSetOf<String>()
        val query = ParadoxFilePathSearch.search(null, selector)
        val files = sortedSetOf(query.getPriorityComparator()) //按照覆盖顺序进行排序
        query.processQuery p@{ file ->
            val fileInfo = file.fileInfo ?: return@p true
            if(fileInfo.pathToEntry.parent != value.path.path) return@p true
            if(file.isDirectory) {
                //直接位于入口目录中，且未被排除
                if(!directoryNames.add(file.name)) return@p true
                val fileData = FileBasedIndex.getInstance().getFileData(ParadoxFilePathIndex.NAME, file, project)
                if(!fileData.values.single().included) return@p true
                files.add(file)
            } else {
                //直接位于游戏或模组目录中
                files.add(file)
            }
            true
        }
        return files.mapNotNull { file ->
            if(file.isDirectory) {
                val fileInfo = file.fileInfo ?: return@mapNotNull null
                val element = ParadoxDirectoryElement(project, fileInfo.pathToEntry, fileInfo.rootInfo.gameType, value.preferredRootFile)
                ParadoxDirectoryElementNode(project, element, settings)
            } else {
                val psiFile = file.toPsiFile(project) ?: return@mapNotNull null
                PsiFileNode(project, psiFile, settings)
            }
        }
    }
    
    override fun isValid(): Boolean {
        if(value == null) return false
        val selector = fileSelector(project, value.preferredRootFile).withGameType(value.gameType)
        return ParadoxFilePathSearch.search(value.path.path, null, selector).findFirst() != null
    }
    
    override fun update(presentation: PresentationData) {
        if(value == null) return
        try {
            if(isValid) {
                presentation.setIcon(PlatformIcons.FOLDER_ICON)
                presentation.presentableText = value.path.fileName
                return
            }
        } catch(e: Exception) {
            //ignored
        }
        value = null
    }
    
    override fun getTitle(): String? {
        if(value == null) return null
        return value.path.fileName
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
