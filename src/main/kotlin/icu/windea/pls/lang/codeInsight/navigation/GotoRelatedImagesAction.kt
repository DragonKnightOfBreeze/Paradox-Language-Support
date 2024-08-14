package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.*
import com.intellij.codeInsight.actions.*
import com.intellij.openapi.actionSystem.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.actions.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * 导航到当前定义/修正的相关图片的动作。
 */
class GotoRelatedImagesAction : BaseCodeInsightAction() {
	private val handler = GotoRelatedImagesHandler()
	
	override fun getHandler(): CodeInsightActionHandler {
		return handler
	}
    
	override fun update(event: AnActionEvent) {
        val presentation = event.presentation
        presentation.isEnabledAndVisible = false
        val project = event.project ?: return
        val editor = event.editor ?: return
        val file = PsiUtilBase.getPsiFileInEditor(editor, project) ?: return
		if(file !is ParadoxScriptFile) return
		presentation.isVisible = true
		if(file.definitionInfo != null) {
			presentation.isEnabled = true
			return
		}
		val offset = editor.caretModel.offset
		val element = findElement(file, offset)
		val isEnabled = when {
			element == null -> false
			element.isDefinitionRootKeyOrName() -> true
			ParadoxModifierHandler.resolveModifier(element) != null -> true
			else -> false
		}
		presentation.isEnabled = isEnabled
	}
	
	private fun findElement(file: PsiFile, offset: Int): ParadoxScriptStringExpressionElement? {
		return ParadoxPsiManager.findScriptExpression(file, offset).castOrNull()
	}
}
