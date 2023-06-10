package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 提供变量名的代码补全。（在`alias_name[effect]`匹配的子句中）
 * @see icu.windea.pls.core.codeInsight.template.postfix.ParadoxVariableOperationExpressionPostfixTemplate
 */
class ParadoxVariableNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeVariableNames) return
        
        val element = parameters.position.parent.castOrNull<ParadoxScriptString>() ?: return
        
        val file = parameters.originalFile
        val quoted = element.text.isLeftQuoted()
        val rightQuoted = element.text.isRightQuoted()
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        
        context.completionIds = mutableSetOf<String>().synced()
        context.parameters = parameters
        context.contextElement = element
        context.originalFile = file
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted
        
        val stringElement = element
        if(!stringElement.isBlockValue()) return
        val parentProperty = stringElement.findParentProperty() ?: return
        val configs = ParadoxConfigResolver.getConfigs(parentProperty, allowDefinition = true, matchOptions = ParadoxConfigMatcher.Options.Default)
        if(configs.isEmpty()) return
        val configGroup = configs.first().info.configGroup
        context.configGroup = configGroup
        val matched = configs.any { config ->
            config.configs?.any { childConfig ->
                childConfig is CwtPropertyConfig && childConfig.key == "alias_name[effect]"
            } ?: false
        }
        if(!matched) return
        val mockConfig = CwtValueConfig.resolve(emptyPointer(), configGroup.info, "value[variable]")
        context.config = mockConfig
        ParadoxConfigHandler.completeValueSetValueExpression(context, result)
    }
}
