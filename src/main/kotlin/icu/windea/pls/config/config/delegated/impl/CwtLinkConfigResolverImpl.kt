package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtLinkConfigResolverImpl : CwtLinkConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config)

    override fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config, true)

    override fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig = doResolve(linkConfig, true)

    private fun doResolve(config: CwtPropertyConfig, isLocalisationLink: Boolean = false): CwtLinkConfig? {
        val name = config.key
        var type: String? = null
        var fromData = false
        var fromArgument = false
        var prefix: String? = null
        val dataSources = mutableListOf<String>()
        var inputScopes: Set<String>? = null
        var outputScope: String? = null
        var forDefinitionType: String? = null
        val props = config.properties ?: return null
        for (prop in props) {
            when (prop.key) {
                "type" -> type = prop.stringValue
                "from_data" -> fromData = prop.booleanValue ?: false
                "from_argument" -> fromArgument = prop.booleanValue ?: false
                "prefix" -> prefix = prop.stringValue?.orNull()
                "data_source" -> prop.stringValue?.orNull()?.let { dataSources += it }
                "input_scopes", "input_scope" -> inputScopes = buildSet {
                    prop.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                    prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
                }
                "output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeManager.getScopeId(v) }
                "for_definition_type" -> forDefinitionType = prop.stringValue
            }
        }

        // when from data or from argument, data sources must not be empty
        if (fromData && dataSources.isEmpty()) return null
        if (fromArgument && dataSources.isEmpty()) return null
        // ensure prefix not ends with ':' when from argument (note that may not end with ':' when from data)
        if (fromArgument && prefix != null) prefix = prefix.removeSuffix(":")
        // optimize input scopes
        inputScopes = inputScopes.orNull() ?: ParadoxScopeManager.anyScopeIdSet

        return CwtLinkConfigImpl(
            config, name, type, fromData, fromArgument, prefix, dataSources.optimized(), inputScopes, outputScope,
            forDefinitionType, isLocalisationLink
        )
    }

    @Suppress("SameParameterValue")
    private fun doResolve(linkConfig: CwtLinkConfig, isLocalisationLink: Boolean = false): CwtLinkConfig {
        return linkConfig.apply {
            CwtLinkConfigImpl(
                config, name, type, fromData, fromArgument, prefix, dataSources, inputScopes, outputScope,
                forDefinitionType, isLocalisationLink
            )
        }
    }

    override fun delegatedWith(linkConfig: CwtLinkConfig, dataSourceIndex: Int): CwtLinkConfig? {
        if (dataSourceIndex < 0 || dataSourceIndex > linkConfig.dataSources.lastIndex) return null
        if (dataSourceIndex == linkConfig.dataSourceIndex) return linkConfig
        if (linkConfig.dataSources.size <= 1) return linkConfig
        return CwtLinkConfigDelegate(linkConfig, dataSourceIndex)
    }
}

private class CwtLinkConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val type: String?,
    override val fromData: Boolean,
    override val fromArgument: Boolean,
    override val prefix: String?,
    override val dataSources: List<String>,
    override val inputScopes: Set<String>,
    override val outputScope: String?,
    override val forDefinitionType: String?,
    override val isLocalisationLink: Boolean
) : UserDataHolderBase(), CwtLinkConfig {
    override val isStatic get() = dataSources.isEmpty()
    override val dataSourceIndex: Int get() = 0
    override val dataSourceExpressions = dataSources.map { CwtDataExpression.resolve(it, false) }
    override val dataSourceExpression = dataSourceExpressions.getOrNull(dataSourceIndex) ?: dataSourceExpressions.firstOrNull()

    override fun toString(): String {
        return "CwtLinkConfigImpl(name='$name')"
    }
}

private class CwtLinkConfigDelegate(
    val delegate: CwtLinkConfig,
    override val dataSourceIndex: Int
) : CwtLinkConfig by delegate {
    // NOTE 需要重载下面两个属性
    override val dataSourceExpression = dataSourceExpressions.getOrNull(dataSourceIndex) ?: dataSourceExpressions.firstOrNull()
    override val configExpression get() = dataSourceExpression
}
