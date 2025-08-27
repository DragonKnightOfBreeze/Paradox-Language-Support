@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.processDescendants
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.expression.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.lang.isIdentifier

/**
 * @property name string
 */
interface CwtDeclarationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val configForDeclaration: CwtPropertyConfig

    object Keys : KeyRegistry()

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String? = null): CwtDeclarationConfig? = doResolve(config, name)
    }
}

//Accessors

val CwtDeclarationConfig.subtypesUsedInDeclaration: Set<String> by createKey(CwtDeclarationConfig.Keys) {
    val result = sortedSetOf<String>()
    config.processDescendants {
        if (it is CwtPropertyConfig) {
            val subtypeExpression = it.key.removeSurroundingOrNull("subtype[", "]")
            if (subtypeExpression != null) {
                val resolved = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression)
                resolved.subtypes.forEach { (_, subtype) -> result.add(subtype) }
            }
        }
        true
    }
    result.optimized()
}

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig, name: String?): CwtDeclarationConfig? {
    val name0 = name ?: config.key.takeIf { it.isIdentifier() } ?: return null
    return CwtDeclarationConfigImpl(config, name0)
}

private class CwtDeclarationConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
) : UserDataHolderBase(), CwtDeclarationConfig {
    override val configForDeclaration: CwtPropertyConfig by lazy {
        CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }

    override fun toString(): String {
        return "CwtDeclarationConfigImpl(name='$name')"
    }
}
