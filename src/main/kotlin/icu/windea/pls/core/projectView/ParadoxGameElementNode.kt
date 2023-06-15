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
import icu.windea.pls.lang.model.*

class ParadoxGameElementNode(
    project: Project,
    value: ParadoxGameType,
    viewSettings: ViewSettings
) : ProjectViewNode<ParadoxGameType>(project, value, viewSettings) {
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
        if(fileInfo.rootInfo.gameType != value) return false
        if(fileInfo.pathToEntry.length != 1) return false
        return true
    }
    
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        //这里仅返回那些直接位于游戏或模组根目录中，且未被排除的子目录
        if(value == null) return emptyList()
        val selector = fileSelector(project).withGameType(value)
        val children = mutableListOf<AbstractTreeNode<*>>()
        val directoryNames = mutableSetOf<String>()
        ParadoxFilePathSearch.search(null, selector).processQuery p@{ file ->
            val fileData = FileBasedIndex.getInstance().getFileData(ParadoxFilePathIndex.NAME, file, project)
            if(!fileData.values.single().included) return@p true
            val fileInfo = file.fileInfo ?: return@p true
            if(fileInfo.pathToEntry.length != 1) return@p true
            if(file.isDirectory && directoryNames.add(file.name)) {
                val element = ParadoxDirectoryElement(project, fileInfo.pathToEntry, fileInfo.rootInfo.gameType)
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
        presentation.presentableText = value.description
    }
    
    override fun getTitle(): String? {
        if(value == null) return null
        return value.description
    }
    
    override fun isAlwaysShowPlus(): Boolean {
        return true
    }
}