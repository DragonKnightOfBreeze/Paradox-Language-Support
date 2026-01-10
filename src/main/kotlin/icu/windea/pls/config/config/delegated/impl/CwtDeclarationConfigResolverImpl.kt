package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.processDescendants
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.optimized
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.lang.isIdentifier
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionSubtypeExpression

class CwtDeclarationConfigResolverImpl : CwtDeclarationConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig, name: String?): CwtDeclarationConfig? = doResolve(config, name)

    private fun doResolve(config: CwtPropertyConfig, inputName: String?): CwtDeclarationConfig? {
        val name = inputName ?: config.key.takeIf { it.isIdentifier() } ?: return null
        logger.debug { "Resolved declaration config (name: $name).".withLocationPrefix(config) }
        return CwtDeclarationConfigImpl(config, name)
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
