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
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import java.util.*

class GotoDefinitionInjectionTargetsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefinitionInjectionTargets"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null
        val info = ParadoxDefinitionInjectionManager.getInfo(element) ?: return null
        if (info.target.isEmpty()) return null // 排除目标为空的情况
        if (info.type.isEmpty()) return null // 排除目标定义的类型为空的情况
        val targets = Collections.synchronizedList(mutableListOf<PsiElement>())
        runWithModalProgressBlocking(project, PlsBundle.message("script.goto.definitionInjectionTargets.search", info.target)) {
            // need read actions here if necessary
            readAction {
                val selector = selector(project, element).definition().contextSensitive()
                val resolved = ParadoxDefinitionSearch.search(info.target, info.type, selector).findAll()
                targets.addAll(resolved)
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
        val target = ParadoxDefinitionInjectionManager.getInfo(sourceElement)?.target
        if (target.isNullOrEmpty()) return ""
        return PlsBundle.message("script.goto.definitionInjectionTargets.chooseTitle", target.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        if (sourceElement !is ParadoxScriptProperty) return ""
        val target = ParadoxDefinitionInjectionManager.getInfo(sourceElement)?.target
        if (target.isNullOrEmpty()) return ""
        return PlsBundle.message("script.goto.definitionInjectionTargets.findUsagesTitle", target.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.definitionInjectionTargets.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
