package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtExtendedComplexEnumValueConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.util.CwtConfigResolverMixin

class CwtExtendedComplexEnumValueConfigResolverImpl : CwtExtendedComplexEnumValueConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig = doResolve(config, type)

    private fun doResolve(config: CwtMemberConfig<*>, type: String): CwtExtendedComplexEnumValueConfig {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val hint = config.optionData { hint }
        logger.debug { "Resolved extended complex enum value config (name: $name, type: $type).".withLocationPrefix(config) }
        return CwtExtendedComplexEnumValueConfigImpl(config, name, type, hint)
    }
}

private class CwtExtendedComplexEnumValueConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedComplexEnumValueConfig {
    override fun toString() = "CwtExtendedComplexEnumValueConfigImpl(name='$name', type='$type')"
}
