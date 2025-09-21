package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.util.psi.ParadoxPsiFinder
import icu.windea.pls.lang.util.psi.ParadoxPsiMatcher
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * 导航到当前封装变量的包括自身在内的相同名称的封装变量（仅限本地+全局）。
 */
class GotoScriptedVariablesAction : BaseCodeInsightAction() {
    private val handler = GotoScriptedVariablesHandler()

    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }

    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        if (file !is ParadoxScriptFile) return
        val fileInfo = file.fileInfo ?: return
        if (fileInfo.path.length <= 1) return //忽略直接位于游戏或模组入口目录下的文件
        presentation.isVisible = true
        val offset = editor.caretModel.offset
        val element = findElement(file, offset)
        presentation.isEnabled = ParadoxPsiMatcher.isScriptedVariable(element)
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptScriptedVariable? {
        return ParadoxPsiFinder.findScriptedVariable(file, offset) { BY_NAME }
    }
}
