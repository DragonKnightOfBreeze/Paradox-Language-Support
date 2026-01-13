package icu.windea.pls.lang.codeInsight.template.postfix

import com.intellij.codeInsight.template.postfix.templates.PostfixTemplateProvider
import com.intellij.openapi.editor.Document
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.util.elementType
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.list
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptTokenSets
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isBlockMember
import org.jetbrains.kotlin.analysis.utils.printer.parentOfType

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
        val element = context.parent?.castOrNull<ParadoxScriptValue>() ?: return emptyList()
        if (!element.isBlockMember()) return emptyList()
        val parentMember = element.parentOfType<ParadoxScriptMember>(withSelf = false) ?: return emptyList()
        val configs = ParadoxExpressionManager.getConfigs(parentMember, matchOptions = ParadoxMatchOptions.Default or ParadoxMatchOptions.AcceptDefinition)
        if (configs.isEmpty()) return emptyList()
        val configGroup = configs.first().configGroup
        val expression = ParadoxScriptExpression.resolve(setting.id, quoted = false, isKey = true)
        val configsToMatch = configs.flatMapTo(mutableListOf()) { it.configs.orEmpty() }
        val matched = configsToMatch.find p@{ config ->
            if (config !is CwtPropertyConfig) return@p false
            ParadoxMatchService.matchScriptExpression(context, expression, config.keyExpression, config, configGroup).get()
        }
        if (matched == null) return emptyList()
        return element.singleton.list()
    }
}
