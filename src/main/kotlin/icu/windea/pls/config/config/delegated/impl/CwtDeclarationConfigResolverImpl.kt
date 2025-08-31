package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.processDescendants
import icu.windea.pls.config.util.CwtConfigManipulator
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.lang.expression.ParadoxDefinitionSubtypeExpression
import icu.windea.pls.lang.isIdentifier

class CwtDeclarationConfigResolverImpl : CwtDeclarationConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig, name: String?): CwtDeclarationConfig? = doResolve(name, config)

    private fun doResolve(name: String?, config: CwtPropertyConfig): CwtDeclarationConfigImpl? {
        val name0 = name ?: config.key.takeIf { it.isIdentifier() } ?: return null
        return CwtDeclarationConfigImpl(config, name0)
    }
}

private class CwtDeclarationConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
) : UserDataHolderBase(), CwtDeclarationConfig {
    override val configForDeclaration: CwtPropertyConfig by lazy {
        CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }

    override val subtypesUsedInDeclaration: Set<String> by lazy {
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

    override fun toString() = "CwtDeclarationConfigImpl(name='$name')"
}
