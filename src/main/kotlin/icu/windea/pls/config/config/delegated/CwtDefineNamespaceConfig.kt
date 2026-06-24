package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.optimized

/**
 * 定值命名空间规则。
 *
 * 用于描述脚本文件中的定值命名空间，提供快速文档文本。
 * 它们位于 `common/defines` 目录中的扩展名为 `.txt` 的脚本文件中。
 *
 * 路径定位：
 * - `defines/{namespace}`。其中 `{namespace}` 匹配命名空间（即规则名称）。
 *
 * 备注：插件会强制忽略名为 `define` 或 `defines` 的类型规则和声明规则。
 *
 * ### 示例
 *
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
 * > CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * @property name 规则名称（即命名空间）。等同于 [namespace]。
 * @property namespace 命名空间。
 * @property variables 变量到对应定值变量规则的映射。
 */
interface CwtDefineNamespaceConfig : CwtDefineConfig {
    @FromName
    val name: String get() = namespace
    @FromMember("*: DefineVariableConfig", multiple = true)
    val variables: Map<String, CwtDefineVariableConfig>

    override val namespace: String

    companion object {
        /** 由属性规则解析为定值命名空间规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtDefineNamespaceConfig? {
            return CwtDefineNamespaceConfigResolver.resolve(config)
        }
    }
}

private object CwtDefineNamespaceConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtDefineNamespaceConfig? {
        // NOTE 2.1.8 a define namespace config can have no within define variable configs
        val namespace = config.key
        val propConfigs = config.properties
        if (propConfigs == null) {
            logger.warn("Skipped invalid define namespace config (namespace: $namespace): Null properties".withLocationPrefix(config))
            return null
        }
        val variables = propConfigs.mapNotNull { CwtDefineVariableConfig.resolve(it, namespace) }.associateBy { it.name }.optimized()
        logger.debug { "Resolved define namespace config (namespace: $namespace)".withLocationPrefix(config) }
        return CwtDefineNamespaceConfigImpl(config, namespace, variables)
    }
}

private class CwtDefineNamespaceConfigImpl(
    override val config: CwtPropertyConfig,
    override val namespace: String,
    override val variables: Map<String, CwtDefineVariableConfig>,
) : UserDataHolderBase(), CwtDefineNamespaceConfig {
    override fun toString() = "CwtDefineNamespaceConfigImpl(namespace=$namespace)"
}
