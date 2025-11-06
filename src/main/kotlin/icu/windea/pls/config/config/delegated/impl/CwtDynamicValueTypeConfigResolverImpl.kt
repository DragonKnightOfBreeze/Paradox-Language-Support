package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.caseInsensitiveStringKeyMap
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

class CwtDynamicValueTypeConfigResolverImpl : CwtDynamicValueTypeConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    // TODO a dynamic value can also be a template expression

    override fun resolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtDynamicValueTypeConfig? {
        val key = config.key
        val name = key.removeSurroundingOrNull("value[", "]")?.orNull()?.optimized() ?: return null
        val valueElements = config.values
        if (valueElements == null) {
            logger.warn("Skipped invalid dynamic value type config (name: $name): Null values.".withLocationPrefix(config))
            return null
        }
        if (valueElements.isEmpty()) {
            logger.debug { "Resolved dynamic value type config with empty values (name: $name).".withLocationPrefix(config) }
            return CwtDynamicValueTypeConfigImpl(config, name, emptySet(), emptyMap())
        }
        val values = caseInsensitiveStringSet() // ignore case
        val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() // ignore case
        for (propertyConfigValue in valueElements) {
            val v = propertyConfigValue.value.optimized()
            values.add(v)
            valueConfigMap.put(v, propertyConfigValue)
        }
        logger.debug { "Resolved dynamic value type config (name: $name).".withLocationPrefix(config) }
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
