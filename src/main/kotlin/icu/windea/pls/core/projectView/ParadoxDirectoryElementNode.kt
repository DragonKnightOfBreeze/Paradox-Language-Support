package icu.windea.pls.core.projectView

import com.intellij.ide.projectView.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.*

class ParadoxDirectoryElementNode(
    project: Project,
    value: ParadoxDirectoryElement,
    viewSettings: ViewSettings
) : ProjectViewNode<ParadoxDirectoryElement>(project, value, viewSettings), ValidateableNode {
    override fun contains(file: VirtualFile): Boolean {
        return file.fileInfo?.pathToEntry?.parent == value.path.parent
    }
    
    override fun getChildren(): MutableCollection<out AbstractTreeNode<*>> {
        TODO("Not yet implemented")
    }
    
    override fun isValid(): Boolean {
        TODO("Not yet implemented")
    }
    
    override fun update(presentation: PresentationData) {
        try {
            
        } catch(e: Exception) {
            //ignored
        }
        value = null
    }
    
    override fun getTitle(): String? {
        return value?.path?.fileName
    }
    
    override fun isAlwaysShowPlus(): Boolean {
        return super.isAlwaysShowPlus()
    }
}