@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name string
 * @property values string[]
 */
interface CwtEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val values: Set<@CaseInsensitive String>
    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtEnumConfig? = doResolve(config)
    }
}

//Implementations (interned if necessary)

//TODO an enum value can be a template expression

private fun doResolve(config: CwtPropertyConfig): CwtEnumConfig? {
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

private class CwtEnumConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtEnumConfig {
    override fun toString(): String {
        return "CwtEnumConfigImpl(name='$name')"
    }
}
