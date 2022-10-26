package icu.windea.pls.script.codeInsight.navigation

import cn.yiiguxing.plugin.translate.action.*
import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.util.*
import icu.windea.pls.*
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
		//当光标位置的元素是是定义元素（定义中的任何key/value，key不能是定义的rootKey，value可以不是string）时显示
		val presentation = event.presentation
		val project = event.project
		val editor = event.editor
		if(editor == null || project == null) return
		val file = PsiUtilBase.getPsiFileInEditor(editor, project)
		if(file is ParadoxScriptFile) {
			presentation.isVisible = true
			val element = PsiUtilCore.getElementAtOffset(file, editor.caretModel.offset).getSelfOrPrevSiblingNotWhitespace()
			val location = element.parentOfTypes(ParadoxScriptPropertyKey::class, ParadoxScriptValue::class)
			if(location == null) {
				presentation.isEnabled = false
				return
			}
			val definition = location.findParentDefinition()
			if(definition == null || (location is ParadoxScriptPropertyKey && location.parent == definition)) {
				presentation.isEnabled = false
				return
			}
			presentation.isEnabled = true
		} else {
			presentation.isEnabledAndVisible = false
		}
	}
}