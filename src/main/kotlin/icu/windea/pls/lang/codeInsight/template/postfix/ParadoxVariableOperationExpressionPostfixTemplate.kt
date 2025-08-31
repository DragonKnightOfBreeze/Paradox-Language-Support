package icu.windea.pls.lang.codeInsight.template.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.config.config.internal.impl.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.ep.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.lang.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxExpressionMatcher.Options
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.findParentProperty
import icu.windea.pls.script.psi.isBlockMember

/**
 * @see icu.windea.pls.lang.codeInsight.completion.script.ParadoxVariableNameCompletionProvider
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
        val expression = ParadoxScriptExpression.resolve(setting.id, quoted = false, isKey = true)
        val configsToMatch = configs.flatMapTo(mutableListOf()) { it.configs.orEmpty() }
        val matched = configsToMatch.find p@{ config ->
            if (config !is CwtPropertyConfig) return@p false
            ParadoxScriptExpressionMatcher.matches(context, expression, config.keyExpression, config, configGroup).get()
        }
        if (matched == null) return emptyList()
        return stringElement.singleton.list()
    }
}
