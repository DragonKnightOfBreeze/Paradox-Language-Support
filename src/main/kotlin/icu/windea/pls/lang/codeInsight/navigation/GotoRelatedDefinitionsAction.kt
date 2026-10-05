package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.actions.BaseCodeInsightAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import icu.windea.pls.core.editor
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxLocalisationType
import icu.windea.pls.script.psi.ParadoxScriptFile

/**
 * 导航到当前目标的相关定义。
 *
 * 支持的目标：
 * - 正常本地化（来自本地化名）
 * - 定义引用片段（来自对应的脚本表达式）
 */
class GotoRelatedDefinitionsAction : BaseCodeInsightAction() {
    private val handler = GotoRelatedDefinitionsHandler()

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
        // 要求是语义上有效的脚本文件或本地化文件
        return ParadoxPsiFileMatchService.isScriptFile(file) || ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    private fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        run {
            if (file !is ParadoxScriptFile) return@run
            val resolved = file.findReferenceAt(offset)?.resolve() ?: return@run
            return ParadoxPsiMatchService.isDefinitionSnippetElement(resolved)
        }
        run {
            if (file !is ParadoxLocalisationFile) return@run
            val element = ParadoxPsiFileService.findLocalisation(file, offset) ?: return@run
            return ParadoxPsiMatchService.isLocalisation(element, ParadoxLocalisationType.Normal)
        }
        return false
    }
}
