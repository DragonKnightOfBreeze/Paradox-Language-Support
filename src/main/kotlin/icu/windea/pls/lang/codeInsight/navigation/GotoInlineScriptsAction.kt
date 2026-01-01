package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.CodeInsightActionHandler
import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.lang.actions.editor
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileManager
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.util.ParadoxInlineScriptManager
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 导航到当前内联脚本用法的对应的（即同名）内联脚本。
 */
class GotoInlineScriptsAction : BaseCodeInsightAction() {
    private val handler = GotoInlineScriptsHandler()

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
        if (fileInfo.path.length <= 1) return // 忽略直接位于游戏或模组入口目录下的文件
        val gameType = fileInfo.rootInfo.gameType
        if (!ParadoxInlineScriptManager.isSupported(gameType)) return // 忽略游戏类型不支持的情况
        val offset = editor.caretModel.offset
        val element = findElement(file, offset) ?: return // 只要向上能找到符合条件的属性就行
        if (!ParadoxPsiMatcher.isInlineScriptUsage(element, gameType)) return
        presentation.isEnabledAndVisible = true
    }

    private fun findElement(file: PsiFile, offset: Int): ParadoxScriptProperty? {
        return ParadoxPsiFileManager.findScriptProperty(file, offset)
    }
}

