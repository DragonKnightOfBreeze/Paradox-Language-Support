package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.codeInsight.navigation.activateFileWithPsiElement
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.scriptedVariable
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.model.codeInsight.ParadoxTargetInfo
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import java.util.*

class GotoScriptedVariablesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxScriptedVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        val name = element.name?.orNull() ?: return null
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        val runResult = ProgressManager.getInstance().runProcessWithProgressSynchronously({
            // need read actions here if necessary
            runReadAction {
                val selector = selector(project, element).scriptedVariable().contextSensitive()
                ParadoxScriptedVariableSearch.searchLocal(name, selector).findAll().let { targets.addAll(it) }
            }
            runReadAction {
                val selector = selector(project, element).scriptedVariable().contextSensitive()
                ParadoxScriptedVariableSearch.searchGlobal(name, selector).findAll().let { targets.addAll(it) }
            }
        }, PlsBundle.message("script.goto.scriptedVariables.search", name), true, project)
        if (!runResult) return null
        if (targets.isNotEmpty()) targets.removeIf { it == element }
        return GotoData(element, targets.distinct().toTypedArray(), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptScriptedVariable? {
        return ParadoxPsiFinder.findScriptedVariable(file, offset) { BY_NAME }
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxTargetInfo.from(sourceElement)?.name ?: return ""
        return PlsBundle.message("script.goto.scriptedVariables.chooseTitle", name.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxTargetInfo.from(sourceElement)?.name ?: return ""
        return PlsBundle.message("script.goto.scriptedVariables.findUsagesTitle", name.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.scriptedVariables.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
