package icu.windea.pls.config.config

import com.google.common.cache.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.util.*
import icu.windea.pls.cwt.psi.*

class CwtTypeImagesConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val locationConfigs: List<Pair<String?, CwtLocationConfig>> //(subtypeExpression, locationConfig)
) : CwtConfig<CwtProperty> {
    private val configsCache: Cache<String, List<CwtLocationConfig>> = CacheBuilder.newBuilder().buildCache()
    
    /**
     * 得到根据子类型列表进行合并后的配置。
     */
    fun getConfigs(subtypes: List<String>): List<CwtLocationConfig> {
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
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtTypeImagesConfig? {
            val locationConfigs: MutableList<Pair<String?, CwtLocationConfig>> = mutableListOf()
            val props1 = config.properties ?: return null
            for(prop1 in props1) {
                val subtypeName = prop1.key.removeSurroundingOrNull("subtype[", "]")
                if(subtypeName != null) {
                    val props2 = prop1.properties ?: continue
                    for(prop2 in props2) {
                        val locationConfig = CwtLocationConfig.resolve(prop2, prop2.key) ?: continue
                        locationConfigs.add(subtypeName to locationConfig)
                    }
                } else {
                    val locationConfig = CwtLocationConfig.resolve(prop1, prop1.key) ?: continue
                    locationConfigs.add(null to locationConfig)
                }
            }
            return CwtTypeImagesConfig(config.pointer, config.info, locationConfigs)
        }
    }
}