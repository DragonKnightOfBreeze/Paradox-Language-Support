@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*

/**
 * @property name string
 * @property supportedScopes (value) string[]
 */
interface CwtLocalisationPromotionConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val supportedScopes: Set<String>

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig = doResolve(config)
    }
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig): CwtLocalisationPromotionConfig {
    val name = config.key
    val supportedScopes = buildSet {
        config.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
        config.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
    }.optimized()
    return CwtLocalisationPromotionConfigImpl(config, name, supportedScopes)
}

private class CwtLocalisationPromotionConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtLocalisationPromotionConfig
