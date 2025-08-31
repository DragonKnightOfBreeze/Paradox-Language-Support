package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtLocalisationPromotionConfigResolverImpl: CwtLocalisationPromotionConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig {
        val name = config.key
        val supportedScopes = buildSet {
            config.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
            config.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
        }.optimized()
        return CwtLocalisationPromotionConfigImpl(config, name, supportedScopes)
    }
}

private class CwtLocalisationPromotionConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtLocalisationPromotionConfig {
    override fun toString() = "CwtLocalisationPromotionConfigImpl(name='$name')"
}
