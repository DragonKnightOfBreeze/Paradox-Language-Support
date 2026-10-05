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
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.ParadoxPsiPresentationService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.lang.util.ParadoxInlineScriptManager

class GotoInlineScriptsHandler : GotoTargetHandler() {
    override fun getFeatureUsedKey(): String {
        return "navigation.goto.paradoxInlineScripts"
    }

    override fun getSourceAndTargetElements(editor: Editor, file: PsiFile): GotoData? {
        if (!ParadoxInlineScriptManager.isSupported(selectGameType(file))) return null // 忽略游戏类型不支持的情况
        val gameType = selectGameType(file) ?: return null
        val project = file.project
        val offset = editor.caretModel.offset
        val targets = mutableListOf<PsiElement>()
        var sourceElement: PsiElement?
        // 只要向上能找到符合条件的属性就行
        val element = ParadoxPsiFileService.findScriptProperty(file, offset) ?: return null
        if (!ParadoxPsiMatchService.isInlineScriptUsage(element, gameType)) return null
        sourceElement = element
        val expression = ParadoxPsiPresentationService.getExpressionForInlineScriptUsage(element) ?: return null
        runWithModalProgressBlocking(project, ChronicleBundle.message("script.goto.inlineScripts.search", expression.orAnonymous().escapeXml())) {
            // need read actions here if necessary
            readAction {
                ParadoxInlineScriptManager.getInlineScriptFiles(expression, project, element).let { targets.addAll(it) }
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
        val name = ParadoxPsiPresentationService.getExpressionForInlineScriptUsage(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.inlineScripts.chooseTitle", name.orAnonymous().escapeXml())
    }

    override fun getFindUsagesTitle(sourceElement: PsiElement, name: String?, length: Int): String {
        val name = ParadoxPsiPresentationService.getExpressionForInlineScriptUsage(sourceElement) ?: name
        return ChronicleBundle.message("script.goto.inlineScripts.findUsagesTitle", name.orAnonymous().escapeXml())
    }

    override fun getNotFoundMessage(project: Project, editor: Editor, file: PsiFile): String {
        return ChronicleBundle.message("script.goto.inlineScripts.notFoundMessage")
    }
}
