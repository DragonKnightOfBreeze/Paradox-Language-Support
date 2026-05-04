package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.codeInsight.navigation.activateFileWithPsiElement
import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.escapeXml
import icu.windea.pls.lang.defineVariableInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.search.ParadoxDefineVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptProperty

class GotoDefineVariablesHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefineVariables"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        val defineVariableInfo = element.defineVariableInfo ?: return null
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, PlsBundle.message("script.goto.defineVariables.search", defineVariableInfo.expression)) {
            // need read actions here if necessary
            readAction {
                val selector = selector(project, element).define().contextSensitive()
                val resolved = ParadoxDefineVariableSearch.search(defineVariableInfo.namespace, defineVariableInfo.variable, selector).findAll()
                targets.addAll(resolved)
            }
        }
        if (targets.isNotEmpty()) targets.removeIf { it == element } // remove current from targets
        return GotoData(element, targets.distinct().toTypedArray(), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFileManager.findScriptProperty(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val defineVariableInfo = sourceElement.castOrNull<ParadoxScriptProperty>()?.defineVariableInfo ?: return ""
        val expression = defineVariableInfo.expression
        return PlsBundle.message("script.goto.defineVariables.chooseTitle", expression.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val defineVariableInfo = sourceElement.castOrNull<ParadoxScriptProperty>()?.defineVariableInfo ?: return ""
        val expression = defineVariableInfo.expression
        return PlsBundle.message("script.goto.defineVariables.findUsagesTitle", expression.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.defineVariables.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
