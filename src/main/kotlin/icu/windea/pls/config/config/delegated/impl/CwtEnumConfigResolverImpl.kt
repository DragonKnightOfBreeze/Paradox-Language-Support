package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtEnumConfigResolverImpl : CwtEnumConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    // TODO an enum value can also be a template expression

    override fun resolve(config: CwtPropertyConfig): CwtEnumConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtEnumConfig? {
        val key = config.key
        val name = key.removeSurroundingOrNull("enum[", "]")?.orNull()?.optimized() ?: return null
        val valueElements = config.values
        if (valueElements == null) {
            logger.warn("Skipped invalid enum config (name: $name): Null values.".withLocationPrefix(config))
            return null
        }
        if (valueElements.isEmpty()) {
            logger.debug { "Resolved enum config with empty values (name: $name).".withLocationPrefix(config) }
            return CwtEnumConfigImpl(config, name, emptySet(), emptyMap())
        }
        val values = caseInsensitiveStringSet() // ignore case
        val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() // ignore case
        for (valueElement in valueElements) {
            values.add(valueElement.value)
            valueConfigMap.put(valueElement.value, valueElement)
        }
        logger.debug { "Resolved enum config (name: $name).".withLocationPrefix(config) }
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
