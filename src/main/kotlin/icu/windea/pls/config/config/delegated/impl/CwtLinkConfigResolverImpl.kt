package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.core.collections.orNull
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtLinkConfigResolverImpl : CwtLinkConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config)

    // 为本地化上下文解析的便捷入口：仅切换 forLocalisation 标志
    override fun resolveForLocalisation(config: CwtPropertyConfig): CwtLinkConfig? = doResolve(config, true)

    // 为本地化上下文解析的便捷入口：从已解析的实例派生，切换 forLocalisation 标志
    override fun resolveForLocalisation(linkConfig: CwtLinkConfig): CwtLinkConfig = doResolve(linkConfig, true)

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
                "type" -> type = prop.stringValue // 链接目标类型（如定义类型、文件引用等）
                "from_data" -> fromData = prop.booleanValue ?: false // 从数据表达式解析
                "from_argument" -> fromArgument = prop.booleanValue ?: false // 从参数（如命令参数）解析
                "prefix" -> prefix = prop.stringValue // 链接文本前缀，规范化为以 ':' 结尾
                "data_source" -> dataSource = prop.value // 原始数据表达式字符串，稍后延迟解析
                "input_scopes", "input_scope" -> inputScopes = buildSet {
                    // 映射为内部作用域ID，兼容单值与数组
                    prop.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                    prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
                }
                "output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeManager.getScopeId(v) }
                "for_definition_type" -> forDefinitionType = prop.stringValue // 仅在该定义类型下生效
            }
        }
        // 合法性校验：当来源于数据/参数时必须提供 data_source
        if (fromData && dataSource == null) return null // invalid
        if (fromArgument && dataSource == null) return null // invalid
        if (prefix == "") prefix = null
        if (prefix != null && !prefix.endsWith(':')) prefix += ":" // ensure prefix ends with ':'
        // 若未声明输入作用域，则默认为任意作用域
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
    // 延迟解析并缓存数据表达式；体量不大，可安全缓存以复用
    override val dataSourceExpression: CwtDataExpression? = dataSource?.let { CwtDataExpression.resolve(it, false) }

    override fun toString(): String {
        return "CwtLinkConfigImpl(name='$name')"
    }
}
