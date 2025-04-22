package icu.windea.pls.ep.dataExpression

import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*

abstract class RuleBasedCwtDataExpressionResolver : CwtDataExpressionResolver {
    sealed interface Rule {
        val type: CwtDataType
    }

    data class ConstantRule(
        override val type: CwtDataType,
        val constant: String,
        val applyAction: CwtDataExpression.() -> Unit
    ) : Rule

    data class DynamicRule(
        override val type: CwtDataType,
        val prefix: String = "",
        val suffix: String = "",
        val applyAction: CwtDataExpression.(String) -> Unit
    ) : Rule

    protected fun rule(
        type: CwtDataType,
        constant: String,
        applyAction: CwtDataExpression.() -> Unit = {}
    ): Rule {
        return ConstantRule(type, constant, applyAction)
    }

    protected fun rule(
        type: CwtDataType,
        prefix: String,
        suffix: String,
        applyAction: CwtDataExpression.(String) -> Unit = {}
    ): Rule {
        return DynamicRule(type, prefix, suffix, applyAction)
    }

    abstract val rules: List<Rule>

    private val constantRuleMap by lazy { rules.filterIsInstance<ConstantRule>().associateBy { it.constant } }
    private val dynamicRules by lazy { rules.filterIsInstance<DynamicRule>() }

    final override fun resolve(expressionString: String, isKey: Boolean): CwtDataExpression? {
        run {
            val rule = constantRuleMap[expressionString]
            if (rule == null) return@run
            return CwtDataExpression.create(expressionString, isKey, rule.type).apply { rule.applyAction(this) }
        }
        run {
            dynamicRules.forEach f@{ rule ->
                val data = expressionString.removeSurroundingOrNull(rule.prefix, rule.suffix)
                if (data == null) return@f
                return CwtDataExpression.create(expressionString, isKey, rule.type).apply { rule.applyAction(this, data) }
            }
        }
        return null
    }
}

abstract class PatternAwareCwtDataExpressionResolver : CwtDataExpressionResolver
