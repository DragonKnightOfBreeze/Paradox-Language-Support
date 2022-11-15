package icu.windea.pls.script.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.script.psi.*

/**
 * 导航到定义的相关本地化的动作。
 */
class GotoRelatedLocalisationAction : BaseCodeInsightAction() {
	override fun getHandler(): CodeInsightActionHandler {
		return GotoRelatedLocalisationHandler()
	}
	
	override fun update(event: AnActionEvent) {
		//当选中的文件是脚本文件时显示
		//当选中的文件是定义或者光标位置的元素是定义的rootKey或者作为名字的字符串时启用
		val presentation = event.presentation
		val project = event.project
		val editor = event.editor
		if(editor == null || project == null) return
		val file = PsiUtilBase.getPsiFileInEditor(editor, project)
		if(file is ParadoxScriptFile) {
			presentation.isVisible = true
			if(file.definitionInfo != null) {
				presentation.isEnabled = true
				return
			}
			val element = PsiUtilCore.getElementAtOffset(file, editor.caretModel.offset).getSelfOrPrevSiblingNotWhitespace()
			val isRootKeyOrName = element.parentOfType<ParadoxScriptExpressionElement>()?.isDefinitionRootKeyOrName() == true
			presentation.isEnabled = isRootKeyOrName
		} else {
			presentation.isEnabledAndVisible = false
		}
	}
}
