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
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive

class GotoScriptedVariablesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxScriptedVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement?
        val element = ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME } ?: return null
        if (!ParadoxPsiMatchService.isScriptedVariable(element)) return null
        sourceElement = element
        val name = ParadoxPsiPresentationService.getNameForScriptedVariable(element) ?: return null
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.scriptedVariables.search", name.orAnonymous().escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxScriptedVariableSearch.selector(project, element).contextSensitive()
                ParadoxScriptedVariableSearch.searchLocal(name, selector).findAll().let { targets.addAll(it) }
            }
            readAction {
                val selector = ParadoxScriptedVariableSearch.selector(project, element).contextSensitive()
                ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll().let { targets.addAll(it) }
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
        val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.scriptedVariables.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.scriptedVariables.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.scriptedVariables.notFoundMessage")
    }
}
