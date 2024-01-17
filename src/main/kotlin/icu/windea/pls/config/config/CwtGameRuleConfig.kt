package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*

class CwtGameRuleConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtMemberElement>,
    override val info: CwtConfigGroupInfo,
    val config: CwtMemberConfig<*>,
    val name: String
) : CwtConfig<CwtMemberElement> {
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtGameRuleConfig {
            val name = when(config) {
                is CwtPropertyConfig -> config.key
                is CwtValueConfig -> config.value
            }
            return CwtGameRuleConfig(config.pointer, config.info, config, name)
        }
    }
}
