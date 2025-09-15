package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDynamicValueConfig
import icu.windea.pls.config.config.optionData

class CwtExtendedDynamicValueConfigResolverImpl : CwtExtendedDynamicValueConfig.Resolver {
    override fun resolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfig = doResolve(config, type)

    private fun doResolve(config: CwtMemberConfig<*>, type: String): CwtExtendedDynamicValueConfigImpl {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val hint = config.optionData { hint }
        return CwtExtendedDynamicValueConfigImpl(config, name, type, hint)
    }
}

private class CwtExtendedDynamicValueConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedDynamicValueConfig {
    override fun toString() = "CwtExtendedDynamicValueConfigImpl(name='$name', type='$type')"
}
