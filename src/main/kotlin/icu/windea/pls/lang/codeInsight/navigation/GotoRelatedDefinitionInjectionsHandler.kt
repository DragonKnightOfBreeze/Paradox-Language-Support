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
import icu.windea.pls.core.orAnonymous
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.lang.search.ParadoxDefinitionInjectionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager

class GotoRelatedDefinitionInjectionsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxRelatedDefinitionInjections"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        if (!ParadoxDefinitionInjectionManager.isSupported(selectGameType(file))) return null // 忽略游戏类型不支持的情况
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement?
        val element = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return null
        if (!element.isDefinitionTypeKeyOrName()) return null
        val definition = selectScope { element.parentDefinition() } ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        if (!ParadoxDefinitionInjectionManager.canApply(definitionInfo)) return null // 排除不期望匹配的定义
        sourceElement = definition
        val name = ParadoxPsiPresentationService.getNameForDefinition(definition) ?: return null
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.relatedDefinitionInjections.search", name.orAnonymous().escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxDefinitionInjectionSearch.selector(project, definition).contextSensitive()
                val resolved = ParadoxDefinitionInjectionSearch.searchElement(null, definitionInfo.name, definitionInfo.type, selector).findAll()
                targets.addAll(resolved)
            }
        }
        if (targets.isEmpty()) return null // unavailable
        targets.removeIf { it == sourceElement } // remove current target from targets
        return GotoData(sourceElement, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }

    override fun shouldSortTargets(): Boolean {
        return false
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.relatedDefinitionInjections.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getNameForDefinition(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.relatedDefinitionInjections.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.relatedDefinitionInjections.notFoundMessage")
    }
}
