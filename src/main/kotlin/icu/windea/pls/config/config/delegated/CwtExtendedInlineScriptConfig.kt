package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.manipulators.CwtConfigManipulator
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.cwt.psi.CwtMember

/**
 * 内联脚本的扩展规则。
 *
 * 用于为对应的内联脚本（inline script）指定规则上下文与作用域上下文。
 *
 * 说明：
 * - 规则名称可以是常量、模板表达式、ANT 表达式或正则（见 [CwtDataTypeSets.PatternAware]）。
 * - 内联脚本文件是指扩展名为 `.txt`，位于 `common/inline_scripts` 目录下的脚本文件。
 *   这些脚本可以在其他脚本文件中（几乎任意位置）被调用。
 * - 名为 `x/y` 的规则会匹配路径为 `common/inline_scripts/x/y.txt` 的内联脚本文件。
 * - 作用域上下文同样是通过 `## replace_scope` 与 `## push_scope` 选项指定的。
 *
 * 路径定位：`inline_scripts/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * inline_scripts = {
 *     ## replace_scopes = { this = country root = country }
 *     triggers/some_trigger_snippet
 *     ## context_configs_type = multiple
 *     triggers/some_trigger_snippet = { ... }
 *     ## context_configs_type = multiple
 *     triggers/some_trigger_snippet = single_alias_right[trigger_clause]
 * }
 * ```
 *
 * @property name 名称。
 * @property contextConfigsType 上下文规则的聚合类型（`single` 或 `multiple`）。
 *
 * @see CwtOptionDataHolder.replaceScopes
 * @see CwtOptionDataHolder.pushScope
 */
interface CwtExtendedInlineScriptConfig : CwtDelegatedConfig<CwtMember, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: String // TODO 2.0.4+ 需要详细说明这个属性的用处与行为

    /** 得到处理后的“上下文规则容器”。 */
    fun getContainerConfig(): CwtMemberConfig<*>

    /** 得到由其声明的上下文规则列表。 */
    fun getContextConfigs(): List<CwtMemberConfig<*>>

    interface Resolver {
        /** 由成员规则解析为内联脚本的扩展规则。 */
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig
    }

    companion object : Resolver by CwtExtendedInlineScriptConfigResolverImpl()
}

// region Implementations

private class CwtExtendedInlineScriptConfigResolverImpl : CwtExtendedInlineScriptConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val contextConfigsType = config.optionData.contextConfigsType
        logger.debug { "Resolved extended inline script config (name: $name).".withLocationPrefix(config) }
        return CwtExtendedInlineScriptConfigImpl(config, name, contextConfigsType)
    }
}

private class CwtExtendedInlineScriptConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextConfigsType: String,
) : UserDataHolderBase(), CwtExtendedInlineScriptConfig {
    private val _containerConfig by lazy { doGetContainerConfig() }
    private val _contextConfigs by lazy { doGetContextConfigs() }

    override fun getContainerConfig(): CwtMemberConfig<*> {
        return _containerConfig
    }

    override fun getContextConfigs(): List<CwtMemberConfig<*>> {
        return _contextConfigs
    }

    private fun doGetContainerConfig(): CwtMemberConfig<*> {
        if (config !is CwtPropertyConfig) return config
        // https://github.com/DragonKnightOfBreeze/Paradox-Language-Support/issues/#76
        return CwtConfigManipulator.inlineSingleAlias(config) ?: config
    }

    private fun doGetContextConfigs(): List<CwtMemberConfig<*>> {
        val containerConfig = _containerConfig
        if (containerConfig !is CwtPropertyConfig) return emptyList()
        val r = when (contextConfigsType) {
            "multiple" -> containerConfig.configs.orEmpty()
            // "single" -> containerConfig.valueConfig.singleton.listOrEmpty()
            else -> containerConfig.valueConfig.to.singletonListOrEmpty()
        }
        if (r.isEmpty()) return emptyList()
        val contextConfig = CwtConfigManipulator.inlineWithConfigs(config, r, config.configGroup)
        return listOf(contextConfig)
    }

    override fun toString() = "CwtExtendedInlineScriptConfigImpl(name='$name')"
}


// endregion
