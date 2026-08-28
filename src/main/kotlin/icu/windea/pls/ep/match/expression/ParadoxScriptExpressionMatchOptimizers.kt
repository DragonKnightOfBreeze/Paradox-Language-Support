package icu.windea.pls.ep.match.expression

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.filterFast
import icu.windea.pls.core.collections.filterIsInstanceFast
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.findChild
import icu.windea.pls.lang.match.ParadoxExpressionMatchService
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchContext
import icu.windea.pls.lang.match.ParadoxScriptExpressionMatchOptimizerContext
import icu.windea.pls.lang.resolve.ParadoxConfigService
import icu.windea.pls.lang.util.ParadoxParameterManager
import icu.windea.pls.model.expressions.ParadoxExpression
import icu.windea.pls.model.type.CwtExpressionType
import icu.windea.pls.model.type.ParadoxExpressionType
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptParameter
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

/**
 * 如果要匹配的是字符串，且匹配结果中存在作为常量匹配的规则，则仅保留这些规则。
 */
@Optimized
class ParadoxScriptExpressionConstantMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    override fun <T : CwtMemberConfig<*>> optimize(input: List<T>, context: ParadoxScriptExpressionMatchOptimizerContext): List<T>? {
        if (input.size <= 1) return null
        if (context.expression.type != ParadoxExpressionType.String) return null
        val filtered = input.filterFast { ParadoxExpressionMatchService.matchesConstant(context.expression, it.configExpression, context.configGroup) }
        if (filtered.isEmpty()) return null
        return filtered
    }
}

/**
 * 如果参与匹配的表达式带参数，且是整个作为参数，且可以（基于扩展规则，而非用法）推断得到参数的上下文规则，则尝试根据这些规则进行进一步的匹配。
 */
@Optimized
class ParadoxScriptExpressionParameterizedMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    override fun <T : CwtMemberConfig<*>> optimize(input: List<T>, context: ParadoxScriptExpressionMatchOptimizerContext): List<T>? {
        if (!context.expression.isFullParameterized()) return null
        val element = context.element
        val expressionElement = when (element) {
            is ParadoxScriptProperty -> element.propertyKey
            is ParadoxScriptValue -> element
            else -> return null
        }
        val parameterElement = expressionElement.findChild<ParadoxScriptParameter>() ?: return null
        val inferredConfigs = ParadoxParameterManager.getInferredConfigsForLiteral(parameterElement)
        if (inferredConfigs.isEmpty()) return null
        var result: MutableList<T>? = null
        input.forEachFast { config ->
            if (CwtConfigManipulationService.mergeAndMatchValueConfigs(inferredConfigs, config.configExpression)) {
                val result = result ?: mutableListOf<T>().also { result = it }
                result += config
            }
        }
        return result
    }
}

/**
 * 如果匹配结果中的规则在分组后，同一分组后存在多个值为块（`{...}`）的规则，则尝试根据块中的内容进行进一步的匹配。
 * 如果是属性规则则按照属性键分组，如果是指规则则单独分组。
 *
 * TODO 如果匹配时发现存在冲突，应直接移除所有参与匹配的规则。
 *  例如，块中使用到了属性键分别为 X 和 Y 的属性，而这两个属性键分别匹配两个不同的属性规则。
 *  另外，对应的代码检查中应提供特殊的报错信息。
 */
@Optimized
class ParadoxScriptExpressionBlockMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    override fun isDynamic(context: ParadoxScriptExpressionMatchOptimizerContext) = true

    override fun <T : CwtMemberConfig<*>> optimize(input: List<T>, context: ParadoxScriptExpressionMatchOptimizerContext): List<T>? {
        if (input.size <= 1) return null
        val filtered = input.filterIsInstanceFast<CwtPropertyConfig> { it.valueType == CwtExpressionType.Block }
        if (filtered.isEmpty()) return null
        val filteredGroup = mutableMapOf<String, MutableList<CwtPropertyConfig>>()
        filtered.forEachFast { c -> filteredGroup.getOrPut(c.key) { mutableListOf() } += c }
        val blockExpression = ParadoxExpression.resolveBlock()
        var block: ParadoxScriptBlock? = null
        var configsToRemove: MutableSet<CwtPropertyConfig>? = null
        filteredGroup.values.forEach f1@{ filteredConfigs ->
            if (filteredConfigs.size <= 1) return@f1
            if (block == null) block = context.element.castOrNull<ParadoxScriptProperty>()?.block ?: return null
            filteredConfigs.forEachFast f2@{ filteredConfig ->
                val valueConfig = filteredConfig.valueConfig ?: return@f2
                val matchContext = ParadoxScriptExpressionMatchContext(block, blockExpression, valueConfig.configExpression, valueConfig, context.configGroup, context.options)
                val matchResult = ParadoxExpressionMatchService.matchScriptExpression(matchContext)
                if (!matchResult.get(matchContext.options)) {
                    val configsToRemove = configsToRemove ?: mutableSetOf<CwtPropertyConfig>().also { configsToRemove = it }
                    configsToRemove += filteredConfig
                }
            }
        }
        if (configsToRemove == null) return null
        val result = input.filterFast { it is CwtPropertyConfig && it !in configsToRemove }
        return result
    }
}

/**
 * 如果匹配结果中存在需要重载的规则，则替换成重载后的规则并再次进行匹配。
 */
@Optimized
class ParadoxScriptExpressionOverriddenMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    override fun isDynamic(context: ParadoxScriptExpressionMatchOptimizerContext) = true

    override fun <T : CwtMemberConfig<*>> optimize(input: List<T>, context: ParadoxScriptExpressionMatchOptimizerContext): List<T>? {
        if (input.isEmpty()) return null
        var result: MutableList<T>? = null
        input.forEachFast f1@{ config ->
            val overriddenConfigs = ParadoxConfigService.getOverriddenConfigs(context.element, config)
            if (overriddenConfigs.isEmpty()) {
                val result = result ?: mutableListOf<T>().also { result = it }
                result += config
                return@f1
            }
            overriddenConfigs.forEachFast f2@{ overriddenConfig ->
                val matchContext = ParadoxScriptExpressionMatchContext(context.element, context.expression, overriddenConfig.configExpression, overriddenConfig, context.configGroup, context.options)
                val matchResult = ParadoxExpressionMatchService.matchScriptExpression(matchContext)
                if (matchResult.get(matchContext.options)) {
                    val result = result ?: mutableListOf<T>().also { result = it }
                    result += overriddenConfig
                }
            }
        }
        return result
    }
}
