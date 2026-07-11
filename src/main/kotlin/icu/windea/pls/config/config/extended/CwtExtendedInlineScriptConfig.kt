package icu.windea.pls.config.config.extended

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.annotations.FromOptionMember
import icu.windea.pls.config.config.CwtContextConfigsType
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.manipulation.CwtConfigManipulationService
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.util.values.singletonListOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.cwt.psi.CwtMember

/**
 * 内联脚本（inline script）的扩展规则。
 *
 * 用于为对应的内联脚本（inline script）提供额外的提示信息（文档注释），以及指定规则上下文与作用域上下文。
 *
 * 说明：
 * - 规则名称可以是常量、模板表达式、ANT 表达式或正则表达式（参见 [CwtDataTypeSets.PatternAware]）。
 * - 内联脚本文件是指扩展名为 `.txt`，位于 `common/inline_scripts` 目录下的脚本文件。
 *   这些脚本可以在其他脚本文件中（几乎任意位置）被调用。
 * - 名为 `x/y` 的规则会匹配路径为 `common/inline_scripts/x/y.txt` 的内联脚本文件。
 * - 作用域上下文同样是通过 `## replace_scope` 与 `## push_scope` 选项指定的。
 *
 * 路径定位：
 * - `inline_scripts/{name}`。其中 `{name}` 匹配规则名称。
 *
 * 示例：
 *
 * ```cwt
 * inline_scripts = {
 *     ### Some documentation
 *     ## replace_scopes = { this = country root = country }
 *     triggers/some_trigger_snippet
 *
 *     ### Some documentation
 *     ## context_configs_type = multiple
 *     triggers/some_trigger_snippet = { ... }
 *
 *     ### Some documentation
 *     ## context_configs_type = multiple
 *     triggers/some_trigger_snippet = single_alias_right[trigger_clause]
 * }
 * ```
 *
 * > CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * @property name 规则名称。
 * @property contextConfigsType 上下文规则的聚合类型（`single`/`multiple`，默认为 `single`）。决定上下文规则是直接来自其属性值规则，还是来自其中的一组子规则。
 *
 * @see CwtOptionDataHolder.replaceScopes
 * @see CwtOptionDataHolder.pushScope
 */
interface CwtExtendedInlineScriptConfig : CwtDelegatedConfig<CwtMember, CwtMemberConfig<*>>, CwtIdMatchableConfig<CwtMember> {
    @FromName
    val name: String
    @FromOptionMember("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: CwtContextConfigsType

    /** 得到经过处理后的上下文容器规则。在后续的语义解析流程中，需要获取的通常是上下文规则，而非上下文容器规则。 */
    fun getContextContainerConfig(): CwtMemberConfig<*>

    /** 得到由其声明的一组上下文规则。在后续的语义解析流程中，会加入作为从扩展规则推断的上下文规则。 */
    fun getContextConfigs(): List<CwtMemberConfig<*>>

    companion object {
        /** 由成员规则解析为内联脚本的扩展规则。 */
        @JvmStatic
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig {
            return CwtExtendedInlineScriptConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtExtendedInlineScriptConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtMemberConfig<*>): CwtExtendedInlineScriptConfig {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val contextConfigsType = config.optionData.contextConfigsType
        logger.debug { "Resolved extended inline script config (name: $name).".withLocationPrefix(config) }
        return CwtExtendedInlineScriptConfigImpl(config, name, contextConfigsType)
    }
}

private class CwtExtendedInlineScriptConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextConfigsType: CwtContextConfigsType,
) : UserDataHolderBase(), CwtExtendedInlineScriptConfig {
    private val _contextContainerConfig by lazy { computeContextContainerConfig() }
    private val _contextConfigs by lazy { computeContextConfigs() }

    override fun getContextContainerConfig(): CwtMemberConfig<*> {
        return _contextContainerConfig
    }

    override fun getContextConfigs(): List<CwtMemberConfig<*>> {
        return _contextConfigs
    }

    private fun computeContextContainerConfig(): CwtMemberConfig<*> {
        if (config !is CwtPropertyConfig) return config
        return CwtConfigManipulationService.inlineForConfig(config)
    }

    private fun computeContextConfigs(): List<CwtMemberConfig<*>> {
        val contextContainerConfig = _contextContainerConfig
        if (contextContainerConfig !is CwtPropertyConfig) return emptyList()
        val sourceConfigs = when (contextConfigsType) {
            CwtContextConfigsType.Single -> contextContainerConfig.valueConfig.to.singletonListOrEmpty()
            CwtContextConfigsType.Multiple -> contextContainerConfig.configs.orEmpty()
        }
        if (sourceConfigs.isEmpty()) return emptyList()
        val contextConfig = CwtConfigManipulationService.inlineForContextConfig(config, sourceConfigs, config.configGroup)
        return listOf(contextConfig)
    }

    override fun toString() = "CwtExtendedInlineScriptConfigImpl(name='$name')"
}

// endregion
