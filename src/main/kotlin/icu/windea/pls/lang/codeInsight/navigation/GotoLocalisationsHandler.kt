package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.collections.toArray
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager

class GotoLocalisationsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxLocalisations"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement?
        val element = ParadoxPsiFileService.findLocalisation(file, offset) { BY_NAME } ?: return null
        if (!ParadoxPsiMatchService.isLocalisation(element)) return null
        val type = element.type ?: return null
        sourceElement = element
        val name = ParadoxPsiPresentationService.getNameForLocalisation(element) ?: return null
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.localisations.search", name.orAnonymous().escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
                val resolved = ParadoxLocalisationSearch.search(name, selector, type).findAll()
                targets.addAll(resolved)
            }
        }
        if (targets.isEmpty()) return null // unavailable
        targets.removeIf { it == sourceElement } // remove current target from targets
        return GotoData(sourceElement, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.localisations.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.localisations.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.localisations.notFoundMessage")
    }
}
