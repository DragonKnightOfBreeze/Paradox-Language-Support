package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import java.util.concurrent.*

class CwtOptionValueConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtValue>, //NOTE 目前并未使用，因此直接传入emptyPointer()即可
    override val info: CwtConfigGroupInfo,
    override val value: String,
    override val valueTypeId: Byte = CwtType.String.id,
    override val options: List<CwtOptionMemberConfig<*>>? = null,
) : CwtOptionMemberConfig<CwtValue>, CwtValueAware {
    companion object Resolver {
        private val cache = ConcurrentHashMap<String, CwtOptionValueConfig>()
        
        fun resolve(
            pointer: SmartPsiElementPointer<out CwtValue>,
            info: CwtConfigGroupInfo,
            value: String,
            valueTypeId: Byte = CwtType.String.id,
            options: List<CwtOptionMemberConfig<*>>? = null,
        ): CwtOptionValueConfig {
            //use cache if possible to optimize memory
            if(options.isNullOrEmpty()) {
                val cacheKey = "${valueTypeId}#${value}"
                return cache.getOrPut(cacheKey) {
                    CwtOptionValueConfig(pointer, info, value, valueTypeId, options)
                }
            }
            return CwtOptionValueConfig(pointer, info, value, valueTypeId, options)
        }
    }
}
