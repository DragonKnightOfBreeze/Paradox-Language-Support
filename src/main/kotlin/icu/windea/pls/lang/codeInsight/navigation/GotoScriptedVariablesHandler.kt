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
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.model.ParadoxTargetInfo
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

class GotoScriptedVariablesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxScriptedVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        if (!ParadoxPsiMatchService.isScriptedVariable(element)) return null
        val name = element.name?.orNull() ?: return null
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.scriptedVariables.search", name)) {
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
        if (targets.isNotEmpty()) targets.removeIf { it == element } // remove current from targets
        return GotoData(element, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptScriptedVariable? {
        return ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME }
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxTargetInfo.from(sourceElement)?.name ?: return ""
        return ChronicleBundle.message("script.goto.scriptedVariables.chooseTitle", name.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxTargetInfo.from(sourceElement)?.name ?: return ""
        return ChronicleBundle.message("script.goto.scriptedVariables.findUsagesTitle", name.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.scriptedVariables.notFoundMessage")
    }
}
