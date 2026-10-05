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
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive

class GotoDefineVariablesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefineVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = ParadoxPsiFileService.findScriptProperty(file, offset) ?: return null
        val expression = ParadoxPsiPresentationService.getExpressionForDefineVariable(element) ?: return null
        val defineVariableInfo = element.defineVariableInfo ?: return null
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.defineVariables.search", expression.escapeXml().or.anonymous())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxDefineVariableSearch.selector(project, element).contextSensitive()
                val resolved = ParadoxDefineVariableSearch.search(defineVariableInfo.namespace, defineVariableInfo.variable, selector).findAll()
                targets.addAll(resolved)
            }
        }
        if (targets.isNotEmpty()) targets.removeIf { it == element } // remove current from targets
        return GotoData(element, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val expression = ParadoxPsiPresentationService.getExpressionForDefineVariable(sourceElement) ?: name.orEmpty()
        return ChronicleBundle.message("script.goto.defineVariables.chooseTitle", expression.escapeXml().or.anonymous())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val expression = ParadoxPsiPresentationService.getExpressionForDefineVariable(sourceElement) ?: name.orEmpty()
        return ChronicleBundle.message("script.goto.defineVariables.findUsagesTitle", expression.escapeXml().or.anonymous())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.defineVariables.notFoundMessage")
    }
}
