package icu.windea.pls.config.config.delegated.impl

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtLocationConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtTypeImagesConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.buildCache
import icu.windea.pls.lang.expression.ParadoxDefinitionSubtypeExpression

internal class CwtTypeImagesConfigResolverImpl : CwtTypeImagesConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtTypeImagesConfig? {
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
        return CwtTypeImagesConfigImpl(config, locationConfigs)
    }
}

private class CwtTypeImagesConfigImpl(
    override val config: CwtPropertyConfig,
    override val locationConfigs: List<Pair<String?, CwtLocationConfig>>
) : UserDataHolderBase(), CwtTypeImagesConfig {
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

    override fun toString() = "CwtTypeImagesConfigImpl(locationConfigs=$locationConfigs)"
}
