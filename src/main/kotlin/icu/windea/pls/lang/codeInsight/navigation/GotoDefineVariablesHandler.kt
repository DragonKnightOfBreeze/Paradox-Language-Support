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
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive

class GotoDefineVariablesHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefineVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val sourceElement = ParadoxPsiFileService.findScriptProperty(file, offset) ?: return null
        val expression = ParadoxPsiPresentationService.getExpressionForDefineVariable(sourceElement) ?: return null
        if (expression.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
        val info = sourceElement.defineVariableInfo ?: return null
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.defineVariables.search", expression.escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxDefineVariableSearch.selector(project, sourceElement).contextSensitive()
                val resolved = ParadoxDefineVariableSearch.search(info.namespace, info.variable, selector).findAll()
                targets.addAll(resolved)
            }
        }
        return getGotoData(sourceElement, targets)
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val expression = ParadoxPsiPresentationService.getExpressionForDefineVariable(sourceElement) ?: name ?: return ""
        return ChronicleBundle.message("script.goto.defineVariables.chooseTitle", expression.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val expression = ParadoxPsiPresentationService.getExpressionForDefineVariable(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.defineVariables.findUsagesTitle", expression.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.defineVariables.notFoundMessage")
    }
}
