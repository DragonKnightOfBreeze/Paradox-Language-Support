package icu.windea.pls.core.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*

/**
 * 导航到当前文件的包括自身在内的相同路径的文件。
 */
class GotoFilesAction : BaseCodeInsightAction() {
	private val handler = GotoFilesHandler()
	
	override fun getHandler(): CodeInsightActionHandler {
		return handler
	}
	
	override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
		return file.fileInfo != null
	}
	
	override fun update(event: AnActionEvent) {
		//当选中的文件位于游戏或模组根目录下显示和启用
	}
}