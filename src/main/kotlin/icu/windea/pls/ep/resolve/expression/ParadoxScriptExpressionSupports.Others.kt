package icu.windea.pls.ep.resolve.expression

import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataType
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.aliasConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.resolved
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.lang.codeInsight.completion.ParadoxCompletionContext
import icu.windea.pls.lang.codeInsight.completion.ParadoxExpressionCompletionManager
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.util.ParadoxExpressionSupportFactory
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.highlighting.ParadoxScriptHighlighterColors
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey

/**
 * @see CwtDataTypes.Constant
 */
class ParadoxConstantScriptExpressionSupport : ParadoxScriptExpressionSupport {
    override fun supports(dataType: CwtDataType): Boolean {
        return dataType == CwtDataTypes.Constant
    }

    override fun annotate(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, holder: AnnotationHolder): Boolean {
        val annotated = annotateByAliasName(element, rangeInExpression, holder, config)
        if (annotated) return false
        val configExpression = config.configExpression ?: return false
        val role = configExpression.role
        when (role) {
            CwtDataExpressionRole.Other -> {
                // unnecessary
                return false
            }
            CwtDataExpressionRole.Value -> {
                // skip
                return false
            }
            CwtDataExpressionRole.Key -> {
                // unnecessary
                if (element is ParadoxScriptPropertyKey && rangeInExpression.startOffset == 0 && rangeInExpression.endOffset == text.length) return false

                val attributesKey = ParadoxScriptHighlighterColors.PROPERTY_KEY
                ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
                return true
            }
        }
    }

    private fun annotateByAliasName(element: ParadoxExpressionElement, rangeInExpression: TextRange, holder: AnnotationHolder, config: CwtConfig<*>): Boolean {
        val aliasConfig = when {
            config is CwtPropertyConfig -> config.aliasConfig
            config is CwtAliasConfig -> config
            else -> null
        } ?: return false
        val type = aliasConfig.configExpression.type
        if (type !in CwtDataTypeSets.ConstantAware) return false
        val aliasName = aliasConfig.name
        val attributesKey = when {
            aliasName == "modifier" -> ParadoxScriptHighlighterColors.MODIFIER
            aliasName == "trigger" -> ParadoxScriptHighlighterColors.TRIGGER
            aliasName == "effect" -> ParadoxScriptHighlighterColors.EFFECT
            else -> return false
        }
        ParadoxExpressionSupportFactory.annotateExpression(element, rangeInExpression, holder, attributesKey)
        return true
    }

    override fun resolve(element: ParadoxExpressionElement, text: String, rangeInExpression: TextRange, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        return config.resolved().pointer.element
    }

    override fun complete(context: ParadoxCompletionContext, result: CompletionResultSet) {
        if (context.keyword.isParameterized()) return // 排除可能带参数的情况
        ParadoxExpressionCompletionManager.completeConstant(context, result)
    }
}
