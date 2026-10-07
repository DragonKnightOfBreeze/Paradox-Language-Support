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
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.util.contextSensitive

class GotoDefinitionInjectionTargetsHandler : GotoHandlerBase() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxDefinitionInjectionTargets"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        val project = file.project
        val offset = editor.caretModel.offset
        // 要求受游戏类型支持（再次检查）
        if (!ParadoxPsiFileMatchService.isDefinitionInjectionSupported(file)) return null
        // 只要向上能找到符合条件的属性就行
        val sourceElement = ParadoxPsiFileService.findScriptProperty(file, offset) ?: return null
        val expression = ParadoxPsiPresentationService.getExpressionForDefinitionInjectionUsage(sourceElement) ?: return null
        if (expression.isEmpty()) return null // 3.0.4 排除匿名或者无法解析的情况
        val info = sourceElement.definitionInjectionInfo ?: return null
        if (!info.isTargetValid()) return null // 排除目标或目标类型为空的情况
        val targets = mutableListOf<PsiElement>()
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.definitionInjectionTargets.search", expression.escapeXml())) {
            // need read actions here if necessary
            readAction {
                val selector = ParadoxDefinitionSearch.selector(project, sourceElement).contextSensitive()
                val resolved = ParadoxDefinitionSearch.searchElement(info.target, info.type, selector).findAll()
                targets.addAll(resolved)
            }
        }
        return getGotoData(sourceElement, targets)
    }

    override fun getChooserTitle(sourceElement: PsiElement, name: String?, length: Int, finished: Boolean): String {
        val name = ParadoxPsiPresentationService.getExpressionForDefinitionInjectionUsage(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.definitionInjectionTargets.chooseTitle", name.orUnresolved().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getExpressionForDefinitionInjectionUsage(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.definitionInjectionTargets.findUsagesTitle", name.orUnresolved().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.definitionInjectionTargets.notFoundMessage")
    }
}
