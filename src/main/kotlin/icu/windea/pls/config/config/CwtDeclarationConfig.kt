package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.cwt.psi.*

class CwtDeclarationConfig(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val propertyConfig: CwtPropertyConfig, //definitionName = ...
) : CwtConfig<CwtProperty> {
    val subtypesToDistinct by lazy {
        val result = sortedSetOf<String>()
        propertyConfig.processDescendants {
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
