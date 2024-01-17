package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.cwt.psi.*

class CwtDeclarationConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val config: CwtPropertyConfig,
    val name: String,
) : CwtConfig<CwtProperty> {
    val subtypesToDistinct by lazy {
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
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtDeclarationConfig {
            return CwtDeclarationConfig(config.pointer, config.info, config, name)
        }
    }
}
