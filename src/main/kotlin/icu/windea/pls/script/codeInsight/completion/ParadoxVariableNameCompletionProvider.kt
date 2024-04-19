package icu.windea.pls.script.codeInsight.completion

import com.intellij.codeInsight.completion.*
import com.intellij.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.codeInsight.completion.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.CwtConfigMatcher.Options
import icu.windea.pls.script.psi.*

/**
 * 提供变量名的代码补全。（在effect子句中）
 * @see icu.windea.pls.lang.codeInsight.template.postfix.ParadoxVariableOperationExpressionPostfixTemplate
 */
class ParadoxVariableNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if(!getSettings().completion.completeVariableNames) return
        
        val position = parameters.position
        val element = position.parent.castOrNull<ParadoxScriptString>() ?: return
        if(element.text.isParameterized()) return
        if(!element.isBlockValue()) return
        val parentProperty = element.findParentProperty() ?: return
        val configs = CwtConfigHandler.getConfigs(parentProperty, matchOptions = Options.Default or Options.AcceptDefinition)
        if(configs.isEmpty()) return
        val configGroup = configs.first().info.configGroup
        context.configGroup = configGroup
        val matched = configs.any { config ->
            config.configs?.any { childConfig ->
                childConfig is CwtPropertyConfig && childConfig.key == "alias_name[effect]"
            } ?: false
        }
        if(!matched) return
        
        val quoted = element.text.isLeftQuoted()
        val rightQuoted = element.text.isRightQuoted()
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)
        
        context.initialize(parameters)
        context.contextElement = element
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted
        
        val mockConfig = CwtValueConfig.resolve(emptyPointer(), configGroup.info, "value[variable]")
        context.config = mockConfig
        
        ParadoxCompletionManager.completeDynamicValueExpression(context, result)
    }
}
