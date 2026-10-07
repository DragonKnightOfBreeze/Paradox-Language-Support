package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.search.ParadoxFilePathSearch
import icu.windea.pls.lang.search.util.contextSensitive

class GotoFilesHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxFiles"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val fileInfo = file.fileInfo ?: return null
        val path = fileInfo.path.path
        val sourceElement = file
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.files.search", file.name)) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxFilePathSearch.selector(project, file).contextSensitive()
                val resolved = ParadoxFilePathSearch.search(path, null, selector, ignoreLocale = true).findAll()
                targets.addAll(resolved.mapNotNull { it.toPsiFile(project) })
            }
        }
        return getGotoData(sourceElement, targets)
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = getFileName(sourceElement) ?: return ""
        return ChronicleBundle.message("script.goto.files.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = getFileName(sourceElement) ?: return ""
        return ChronicleBundle.message("script.goto.files.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.files.notFoundMessage")
    }

    private fun getFileName(sourceElement: PsiElement): String? {
        return sourceElement.castOrNull<PsiFile>()?.name
    }
}
