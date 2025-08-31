package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtEnumConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.values
import icu.windea.pls.core.caseInsensitiveStringKeyMap
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtEnumConfigResolverImpl: CwtEnumConfig.Resolver{
    // TODO an enum value can also be a template expression

    override fun resolve(config: CwtPropertyConfig): CwtEnumConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtEnumConfigImpl? {
        val key = config.key
        val name = key.removeSurroundingOrNull("enum[", "]")?.orNull()?.intern() ?: return null
        val propertyConfigValues = config.values ?: return null
        if (propertyConfigValues.isEmpty()) {
            return CwtEnumConfigImpl(config, name, emptySet(), emptyMap())
        }
        val values = caseInsensitiveStringSet() //忽略大小写
        val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() //忽略大小写
        for (propertyConfigValue in propertyConfigValues) {
            val v = propertyConfigValue.value.intern()
            values.add(v)
            valueConfigMap.put(v, propertyConfigValue)
        }
        return CwtEnumConfigImpl(config, name, values.optimized(), valueConfigMap.optimized())
    }
}

private class CwtEnumConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtEnumConfig {
    override fun toString() = "CwtEnumConfigImpl(name='$name')"
}
