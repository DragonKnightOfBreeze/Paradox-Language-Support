@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.buildCache
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.expression.ParadoxDefinitionSubtypeExpression

interface CwtTypeLocalisationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> //(subtypeExpression, locationConfig)

    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>

    companion object {
        fun resolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtTypeLocalisationConfig? {
    val locationConfigs: MutableList<Pair<String?, CwtLocationConfig>> = mutableListOf()
    val props1 = config.properties ?: return null
    for (prop1 in props1) {
        val subtypeName = prop1.key.removeSurroundingOrNull("subtype[", "]")?.intern()
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
    return CwtTypeLocalisationConfigImpl(config, locationConfigs)
}

private class CwtTypeLocalisationConfigImpl(
    override val config: CwtPropertyConfig,
    override val locationConfigs: List<Pair<String?, CwtLocationConfig>> //(subtypeExpression, locationConfig)
) : UserDataHolderBase(), CwtTypeLocalisationConfig {
    private val configsCache: Cache<String, List<CwtLocationConfig>> = CacheBuilder.newBuilder().buildCache()

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

    override fun toString(): String {
        return "CwtTypeLocalisationConfigImpl(locationConfigs=$locationConfigs)"
    }
}
