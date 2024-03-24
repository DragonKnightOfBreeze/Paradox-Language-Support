package icu.windea.pls.lang.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.indexing.*
import icons.*
import icu.windea.pls.core.*
import icu.windea.pls.core.index.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*

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
        if(!file.isDirectory) return false
        val fileInfo = file.fileInfo ?: return false
        if(value.gameType != fileInfo.rootInfo.gameType) return false
        if(fileInfo.pathToEntry.isNotEmpty()) return false
        return true
    }
    
    override fun contains(file: VirtualFile): Boolean {
        if(value == null) return false
        val fileInfo = file.fileInfo ?: return false
        if(value.gameType != fileInfo.rootInfo.gameType) return false
        if(fileInfo.pathToEntry.isEmpty()) return false
        return true
    }
    
    override fun getChildren(): Collection<AbstractTreeNode<*>> {
        if(value == null) return emptySet()
        val selector = fileSelector(project, value.preferredRootFile).withGameType(value.gameType)
        val children = mutableSetOf<AbstractTreeNode<*>>()
        val directoryNames = mutableSetOf<String>()
        ParadoxFilePathSearch.search(null, selector).processQuery p@{ file ->
            val fileInfo = file.fileInfo ?: return@p true
            if(fileInfo.pathToEntry.length != 1) return@p true
            if(file.isDirectory) {
                //直接位于游戏或模组目录中，且未被排除
                if(!directoryNames.add(file.name)) return@p true
                val fileData = FileBasedIndex.getInstance().getFileData(ParadoxFilePathIndexInstance.name, file, project)
                if(!fileData.values.single().included) return@p true
                val element = ParadoxDirectoryElement(project, fileInfo.pathToEntry, fileInfo.rootInfo.gameType, value.preferredRootFile)
                val elementNode = ParadoxDirectoryElementNode(project, element, settings)
                children += elementNode
            } else {
                ////排除直接位于根目录下的文件
                //val psiFile = file.toPsiFile(project) ?: return@p true
                //val elementNode = PsiFileNode(project, psiFile, settings)
                //children += elementNode
            }
            true
        }
        return children
    }
    
    override fun update(presentation: PresentationData) {
        if(value == null) return
        presentation.setIcon(PlsIcons.GameDirectory)
        presentation.presentableText = value.gameType.title
    }
    
    override fun getTitle(): String? {
        if(value == null) return null
        return value.gameType.title
    }
    
    override fun isAlwaysShowPlus(): Boolean {
        return true
    }
}