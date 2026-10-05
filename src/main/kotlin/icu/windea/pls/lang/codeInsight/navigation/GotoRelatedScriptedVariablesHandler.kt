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
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.util.ParadoxLocalisationManager
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile

class GotoRelatedScriptedVariablesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedScriptedVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement? = null
        run {
            if (file !is ParadoxLocalisationFile) return@run
            val element = ParadoxPsiFileService.findLocalisation(file, offset) ?: return@run
            sourceElement = element
            val name = ParadoxPsiPresentationService.getNameForLocalisation(element) ?: return@run
            runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedScriptedVariables.search.l", name.orAnonymous().escapeXml())) {
                // need read actions here if necessary
                readAction {
                    val resolved = ParadoxLocalisationManager.getRelatedScriptedVariables(element)
                    targets.addAll(resolved)
                }
            }
        }
        if (targets.isEmpty() || sourceElement == null) return null // unavailable
        targets.removeIf { it == sourceElement } // remove current target from targets
        return GotoData(sourceElement, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedScriptedVariables.chooseTitle.l", name.orAnonymous().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        run {
            val name = ParadoxPsiPresentationService.getNameForLocalisation(sourceElement) ?: return@run
            return ChronicleBundle.message("script.goto.relatedScriptedVariables.findUsagesTitle.l", name.orAnonymous().escapeXml())
        }
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedScriptedVariables.notFoundMessage")
    }
}
