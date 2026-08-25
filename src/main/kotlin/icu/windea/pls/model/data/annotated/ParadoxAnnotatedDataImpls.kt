@file:Optimized

package icu.windea.pls.model.data.annotated

import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.joinToStringFast
import icu.windea.pls.core.orNull
import icu.windea.pls.core.quoteIfNeeded
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.type.ParadoxExpressionType

/**
 * 表达式类型的注解数据。
 *
 * 格式：
 * - `## @type key_type = value_type` - 对于 [FromProperty]
 * - `## @type value_type` - 对于 [FromValue]
 * - `## @type type_1;type_2` - 对于 [FromColumns]
 */
sealed class ParadoxTypeAnnotatedData : ParadoxAnnotatedData.Base("type") {
    abstract class FromMember : ParadoxTypeAnnotatedData() {
        abstract val valueType: ParadoxExpressionType
    }

    data class FromProperty(val keyType: ParadoxExpressionType, override val valueType: ParadoxExpressionType) : FromMember() {
        override fun render(): String {
            val keyText = keyType.text.quoteIfNeeded()
            val valueText = valueType.text.quoteIfNeeded()
            return "$keyText = $valueText"
        }
    }

    data class FromValue(override val valueType: ParadoxExpressionType) : FromMember() {
        override fun render(): String {
            return valueType.text.quoteIfNeeded()
        }
    }

    data class FromColumns(val types: List<ParadoxExpressionType>) : ParadoxTypeAnnotatedData() {
        override fun render(): String {
            return types.joinToStringFast(";") { it.text.quoteIfNeeded() }
        }
    }
}

/**
 * 规则表达式的注解数据。
 *
 * 格式：
 * - `## @config_expression key_expression = value_expression` - 对于 [FromProperty]
 * - `## @config_expression value_expression` - 对于 [FromValue]
 * - `## @type expression_1;expression_2` - 对于 [FromColumns]
 */
sealed class ParadoxConfigExpressionAnnotatedData : ParadoxAnnotatedData.Base("config_expression") {
    abstract val configGroup: CwtConfigGroup

    abstract class FromMember : ParadoxConfigExpressionAnnotatedData() {
        abstract val valueExpression: CwtDataExpression
    }

    data class FromProperty(val keyExpression: CwtDataExpression, override val valueExpression: CwtDataExpression, override val configGroup: CwtConfigGroup) : FromMember() {
        override fun render(): String {
            val keyText = keyExpression.expressionString.orNull()?.quoteIfNeeded() ?: "?"
            val valueText = valueExpression.expressionString.orNull()?.quoteIfNeeded() ?: "?"
            return "$keyText = $valueText"
        }
    }

    data class FromValue(override val valueExpression: CwtDataExpression, override val configGroup: CwtConfigGroup) : FromMember() {
        override fun render(): String {
            return valueExpression.expressionString.quoteIfNeeded()
        }
    }

    data class FromColumns(val configExpressions: List<CwtDataExpression>, override val configGroup: CwtConfigGroup) : ParadoxConfigExpressionAnnotatedData() {
        override fun render(): String {
            return configExpressions.joinToStringFast(";") { it.expressionString.orNull()?.quoteIfNeeded() ?: "?" }
        }
    }
}

/**
 * 定义类型的注解数据。
 *
 * 格式：
 * - `## @definition_type = type`
 * - `## @definition_type = type, subtype_1, subtype_2`
 */
data class ParadoxDefinitionTypeAnnotatedData(
    val type: String?,
    val subtypes: List<String> = emptyList(),
    val configGroup: CwtConfigGroup,
) : ParadoxAnnotatedData.Base("definition_type") {
    override fun render(): String {
        if (type == null) return "?"
        return buildString {
            append(type)
            subtypes.forEachFast { append(", ").append(it) }
        }
    }
}

/**
 * 覆盖策略的注解数据。
 *
 * 格式：
 * - `## @override_strategy = STRATEGY`
 */
@Optimized
data class ParadoxOverrideStrategyAnnotatedData(
    val overrideStrategy: ParadoxOverrideStrategy,
) : ParadoxAnnotatedData.Base("override_strategy") {
    override fun render(): String {
        return overrideStrategy.id
    }
}

/**
 * 作用域上下文的注解数据。
 *
 * 格式：
 * - `## @scope_context this = scope_1 root = scope_2`
 *
 * @property unchanged 是否包含未发生更改的作用域上下文信息。
 * @property detailed 是否包含详细的作用域上下文信息。这意味着会包含 `prev` `prevprev` 等回溯型系统作用域。
 */
@Suppress("UNUSED_PARAMETER")
data class ParadoxScopeContextAnnotatedData(
    val scopeContext: ParadoxScopeContext,
    val unchanged: Boolean = false,
    val detailed: Boolean = false,
) : ParadoxAnnotatedData.Base("scope_context") {
    override fun render(): String {
        return scopeContext.toPresentableString(showPrev = detailed)
    }
}
