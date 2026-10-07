package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.isDefinitionTypeKeyOrName

/**
 * 导航到当前定义的包括自身在内的拥有相同名称和主要类型的定义。
 *
 * 不支持直接声明为文件的定义。
 */
class GotoDefinitionsAction : GotoActionBase() {
    private val handler = GotoDefinitionsHandler()

    override fun getHandler() = handler

    override fun isVisible(file: PsiFile, editor: Editor): Boolean {
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 要求是语义上有效的脚本文件
        return ParadoxPsiFileMatchService.isScriptFile(file)
    }

    override fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        val element = ParadoxPsiFileService.findScriptExpression(file, offset) ?: return false
        return element.isDefinitionTypeKeyOrName()
    }
}
