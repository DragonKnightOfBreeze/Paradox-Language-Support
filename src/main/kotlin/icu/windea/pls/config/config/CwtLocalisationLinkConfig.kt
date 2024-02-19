package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

/**
 * @property desc desc: string
 * @property inputScopes input_scopes | input_scopes: string[]
 * @property outputScope output_scope: string?
 */
class CwtLocalisationLinkConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val config: CwtPropertyConfig,
    val name: String,
    val desc: String? = null,
    val inputScopes: Set<String>,
    val outputScope: String?
) : CwtConfig<CwtProperty> {
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtLocalisationLinkConfig? {
            var desc: String? = null
            var inputScopes: Set<String>? = null
            var outputScope: String? = null
            val props = config.properties ?: return null
            for(prop in props) {
                when(prop.key) {
                    "desc" -> desc = prop.stringValue?.trim() //排除占位码 & 去除首尾空白
                    "input_scopes" -> inputScopes = buildSet {
                        prop.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
                        prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
                    }
                    "output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) }
                }
            }
            inputScopes = inputScopes.orNull() ?: ParadoxScopeHandler.anyScopeIdSet
            return CwtLocalisationLinkConfig(config.pointer, config.info, config, name, desc, inputScopes, outputScope)
        }
        
        fun resolveFromLink(config: CwtLinkConfig): CwtLocalisationLinkConfig? {
            return CwtLocalisationLinkConfig(config.pointer, config.info, config.config, config.name, config.desc, config.inputScopes, config.outputScope)
        }
    }
}
