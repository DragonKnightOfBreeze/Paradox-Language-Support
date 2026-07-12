package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.core.collections.getOne
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
 * - `system_scopes/{name}`。其中 `{name}` 匹配规则名称（即系统作用域的 ID）。
 *
 * 示例：
 *
 * ```cwt
 * system_scopes = {
 *     This = {}
 *     Root = {}
 *     Prev = { base = Prev }
 *     From = { base = From }
 *     # ...
 * }
 * ```
 *
 * > CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * @property name 规则名称。即系统作用域的 ID。
 * @property base 基础系统作用域的 ID。未指定时等同于规则名称。用于归类同族的系统作用域（如 `Prev` `PrevPrev` ...）。
 *
 * @see ParadoxScope
 * @see ParadoxScopeContext
 */
interface CwtSystemScopeConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty> {
    @FromName
    val name: String
    @FromMember("base: string")
    val base: String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        /** 由属性规则解析为系统作用域规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig? {
            return CwtSystemScopeConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtSystemScopeConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtSystemScopeConfig? {
        val name = config.key
        val propConfigs = config.properties
        if (propConfigs == null) {
            logger.warn("Skipped invalid system scope config (name: $name): Null properties.".withLocationPrefix(config))
            return null
        }
        val propGroup = propConfigs.groupBy { it.key }
        val base = propGroup.getOne("base")?.stringValue ?: name
        logger.debug { "Resolved system scope config (name: $name).".withLocationPrefix(config) }
        return CwtSystemScopeConfigImpl(config, name, base)
    }
}

private class CwtSystemScopeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val base: String,
) : UserDataHolderBase(), CwtSystemScopeConfig {
    override fun equals(other: Any?) = this === other || other is CwtSystemScopeConfig && name == other.name
    override fun hashCode() = name.hashCode()
    override fun toString() = "CwtSystemScopeConfigImpl(name='$name')"
}

// endregion
