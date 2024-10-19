package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*

/**
 * @property name string
 * @property values template_expression[]
 */
interface CwtDynamicValueTypeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val values: Set<@CaseInsensitive String>
    val valueConfigMap: Map<@CaseInsensitive String, CwtValueConfig>

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig? {
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

private class CwtDynamicValueTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val values: Set<String>,
    override val valueConfigMap: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtDynamicValueTypeConfig
