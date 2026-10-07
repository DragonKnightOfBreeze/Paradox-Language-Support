package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager

class GotoLocalisationsHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val sourceElement = ParadoxPsiFileService.findLocalisation(file, offset) { BY_NAME } ?: return null
        if (!ParadoxPsiMatchService.isLocalisation(sourceElement)) return null
        val type = sourceElement.type ?: return null
        val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return null
        if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.localisations.search", name.escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxLocalisationSearch.selector(project, sourceElement).contextSensitive()
                    .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                val resolved = ParadoxLocalisationSearch.search(name, selector, type).findAll()
                targets.addAll(resolved)
            }
        }
        return getGotoData(sourceElement, targets)
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.localisations.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.localisations.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.localisations.notFoundMessage")
    }
}
