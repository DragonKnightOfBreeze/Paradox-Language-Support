package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
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
 * 路径定位：
 * - `system_scopes/{name}`。其中 `{name}` 匹配系统作用域 ID。
 *
 * 示例：
 *
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
 * > CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * @property id 系统作用域 ID。
 * @property baseId 基底作用域 ID（用于继承/归类）。
 * @property name 可读名称。
 *
 * @see ParadoxScope
 * @see ParadoxScopeContext
 */
interface CwtSystemScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty> {
    @FromName
    val id: String
    @FromMember("base_id: string")
    val baseId: String
    @FromMember(": string")
    val name: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        /** 由属性规则解析为系统作用域规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig {
            return CwtSystemScopeConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtSystemScopeConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig {
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
