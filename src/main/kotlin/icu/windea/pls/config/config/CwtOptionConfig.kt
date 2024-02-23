package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import java.util.concurrent.*

class CwtOptionConfig private constructor(
    override val key: String,
    override val value: String,
    override val valueTypeId: Byte = CwtType.String.id,
    override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
    override val options: List<CwtOptionMemberConfig<*>>? = null,
) : CwtOptionMemberConfig<CwtOption>, CwtKeyAware {
    companion object Resolver {
        private val cache = ConcurrentHashMap<String, CwtOptionConfig>()
        
        fun resolve(
            key: String,
            value: String,
            valueTypeId: Byte = CwtType.String.id,
            separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
            options: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionConfig {
            //use cache if possible to optimize memory
            if(options.isNullOrEmpty()) {
                val cacheKey = "${valueTypeId}#${separatorTypeId}#${key}#${value}"
                return cache.getOrPut(cacheKey) {
                    CwtOptionConfig(key, value, valueTypeId, separatorTypeId, options)
                }
            }
            return CwtOptionConfig(key, value, valueTypeId, separatorTypeId, options)
        }
    }
}