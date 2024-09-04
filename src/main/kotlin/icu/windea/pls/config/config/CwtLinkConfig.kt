package icu.windea.pls.config.config

import com.intellij.openapi.util.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.util.*

/**
 * @property name string
 * @property fromData (property) from_data: boolean
 * @property type (property) type: string?
 * @property prefix (property) prefix: string?
 * @property dataSource (property) data_source: expression?
 * @property forDefinitionType (property) for_definition_type: string?
 * @property inputScopes (property) input_scopes: string[]
 * @property outputScope (property) output_scope: string?
 */
interface CwtLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val fromData: Boolean
    val type: String?
    val prefix: String?
    val dataSource: String?
    val inputScopes: Set<String>
    val outputScope: String?
    val forDefinitionType: String?
    
    val dataSourceExpression: CwtDataExpression? get() = dataSource?.let { CwtDataExpression.resolve(it, false) }
    override val expression: CwtDataExpression? get() = dataSourceExpression
    
    //output_scope = null -> transfer scope based on data source
    //e.g., for 'event_target', output_scope should be null
    
    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config)
    }
}

fun CwtLinkConfig.forScope() = this.type == "scope" || this.type == "both"

fun CwtLinkConfig.forValue() = this.type == "scope" || this.type == "both"

fun CwtLinkConfig.withPrefix() = this.prefix.isNotNullOrEmpty()

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig): CwtLinkConfigImpl? {
    val name = config.key
    var fromData = false
    var type: String? = null
    var prefix: String? = null
    var dataSource: String? = null
    var inputScopes: Set<String>? = null
    var outputScope: String? = null
    var forDefinitionType: String? = null
    val props = config.properties ?: return null
    for(prop in props) {
        when(prop.key) {
            "from_data" -> fromData = prop.booleanValue ?: false
            "type" -> type = prop.stringValue
            "prefix" -> prefix = prop.stringValue
            "data_source" -> dataSource = prop.value
            "input_scopes" -> inputScopes = buildSet {
                prop.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
            }
            "output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeManager.getScopeId(v) }
            "for_definition_type" -> forDefinitionType = prop.stringValue
        }
    }
    if(fromData && dataSource == null) return null //invalid
    inputScopes = inputScopes.orNull() ?: ParadoxScopeManager.anyScopeIdSet
    return CwtLinkConfigImpl(config, name, fromData, type, prefix, dataSource, inputScopes, outputScope, forDefinitionType)
}

private class CwtLinkConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val fromData: Boolean = false,
    override val type: String? = null,
    override val prefix: String?,
    override val dataSource: String?,
    override val inputScopes: Set<String>,
    override val outputScope: String?,
    override val forDefinitionType: String?
) : UserDataHolderBase(), CwtLinkConfig
