package icu.windea.pls.lang.codeInsight.template.postfix

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import kotlin.collections.component1
import kotlin.collections.component2

class ParadoxPostfixTemplateProvider: PostfixTemplateProvider {
	private val defaultTemplates by lazy {
		buildSet<PostfixTemplate> {
			val provider = this@ParadoxPostfixTemplateProvider
			val postfixTemplateSettings = getConfigGroup(null).postfixTemplateSettings
			for((groupName, settings) in postfixTemplateSettings) {
				when(groupName) {
					ParadoxVariableOperationExpressionPostfixTemplate.Data.GROUP_NAME -> {
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
	
	//not allow customizing postfix templates so far
	
	//override fun getPresentableName(): String {
	//	return PlsBundle.message("postfixTemplateProvider.script.name")
	//}
	
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
