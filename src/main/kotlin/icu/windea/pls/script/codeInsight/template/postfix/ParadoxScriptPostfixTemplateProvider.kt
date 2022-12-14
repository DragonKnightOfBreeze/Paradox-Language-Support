package icu.windea.pls.script.codeInsight.template.postfix

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*

class ParadoxScriptPostfixTemplateProvider: PostfixTemplateProvider {
	private val defaultTemplates by lazy {
		buildSet<PostfixTemplate> {
			val provider = this@ParadoxScriptPostfixTemplateProvider
			val postfixTemplateSettings = getCwtConfig().core.postfixTemplateSettings
			for((groupName, settings) in postfixTemplateSettings) {
				when(groupName) {
					ParadoxVariableOperationExpressionPostfixTemplate.GROUP_NAME -> {
						for(setting in settings.values) {
							add(ParadoxVariableOperationExpressionPostfixTemplate(setting, provider))
						}
					}
				}
			}
		}
	}
	
	override fun getTemplates(): Set<PostfixTemplate> {
		return defaultTemplates
	}
	
	override fun getId(): String {
		return "paradox.script"
	}
	
	override fun getPresentableName(): String {
		return PlsBundle.message("postfixTemplateProvider.script.name")
	}
	
	override fun isTerminalSymbol(currentChar: Char): Boolean {
		return currentChar == '.'
	}
	
	override fun preExpand(file: PsiFile, editor: Editor) {
		
	}
	
	override fun afterExpand(file: PsiFile, editor: Editor) {
		
	}
	
	override fun preCheck(copyFile: PsiFile, realEditor: Editor, currentOffset: Int): PsiFile {
		return copyFile
	}
}