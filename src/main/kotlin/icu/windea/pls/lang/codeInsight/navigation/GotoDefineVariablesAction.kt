package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.model.constraints.ParadoxPathConstraint

/**
 * 导航到当前定值变量的包括自身在内的拥有相同命名空间和名称的定值变量。
 */
class GotoDefineVariablesAction : GotoActionBase() {
    private val handler = GotoDefineVariablesHandler()

    override fun getHandler() = handler

    override fun isVisible(file: PsiFile, editor: Editor): Boolean {
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file, ParadoxPathConstraint.ForDefine)
    }

    override fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        val element = ParadoxPsiFileService.findScriptProperty(file, offset) ?: return false
        return ParadoxPsiMatchService.isDefineVariable(element)
    }
}
