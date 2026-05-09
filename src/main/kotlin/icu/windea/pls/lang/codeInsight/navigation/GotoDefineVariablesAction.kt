package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.model.ParadoxModSource
import icu.windea.pls.model.constraints.ParadoxPathConstraint
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.isDefinitionTypeKeyOrName

/**
 * 导航到当前定值变量的包括自身在内的拥有相同命名空间和名称的定值变量。
 */
class GotoDefineVariablesAction : BaseCodeInsightAction() {
    private val handler = GotoDefineVariablesHandler()

    override fun getHandler() = handler

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (!ParadoxPsiFileMatcher.isScriptFile(file, ParadoxPathConstraint.ForDefine, injectable = true)) return
        if (ParadoxPsiFileMatcher.isTopFileFromRoot(file)) return // 忽略直接位于游戏或模组的根目录下的文件
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return
        if (!ParadoxPsiMatcher.isDefineVariable(element)) return
        presentation.isEnabledAndVisible = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFileManager.findScriptProperty(file, offset)
    }
}
