package icu.windea.pls.config.config.delegated

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configExpression.CwtImageLocationExpression
import icu.windea.pls.config.configExpression.CwtLocationExpression
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.cache.CacheBuilder
import icu.windea.pls.core.cache.cancelable
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression

/**
 * 类型图片规则。
 *
 * 用于定位对应类型的定义的相关图片，以便在 UI 与各种提示信息中展示。
 * 具体而言，通过位置表达式（[CwtImageLocationExpression]）进行定位，并最终解析为处理后的图片。
 *
 * 路径定位：`types/type[{type}]/images`，`{type}` 匹配定义类型。
 *
 * CWTools 兼容性：兼容，但存在一些扩展。
 *
 * 示例：
 * ```cwt
 * types = {
 *     type[component_template] = {
 *         # ...
 *         images = {
 *             ## primary
 *             ## required
 *             icon = "icon|icon_frame"
 *         }
 *     }
 * }
 * ```
 *
 * @property locationConfigs 子类型表达式与位置规则的配对列表。
 *
 * @see CwtLocationConfig
 * @see CwtLocationExpression
 * @see CwtImageLocationExpression
 */
interface CwtTypeImagesConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> // (subtypeExpression, locationConfig)

    /** 按给定的 [subtypes] 合并与筛选后，返回生效的图片位置规则列表。 */
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    interface Resolver {
        /** 由属性规则解析为类型图片规则。 */
        fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig?
    }

    companion object : Resolver by CwtTypeImagesConfigResolverImpl()
}

// region Implementations

private class CwtTypeImagesConfigResolverImpl : CwtTypeImagesConfig.Resolver, CwtConfigResolverScope {
    // no logger here (unnecessary)

    override fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtTypeImagesConfig? {
        val locationConfigs: MutableList<Pair<String?, CwtLocationConfig>> = mutableListOf()
        val props1 = config.properties ?: return null
        for (prop1 in props1) {
            val subtypeName = prop1.key.removeSurroundingOrNull("subtype[", "]")?.optimized()
            if (subtypeName != null) {
                val props2 = prop1.properties ?: continue
                for (prop2 in props2) {
                    val locationConfig = CwtLocationConfig.resolve(prop2) ?: continue
                    locationConfigs.add(subtypeName to locationConfig)
                }
            } else {
                val locationConfig = CwtLocationConfig.resolve(prop1) ?: continue
                locationConfigs.add(null to locationConfig)
            }
        }
        return CwtTypeImagesConfigImpl(config, locationConfigs.optimized())
    }
}

private class CwtTypeImagesConfigImpl(
    override val config: CwtPropertyConfig,
    override val locationConfigs: List<Pair<String?, CwtLocationConfig>>
) : UserDataHolderBase(), CwtTypeImagesConfig {
    private val configsCache = CacheBuilder().build<String, List<CwtLocationConfig>>().cancelable()

    override fun getConfigs(subtypes: List<String>): List<CwtLocationConfig> {
        val cacheKey = subtypes.joinToString(",")
        return configsCache.get(cacheKey) {
            val result = mutableListOf<CwtLocationConfig>()
            for ((subtypeExpression, locationConfig) in locationConfigs) {
                if (subtypeExpression == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
                    result.add(locationConfig)
                }
            }
            result
        }
    }

    override fun toString() = "CwtTypeImagesConfigImpl(locationConfigs=$locationConfigs)"
}

// endregion
