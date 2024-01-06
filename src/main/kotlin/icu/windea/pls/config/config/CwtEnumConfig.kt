package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*

class CwtEnumConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val values: Set<@CaseInsensitive String>,
    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>
) : UserDataHolderBase(), CwtConfig<CwtProperty> {
    companion object Resolver {
        fun resolve(propertyConfig: CwtPropertyConfig, name: String): CwtEnumConfig? {
            val pointer = propertyConfig.pointer
            val info = propertyConfig.info
            val propertyConfigValues = propertyConfig.values ?: return null
            if(propertyConfigValues.isEmpty()) {
                return CwtEnumConfig(pointer, info, name, emptySet(), emptyMap())
            }
            val values = caseInsensitiveStringSet() //忽略大小写
            val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() //忽略大小写
            for(propertyConfigValue in propertyConfigValues) {
                values.add(propertyConfigValue.value)
                valueConfigMap.put(propertyConfigValue.value, propertyConfigValue)
            }
            return CwtEnumConfig(pointer, info, name, values, valueConfigMap)
        }
    }
}
