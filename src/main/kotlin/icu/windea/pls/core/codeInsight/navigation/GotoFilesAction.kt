package icu.windea.pls.core.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*

/**
 * 导航到当前文件的包括自身在内的相同路径的文件。如果是本地化文件的话也忽略路径中的语言区域。
 */
class GotoFilesAction : BaseCodeInsightAction() {
	private val handler = GotoFilesHandler()
	
	override fun getHandler(): CodeInsightActionHandler {
		return handler
	}
	
	override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
		return file.fileInfo != null
	}
	
	//当选中的文件位于游戏或模组根目录下显示和启用
}