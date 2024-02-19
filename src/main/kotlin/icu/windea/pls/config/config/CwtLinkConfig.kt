package icu.windea.pls.config.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

/**
 * @property desc desc: string
 * @property fromData from_data: string
 * @property type type: string
 * @property dataSource data_source: string (expression)
 * @property prefix prefix: string
 * @property forDefinitionType for_definition_type: string
 * @property inputScopes input_scopes | input_scopes: string[]
 * @property outputScope output_scope: string?
 */
class CwtLinkConfig private constructor(
    override val pointer: SmartPsiElementPointer<out CwtProperty>,
    override val info: CwtConfigGroupInfo,
    val config: CwtPropertyConfig,
    val name: String,
    val desc: String? = null,
    val fromData: Boolean = false,
    val type: String? = null,
    val dataSource: CwtValueExpression?,
    val prefix: String?,
    val forDefinitionType: String?,
    val inputScopes: Set<String>,
    val outputScope: String?
) : CwtConfig<CwtProperty> {
    override val expression get() = dataSource
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig, name: String): CwtLinkConfig? {
            var desc: String? = null
            var fromData = false
            var type: String? = null
            var dataSource: CwtValueExpression? = null
            var prefix: String? = null
            var inputScopes: Set<String>? = null
            var outputScope: String? = null
            var forDefinitionType: String? = null
            val props = config.properties ?: return null
            for(prop in props) {
                when(prop.key) {
                    "desc" -> desc = prop.stringValue?.trim() //去除首尾空白
                    "from_data" -> fromData = prop.booleanValue ?: false
                    "type" -> type = prop.stringValue
                    "data_source" -> dataSource = prop.valueExpression
                    "prefix" -> prefix = prop.stringValue
                    "for_definition_type" -> forDefinitionType = prop.stringValue
                    "input_scopes" -> inputScopes = buildSet {
                        prop.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
                        prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
                    }
                    "output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) }
                }
            }
            inputScopes = inputScopes.orNull() ?: ParadoxScopeHandler.anyScopeIdSet
            return CwtLinkConfig(config.pointer, config.info, config, name, desc, fromData, type, dataSource, prefix, forDefinitionType, inputScopes, outputScope)
        }
    }
}

