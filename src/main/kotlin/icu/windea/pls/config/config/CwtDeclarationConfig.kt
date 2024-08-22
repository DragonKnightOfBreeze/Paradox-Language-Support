package icu.windea.pls.config.config

import icu.windea.pls.config.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*

/**
 * @property name string
 */
interface CwtDeclarationConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val configForDeclaration: CwtPropertyConfig
    val subtypesUsedInDeclaration: Set<String>
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String? = null): CwtDeclarationConfig? = doResolve(config, name)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig, name: String?): CwtDeclarationConfig? {
    val name0 = name ?: config.key.takeIf { it.isIdentifier() } ?: return null
    return CwtDeclarationConfigImpl(config, name0)
}

private class CwtDeclarationConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
) : CwtDeclarationConfig {
    override val configForDeclaration: CwtPropertyConfig by lazy {
        CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }
    
    override val subtypesUsedInDeclaration by lazy {
        val result = sortedSetOf<String>()
        config.processDescendants {
            if(it is CwtPropertyConfig) {
                val subtypeExpression = it.key.removeSurroundingOrNull("subtype[", "]")
                if(subtypeExpression != null) {
                    val resolved = ParadoxDefinitionSubtypeExpression.resolve(subtypeExpression)
                    resolved.subtypes.forEach { (_, subtype) -> result.add(subtype) }
                }
            }
            true
        }
        result
    }
}
