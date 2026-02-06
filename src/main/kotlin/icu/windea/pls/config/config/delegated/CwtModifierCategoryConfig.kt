package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.option.CwtOptionDataHolder
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtProperty
import icu.windea.pls.lang.util.ParadoxScopeManager

/**
 * 修正分类规则。
 *
 * 用于分组修正（modifier），为其指定允许的作用域类型。
 *
 * 路径定位：`modifier_categories/{name}`，`{name}` 匹配规则名称（分类名）。
 *
 * CWTools 兼容性：兼容。
 *
 * 示例：
 * ```cwt
 * modifier_categories = {
 *     Pops = { supported_scopes = { species pop_group planet ... } }
 * }
 * ```
 *
 * @property name 名称（分类名）。
 * @property supportedScopes 允许的作用域（类型）的集合。
 *
 * @see CwtModifierConfig
 * @see CwtOptionDataHolder.replaceScopes
 * @see CwtOptionDataHolder.pushScope
 * @see icu.windea.pls.lang.util.ParadoxModifierManager
 */
interface CwtModifierCategoryConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    @FromKey
    val name: String
    @FromProperty("supported_scopes: string | string[]")
    val supportedScopes: Set<String>

    interface Resolver {
        /** 由属性规则解析为修正分类规则。 */
        fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig?
    }

    companion object : Resolver by CwtModifierCategoryConfigResolverImpl()
}

// region Implementations

private class CwtModifierCategoryConfigResolverImpl : CwtModifierCategoryConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtModifierCategoryConfig? {
        val name = config.key
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid modifier category config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }
        // may be empty here (e.g., "AI Economy")
        val supportedScopes = propElements.find { it.key == "supported_scopes" }?.let { prop ->
            buildSet {
                prop.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) }
                prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeManager.getScopeId(v)) } }
            }
        }?.optimized() ?: ParadoxScopeManager.anyScopeIdSet
        logger.debug { "Resolved modifier category config (name: $name).".withLocationPrefix(config) }
        return CwtModifierCategoryConfigImpl(config, name, supportedScopes)
    }
}

private class CwtModifierCategoryConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val supportedScopes: Set<String>
) : UserDataHolderBase(), CwtModifierCategoryConfig {
    override fun toString() = "CwtModifierCategoryConfigImpl(name='$name')"
}

// endregion
