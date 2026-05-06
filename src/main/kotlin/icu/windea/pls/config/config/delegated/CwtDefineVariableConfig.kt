package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix

/**
 * 定值变量规则。
 *
 * 用于描述脚本文件中的定值变量，提供快速文档文本和规则上下文。
 * 它们位于 `common/defines` 目录中的扩展名为 `.txt` 的脚本文件中。
 *
 * 路径定位：`defines/ * /{name}`，`{name}` 匹配规则名称（变量名）。
 *
 * CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * 备注：插件会强制忽略名为 `define` 的类型规则和声明规则。
 *
 * 示例：
 * ```cwt
 * defines = {
 *     # define namespace config `NAMESPACE`
 *     NAMESPACE = {
 *         # define variable config `STRING`
 *         STRING = scalar
 *         # define variable config `STRING_SET`
 *         STRING_SET = {
 *             ## cardinality = 0..inf
 *             scalar
 *         }
 *     }
 * }
 * ```
 *
 * @property name 名称（变量名）。等同于 [variable]。
 * @property namespace 命名空间。
 * @property variable 变量名。
 */
interface CwtDefineVariableConfig : CwtDefineConfig {
    @FromName
    val name: String get() = variable

    override val namespace: String
    val variable: String

    interface Resolver {
        /** 由属性规则解析为定值变量规则。 */
        fun resolve(config: CwtPropertyConfig, namespace: String): CwtDefineVariableConfig?
    }

    companion object : Resolver by CwtDefineVariableConfigResolverImpl()
}

private class CwtDefineVariableConfigResolverImpl : CwtDefineVariableConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig, namespace: String): CwtDefineVariableConfig {
        val variable = config.key
        logger.debug { "Resolved define variable config (namespace: $namespace, variable: $variable)".withLocationPrefix(config) }
        return CwtDefineVariableConfigImpl(config, namespace, variable)
    }
}

private class CwtDefineVariableConfigImpl(
    override val config: CwtPropertyConfig,
    override val namespace: String,
    override val variable: String,
) : UserDataHolderBase(), CwtDefineVariableConfig {
    override fun toString() = "CwtDefineVariableConfigImpl(namespace=$namespace, variable=$variable)"
}
