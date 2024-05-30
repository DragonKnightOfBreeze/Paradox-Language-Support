package icu.windea.pls.config.config

import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name (key) string
 * @property values (value) string[]
 */
interface CwtScopeGroupConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val values: Set<@CaseInsensitive String>
    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtScopeGroupConfig? = doResolve(config)
    }
}
//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtScopeGroupConfig? {
    val name = config.key
    val propertyConfigValues = config.values ?: return null
    if(propertyConfigValues.isEmpty()) return CwtScopeGroupConfigImpl(config, name, emptySet(), emptyMap())
    val values = caseInsensitiveStringSet() //忽略大小写
    val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() //忽略大小写
    for(propertyConfigValue in propertyConfigValues) {
        values.add(propertyConfigValue.value)
        valueConfigMap.put(propertyConfigValue.value, propertyConfigValue)
    }
    return CwtScopeGroupConfigImpl(config, name, values, valueConfigMap)
}

private class CwtScopeGroupConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : CwtScopeGroupConfig