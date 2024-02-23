package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import java.util.concurrent.*

class CwtOptionValueConfig private constructor(
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
                    CwtOptionValueConfig(value, valueTypeId, options)
                }
            }
            return CwtOptionValueConfig(value, valueTypeId, options)
        }
    }
}
