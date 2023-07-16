package icu.windea.pls.core.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.actions.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * 导航到定义成员对应的CWT规则的动作。
 */
class GotoRelatedCwtConfigsAction : BaseCodeInsightAction() {
	private val handler = GotoRelatedCwtConfigsHandler()
	
	override fun getHandler(): CodeInsightActionHandler {
		return handler
	}
	
	override fun isValidForFile(project: Project, editor: Editor, file: PsiFile): Boolean {
		return file is ParadoxScriptFile && file.fileInfo != null
	}
	
	override fun update(event: AnActionEvent) {
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
		//这里不判断能否向上跨内联查找到定义，或者是否真的存在对应的CWT规则
		presentation.isEnabled = true
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxScriptExpressionElement? {
		return ParadoxPsiFinder.findScriptExpression(file, offset)
	}
}
