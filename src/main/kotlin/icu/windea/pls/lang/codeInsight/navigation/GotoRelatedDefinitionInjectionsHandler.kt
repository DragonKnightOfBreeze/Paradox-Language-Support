package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.application.readAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.platform.ide.progress.runWithModalProgressBlocking
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import icu.windea.pls.ChronicleBundle
import icu.windea.pls.core.escapeXml
import icu.windea.pls.core.orNull
import icu.windea.pls.core.orUnresolved
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager

class GotoRelatedDefinitionInjectionsHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedDefinitionInjections"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        if (!ParadoxDefinitionInjectionManager.isSupported(selectGameType(file))) return null // 忽略游戏类型不支持的情况
        val project = file.project
        val offset = editor.caretModel.offset
        val expressionElement = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return null
        if (!expressionElement.isDefinitionTypeKeyOrName()) return null
        val sourceElement = selectScope { expressionElement.parentDefinition() } ?: return null
        val name = getName(sourceElement)?.orNull() ?: return null // 3.0.4 排除匿名或者无法解析的情况
        val definitionInfo = sourceElement.definitionInfo ?: return null
        if (!ParadoxDefinitionInjectionManager.canApply(definitionInfo)) return null // 排除不期望匹配的定义
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedDefinitionInjections.search", name.escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxDefinitionInjectionSearch.selector(project, sourceElement).contextSensitive()
                val resolved = ParadoxDefinitionInjectionSearch.searchElement(null, definitionInfo.name, definitionInfo.type, selector).findAll()
                targets.addAll(resolved)
            }
        }
        return getGotoData(sourceElement, targets)
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = getName(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.relatedDefinitionInjections.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = getName(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.relatedDefinitionInjections.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedDefinitionInjections.notFoundMessage")
    }

    private fun getName(sourceElement: PsiElement): String? {
        return ParadoxPsiPresentationService.getNameForDefinition(sourceElement)
    }
}
