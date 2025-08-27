@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtConfig.Property
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.ParadoxScopeManager

interface CwtLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    @Property("type: string?")
    val type: String?
    @Property("from_data: boolean", defaultValue = "false")
    val fromData: Boolean
    @Property("from_argument: boolean", defaultValue = "false")
    val fromArgument: Boolean
    @Property("prefix: string?")
    val prefix: String?
    @Property("data_source: string?")
    val dataSource: String?
    @Property("input_scopes: string[]")
    val inputScopes: Set<String>
    @Property("output_scope: string?")
    val outputScope: String?
    @Property("for_definition_type: string?")
    val forDefinitionType: String?
    val forLocalisation: Boolean

    val dataSourceExpression: CwtDataExpression?
    override val configExpression: CwtDataExpression? get() = dataSourceExpression

    //type = null -> default to "scope"

    //output_scope = null -> transfer scope based on data source
    //e.g., for event_target, output_scope should be null

    companion object Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config)

        fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config, true)

        fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig = doResolve(linkConfig, true)
    }
}

fun CwtLinkConfig.forScope() = type != "value" /* type.isNullOrEmpty() || type == "both" || type == "scope" */

fun CwtLinkConfig.forValue() = type == "both" || type == "value"

//Implementations (interned if necessary)

private fun doResolve(config: CwtPropertyConfig, forLocalisation: Boolean = false): CwtLinkConfig? {
    val name = config.key
    var type: String? = null
    var fromData = false
    var fromArgument = false
    var prefix: String? = null
    var dataSource: String? = null
    var inputScopes: Set<String>? = null
    var outputScope: String? = null
    var forDefinitionType: String? = null
    val props = config.properties ?: return null
    for (prop in props) {
        when (prop.key) {
            "type" -> type = prop.stringValue
            "from_data" -> fromData = prop.booleanValue ?: false
            "from_argument" -> fromArgument = prop.booleanValue ?: false
            "prefix" -> prefix = prop.stringValue
            "data_source" -> dataSource = prop.value
            "input_scopes", "input_scope" -> inputScopes = buildSet {
                prop.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
            }
            "output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeManager.getScopeId(v) }
            "for_definition_type" -> forDefinitionType = prop.stringValue
        }
    }
    if (fromData && dataSource == null) return null //invalid
    if (fromArgument && dataSource == null) return null //invalid
    if (prefix == "") prefix = null
    if (prefix != null && !prefix.endsWith(':')) prefix += ":" //ensure prefix ends with ':'
    inputScopes = inputScopes.orNull() ?: ParadoxScopeManager.anyScopeIdSet
    return CwtLinkConfigImpl(
        config, name, type, fromData, fromArgument, prefix, dataSource, inputScopes, outputScope,
        forDefinitionType, forLocalisation
    )
}

@Suppress("SameParameterValue")
private fun doResolve(linkConfig: CwtLinkConfig, forLocalisation: Boolean = false): CwtLinkConfig {
    return linkConfig.apply {
        CwtLinkConfigImpl(
            config, name, type, fromData, fromArgument, prefix, dataSource, inputScopes, outputScope,
            forDefinitionType, forLocalisation
        )
    }
}

private class CwtLinkConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val type: String?,
    override val fromData: Boolean,
    override val fromArgument: Boolean,
    override val prefix: String?,
    override val dataSource: String?,
    override val inputScopes: Set<String>,
    override val outputScope: String?,
    override val forDefinitionType: String?,
    override val forLocalisation: Boolean
) : UserDataHolderBase(), CwtLinkConfig {
    //not much memory will be used, so cached
    override val dataSourceExpression: CwtDataExpression? = dataSource?.let { CwtDataExpression.resolve(it, false) }

    override fun toString(): String {
        return "CwtLinkConfigImpl(name='$name')"
    }
}
