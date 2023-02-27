package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 提供变量名的代码补全。（在`alias_name[effect]`匹配的子句中）
 * @see icu.windea.pls.core.codeInsight.template.postfix.ParadoxVariableOperationExpressionPostfixTemplate
 */
class ParadoxVariableNameCompletionProvider: CompletionProvider<CompletionParameters>() {
	override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
		if(!getSettings().completion.completeVariableNames) return
		
		val element = parameters.position.parent.castOrNull<ParadoxScriptString>() ?: return
		
		val file = parameters.originalFile
		val quoted = element.text.isLeftQuoted()
		val rightQuoted = element.text.isRightQuoted()
		val offsetInParent = parameters.offset - element.textRange.startOffset
		val keyword = element.getKeyword(offsetInParent)
		
		context.put(PlsCompletionKeys.completionTypeKey, parameters.completionType)
		context.put(PlsCompletionKeys.contextElementKey, element)
		context.put(PlsCompletionKeys.originalFileKey, file)
		context.put(PlsCompletionKeys.quotedKey, quoted)
		context.put(PlsCompletionKeys.rightQuotedKey, rightQuoted)
		context.put(PlsCompletionKeys.offsetInParentKey, offsetInParent)
		context.put(PlsCompletionKeys.keywordKey, keyword)
		context.put(PlsCompletionKeys.completionIdsKey, mutableSetOf())
		
		val stringElement = element
		if(!stringElement.isBlockValue()) return
		val parentProperty = stringElement.findParentProperty() ?: return
		val configs = ParadoxCwtConfigHandler.getConfigs(parentProperty, allowDefinition = true)
		if(configs.isEmpty()) return
		val configGroup = configs.first().info.configGroup
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		val matched = configs.any { config ->
			config.configs?.any { childConfig ->
				childConfig is CwtPropertyConfig && childConfig.key == "alias_name[effect]"
			} ?: false
		}
		if(!matched) return
		val mockConfig = CwtValueConfig(emptyPointer(), configGroup.info, "value[variable]")
		context.put(PlsCompletionKeys.configKey, mockConfig)
        CwtConfigHandler.completeValueSetValueExpression(context, result)
	}
}
