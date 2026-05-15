package icu.windea.pls.lang.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.model.ParadoxGameType
import javax.swing.JComponent

class BrowseUrlsDialog(val contextFile: VirtualFile?, val project: Project?): DialogWrapper(project) {
    // com.intellij.diagnostic.specialPaths.BrowseSpecialPathsDialog

    init {
        title = PlsBundle.message("dialog.title.browseUrls")
        init()
        pack()
    }

    override fun createCenterPanel(): JComponent? {
        TODO("Not yet implemented")
    }

    // TODO 2.1.9
}
