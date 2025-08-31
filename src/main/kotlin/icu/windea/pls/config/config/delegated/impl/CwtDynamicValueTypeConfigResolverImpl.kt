package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.values
import icu.windea.pls.core.caseInsensitiveStringKeyMap
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

class CwtDynamicValueTypeConfigResolverImpl : CwtDynamicValueTypeConfig.Resolver {
    // TODO a dynamic value can also be a template expression

    override fun resolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfigImpl? {
        val key = config.key
        val name = key.removeSurroundingOrNull("value[", "]")?.orNull()?.intern() ?: return null
        val propertyConfigValues = config.values ?: return null
        if (propertyConfigValues.isEmpty()) {
            return CwtDynamicValueTypeConfigImpl(config, name, emptySet(), emptyMap())
        }
        val values = caseInsensitiveStringSet() //忽略大小写
        val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() //忽略大小写
        for (propertyConfigValue in propertyConfigValues) {
            val v = propertyConfigValue.value.intern()
            values.add(v)
            valueConfigMap.put(v, propertyConfigValue)
        }
        return CwtDynamicValueTypeConfigImpl(config, name, values.optimized(), valueConfigMap.optimized())
    }
}

private class CwtDynamicValueTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtDynamicValueTypeConfig {
    override fun toString() = "CwtDynamicValueTypeConfigImpl(name='$name')"
}
