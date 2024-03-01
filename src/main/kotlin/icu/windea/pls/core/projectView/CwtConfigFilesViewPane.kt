package icu.windea.pls.core.projectView

import com.intellij.icons.*
import com.intellij.ide.*
import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.registry.*
import com.intellij.psi.*
import icu.windea.pls.*
import javax.swing.tree.*

//com.intellij.ide.projectView.impl.PackageViewPane

class CwtConfigFilesViewPane(project: Project): AbstractProjectViewPaneWithAsyncSupport(project) {
    companion object {
        const val ID = "CwtConfigFilesPane"
    }
    
    override fun getTitle() = PlsBundle.message("title.cwt.config.files")
    
    override fun getIcon() = AllIcons.Nodes.CopyOfFolder
    
    override fun getId() = ID
    
    override fun getSelectedDirectories(selectedUserObjects: Array<out Any>): Array<PsiDirectory> {
        return super.getSelectedDirectories(selectedUserObjects)
    }
    
    override fun createSelectInTarget(): SelectInTarget {
        return CwtConfigFilesPaneSelectInTarget(myProject)
    }
    
    override fun createStructure(): AbstractTreeStructureBase {
        return object : ProjectTreeStructure(myProject, ID) {
            override fun createRoot(project: Project, settings: ViewSettings): AbstractTreeNode<*> {
                return CwtConfigFilesViewProjectNode(project, settings)
            }
            
            override fun isToBuildChildrenInBackground(element: Any): Boolean {
                return Registry.`is`("ide.projectView.PackageViewTreeStructure.BuildChildrenInBackground")
            }
        }
    }
    
        override fun createTree(treeModel: DefaultTreeModel): ProjectViewTree {
        return object : ProjectViewTree(treeModel) {
            override fun toString(): String {
                return title + " " + super.toString()
            }
        }
    }
    
    //icu.windea.pls.core.projectView.CwtConfigFilesViewProjectNode
    //  icu.windea.pls.core.projectView.CwtConfigGameElementNode
    //    icu.windea.pls.core.projectView.CwtConfigDirectoryElementNode / PsiFiles
    
    override fun getWeight() = 200 //lower than ParadoxFilesViewPane
}
