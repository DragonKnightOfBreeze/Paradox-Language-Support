package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.model.scope.ParadoxScope
import icu.windea.pls.model.scope.ParadoxScopeContext

/**
 * 系统作用域规则。
 *
 * 用于提供系统作用域（system scope）的相关信息（快速文档、基底 ID、可读名称）。
 *
 * **系统作用域（system scope）** 是一组预定义的 **作用域连接（scope link）**，用于获取或切换到需要的作用域。
 *
 * 路径定位：`system_scopes/{name}`，`{name}` 匹配规则名称（系统作用域 ID）。
 *
 * CWTools 兼容性：PLS 扩展。
 *
 * 示例：
 * ```cwt
 * system_scopes = {
 *     This = {}
 *     Root = {}
 *     Prev = { base_id = Prev }
 *     From = { base_id = From }
 *     # ...
 * }
 * ```
 *
 * @property id 系统作用域 ID。
 * @property baseId 基底作用域 ID（用于继承/归类）。
 * @property name 可读名称。
 *
 * @see ParadoxScope
 * @see ParadoxScopeContext
 */
interface CwtSystemScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val id: String
    @FromProperty("base_id: string")
    val baseId: String
    @FromProperty(": string")
    val name: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        /** 由属性规则解析为系统作用域规则。 */
        fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig
    }

    companion object : Resolver by CwtSystemScopeConfigResolverImpl()
}

// region Implementations

private class CwtSystemScopeConfigResolverImpl : CwtSystemScopeConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtSystemScopeConfig {
        val id = config.key
        val baseId = config.properties?.find { p -> p.key == "base_id" }?.stringValue ?: id
        val name = config.stringValue ?: id
        logger.debug { "Resolved system scope config (id: $id).".withLocationPrefix(config) }
        return CwtSystemScopeConfigImpl(config, id, baseId, name)
    }
}

private class CwtSystemScopeConfigImpl(
    override val config: CwtPropertyConfig,
    override val id: String,
    override val baseId: String,
    override val name: String
) : UserDataHolderBase(), CwtSystemScopeConfig {
    override fun equals(other: Any?) = this === other || other is CwtSystemScopeConfig && id == other.id
    override fun hashCode() = id.hashCode()
    override fun toString() = "CwtSystemScopeConfigImpl(name='$name')"
}

// endregion
