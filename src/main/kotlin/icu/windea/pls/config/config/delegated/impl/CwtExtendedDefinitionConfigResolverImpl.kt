package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix

class CwtExtendedDefinitionConfigResolverImpl : CwtExtendedDefinitionConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val type = config.optionData { type }
        if (type == null) {
            logger.warn("Skipped invalid extended definition config (name: $name): Missing type option.".withLocationPrefix(config))
            return null
        }
        val hint = config.optionData { hint }
        logger.debug { "Resolved extended definition config (name: $name, type: $type).".withLocationPrefix(config) }
        return CwtExtendedDefinitionConfigImpl(config, name, type, hint)
    }
}

private class CwtExtendedDefinitionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedDefinitionConfig {
    override fun toString() = "CwtExtendedDefinitionConfigImpl(name='$name', type='$type')"
}
