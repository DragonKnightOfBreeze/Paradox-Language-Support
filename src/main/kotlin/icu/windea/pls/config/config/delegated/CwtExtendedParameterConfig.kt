package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.psi.util.parentOfType
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
import icu.windea.pls.ep.resolve.parameter.containingContextReference
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.lang.resolve.dynamic
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.ParadoxScriptMember

/**
 * 参数的扩展规则。
 *
 * 用于为对应的参数（parameter）提供额外的提示信息（如文档注释），以及指定规则上下文与作用域上下文。
 *
 * 说明：
 * - 规则名称可以是常量、模板表达式、ANT 表达式或正则（见 [CwtDataTypeSets.PatternAware]）。
 * - 参数是指特定类型的定义（如封装触发器，scripted trigger）或内联脚本（inline script）的参数，格式为 `$PARAM$` 或 `$PARAM|DEFAULT_VALUE$`。
 * - 作用域上下文同样是通过 `## replace_scope` 与 `## push_scope` 选项指定的。
 *
 * 路径定位：`parameters/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * parameters = {
 *     ## replace_scopes = { this = country root = country }
 *     ## context_key = some_trigger
 *     PARAM
 *     ## context_configs_type = multiple
 *     ## context_key = some_trigger
 *     PARAM = { ... }
 *     ## context_configs_type = multiple
 *     ## context_key = some_trigger
 *     PARAM = single_alias_right[trigger_clause]
 * }
 * ```
 *
 * @property name 名称。
 * @property contextKey 上下文键（如 `scripted_trigger@x`、`inline_script@x`）。
 * @property contextConfigsType 上下文规则的聚合类型（`single` 或 `multiple`）。
 * @property inherit 是否继承使用处的规则上下文与作用域上下文。
 *
 * @see CwtOptionDataHolder.replaceScopes
 * @see CwtOptionDataHolder.pushScope
 */
interface CwtExtendedParameterConfig : CwtDelegatedConfig<CwtMember, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("context_key: string")
    val contextKey: String
    @FromOption("context_configs_type: string", defaultValue = "single", allowedValues = ["single", "multiple"])
    val contextConfigsType: String
    @FromOption("inherit", defaultValue = "no")
    val inherit: Boolean

    /** 得到处理后的“上下文规则容器”。 */
    fun getContainerConfig(parameterElement: ParadoxParameterElement): CwtMemberConfig<*>

    /** 得到由其声明的上下文规则列表。 */
    fun getContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>>

    interface Resolver {
        /** 由成员规则解析为参数的扩展规则。 */
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig?
    }

    companion object : Resolver by CwtExtendedParameterConfigResolverImpl()
}

// region Implementations

private class CwtExtendedParameterConfigResolverImpl : CwtExtendedParameterConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedParameterConfig? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val contextKey = config.optionData.contextKey
        if (contextKey == null) {
            logger.warn("Skipped invalid extended parameter config (name: $name): Missing context_key option.".withLocationPrefix(config))
            return null
        }
        val contextConfigsType = config.optionData.contextConfigsType
        val inherit = config.optionData.inherit
        logger.debug { "Resolved extended parameter config (name: $name, context key: $contextKey).".withLocationPrefix(config) }
        return CwtExtendedParameterConfigImpl(config, name, contextKey, contextConfigsType, inherit)
    }
}

private class CwtExtendedParameterConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val contextKey: String,
    override val contextConfigsType: String,
    override val inherit: Boolean,
) : UserDataHolderBase(), CwtExtendedParameterConfig {
    private val _containerConfig by lazy { doGetContainerConfig() }
    private val _contextConfigs by lazy { doGetContextConfigs() }

    override fun getContainerConfig(parameterElement: ParadoxParameterElement): CwtMemberConfig<*> {
        return _containerConfig
    }

    override fun getContextConfigs(parameterElement: ParadoxParameterElement): List<CwtMemberConfig<*>> {
        if (inherit) {
            run {
                val contextReferenceElement = parameterElement.containingContextReference?.element ?: return@run
                val parentElement = contextReferenceElement.parentOfType<ParadoxScriptMember>(false) ?: return@run
                val contextConfigs = ParadoxConfigManager.getConfigContext(parentElement)?.getConfigs().orEmpty()
                PlsStates.resolvingConfigContextStack.get()?.peekLast()?.dynamic = true // NOTE 2.1.2 需要把正在解析的规则上下文标记为动态的
                return contextConfigs
            }
            return emptyList()
        }
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

    override fun toString() = "CwtExtendedParameterConfigImpl(name='$name', contextKey='$contextKey')"
}

// endregion
