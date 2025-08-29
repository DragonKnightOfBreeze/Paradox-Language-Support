package icu.windea.pls.lang.projectView

import com.intellij.icons.AllIcons
import com.intellij.ide.SelectInTarget
import com.intellij.ide.projectView.ViewSettings
import com.intellij.ide.projectView.impl.AbstractProjectViewPaneWithAsyncSupport
import com.intellij.ide.projectView.impl.ProjectAbstractTreeStructureBase
import com.intellij.ide.projectView.impl.ProjectTreeStructure
import com.intellij.ide.projectView.impl.ProjectViewTree
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.registry.Registry
import com.intellij.psi.PsiDirectory
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.toPsiDirectory
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withGameType
import javax.swing.tree.DefaultTreeModel

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
        for (obj in objects) {
            when (obj) {
                is ParadoxDirectoryElementNode -> {
                    val directoryElement = obj.value
                    if (directoryElement != null) {
                        val path = directoryElement.path.path
                        val preferredRootFile = directoryElement.preferredRootFile
                        val selector = selector(myProject, preferredRootFile).file().withGameType(directoryElement.gameType)
                        val files = ParadoxFilePathSearch.search(path, null, selector).findAll()
                        files.forEach { file ->
                            if (file.isDirectory) {
                                val dir = file.toPsiDirectory(myProject)
                                if (dir != null) directories.add(dir)
                            }
                        }
                    }
                }
                is ParadoxGameElementNode -> {
                    val directoryElement = obj.value
                    if (directoryElement != null) {
                        val preferredRootFile = directoryElement.preferredRootFile
                        val selector = selector(myProject, preferredRootFile).file().withGameType(directoryElement.gameType)
                        val files = ParadoxFilePathSearch.search("", null, selector).findAll()
                        files.forEach { file ->
                            if (file.isDirectory) {
                                val dir = file.toPsiDirectory(myProject)
                                if (dir != null) directories.add(dir)
                            }
                        }
                    }
                }
            }
        }
        if (directories.isNotEmpty()) return directories.toTypedArray()
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

    //only include main entry directories and game files under them
    //e.g., DO NOT include directory "previewer_assets" and game files under it

    override fun getWeight() = 100 //very low
}
