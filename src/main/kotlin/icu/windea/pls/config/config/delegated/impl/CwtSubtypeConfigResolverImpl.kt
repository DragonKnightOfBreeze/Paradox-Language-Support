package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtSubtypeConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.ReversibleValue

internal class CwtSubtypeConfigResolverImpl : CwtSubtypeConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtSubtypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSubtypeConfig? {
        val name = config.key.removeSurroundingOrNull("subtype[", "]")?.orNull()?.intern() ?: return null
        val typeKeyFilter: ReversibleValue<Set<String>>? = config.optionData { typeKeyFilter }
        val typeKeyRegex: Regex? = config.optionData { typeKeyRegex }
        val startsWith: String? = config.optionData { startsWith }
        val onlyIfNot: Set<String>? = config.optionData { onlyIfNot }
        return CwtSubtypeConfigImpl(config, name, typeKeyFilter, typeKeyRegex, startsWith, onlyIfNot?.optimized())
    }
}

private class CwtSubtypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val typeKeyFilter: ReversibleValue<Set<String>>? = null,
    override val typeKeyRegex: Regex? = null,
    override val startsWith: String? = null,
    override val onlyIfNot: Set<String>? = null
) : UserDataHolderBase(), CwtSubtypeConfig {
    override fun inGroup(groupName: String): Boolean {
        return config.optionData { group } == groupName
    }

    override fun toString() = "CwtSubtypeConfigImpl(name='$name')"
}
