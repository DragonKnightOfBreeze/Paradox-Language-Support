package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.cwt.psi.*

/**
 * @property aliases aliases: string[]
 */
class CwtScopeConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val aliases: Set<@CaseInsensitive String>
) : CwtConfig<CwtProperty> {
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtScopeConfig? {
            var aliases: Set<String>? = null
            val props = config.properties
            if(props.isNullOrEmpty()) return null
            for(prop in props) {
                if(prop.key == "aliases") aliases = prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
            }
            if(aliases == null) aliases = emptySet()
            return CwtScopeConfig(config.pointer, config.info, name, aliases)
        }
    }
}
