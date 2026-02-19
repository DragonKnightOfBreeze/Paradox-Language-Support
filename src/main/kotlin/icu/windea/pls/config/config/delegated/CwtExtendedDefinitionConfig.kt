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
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.cwt.psi.CwtMember

/**
 * 定义的扩展规则。
 *
 * 用于为对应的定义提供额外的提示信息（如文档注释、内嵌提示），以及指定作用域上下文（如果支持）。
 *
 * 说明：
 * - 规则名称可以是常量、模板表达式、ANT 表达式或正则（见 [CwtDataTypeSets.PatternAware]）。
 * - 作用域上下文同样是通过 `## replace_scope` 与 `## push_scope` 选项指定的。
 *
 * 路径定位：`definitions/{name}`，`{name}` 匹配规则名称。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * definitions = {
 *     ### Some documentation
 *     ## hint = §RSome hint text§!
 *     ## replace_scopes = { this = country root = country }
 *     ## type = scripted_trigger
 *     x # or `x = xxx`
 * }
 * ```
 *
 * @property name 名称。
 * @property type 定义类型。
 * @property hint 提示文本（可选）。
 *
 * @see CwtOptionDataHolder.replaceScopes
 * @see CwtOptionDataHolder.pushScope
 */
interface CwtExtendedDefinitionConfig : CwtDelegatedConfig<CwtMember, CwtMemberConfig<*>> {
    @FromKey
    val name: String
    @FromOption("type: string")
    val type: String
    @FromOption("hint: string?")
    val hint: String?

    interface Resolver {
        /** 由成员规则解析为定义的扩展规则。 */
        fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig?
    }

    companion object : Resolver by CwtExtendedDefinitionConfigResolverImpl()
}

// region

private class CwtExtendedDefinitionConfigResolverImpl : CwtExtendedDefinitionConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? = doResolve(config)

    private fun doResolve(config: CwtMemberConfig<*>): CwtExtendedDefinitionConfig? {
        val name = if (config is CwtPropertyConfig) config.key else config.value
        val type = config.optionData.type
        if (type == null) {
            logger.warn("Skipped invalid extended definition config (name: $name): Missing type option.".withLocationPrefix(config))
            return null
        }
        val hint = config.optionData.hint
        logger.debug { "Resolved extended definition config (name: $name, type: $type).".withLocationPrefix(config) }
        return CwtExtendedDefinitionConfigImpl(config, name, type, hint)
    }
}

private class CwtExtendedDefinitionConfigImpl(
    override val config: CwtMemberConfig<*>,
    override val name: String,
    override val type: String,
    override val hint: String?
) : UserDataHolderBase(), CwtExtendedDefinitionConfig {
    override fun toString() = "CwtExtendedDefinitionConfigImpl(name='$name', type='$type')"
}

// endregion

