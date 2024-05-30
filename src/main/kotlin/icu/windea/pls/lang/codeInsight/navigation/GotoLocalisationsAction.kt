package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*

/**
 * 导航到当前本地化的包括自身在内的相同名称的本地化。
 */
class GotoLocalisationsAction : BaseCodeInsightAction() {
	private val handler = GotoLocalisationsHandler()
	
	override fun getHandler(): CodeInsightActionHandler {
		return handler
	}
	
	override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
		return file is ParadoxLocalisationFile && file.fileInfo != null
	}
	
	override fun update(event: AnActionEvent) {
		val presentation = event.presentation
		presentation.isEnabledAndVisible = false
		val project = event.project
		val editor = event.editor
		if(editor == null || project == null) return
		val file = PsiUtilBase.getPsiFileInEditor(editor, project)
		if(file !is ParadoxLocalisationFile) return
		val fileInfo = file.fileInfo ?: return
		if(fileInfo.pathToEntry.length <= 1) return //忽略直接位于游戏或模组入口目录下的文件
		presentation.isVisible = true
		val offset = editor.caretModel.offset
		val localisation = findElement(file, offset)
		presentation.isEnabled = localisation != null
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationProperty? {
		return ParadoxPsiManager.findLocalisation(file, offset)
	}
}