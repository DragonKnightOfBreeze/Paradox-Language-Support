package icu.windea.pls.ep.match

import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.ep.config.CwtOverriddenConfigProvider
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.util.ParadoxExpressionManager.isConstantMatch
import icu.windea.pls.model.CwtType
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxScriptExpressionConstantMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    // 如果要匹配的是字符串，且匹配结果中存在作为常量匹配的规则，则仅保留这些规则

    override fun optimize(configs: List<CwtMemberConfig<*>>, context: ParadoxScriptExpressionMatchOptimizer.Context): List<CwtMemberConfig<*>> {
        if (configs.size <= 1) return configs
        if (context.expression.type != ParadoxType.String) return configs
        val filtered = configs.filter { isConstantMatch(context.configGroup, context.expression, it.configExpression) }
        if (filtered.isEmpty()) return configs
        return filtered
    }
}

class ParadoxScriptExpressionBlockMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    // 如果匹配结果中存在键相同的规则，且其值是子句，则尝试根据子句内容进行进一步的匹配

    override fun optimize(configs: List<CwtMemberConfig<*>>, context: ParadoxScriptExpressionMatchOptimizer.Context): List<CwtMemberConfig<*>> {
        if (configs.isEmpty()) return configs
        val groupedByKey = configs.filterIsInstance<CwtPropertyConfig>().groupBy { it.key }.values
        if (groupedByKey.isEmpty()) return configs
        val filteredGroup = groupedByKey.filter { configsPerKey ->
            configsPerKey.size > 1 && configsPerKey.count { it.valueType == CwtType.Block } > 1
        }
        if (filteredGroup.isEmpty()) return configs
        val blockElement = context.element.castOrNull<ParadoxScriptProperty>()?.block ?: return configs
        val blockExpression = ParadoxScriptExpression.resolveBlock()
        val configsToRemove = mutableSetOf<CwtPropertyConfig>()
        for (group in filteredGroup) {
            for (config in group) {
                val valueConfig = config.valueConfig ?: continue
                val matchResult = ParadoxMatchService.matchScriptExpression(
                    blockElement,
                    blockExpression,
                    valueConfig.configExpression,
                    valueConfig,
                    context.configGroup,
                    context.options
                )
                if (!matchResult.get(context.options)) {
                    configsToRemove += config
                }
            }
        }
        if (configsToRemove.isEmpty()) return configs
        return configs.filter { it !in configsToRemove }
    }
}

class ParadoxScriptExpressionOverriddenMatchOptimizer : ParadoxScriptExpressionMatchOptimizer {
    // 如果结果中存在需要重载的规则，则替换成重载后的规则并再次进行匹配

    override fun optimize(configs: List<CwtMemberConfig<*>>, context: ParadoxScriptExpressionMatchOptimizer.Context): List<CwtMemberConfig<*>> {
        if (configs.isEmpty()) return configs
        val result = mutableListOf<CwtMemberConfig<*>>()
        for (config in configs) {
            val overriddenConfigs = CwtOverriddenConfigProvider.getOverriddenConfigs(context.element, config)
            if (overriddenConfigs.isEmpty()) {
                result += config
                continue
            }
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
        return result
    }
}
