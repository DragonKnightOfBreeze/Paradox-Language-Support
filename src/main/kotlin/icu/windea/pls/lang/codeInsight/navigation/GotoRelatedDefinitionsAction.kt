package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * 导航到当前本地化的相关定义。
 */
class GotoRelatedDefinitionsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedDefinitionsHandler()

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (!ParadoxPsiFileMatcher.isLocalisationFile(file, injectable = true)) return
        if (ParadoxPsiFileMatcher.isTopFile(file)) return // 忽略直接位于游戏或模组目录（或者对应的入口目录）中的文件
        presentation.isVisible = true
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        if (!ParadoxPsiMatcher.isNormalLocalisation(element)) return
        presentation.isEnabled = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
        return ParadoxPsiFileManager.findLocalisation(file, offset)
    }
}
