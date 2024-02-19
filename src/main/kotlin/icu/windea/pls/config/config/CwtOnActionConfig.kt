package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.cwt.psi.*

class CwtOnActionConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtMemberElement>,
    override val info: CwtConfigGroupInfo,
    val config: CwtMemberConfig<*>,
    val name: String,
    val eventType: String
) : CwtConfig<CwtMemberElement> {
    companion object Resolver {
        fun resolve(config: CwtMemberConfig<*>): CwtOnActionConfig? {
            val name = when(config) {
                is CwtPropertyConfig -> config.key
                is CwtValueConfig -> config.value
            }
            val eventType = config.findOption("event_type")?.stringValue ?: return null
            return CwtOnActionConfig(config.pointer, config.info, config, name, eventType)
        }
    }
}
