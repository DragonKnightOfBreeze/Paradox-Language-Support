package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.optionData
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.config.util.CwtConfigResolverUtil.withLocationPrefix
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtModifierConfigResolverImpl : CwtModifierConfig.Resolver {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig, name: String): CwtModifierConfig? = doResolve(config, name)
    // override fun resolveFromAlias(config: CwtAliasConfig): CwtModifierConfig = doResolveFromAlias(config)
    override fun resolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? = doResolveFromDefinitionModifier(config, name, typeExpression)

    private fun doResolve(config: CwtPropertyConfig, name: String): CwtModifierConfig? {
        // string | string[]
        val categories = config.stringValue?.let { setOf(it) } ?: config.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }?.optimized()
        if (categories == null) {
            logger.debug { "Skipped invalid modifier config (name: $name): Null categories".withLocationPrefix(config) }
            return null
        }
        logger.debug { "Resolved modifier config (name: $name).".withLocationPrefix(config) }
        return CwtModifierConfigImpl(config, name, categories)
    }

    // private fun doResolveFromAlias(config: CwtAliasConfig): CwtModifierConfig {
    //     return CwtModifierConfigImpl(config.config, config.subName)
    // }

    private fun doResolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? {
        // string | string[]
        val modifierName = name.replace("$", "<$typeExpression>").intern()
        val categories = config.stringValue?.let { setOf(it) }
            ?: config.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
        if (categories == null) {
            logger.debug { "Skipped invalid modifier config from definition modifier (name: $name): Null categories".withLocationPrefix(config) }
            return null
        }
        logger.debug { "Resolved modifier config from definition modifier (name: $name).".withLocationPrefix(config) }
        return CwtModifierConfigImpl(config, modifierName, categories)
    }
}

private class CwtModifierConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String, // template name, not actual modifier name!
    override val categories: Set<String> = emptySet() // category names
) : UserDataHolderBase(), CwtModifierConfig {
    override val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()

    override val template = CwtTemplateExpression.resolve(name)

    override val supportedScopes: Set<String> by lazy {
        if (categoryConfigMap.isNotEmpty()) {
            ParadoxScopeManager.getSupportedScopes(categoryConfigMap)
        } else {
            // 没有注明categories时从scopes选项中获取
            config.optionData { this.supportedScopes }
        }
    }

    override fun toString() = "CwtModifierConfigImpl(name='$name')"
}
