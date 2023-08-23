package icu.windea.pls.lang.cwt.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import java.util.concurrent.*

class CwtOptionConfig private constructor(
    override val pointer: SmartPsiElementPointer<CwtOption>, //NOTE 目前并未使用，因此直接传入emptyPointer()即可
    override val info: CwtConfigGroupInfo,
    override val key: String,
    override val value: String,
    override val valueTypeId: Byte = CwtType.String.id,
    override val separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
    override val options: List<CwtOptionMemberConfig<*>>? = null,
) : CwtOptionMemberConfig<CwtOption>, CwtKeyAware {
    companion object Resolver {
        private val cache = ConcurrentHashMap<String, CwtOptionConfig>()
        
        fun resolve(
            pointer: SmartPsiElementPointer<CwtOption>,
            info: CwtConfigGroupInfo,
            key: String,
            value: String,
            valueTypeId: Byte = CwtType.String.id,
            separatorTypeId: Byte = CwtSeparatorType.EQUAL.id,
            options: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionConfig {
            //use cache if possible to optimize memory
            if(options.isNullOrEmpty()) {
                val cacheKey = "${valueTypeId}#${separatorTypeId}#${key}#${value}"
                return cache.computeIfAbsent(cacheKey) {
                    CwtOptionConfig(pointer, info, key, value, valueTypeId, separatorTypeId, options)
                }
            }
            return CwtOptionConfig(pointer, info, key, value, valueTypeId, separatorTypeId, options)
        }
    }
}