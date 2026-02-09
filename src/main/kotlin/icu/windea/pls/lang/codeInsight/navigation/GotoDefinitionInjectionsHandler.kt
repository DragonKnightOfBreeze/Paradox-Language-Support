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
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty

class GotoDefinitionInjectionsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefinitionInjections"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        if (!ParadoxDefinitionInjectionManager.isSupported(selectGameType(file))) return null // 忽略游戏类型不支持的情况
        val project = file.project
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return null // 只要向上能找到符合条件的属性就行
        val info = ParadoxDefinitionInjectionManager.getInfo(element) ?: return null
        if (info.target.isNullOrEmpty()) return null // 排除目标为空的情况
        if (info.type.isNullOrEmpty()) return null // 排除目标定义的类型为空的情况
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, PlsBundle.message("script.goto.definitionInjections.search", info.target)) {
            // need read actions here if necessary
            readAction {
                val selector = selector(project, element).definitionInjection().contextSensitive()
                val resolved = ParadoxDefinitionInjectionSearch.search(null, info.target, info.type, selector).findAll()
                targets.addAll(resolved.mapNotNull { it.element })
            }
        }
        return GotoData(element, targets.distinct().toTypedArray(), emptyList())
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFileManager.findScriptProperty(file, offset)
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        if (sourceElement !is ParadoxScriptProperty) return ""
        val target = ParadoxDefinitionInjectionManager.getInfo(sourceElement)?.target
        if (target.isNullOrEmpty()) return ""
        return PlsBundle.message("script.goto.definitionInjections.chooseTitle", target.escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        if (sourceElement !is ParadoxScriptProperty) return ""
        val target = ParadoxDefinitionInjectionManager.getInfo(sourceElement)?.target
        if (target.isNullOrEmpty()) return ""
        return PlsBundle.message("script.goto.definitionInjections.findUsagesTitle", target.escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return PlsBundle.message("script.goto.definitionInjections.notFoundMessage")
    }

    override fun navigateToElement(descriptor: Navigatable) {
        if (descriptor is PsiElement) {
            activateFileWithPsiElement(descriptor, true)
        } else {
            descriptor.navigate(true)
        }
    }
}
