package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 提供关键字的代码补全（要求不在定义声明中提供）。
 */
class ParadoxKeywordCompletionProvider : CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		val position = parameters.position
		val element = position.parent?.castOrNull<ParadoxScriptString>() ?: return
		if(element.text.isLeftQuoted()) return
		if(element.text.isParameterized()) return
		if(element.isExpression()) {
			//判断光标位置是否在定义声明中，更加准确，更具兼容性
			val configContext = CwtConfigHandler.getConfigContext(element)
			if(configContext != null && configContext.isRootOrMember()) return
		}
		
		result.addElement(PlsLookupElements.yesLookupElement)
		result.addElement(PlsLookupElements.noLookupElement)
		result.addElement(PlsLookupElements.blockLookupElement)
	}
}
