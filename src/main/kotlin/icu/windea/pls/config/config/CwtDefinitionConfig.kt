package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtDefinitionConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtMemberElement>,
    override val info: CwtConfigGroupInfo,
    val config: CwtMemberConfig<*>,
    val name: String,
    val type: String
) : CwtConfig<CwtMemberElement> {
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtDefinitionConfig? {
            val name = when(config) {
                is CwtPropertyConfig -> config.key
                is CwtValueConfig -> config.value
            }
            val type = config.findOption("type")?.stringValue ?: return null
            return CwtDefinitionConfig(config.pointer, config.info, config, name, type)
        }
    }
}
