package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.util.CwtConfigResolverManager
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 别名规则。
 *
 * 别名规则是一种可以按一对多的形式，在多个位置复用（作为属性）的规则。
 * 别名规则可用来简化规则文件，提升可读性和复用性。
 * 另外，包括触发器（trigger）、效果（effect）在内的多种概念对应的规则，都是以别名规则的形式提供的。
 *
 * 路径定位：`alias[{name}:{subName}]`，`{name}` 匹配名称，`{subName}`匹配子名（受限支持的数据表达式）。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * # declaration
 * alias[effect:some_effect] = { ... }
 * alias[effect:some_other_effect] = { ... }
 *
 * # usage
 * scripted_effect = {
 *     alias_name[effect] = alias_match_left[effect]
 * }
 * ```
 *
 * @property name 名称。
 * @property subName 子名（受限支持的数据表达式）。
 * @property supportedScopes 允许的作用域（类型）的集合。
 * @property outputScope 输出的作用域。
 * @property subNameExpression 子名对应的数据表达式。
 * @property configExpression 绑定到该规则的数据表达式（等同于 [subNameExpression]）。
 *
 * @see icu.windea.pls.config.util.manipulators.CwtConfigManipulator.inlineAlias
 */
interface CwtAliasConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey("alias[$:*]")
    val name: String
    @FromKey("alias[*:$]")
    val subName: String
    @FromOption("scope/scopes: string | string[]")
    val supportedScopes: Set<String>
    @FromOption("push_scope: string?")
    val outputScope: String?

    val subNameExpression: CwtDataExpression
    override val configExpression: CwtDataExpression

    interface Resolver {
        /** 由属性规则解析为别名规则。 */
        fun resolve(config: CwtPropertyConfig): CwtAliasConfig?
        /** 进行解析后的后续处理（从数据表达式收集信息）。 */
        fun postProcess(config: CwtAliasConfig)
    }

    companion object : Resolver by CwtAliasConfigResolverImpl()
}

// region Implementations

private class CwtAliasConfigResolverImpl : CwtAliasConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtAliasConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtAliasConfig? {
        val key = config.key
        val tokens = key.removeSurroundingOrNull("alias[", "]")?.orNull()
            ?.split(':', limit = 2)?.takeIf { it.size == 2 }
            ?: return null
        val name = tokens[0].optimized()
        val subName = tokens[1].optimized()
        logger.debug { "Resolved alias config (name: $name, subName: $subName).".withLocationPrefix(config) }
        return CwtAliasConfigImpl(config, name, subName)
    }

    override fun postProcess(config: CwtAliasConfig) {
        // collect information
        CwtConfigResolverManager.collectFromConfigExpression(config, config.configExpression)
    }
}

private class CwtAliasConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val subName: String
) : UserDataHolderBase(), CwtAliasConfig {
    override val supportedScopes get() = config.optionData.supportedScopes
    override val outputScope get() = config.optionData.pushScope
    override val subNameExpression = CwtDataExpression.resolve(subName, true) // cached
    override val configExpression: CwtDataExpression get() = subNameExpression

    override fun toString() = "CwtAliasConfigImpl(name='$name', subName='$subName')"
}

// endregion
