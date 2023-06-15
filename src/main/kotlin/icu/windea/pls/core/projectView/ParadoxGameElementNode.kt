package icu.windea.pls.core.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*

class ParadoxGameElementNode(
    project: Project,
    value: ParadoxGameElement,
    viewSettings: ViewSettings
) : ProjectViewNode<ParadoxGameElement>(project, value, viewSettings) {
    override fun canRepresent(element: Any?): Boolean {
        return when {
            element is PsiDirectory -> element.fileInfo?.pathToEntry?.length == 0
            element is VirtualFile -> element.isDirectory && element.fileInfo?.pathToEntry?.length == 0
            else -> false
        }
    }
    
    override fun contains(file: VirtualFile): Boolean {
        if(value == null) return false
        val fileInfo = file.fileInfo ?: return false
        if(fileInfo.rootInfo.gameType != value.gameType) return false
        if(fileInfo.pathToEntry.length != 1) return false
        return true
    }
    
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        if(value == null) return emptyList()
        val selector = fileSelector(project, value.preferredRootFile).withGameType(value.gameType)
        val children = mutableListOf<AbstractTreeNode<*>>()
        val directoryNames = mutableSetOf<String>()
        ParadoxFilePathSearch.search(null, selector).processQuery p@{ file ->
            val fileInfo = file.fileInfo ?: return@p true
            if(fileInfo.pathToEntry.length != 1) return@p true
            if(file.isDirectory && directoryNames.add(file.name)) {
                //直接位于游戏或模组目录中，且未被排除
                val fileData = FileBasedIndex.getInstance().getFileData(ParadoxFilePathIndex.NAME, file, project)
                if(!fileData.values.single().included) return@p true
                val element = ParadoxDirectoryElement(project, fileInfo.pathToEntry, fileInfo.rootInfo.gameType, value.preferredRootFile)
                val elementNode = ParadoxDirectoryElementNode(project, element, settings)
                children.add(elementNode)
            }
            true
        }
        return children
    }
    
    override fun update(presentation: PresentationData) {
        if(value == null) return
        presentation.setIcon(PlsIcons.GameDirectory)
        presentation.presentableText = value.gameType.description
    }
    
    override fun getTitle(): String? {
        if(value == null) return null
        return value.gameType.description
    }
    
    override fun isAlwaysShowPlus(): Boolean {
        return true
    }
}