package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService
import icu.windea.pls.lang.psi.ParadoxPsiFileService
import icu.windea.pls.lang.psi.ParadoxPsiMatchService

/**
 * 导航到当前本地化的包括自身在内的拥有相同名称的本地化。
 */
class GotoLocalisationsAction : GotoActionBase() {
    private val handler = GotoLocalisationsHandler()

    override fun getHandler() = handler

    override fun isVisible(file: PsiFile, editor: Editor): Boolean {
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        // 要求是语义上有效的本地化文件
        return ParadoxPsiFileMatchService.isLocalisationFile(file)
    }

    override fun isEnabled(file: PsiFile, editor: Editor): Boolean {
        val offset = editor.caretModel.offset
        val element = ParadoxPsiFileService.findLocalisation(file, offset) { BY_NAME }
        return ParadoxPsiMatchService.isLocalisation(element)
    }
}
