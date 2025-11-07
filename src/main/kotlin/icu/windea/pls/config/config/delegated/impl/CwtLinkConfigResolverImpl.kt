package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtLinkArgumentSeparator
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLinkType
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtConfigResolverMixin
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtLinkConfigResolverImpl : CwtLinkConfig.Resolver, CwtConfigResolverMixin {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config)

    override fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config, true)

    override fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig = doResolve(linkConfig, true)

    private fun doResolve(config: CwtPropertyConfig, isLocalisationLink: Boolean = false): CwtLinkConfig? {
        val name = config.key
        val props = config.properties ?: run {
            logger.warn("Skipped invalid link config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = props.groupBy { it.key }
        val type = propGroup.getOne("type")?.stringValue.let { CwtLinkType.resolve(it) }
        val fromData = propGroup.getOne("from_data")?.booleanValue ?: false
        val fromArgument = propGroup.getOne("from_argument")?.booleanValue ?: false
        val argumentSeparator = propGroup.getOne("argument_separator")?.stringValue.let { CwtLinkArgumentSeparator.resolve(it) }
        var prefix = propGroup.getOne("prefix")?.stringValue?.orNull()
        val dataSources = propGroup.getAll("data_source").mapNotNull { it.stringValue }.optimized()
        val inputScopes = buildSet {
            // both input_scopes and input_scope are supported
            propGroup.getAll("input_scopes").forEach { p ->
                p.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                p.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
            }
            propGroup.getAll("input_scope").forEach { p ->
                p.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                p.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
            }
        }.optimized().orNull() ?: ParadoxScopeManager.anyScopeIdSet
        val outputScope = propGroup.getOne("output_scope")?.stringValue?.let { v -> ParadoxScopeManager.getScopeId(v) }
        val forDefinitionType = propGroup.getOne("for_definition_type")?.stringValue

        // when from data or from argument, data sources must not be empty
        if (fromData && dataSources.isEmpty()) {
            logger.warn("Skipped invalid link config (name: $name): No data_source properties while from_data = yes.".withLocationPrefix(config))
            return null
        }
        if (fromArgument && dataSources.isEmpty()) {
            logger.warn("Skipped invalid link config (name: $name): No data_source properties while from_argument = yes.".withLocationPrefix(config))
            return null
        }
        // ensure prefix not ends with ':' when from argument (note that may not end with ':' when from data)
        if (fromArgument && prefix != null) prefix = prefix.removeSuffix(":")

        logger.debug { "Resolved link config (name: $name).".withLocationPrefix(config) }
        return CwtLinkConfigImpl(
            config, name, type, fromData, fromArgument, argumentSeparator,
            prefix, dataSources, inputScopes, outputScope,
            forDefinitionType, isLocalisationLink
        )
    }

    @Suppress("SameParameterValue")
    private fun doResolve(linkConfig: CwtLinkConfig, isLocalisationLink: Boolean = false): CwtLinkConfig {
        return linkConfig.apply {
            CwtLinkConfigImpl(
                config, name, type, fromData, fromArgument, argumentSeparator,
                prefix, dataSources, inputScopes, outputScope,
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
    override val type: CwtLinkType,
    override val fromData: Boolean,
    override val fromArgument: Boolean,
    override val argumentSeparator: CwtLinkArgumentSeparator,
    override val prefix: String?,
    override val dataSources: List<String>,
    override val inputScopes: Set<String>,
    override val outputScope: String?,
    override val forDefinitionType: String?,
    override val isLocalisationLink: Boolean,
) : UserDataHolderBase(), CwtLinkConfig {
    override val isStatic get() = dataSources.isEmpty()
    override val dataSourceIndex: Int get() = 0
    override val dataSourceExpressions = dataSources.map { CwtDataExpression.resolve(it, false) }
    override val dataSourceExpression = dataSourceExpressions.getOrNull(dataSourceIndex) ?: dataSourceExpressions.firstOrNull()

    override fun toString() = "CwtLinkConfigImpl(name='$name')"
}

private class CwtLinkConfigDelegate(
    val delegate: CwtLinkConfig,
    override val dataSourceIndex: Int
) : CwtLinkConfig by delegate {
    // NOTE 需要重载下面两个属性
    override val dataSourceExpression = dataSourceExpressions.getOrNull(dataSourceIndex) ?: dataSourceExpressions.firstOrNull()
    override val configExpression get() = dataSourceExpression

    override fun toString() = "CwtLinkConfigDelegate(name='$name', dataSourceIndex='$dataSourceIndex')"
}
