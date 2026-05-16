package icu.windea.pls.lang.ui.tools

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.ep.tools.SpecialUrlProvider
import icu.windea.pls.model.ParadoxGameType
import javax.swing.JComponent

/**
 * @see SpecialUrlProvider
 */
class BrowseSpecialUrlsDialog(
    val project: Project?,
    val file: VirtualFile? = null,
    val gameType: ParadoxGameType? = null,
) : DialogWrapper(project, false, IdeModalityType.MODELESS) { // NOTE modeless dialog
    // com.intellij.diagnostic.specialPaths.BrowseSpecialPathsDialog

    init {
        title = PlsBundle.message("dialog.title.browseSpecialUrls")
        init()
        pack()
    }

    override fun createCenterPanel(): JComponent? {
        TODO("Not yet implemented")
    }

    // TODO 2.1.9
}
