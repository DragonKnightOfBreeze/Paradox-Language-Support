package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.psi.util.startOffset
import com.intellij.util.ProcessingContext
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configGroup.mockVariableConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.getKeyword
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isRightQuoted
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionManager
import icu.windea.pls.lang.codeInsight.completion.config
import icu.windea.pls.lang.codeInsight.completion.configGroup
import icu.windea.pls.lang.codeInsight.completion.contextElement
import icu.windea.pls.lang.codeInsight.completion.expressionOffset
import icu.windea.pls.lang.codeInsight.completion.keyword
import icu.windea.pls.lang.codeInsight.completion.offsetInParent
import icu.windea.pls.lang.codeInsight.completion.quoted
import icu.windea.pls.lang.codeInsight.completion.rightQuoted
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.findParentProperty
import icu.windea.pls.script.psi.isBlockMember

/**
 * 提供变量名的代码补全。（在effect子句中）
 * @see icu.windea.pls.lang.codeInsight.template.postfix.ParadoxVariableOperationExpressionPostfixTemplate
 */
class ParadoxVariableNameCompletionProvider : CompletionProvider<CompletionParameters>() {
    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!PlsFacade.getSettings().completion.completeVariableNames) return

        val position = parameters.position
        val element = position.parent.castOrNull<ParadoxScriptString>() ?: return
        if (element.text.isParameterized()) return
        if (!element.isBlockMember()) return
        val parentProperty = element.findParentProperty() ?: return
        val configs = ParadoxExpressionManager.getConfigs(parentProperty, matchOptions = Options.Default or Options.AcceptDefinition)
        if (configs.isEmpty()) return
        val configGroup = configs.first().configGroup
        context.configGroup = configGroup
        val matched = configs.any { config ->
            config.configs?.any { childConfig ->
                childConfig is CwtPropertyConfig && childConfig.key == "alias_name[effect]"
            } ?: false
        }
        if (!matched) return

        val quoted = element.text.isLeftQuoted()
        val rightQuoted = element.text.isRightQuoted()
        val offsetInParent = parameters.offset - element.startOffset
        val keyword = element.getKeyword(offsetInParent)

        ParadoxCompletionManager.initializeContext(parameters, context)
        context.contextElement = element
        context.offsetInParent = offsetInParent
        context.keyword = keyword
        context.quoted = quoted
        context.rightQuoted = rightQuoted
        context.expressionOffset = ParadoxExpressionManager.getExpressionOffset(element)

        context.config = configGroup.mockVariableConfig

        ParadoxCompletionManager.completeDynamicValueExpression(context, result)
    }
}
