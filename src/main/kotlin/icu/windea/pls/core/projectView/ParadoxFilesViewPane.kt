package icu.windea.pls.core.projectView

import com.intellij.ide.*
import com.intellij.ide.projectView.impl.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icons.*
import icu.windea.pls.*
import javax.swing.tree.*

//com.intellij.ide.projectView.impl.PackageViewPane

class ParadoxFilesViewPane(project: Project) : AbstractProjectViewPaneWithAsyncSupport(project) {
    companion object {
        const val ID = "ParadoxFilesPane"
    }
    
    override fun getTitle() = PlsBundle.message("title.paradox.files")
    
    override fun getIcon() = PlsIcons.GameDirectory
    
    override fun getId() = ID
    
    override fun getSelectedDirectories(selectedUserObjects: Array<out Any>): Array<PsiDirectory> {
        TODO("Not yet implemented")
    }
    
    override fun createSelectInTarget(): SelectInTarget {
        return ParadoxFilesPaneSelectInTarget(myProject)
    }
    
    override fun createStructure(): ProjectAbstractTreeStructureBase {
        TODO("Not yet implemented")
    }
    
    override fun createTree(treeModel: DefaultTreeModel): ProjectViewTree {
        return object : ProjectViewTree(treeModel) {
            override fun toString(): String {
                return title + " " + super.toString()
            }
        }
    }
    
    override fun getWeight(): Int {
        return 100 //very bottom
    }
}