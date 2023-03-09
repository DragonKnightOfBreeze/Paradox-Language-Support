package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
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
		return file is ParadoxLocalisationFile
	}
	
	override fun update(event: AnActionEvent) {
		//当选中的文件是本地化文件时显示
		//当光标位置的元素本地化的名字时启用
		val presentation = event.presentation
		presentation.isEnabledAndVisible = false
		val project = event.project
		val editor = event.editor
		if(editor == null || project == null) return
		val file = PsiUtilBase.getPsiFileInEditor(editor, project)
		if(file !is ParadoxLocalisationFile) return
		presentation.isVisible = true
		val offset = editor.caretModel.offset
		val element = findElement(file, offset)
		val isEnabled = when {
			element == null -> false
			element.parent.castOrNull<ParadoxLocalisationProperty>()?.localisationInfo != null -> true
			else -> false
		}
		presentation.isEnabled = isEnabled
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxLocalisationPropertyKey? {
		//direct parent
		return file.findElementAt(offset) {
			it.parent as? ParadoxLocalisationPropertyKey
		}
	}
}