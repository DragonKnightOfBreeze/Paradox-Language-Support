package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*

/**
 * 导航到当前文件的包括自身在内的相同路径的文件。如果是本地化文件的话也忽略路径中的语言区域。
 */
class GotoFilesAction : BaseCodeInsightAction() {
    private val handler = GotoFilesHandler()
    
    override fun getHandler(): CodeInsightActionHandler {
        return handler
    }
    
    override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
        val fileInfo = file.fileInfo ?: return
        if(fileInfo.pathToEntry.length <= 1) return //忽略直接位于游戏或模组入口目录下的文件
        presentation.isEnabledAndVisible = true
    }
}
