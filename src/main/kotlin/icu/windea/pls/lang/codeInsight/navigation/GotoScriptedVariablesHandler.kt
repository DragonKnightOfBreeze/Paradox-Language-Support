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
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive

class GotoScriptedVariablesHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxScriptedVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        val sourceElement = ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME } ?: return null
        val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: return null
        if (name.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.scriptedVariables.search", name.escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxScriptedVariableSearch.selector(project, sourceElement).contextSensitive()
                ParadoxScriptedVariableSearch.searchLocal(name, selector).findAll().let { targets.addAll(it) }
            }
            readAction {
                val selector = ParadoxScriptedVariableSearch.selector(project, sourceElement).contextSensitive()
                ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll().let { targets.addAll(it) }
            }
        }
        return getGotoData(sourceElement, targets)
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.scriptedVariables.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getNameForScriptedVariable(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.scriptedVariables.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.scriptedVariables.notFoundMessage")
    }
}
