package icu.windea.pls.script.codeInsight.navigation

import cn.yiiguxing.plugin.translate.action.*
import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.util.*
import icu.windea.pls.script.psi.*

/**
 * 导航到定义的相关本地化的动作。
 */
class GotoRelatedLocalisationAction : BaseCodeInsightAction() {
	override fun getHandler(): CodeInsightActionHandler {
		return GotoRelatedLocalisationHandler()
	}
	
	override fun update(event: AnActionEvent) {
		val presentation = event.presentation
		val project = event.project
		val editor = event.editor
		if(editor == null || project == null) return
		val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
		presentation.isVisible = file is ParadoxScriptFile
		if(!presentation.isVisible) return
		val element = PsiUtilCore.getElementAtOffset(file, editor.caretModel.offset)
		val definition = element.findParentDefinition() //鼠标位置可以在rootKey上，也可以在声明中
		presentation.isEnabled = definition != null
	}
}