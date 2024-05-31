package icu.windea.pls.config.config

import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*

/**
 * @property name string
 * @property desc (property) desc: string?
 * @property fromData (property) from_data: boolean
 * @property type (property) type: string?
 * @property dataSource (property) data_source: expression?
 * @property prefix (property) prefix: string?
 * @property forDefinitionType (property) for_definition_type: string?
 * @property inputScopes (property) input_scopes: string[]
 * @property outputScope (property) output_scope: string?
 */
interface CwtLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val desc: String?
    val fromData: Boolean
    val type: String?
    val dataSource: CwtDataExpression?
    val prefix: String?
    val forDefinitionType: String?
    val inputScopes: Set<String>
    val outputScope: String?
    
    //output_scope = null -> transfer scope based on data source
    //e.g., for 'event_target', output_scope should be null
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtLinkConfigImpl? {
    val name = config.key
    var desc: String? = null
    var fromData = false
    var type: String? = null
    var dataSource: CwtDataExpression? = null
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
    return CwtLinkConfigImpl(config, name, desc, fromData, type, dataSource, prefix, forDefinitionType, inputScopes, outputScope)
}

private class CwtLinkConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val desc: String? = null,
    override val fromData: Boolean = false,
    override val type: String? = null,
    override val dataSource: CwtDataExpression?,
    override val prefix: String?,
    override val forDefinitionType: String?,
    override val inputScopes: Set<String>,
    override val outputScope: String?
) : CwtLinkConfig {
    override val expression get() = dataSource
}

