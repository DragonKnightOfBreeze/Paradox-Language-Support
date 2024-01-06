package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*

class CwtDynamicValueConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val values: Set<@CaseInsensitive String>,
    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>
) : UserDataHolderBase(), CwtConfig<CwtProperty> {
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtDynamicValueConfig? {
            val pointer = config.pointer
            val info = config.info
            val propertyConfigValues = config.values ?: return null
            if(propertyConfigValues.isEmpty()) {
                return CwtDynamicValueConfig(pointer, info, name, emptySet(), emptyMap())
            }
            val values = caseInsensitiveStringSet() //忽略大小写
            val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() //忽略大小写
            for(propertyConfigValue in propertyConfigValues) {
                values.add(propertyConfigValue.value)
                valueConfigMap.put(propertyConfigValue.value, propertyConfigValue)
            }
            return CwtDynamicValueConfig(pointer, info, name, values, valueConfigMap)
        }
    }
}
