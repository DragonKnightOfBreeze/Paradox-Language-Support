package icu.windea.pls.core.projectView

import com.intellij.icons.*
import com.intellij.ide.*
import com.intellij.ide.projectView.*
import com.intellij.ide.projectView.impl.*
import com.intellij.ide.util.treeView.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.registry.*
import com.intellij.psi.*
import com.intellij.util.concurrency.annotations.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import javax.swing.tree.*

//com.intellij.ide.projectView.impl.PackageViewPane

class ParadoxFilesViewPane(project: Project) : AbstractProjectViewPaneWithAsyncSupport(project) {
    companion object {
        const val ID = "ParadoxFilesPane"
    }
    
    override fun getTitle() = PlsBundle.message("title.paradox.files")
    
    override fun getIcon() = AllIcons.Nodes.CopyOfFolder
    
    override fun getId() = ID
    
    @RequiresBackgroundThread(generateAssertion = false)
    override fun getSelectedDirectories(objects: Array<out Any>): Array<PsiDirectory> {
        val directories = mutableListOf<PsiDirectory>()
        for(obj in objects) {
            when(obj) {
                is ParadoxDirectoryElementNode -> {
                    val directoryElement = obj.value
                    if(directoryElement != null) {
                        val path = directoryElement.path.path
                        val preferredRootFile = directoryElement.preferredRootFile
                        val selector = fileSelector(myProject, preferredRootFile).withGameType(directoryElement.gameType)
                        val files = ParadoxFilePathSearch.search(path, null, selector).findAll()
                        files.forEach { file ->
                            if(file.isDirectory) {
                                val dir = file.toPsiDirectory(myProject)
                                if(dir != null) directories.add(dir)
                            }
                        }
                    }
                }
                is ParadoxGameElementNode -> {
                    val directoryElement = obj.value
                    if(directoryElement != null) {
                        val preferredRootFile = directoryElement.preferredRootFile
                        val selector = fileSelector(myProject, preferredRootFile).withGameType(directoryElement.gameType)
                        val files = ParadoxFilePathSearch.search("", null, selector).findAll()
                        files.forEach { file ->
                            if(file.isDirectory) {
                                val dir = file.toPsiDirectory(myProject)
                                if(dir != null) directories.add(dir)
                            }
                        }
                    }
                }
            }
        }
        if(directories.isNotEmpty()) return directories.toTypedArray()
        return super.getSelectedDirectories(objects)
    }
    
    override fun createSelectInTarget(): SelectInTarget {
        return ParadoxFilesPaneSelectInTarget(myProject)
    }
    
    override fun createStructure(): ProjectAbstractTreeStructureBase {
        return object : ProjectTreeStructure(myProject, ID) {
            override fun createRoot(project: Project, settings: ViewSettings): AbstractTreeNode<*> {
                return ParadoxFilesViewProjectNode(project, settings)
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
    
    //icu.windea.pls.core.projectView.ParadoxFilesViewProjectNode
    //  icu.windea.pls.core.projectView.ParadoxGameElementNode
    //    icu.windea.pls.core.projectView.ParadoxDirectoryElementNode / PsiFiles
    
    override fun getWeight() = 100 //very low
}
