package icu.windea.pls.script.refactoring

import com.intellij.codeInsight.template.*
import com.intellij.codeInsight.template.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.command.impl.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.refactoring.*
import icu.windea.pls.*
import icu.windea.pls.core.codeInsight.*
import icu.windea.pls.script.psi.*

/**
 * 声明全局封装变量的重构。
 */
object ParadoxScriptIntroduceGlobalScriptedVariableHandler: ContextAwareRefactoringActionHandler() {
	private const val commandName = "Introduce Global Scripted Variable"
	
	override fun isAvailable(editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		val offset = editor.caretModel.offset
		val position = file.findElementAt(offset) ?: return false
		val positionType = position.elementType
		if(positionType != ParadoxScriptElementTypes.INT_TOKEN && positionType != ParadoxScriptElementTypes.FLOAT_TOKEN) return false
		return position.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() != null
	}
	
	@Suppress("UnstableApiUsage")
	override fun invokeAction(project: Project, editor: Editor, file: PsiFile, dataContext: DataContext): Boolean {
		val offset = editor.caretModel.offset
		val position = file.findElementAt(offset) ?: return false
		val positionType = position.elementType
		if(positionType != ParadoxScriptElementTypes.INT_TOKEN && positionType != ParadoxScriptElementTypes.FLOAT_TOKEN) return false
		val parentDefinition = position.findParentDefinition()?.castOrNull<ParadoxScriptProperty>() ?: return false
		
		//TODO
		return true
	}
}