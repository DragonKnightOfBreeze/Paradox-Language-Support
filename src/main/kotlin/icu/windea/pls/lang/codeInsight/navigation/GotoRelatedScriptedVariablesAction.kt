package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.model.ParadoxLocalisationType

/**
 * 导航到当前目标的相关封装变量。
 *
 * 支持的目标：
 * - 普通本地化（来自本地化名）
 */
class GotoRelatedScriptedVariablesAction : GotoActionBase() {
    private val handler = GotoRelatedScriptedVariablesHandler()

    override fun getHandler() = handler

    override fun isVisible(file: PsiFile, editor: Editor): Boolean {
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 要求是语义上有效的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        run {
            if (file !is ParadoxLocalisationFile) return@run
            val element = ParadoxPsiFileService.findLocalisation(file, offset) ?: return false
            return ParadoxPsiMatchService.isLocalisation(element, ParadoxLocalisationType.Normal)
        }
        return false
    }
}
