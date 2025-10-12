package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.values
import icu.windea.pls.config.util.CwtConfigResolverUtil.withLocationPrefix
import icu.windea.pls.core.caseInsensitiveStringKeyMap
import icu.windea.pls.core.caseInsensitiveStringSet
import icu.windea.pls.core.collections.optimized

internal class CwtScopeGroupConfigResolverImpl : CwtScopeGroupConfig.Resolver {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtScopeGroupConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtScopeGroupConfig? {
        val name = config.key
        val valueElements = config.values
        if (valueElements == null) {
            logger.warn("Skipped invalid scope group config (name: $name): Null values.".withLocationPrefix(config))
            return null
        }
        if (valueElements.isEmpty()) {
            logger.debug { "Resolved scope group config with empty values (name: $name).".withLocationPrefix(config) }
            return CwtScopeGroupConfigImpl(config, name, emptySet(), emptyMap())
        }
        val values = caseInsensitiveStringSet() // ignore case
        val valueConfigMap = caseInsensitiveStringKeyMap<CwtValueConfig>() // ignore case
        for (valueElement in valueElements) {
            values.add(valueElement.value)
            valueConfigMap.put(valueElement.value, valueElement)
        }
        logger.debug { "Resolved scope group config (name: $name).".withLocationPrefix(config) }
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
