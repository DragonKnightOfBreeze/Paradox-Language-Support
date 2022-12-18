package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.script.psi.*

/**
 * 导航到定义成员对应的CWT规则的动作。
 */
class GotoRelatedCwtConfigAction : BaseCodeInsightAction() {
	private val handler = GotoRelatedCwtConfigHandler()
	
	override fun getHandler(): CodeInsightActionHandler {
		return handler
	}
	
	override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
		return file is ParadoxScriptFile
	}
	
	override fun update(event: AnActionEvent) {
		//当选中的文件是脚本文件时显示
		//当光标位置的元素是定义的rootKey或者定义成员时显示
		//在PSI中向上查找，定义中的任何key/value，key可以是定义的rootKey，value可以不是string
		val presentation = event.presentation
		presentation.isEnabledAndVisible = false
		val project = event.project
		val editor = event.editor
		if(editor == null || project == null) return
		val file = PsiUtilBase.getPsiFileInEditor(editor, project)
		if(file !is ParadoxScriptFile) return
		presentation.isVisible = true
		val offset = editor.caretModel.offset
		val element = findElement(file, offset)
		if(element == null) {
			presentation.isEnabled = false
			return
		}
		val definition = element.findParentDefinition()
		presentation.isEnabled = definition != null
	}
	
	private fun findElement(file: PsiFile, offset: Int): PsiElement? {
		return file.findElementAt(offset) {
			it.parentOfTypes(ParadoxScriptPropertyKey::class, ParadoxScriptValue::class)
		}?.takeIf { it.isExpression() }
	}
}
