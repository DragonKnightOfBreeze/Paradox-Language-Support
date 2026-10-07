package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiFile
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileMatchService

/**
 * 导航到当前文件的包括自身在内的拥有相同路径的文件。
 * 如果是本地化文件的话，也忽略路径中的语言环境。
 */
class GotoFilesAction : GotoActionBase() {
    private val handler = GotoFilesHandler()

    override fun getHandler() = handler

    override fun isVisible(file: PsiFile, editor: Editor): Boolean {
        // 忽略不存在文件信息的文件（如注入的文件）
        if (file.fileInfo == null) return false
        // 忽略直接位于游戏或模组的根目录下的文件
        if (ParadoxPsiFileMatchService.isTopFromRootFile(file)) return false
        return true
    }
}
