package icu.windea.pls.core.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.util.indexing.FileBasedIndex
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
        return false //nothing, not for game and mod directories
    }
    
    override fun contains(file: VirtualFile): Boolean {
        val fileGameType = file.fileInfo?.rootInfo?.gameType
        return fileGameType != null && fileGameType == value
    }
    
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        //这里仅返回那些直接位于游戏或模组根目录中，且未被排除的子目录
        val selector = fileSelector(project).withGameType(value).distinctByFilePath()
        val children = mutableListOf<AbstractTreeNode<*>>()
        ParadoxFilePathSearch.search(null, selector).processQueryAsync p@{ file ->
            val fileData = FileBasedIndex.getInstance().getFileData(ParadoxFilePathIndex.NAME, file, project)
            if(!fileData.values.single().included) return@p true
            if(!file.isDirectory) return@p true
            val fileInfo = file.fileInfo ?: return@p true
            val node = ParadoxDirectoryElementNode(project, ParadoxDirectoryElement(fileInfo.pathToEntry, fileInfo.rootInfo.gameType), settings)
            children.add(node)
            true
        }
        return children
    }
    
    override fun update(presentation: PresentationData) {
        presentation.setIcon(PlsIcons.GameDirectory)
        presentation.presentableText = value.description
    }
    
    override fun getTitle(): String {
        return value.description
    }
    
    override fun isAlwaysShowPlus(): Boolean {
        return true
    }
}