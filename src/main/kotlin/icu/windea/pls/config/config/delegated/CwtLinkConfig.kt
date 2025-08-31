@file:Suppress("PackageDirectoryMismatch")

package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromKey
import icu.windea.pls.config.config.delegated.FromProperty
import icu.windea.pls.config.config.delegated.impl.CwtLinkConfigResolverImpl
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtLinkConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("")
    val name: String
    @FromProperty("type: string?")
    val type: String?
    @FromProperty("from_data: boolean", defaultValue = "false")
    val fromData: Boolean
    @FromProperty("from_argument: boolean", defaultValue = "false")
    val fromArgument: Boolean
    @FromProperty("prefix: string?")
    val prefix: String?
    @FromProperty("data_source: string?")
    val dataSource: String?
    @FromProperty("input_scopes: string[]")
    val inputScopes: Set<String>
    @FromProperty("output_scope: string?")
    val outputScope: String?
    @FromProperty("for_definition_type: string?")
    val forDefinitionType: String?

    val forLocalisation: Boolean
    val dataSourceExpression: CwtDataExpression?

    override val configExpression: CwtDataExpression? get() = dataSourceExpression

    // type = null -> default to "scope"
    // output_scope = null -> transfer scope based on data source
    // e.g., for event_target, output_scope should be null

    fun forScope() = type != "value" /* type.isNullOrEmpty() || type == "both" || type == "scope" */
    fun forValue() = type == "both" || type == "value"

   interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtLinkConfig?
        fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig?
        fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig
    }

    companion object : Resolver by CwtLinkConfigResolverImpl()
}
