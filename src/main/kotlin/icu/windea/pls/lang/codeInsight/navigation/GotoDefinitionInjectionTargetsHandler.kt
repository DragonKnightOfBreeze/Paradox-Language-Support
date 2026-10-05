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
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager

class GotoDefinitionInjectionTargetsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefinitionInjectionTargets"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        if (!ParadoxDefinitionInjectionManager.isSupported(selectGameType(file))) return null // 忽略游戏类型不支持的情况
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement?
        // 只要向上能找到符合条件的属性就行
        val element = ParadoxPsiFileService.findScriptProperty(file, offset) ?: return null
        val info = element.definitionInjectionInfo ?: return null
        if (!info.isTargetValid()) return null // 排除目标或目标类型为空的情况
        sourceElement = element
        val expression = ParadoxPsiPresentationService.getExpressionForDefinitionInjectionUsage(element) ?: return null
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.definitionInjectionTargets.search", expression.orAnonymous().escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxDefinitionSearch.selector(project, element).contextSensitive()
                val resolved = ParadoxDefinitionSearch.searchElement(info.target, info.type, selector).findAll()
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
        val name = ParadoxPsiPresentationService.getExpressionForDefinitionInjectionUsage(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.definitionInjectionTargets.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getExpressionForDefinitionInjectionUsage(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.definitionInjectionTargets.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.definitionInjectionTargets.notFoundMessage")
    }
}
