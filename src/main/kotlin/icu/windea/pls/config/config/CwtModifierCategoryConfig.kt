package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

/**
 * @property supportedScopes supported_scopes: string | string[]
 */
class CwtModifierCategoryConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val name: String,
    val supportedScopes: Set<String>
) : CwtConfig<CwtProperty> {
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtModifierCategoryConfig? {
            var supportedScopes: Set<String>? = null
            val props = config.properties
            if(props.isNullOrEmpty()) return null
            for(prop in props) {
                when(prop.key) {
                    "supported_scopes" -> supportedScopes = buildSet {
                        prop.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
                        prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
                    } //may be empty here (e.g. "AI Economy")
                }
            }
            supportedScopes = supportedScopes ?: ParadoxScopeHandler.anyScopeIdSet
            return CwtModifierCategoryConfig(config.pointer, config.info, name, supportedScopes)
        }
    }
}
