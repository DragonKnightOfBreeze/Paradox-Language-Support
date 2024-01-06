package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtParameterConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtMemberElement>,
    override val info: CwtConfigGroupInfo,
    val config: CwtMemberConfig<*>,
    val name: String,
    val contextKey: String
) : CwtConfig<CwtMemberElement> {
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtParameterConfig? {
            val name = when(config) {
                is CwtPropertyConfig -> config.key
                is CwtValueConfig -> config.value
            }
            val contextKey = config.findOption("context_key")?.stringValue ?: return null
            return CwtParameterConfig(config.pointer, config.info, config, name, contextKey)
        }
    }
}

