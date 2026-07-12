package icu.windea.pls.config.config.extended

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.util.parentOfType
import icu.windea.pls.base.context.ChronicleThreadContext
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
import icu.windea.pls.ep.resolve.parameter.containingContextReference
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.resolve.dynamic
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 参数的扩展规则。
 *
 * 用于为对应的参数（parameter）提供额外的提示信息（文档注释），以及指定规则上下文与作用域上下文。
 *
 * 说明：
 * - 规则名称可以是常量、模板表达式、ANT 表达式或正则表达式（参见 [CwtDataTypeSets.PatternAware]）。
 * - 参数是指特定类型的定义（如封装触发器，scripted trigger）或内联脚本（inline script）的参数，格式为 `$PARAM$` 或 `$PARAM|DEFAULT_VALUE$`。
 * - 作用域上下文同样是通过 `## replace_scope` 与 `## push_scope` 选项指定的。
 *
 * 路径定位：
 * - `parameters/{name}`。其中 `{name}` 匹配规则名称。
 *
 * 示例：
 *
 * ```cwt
 * parameters = {
 *     ### Some documentation
 *     ## replace_scopes = { this = country root = country }
 *     ## context_key = some_trigger
 *     PARAM
 *
 *     ### Some documentation
 *     ## context_configs_type = multiple
 *     ## context_key = some_trigger
 *     PARAM = { ... }
 *
 *     ### Some documentation
 *     ## context_configs_type = multiple
 *     ## context_key = some_trigger
 *     PARAM = single_alias_right[trigger_clause]
 * }
 * ```
 *
 * > CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * @property name 规则名称。
 * @property contextKey 上下文键（如 `scripted_trigger@x`、`inline_script@x`）。
 * @property contextConfigsType 上下文规则的聚合类型（`single`/`multiple`，默认为 `single`）。决定上下文规则是直接来自其属性值规则，还是来自其中的一组子规则。
 * @property inherit 是否继承使用处的规则上下文与作用域上下文。
 *
 * @see CwtOptionDataHolder.replaceScopes
 * @see CwtOptionDataHolder.pushScope
 */
interface CwtExtendedParameterConfig : CwtDelegatedConfig<CwtMember, CwtMemberConfig<*>>, CwtIdMatchableConfig<CwtMember> {
    @FromName
    val name: String
    @FromOptionMember("context_key: string")
    val contextKey: String
    @FromOptionMember("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: CwtContextConfigsType
    @FromOptionMember("inherit", defaultValue = "no")
    val inherit: Boolean

    /** 得到经过处理后的上下文容器规则。在后续的语义解析流程中，需要获取的通常是上下文规则，而非上下文容器规则。 */
    fun getContextContainerConfig(parameterElement: ParadoxParameterLightElement): CwtMemberConfig<*>

    /** 得到由其声明的一组上下文规则。在后续的语义解析流程中，会加入作为从扩展规则推断的上下文规则。 */
    fun getContextConfigs(parameterElement: ParadoxParameterLightElement): List<CwtMemberConfig<*>>

    companion object {
        /** 由成员规则解析为参数的扩展规则。 */
        @JvmStatic
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? {
            return CwtExtendedParameterConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtExtendedParameterConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val contextKey = config.optionData.contextKey
        if (contextKey == null) {
            logger.warn("Skipped invalid extended parameter config (name: $name): Missing context_key option.".withLocationPrefix(config))
            return null
        }
        val contextConfigsType = config.optionData.contextConfigsType.let { CwtContextConfigsType.resolve(it) }
        val inherit = config.optionData.inherit
        logger.debug { "Resolved extended parameter config (name: $name, context key: $contextKey).".withLocationPrefix(config) }
        return CwtExtendedParameterConfigImpl(config, name, contextKey, contextConfigsType, inherit)
    }
}

private class CwtExtendedParameterConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextKey: String,
    override val contextConfigsType: CwtContextConfigsType,
    override val inherit: Boolean,
) : UserDataHolderBase(), CwtExtendedParameterConfig {
    private val _contextContainerConfig by lazy { computeContextContainerConfig() }
    private val _contextConfigs by lazy { computeContextConfigs() }

    override fun getContextContainerConfig(parameterElement: ParadoxParameterLightElement): CwtMemberConfig<*> {
        return _contextContainerConfig
    }

    override fun getContextConfigs(parameterElement: ParadoxParameterLightElement): List<CwtMemberConfig<*>> {
        if (inherit) {
            run {
                val contextReferenceElement = parameterElement.containingContextReference?.element ?: return@run
                val parentElement = contextReferenceElement.parentOfType<ParadoxScriptMember>(false) ?: return@run
                val contextConfigs = ParadoxConfigManager.getContextConfigs(parentElement)
                ChronicleThreadContext.resolvingConfigContextStack.get()?.peekLast()?.dynamic = true // NOTE 2.1.2 需要把正在解析的规则上下文标记为动态的
                return contextConfigs
            }
            return emptyList()
        }
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

    override fun toString() = "CwtExtendedParameterConfigImpl(name='$name', contextKey='$contextKey')"
}

// endregion
