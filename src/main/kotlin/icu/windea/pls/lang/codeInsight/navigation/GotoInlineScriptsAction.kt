package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.selectGameType
import icu.windea.pls.model.constraints.ParadoxPathConstraint

/**
 * 导航到当前内联脚本用法的对应的（即同名）内联脚本。
 */
class GotoInlineScriptsAction : GotoActionBase() {
    private val handler = GotoInlineScriptsHandler()

    override fun getHandler() = handler

    override fun isVisible(file: PsiFile, editor: Editor): Boolean {
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 要求规则分组数据已加载完毕
        if (!ParadoxPsiFileMatchService.checkConfigGroupInitialized(file)) return false
        // 要求受游戏类型支持
        if (!ParadoxPsiFileMatchService.isInlineScriptSupported(file)) return false
        // 要求是语义上有效的脚本文件（可接受内联脚本）
        // 内联脚本文件中也能嵌套使用内联脚本，因此这里的约束是可行的
        return ParadoxPsiFileMatchService.isScriptFile(file, ParadoxPathConstraint.AcceptInlineScriptUsage)
    }

    override fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        val gameType = selectGameType(file) ?: return false
        // 只要向上能找到符合条件的属性就行
        val element = ParadoxPsiFileService.findScriptProperty(file, offset) ?: return false
        return ParadoxPsiMatchService.isInlineScriptUsage(element, gameType)
    }
}
