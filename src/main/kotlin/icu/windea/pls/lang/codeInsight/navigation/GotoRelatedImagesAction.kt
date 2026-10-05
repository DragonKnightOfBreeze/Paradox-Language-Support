package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 导航到当前目标的相关图片。
 *
 * 支持的目标：
 * - 定义（来自类型键或名字）
 * - 修正（来自引用解析）
 */
class GotoRelatedImagesAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedImagesHandler()

    override fun getHandler() = handler

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val project = e.project ?: return
        val editor = e.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        val visible = isVisible(file)
        e.presentation.isVisible = visible
        val enabled = visible && isEnabled(file, editor)
        e.presentation.isEnabled = enabled
    }

    private fun isVisible(file: PsiFile): Boolean {
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    private fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        run {
            if (file !is ParadoxDefinitionElement) return@run
            if (file.definitionInfo != null) return true
        }
        val offset = editor.caretModel.offset
        run {
            if (file !is ParadoxScriptFile) return@run
            val element = ParadoxPsiFileService.findScriptedVariable(file, offset) { BY_NAME } ?: return@run
            if (ParadoxPsiMatchService.isScriptedVariable(element)) return true
        }
        run {
            if (file !is ParadoxScriptFile) return@run
            val element = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return@run
            if (element.isDefinitionTypeKeyOrName()) return true
        }
        run {
            if (file !is ParadoxScriptFile) return@run
            val resolved = file.findReferenceAt(offset)?.resolve() ?: return@run
            if (ParadoxPsiMatchService.isModifierElement(resolved)) return true
        }
        return false
    }
}
