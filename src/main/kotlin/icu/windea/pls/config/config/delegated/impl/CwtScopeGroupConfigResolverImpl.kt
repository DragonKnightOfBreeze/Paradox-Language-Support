package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtScopeGroupConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.values
import icu.windea.pls.core.caseInsensitiveStringKeyMap
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimized

internal class CwtScopeGroupConfigResolverImpl : CwtScopeGroupConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtScopeGroupConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtScopeGroupConfig? {
        val name = config.key
        val propertyConfigValues = config.values ?: return null
        if (propertyConfigValues.isEmpty()) return CwtScopeGroupConfigImpl(config, name, emptySet(), emptyMap())
        val values = caseInsensitiveStringSet() //忽略大小写
        val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() //忽略大小写
        for (propertyConfigValue in propertyConfigValues) {
            values.add(propertyConfigValue.value)
            valueConfigMap.put(propertyConfigValue.value, propertyConfigValue)
        }
        return CwtScopeGroupConfigImpl(config, name, values.optimized(), valueConfigMap.optimized())
    }
}

private class CwtScopeGroupConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtScopeGroupConfig {
    override fun toString() = "CwtScopeGroupConfigImpl(name='$name')"
}
