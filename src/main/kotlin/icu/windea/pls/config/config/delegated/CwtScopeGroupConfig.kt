@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
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
//Implementations (interned if necessary)

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

private class CwtScopeGroupConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtScopeGroupConfig {
    override fun toString(): String {
        return "CwtScopeGroupConfigImpl(name='$name')"
    }
}
