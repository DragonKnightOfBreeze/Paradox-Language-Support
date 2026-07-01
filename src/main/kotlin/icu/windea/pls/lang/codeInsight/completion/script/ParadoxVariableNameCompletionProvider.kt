package icu.windea.pls.lang.codeInsight.completion.script

import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.patterns.PlatformPatterns.*
import com.intellij.psi.util.parentOfType
import com.intellij.util.ProcessingContext
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configGroup.mockVariableConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.codeInsight.completion.GlobalCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionProvider
import icu.windea.pls.lang.codeInsight.completion.ParadoxComplexExpressionCompletionManager
import icu.windea.pls.lang.codeInsight.template.postfix.ParadoxVariableOperationExpressionPostfixTemplate
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.settings.ChronicleSettings
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptTokenSets.STRING_TOKENS
import icu.windea.pls.script.psi.isBlockMember

/**
 * 提供已有的变量的名字的代码补全。
 *
 * 适用条件：
 * - 直接位于效果子句中。
 *
 * @see ParadoxVariableOperationExpressionPostfixTemplate
 */
class ParadoxVariableNameCompletionProvider : ParadoxCompletionProvider() {
    val elementPattern get() = psiElement().withElementType(STRING_TOKENS)

    override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
        if (!ChronicleSettings.getInstance().state.completion.completeVariableNames) return

        val position = parameters.position
        val element = position.parent.castOrNull<ParadoxScriptString>() ?: return
        if (element.text.isParameterized()) return
        if (!element.isBlockMember()) return
        val parentMember = element.parentOfType<ParadoxScriptMember>(withSelf = false) ?: return
        val configs = ParadoxConfigManager.getConfigs(parentMember, ParadoxMatchOptions(forDeclarationRoot = true))
        if (configs.isEmpty()) return
        val configGroup = configs.first().configGroup
        val matched = configs.any { config ->
            config.configs?.any { childConfig ->
                childConfig is CwtPropertyConfig && childConfig.key == "alias_name[effect]"
            } ?: false
        }
        if (!matched) return

        val globalContext = GlobalCompletionContext.create(element, parameters, context)
        val context = ParadoxCompletionContext.create(globalContext).copy(
            expressionOffset = ParadoxExpressionManager.getExpressionOffset(element),
            config = configGroup.mockVariableConfig,
        )

        ParadoxComplexExpressionCompletionManager.completeDynamicValueExpression(context, result)
    }
}
