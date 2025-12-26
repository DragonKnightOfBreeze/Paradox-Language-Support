package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtMacroConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtMacroConfigResolverImpl : CwtMacroConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtMacroConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtMacroConfig? {
        val name = config.key.removeSurroundingOrNull("macro[", "]")?.orNull()?.optimized() ?: return null
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid macro config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }
        val propGroup = propElements.groupBy { it.key }
        val modeConfigs = propGroup.getOne("modes")?.let { prop ->
            prop.values?.associateByTo(caseInsensitiveStringKeyMap()) { it.stringValue }
        }?.optimized().orEmpty()
        logger.debug { "Resolved macro config (name: $name).".withLocationPrefix(config) }
        return CwtMacroConfigImpl(config, name, modeConfigs)
    }
}

private class CwtMacroConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val modeConfigs: Map<String, CwtValueConfig>
) : UserDataHolderBase(), CwtMacroConfig {
    override fun toString() = "CwtMacroConfigImpl(name='$name')"
}
