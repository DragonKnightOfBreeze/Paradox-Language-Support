package icu.windea.pls.config.config

import com.google.common.cache.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.expression.*

interface CwtTypeImagesConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> //(subtypeExpression, locationConfig)
    
    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig>
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtTypeImagesConfig? {
    val locationConfigs: MutableList<Pair<String?, CwtLocationConfig>> = mutableListOf()
    val props1 = config.properties ?: return null
    for(prop1 in props1) {
        val subtypeName = prop1.key.removeSurroundingOrNull("subtype[", "]")?.intern()
        if(subtypeName != null) {
            val props2 = prop1.properties ?: continue
            for(prop2 in props2) {
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

private class CwtTypeImagesConfigImpl(
    override val config: CwtPropertyConfig,
    override val locationConfigs: List<Pair<String?, CwtLocationConfig>>
) : CwtTypeImagesConfig {
    private val configsCache: Cache<String, List<CwtLocationConfig>> = CacheBuilder.newBuilder().buildCache()
    
    override fun getConfigs(subtypes: List<String>): List<CwtLocationConfig> {
        val cacheKey = subtypes.joinToString(",")
        return configsCache.get(cacheKey) {
            val result = mutableListOf<CwtLocationConfig>()
            for((subtypeExpression, locationConfig) in locationConfigs) {
                if(subtypeExpression == null || ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression).matches(subtypes)) {
                    result.add(locationConfig)
                }
            }
            result
        }
    }
}
