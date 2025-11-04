package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.ReversibleValue

internal class CwtSubtypeConfigResolverImpl : CwtSubtypeConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSubtypeConfig? {
        val name = config.key.removeSurroundingOrNull("subtype[", "]")?.orNull()?.intern() ?: return null
        val typeKeyFilter = config.optionData { typeKeyFilter }
        val typeKeyRegex = config.optionData { typeKeyRegex }
        val startsWith = config.optionData { startsWith }
        val onlyIfNot = config.optionData { onlyIfNot }
        val group = config.optionData { group }
        logger.debug { "Resolved subtype config (name: $name).".withLocationPrefix(config) }
        return CwtSubtypeConfigImpl(config, name, typeKeyFilter, typeKeyRegex, startsWith, onlyIfNot, group)
    }
}

private class CwtSubtypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val typeKeyFilter: ReversibleValue<Set<String>>? = null,
    override val typeKeyRegex: Regex? = null,
    override val startsWith: String? = null,
    override val onlyIfNot: Set<String>? = null,
    override val group: String? = null,
) : UserDataHolderBase(), CwtSubtypeConfig {
    override fun toString() = "CwtSubtypeConfigImpl(name='$name')"
}
