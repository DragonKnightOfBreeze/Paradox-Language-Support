package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configExpression.CwtLocalisationLocationExpression
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.config.manipulation.CwtConfigCopyService
import icu.windea.pls.core.optimized
import icu.windea.pls.model.expressions.ParadoxDefinitionSubtypeExpression

/**
 * 类型本地化规则。
 *
 * 用于定位对应类型的定义的相关本地化，以便在 UI 与各种提示信息中展示。
 * 具体而言，通过位置表达式（[CwtLocalisationLocationExpression]）进行定位，并最终解析为本地化。
 *
 * 说明：
 * - 可在其中通过 `subtype[{expression}] = {...}` 指定需要匹配的子类型。其中 `{expression}` 为子类型表达式（[ParadoxDefinitionSubtypeExpression]）。支持嵌套使用。
 *
 * 路径定位：
 * - `types/type[{type}]/localisation`。其中 `{type}` 匹配定义类型。
 *
 * ### CWTools 兼容性
 *
 * 部分兼容。插件进行了额外的扩展和改进。
 *
 * ### 示例
 *
 * ```cwt
 * types = {
 *     type[ship_design] = {
 *         # ...
 *         localisation = {
 *             ## primary
 *             name = some_loc_key
 *             subtype[corvette] = {
 *                 name = some_corvette_loc_key
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * @property locationConfigGroup 子类型表达式与位置规则的配对列表。
 *
 * @see CwtLocationConfig
 * @see CwtLocationExpression
 * @see CwtLocalisationLocationExpression
 */
interface CwtTypeLocalisationConfig : CwtTypePresentationConfig {
    interface Resolver {
        /** 由属性规则解析为类型本地化规则。 */
        fun resolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig?
    }

    companion object : Resolver by CwtTypeLocalisationConfigResolverImpl()
}

// region Implementations

private class CwtTypeLocalisationConfigResolverImpl : CwtTypeLocalisationConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig? {
        val locationConfigGroup = mutableMapOf<String, MutableList<CwtLocationConfig>>()

        // #324
        CwtConfigCopyService.flattenBySubtypeExpression(config) action@{ c, e ->
            if (c !is CwtPropertyConfig) return@action
            val locationConfig = CwtLocationConfig.resolve(c) ?: return@action
            val key = e.optimized()
            locationConfigGroup.getOrPut(key) { mutableListOf() }.add(locationConfig)
        }

        if (locationConfigGroup.isEmpty()) {
            logger.warn("Skipped invalid type images config: Missing properties (after flatten).".withLocationPrefix(config))
            return null
        }
        return CwtTypeLocalisationConfigImpl(config, locationConfigGroup.mapValues { (_, v) -> v.optimized() }.optimized())
    }
}

private class CwtTypeLocalisationConfigImpl(
    override val config: CwtPropertyConfig,
    override val locationConfigGroup: Map<String, List<CwtLocationConfig>>,
) : UserDataHolderBase(), CwtTypeLocalisationConfig {
    override fun getLocationConfigs(subtypes: List<String>): List<CwtLocationConfig> {
        val result = mutableListOf<CwtLocationConfig>()
        for ((subtypeExpression, locationConfigs) in locationConfigGroup) {
            if (subtypeExpression.isEmpty() || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
                result.addAll(locationConfigs)
            }
        }
        return result
    }

    override fun toString() = "CwtTypeLocalisationConfigImpl(locationConfigs=$locationConfigGroup)"
}

// endregion
