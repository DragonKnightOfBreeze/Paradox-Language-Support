@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.psi.*
import icons.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*

class CwtLocalisationPredefinedParameterConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val id: String,
    val mockValue: String,
    val description: String
) : CwtConfig<CwtProperty> {
    val icon: Icon get() = PlsIcons.Nodes.PredefinedParameter
    
    override fun equals(other: Any?): Boolean {
        return this === other || other is CwtLocalisationPredefinedParameterConfig && id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return description.ifEmpty { id }
    }
    
    companion object {
        fun resolve(config: CwtPropertyConfig): CwtLocalisationPredefinedParameterConfig {
            val id = config.key
            val mockValue = config.value
            val description = config.documentation.orEmpty()
            return CwtLocalisationPredefinedParameterConfig(config.pointer, config.info, id, mockValue, description)
        }
    }
}