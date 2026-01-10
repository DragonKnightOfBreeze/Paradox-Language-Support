package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtDirectiveConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.caseInsensitiveStringKeyMap
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

internal class CwtDirectiveConfigResolverImpl : CwtDirectiveConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtDirectiveConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtDirectiveConfig? {
        val name = config.key.removeSurroundingOrNull("directive[", "]")?.orNull()?.optimized() ?: return null
        val propElements = config.properties.orEmpty()
        val propGroup = propElements.groupBy { it.key }
        val modeConfigs = propGroup.getOne("modes")?.let { prop ->
            prop.values?.associateByTo(caseInsensitiveStringKeyMap()) { it.stringValue }
        }?.optimized().orEmpty()
        val relaxModes = propGroup.getOne("relax_modes")?.let { prop ->
            prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
        }?.optimized().orEmpty()
        logger.debug { "Resolved directive config (name: $name).".withLocationPrefix(config) }
        return CwtDirectiveConfigImpl(config, name, modeConfigs, relaxModes)
    }
}

private class CwtDirectiveConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val modeConfigs: Map<String, CwtValueConfig>,
    override val relaxModes: Set<String>,
) : UserDataHolderBase(), CwtDirectiveConfig {
    override fun toString() = "CwtDirectiveConfigImpl(name='$name')"
}
