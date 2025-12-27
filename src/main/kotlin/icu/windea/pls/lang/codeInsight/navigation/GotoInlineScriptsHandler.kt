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
import icu.windea.pls.core.escapeXml
import icu.windea.pls.lang.psi.ParadoxPsiFinder
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.util.*

class GotoInlineScriptsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxInlineScripts"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        if (!ParadoxPsiMatcher.isInlineScriptUsage(element, gameType)) return null
        val expression = ParadoxInlineScriptManager.getInlineScriptExpressionFromUsageElement(element, resolve = true) ?: return null
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        runWithModalProgressBlocking(project, PlsBundle.message("script.goto.inlineScripts.search", expression)) {
            // need read actions here if necessary
            readAction {
                ParadoxInlineScriptManager.getInlineScriptFiles(expression, project, element).let { targets.addAll(it) }
            }
        }
        return GotoData(element, targets.distinct().toTypedArray(), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFinder.findScriptProperty(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        if (sourceElement !is ParadoxScriptProperty) return ""
        val expression = ParadoxInlineScriptManager.getInlineScriptExpressionFromUsageElement(sourceElement, resolve = true)
        if (expression.isNullOrEmpty()) return ""
        return PlsBundle.message("script.goto.inlineScripts.chooseTitle", expression.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        if (sourceElement !is ParadoxScriptProperty) return ""
        val expression = ParadoxInlineScriptManager.getInlineScriptExpressionFromUsageElement(sourceElement, resolve = true)
        if (expression.isNullOrEmpty()) return ""
        return PlsBundle.message("script.goto.inlineScripts.findUsagesTitle", expression.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.inlineScripts.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
