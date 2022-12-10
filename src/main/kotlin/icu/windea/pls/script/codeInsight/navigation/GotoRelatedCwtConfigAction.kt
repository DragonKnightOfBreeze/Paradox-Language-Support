package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.script.psi.*

/**
 * 导航到定义元素对应的CWT规则的动作。
 */
class GotoRelatedCwtConfigAction : BaseCodeInsightAction() {
	override fun getHandler(): CodeInsightActionHandler {
		return GotoRelatedCwtConfigHandler()
	}
	
	override fun update(event: AnActionEvent) {
		//当选中的文件是脚本文件时显示
		//当光标位置的元素是定义的rootKey或者定义元素时显示
		//在PSI中向上查找，定义中的任何key/value，key可以是定义的rootKey，value可以不是string
		val presentation = event.presentation
		val project = event.project
		val editor = event.editor
		if(editor == null || project == null) return
		val file = PsiUtilBase.getPsiFileInEditor(editor, project)
		if(file is ParadoxScriptFile) {
			presentation.isVisible = true
			val offset = editor.caretModel.offset
			val element = findElement(file, offset)
			if(element == null) {
				presentation.isEnabled = false
				return
			}
			val definition = element.findParentDefinition()
			presentation.isEnabled = definition != null
		} else {
			presentation.isEnabledAndVisible = false
		}
	}
	
	private fun findElement(file: PsiFile, offset: Int): PsiElement? {
		return file.findElementAt(offset) {
			it.parentOfTypes(ParadoxScriptPropertyKey::class, ParadoxScriptValue::class)
		}?.takeIf { it.isExpressionElement() }
	}
}
