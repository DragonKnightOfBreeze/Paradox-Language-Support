package icu.windea.pls.ep.match

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.util.CwtConfigService
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.filterIsInstance
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptExpressionConstantMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    // 如果要匹配的是字符串，且匹配结果中存在作为常量匹配的规则，则仅保留这些规则

    override fun isDynamic(context: ParadoxScriptExpressionMatchOptimizer.Context): Boolean {
        return false
    }

    override fun optimize(configs: List<CwtMemberConfig<*>>, context: ParadoxScriptExpressionMatchOptimizer.Context): List<CwtMemberConfig<*>>? {
        if (configs.size <= 1) return null
        if (context.expression.type != ParadoxType.String) return null
        val filtered = configs.filter { ParadoxMatchService.isConstantMatch(context.expression, it.configExpression, context.configGroup) }
        if (filtered.isEmpty()) return null
        return filtered
    }
}

class ParadoxScriptExpressionBlockMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    // 如果匹配结果中存在键相同的规则，且其值是子句，则尝试根据子句内容进行进一步的匹配

    override fun isDynamic(context: ParadoxScriptExpressionMatchOptimizer.Context): Boolean {
        return true
    }

    override fun optimize(configs: List<CwtMemberConfig<*>>, context: ParadoxScriptExpressionMatchOptimizer.Context): List<CwtMemberConfig<*>>? {
        if (configs.isEmpty()) return null
        val filteredGroup = configs.filterIsInstance<CwtPropertyConfig> { it.valueType == CwtType.Block }
            .groupBy { it.key }.values
            .filter { it.count() > 1 }
        if (filteredGroup.isEmpty()) return null
        val blockElement = context.element.castOrNull<ParadoxScriptProperty>()?.block ?: return null
        val blockExpression = ParadoxScriptExpression.resolveBlock()
        val configsToRemove = mutableSetOf<CwtPropertyConfig>()
        for (filteredConfigs in filteredGroup) {
            for (filteredConfig in filteredConfigs) {
                val valueConfig = filteredConfig.valueConfig ?: continue
                val matchResult = ParadoxMatchService.matchScriptExpression(
                    blockElement,
                    blockExpression,
                    valueConfig.configExpression,
                    valueConfig,
                    context.configGroup,
                    context.options
                )
                if (!matchResult.get(context.options)) {
                    configsToRemove += filteredConfig
                }
            }
        }
        if (configsToRemove.isEmpty()) return null
        return configs.filter { it !in configsToRemove }
    }
}

class ParadoxScriptExpressionOverriddenMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    // 如果结果中存在需要重载的规则，则替换成重载后的规则并再次进行匹配

    override fun isDynamic(context: ParadoxScriptExpressionMatchOptimizer.Context): Boolean {
        return true
    }

    override fun optimize(configs: List<CwtMemberConfig<*>>, context: ParadoxScriptExpressionMatchOptimizer.Context): List<CwtMemberConfig<*>>? {
        if (configs.isEmpty()) return null
        val result = mutableListOf<CwtMemberConfig<*>>()
        var hasOverride = false
        for (config in configs) {
            val overriddenConfigs = CwtConfigService.getOverriddenConfigs(context.element, config)
            if (overriddenConfigs.isEmpty()) {
                result += config
                continue
            }
            hasOverride = true
            for (overriddenConfig in overriddenConfigs) {
                val matchResult = ParadoxMatchService.matchScriptExpression(
                    context.element,
                    context.expression,
                    overriddenConfig.configExpression,
                    overriddenConfig,
                    context.configGroup,
                    context.options
                )
                if (matchResult.get(context.options)) {
                    result += overriddenConfig
                }
            }
        }
        if (!hasOverride) return null
        return result
    }
}
