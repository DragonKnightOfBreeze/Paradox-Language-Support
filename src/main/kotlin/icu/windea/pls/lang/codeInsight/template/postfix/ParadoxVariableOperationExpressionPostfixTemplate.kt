package icu.windea.pls.lang.codeInsight.template.postfix

import com.intellij.codeInsight.template.postfix.templates.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.script.psi.*

/**
 * @see icu.windea.pls.script.codeInsight.completion.ParadoxVariableNameCompletionProvider
 */
class ParadoxVariableOperationExpressionPostfixTemplate(
    setting: CwtPostfixTemplateSettingsConfig,
    provider: PostfixTemplateProvider
) : ParadoxExpressionEditablePostfixTemplate(setting, provider) {
    object Constants {
        const val GROUP_NAME = "variable_operation_expressions"
    }

    override val groupName: String get() = Constants.GROUP_NAME

    override fun getExpressions(context: PsiElement, document: Document, offset: Int): List<PsiElement> {
        if (!ParadoxScriptTokenSets.VARIABLE_VALUE_TOKENS.contains(context.elementType)) return emptyList()
        ProgressManager.checkCanceled()
        val stringElement = context.parent?.castOrNull<ParadoxScriptValue>() ?: return emptyList()
        if (!stringElement.isBlockMember()) return emptyList()
        val parentProperty = stringElement.findParentProperty() ?: return emptyList()
        val configs = ParadoxExpressionManager.getConfigs(parentProperty, matchOptions = Options.Default or Options.AcceptDefinition)
        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        val expression = ParadoxDataExpression.resolve(setting.id, quoted = false, isKey = true)
        val configsToMatch = configs.flatMapTo(mutableListOf()) { it.configs.orEmpty() }
        val matched = configsToMatch.find p@{ config ->
            if (config !is CwtPropertyConfig) return@p false
            ParadoxExpressionMatcher.matches(context, expression, config.keyExpression, config, configGroup).get()
        }
        if (matched == null) return emptyList()
        return stringElement.toSingletonList()
    }
}
