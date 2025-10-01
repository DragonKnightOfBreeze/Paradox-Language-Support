package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.codeInsight.navigation.activateFileWithPsiElement
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.file
import icu.windea.pls.lang.search.selector.selector
import java.util.*

class GotoFilesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxFiles"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path.path
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        runWithModalProgressBlocking(project, PlsBundle.message("script.goto.files.search", file.name)) {
            // need read actions here if necessary
            readAction {
                val selector = selector(project, file).file().contextSensitive()
                val resolved = ParadoxFilePathSearch.search(path, null, selector, ignoreLocale = true).findAll()
                resolved.forEach { targets.add(it.toPsiFile(project)) }
            }
        }
        if (targets.isNotEmpty()) targets.removeIf { it == file } // remove current file from targets
        return GotoData(file, targets.distinct().toTypedArray(), emptyList())
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val fileName = sourceElement.castOrNull<PsiFile>()?.name ?: return ""
        return PlsBundle.message("script.goto.files.chooseTitle", fileName.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val fileName = sourceElement.castOrNull<PsiFile>()?.name ?: return ""
        return PlsBundle.message("script.goto.files.findUsagesTitle", fileName.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.files.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
