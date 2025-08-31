package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtModifierCategoryConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtModifierCategoryConfigResolverImpl: CwtModifierCategoryConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? {
        val name = config.key
        var supportedScopes: Set<String>? = null
        val props = config.properties
        if (props.isNullOrEmpty()) return null
        for (prop in props) {
            when (prop.key) {
                //may be empty here (e.g., "AI Economy")
                "supported_scopes" -> supportedScopes = buildSet {
                    prop.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                    prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
                }
            }
        }
        supportedScopes = supportedScopes?.optimized() ?: ParadoxScopeManager.anyScopeIdSet
        return CwtModifierCategoryConfigImpl(config, name, supportedScopes)
    }
}

private class CwtModifierCategoryConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtModifierCategoryConfig {
    override fun toString() = "CwtModifierCategoryConfigImpl(name='$name')"
}
