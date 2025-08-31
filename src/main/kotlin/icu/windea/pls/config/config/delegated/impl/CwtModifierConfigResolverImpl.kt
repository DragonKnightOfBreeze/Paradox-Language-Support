package icu.windea.pls.config.config.delegated.impl

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.supportedScopes
import icu.windea.pls.config.config.values
import icu.windea.pls.config.configExpression.CwtTemplateExpression
import icu.windea.pls.core.collections.optimized
import icu.windea.pls.lang.util.ParadoxScopeManager

internal class CwtModifierConfigResolverImpl : CwtModifierConfig.Resolver {
    override fun resolve(config: CwtPropertyConfig, name: String): CwtModifierConfig? = doResolve(config, name)
    override fun resolveFromAlias(config: CwtAliasConfig): CwtModifierConfig = doResolveFromAlias(config)
    override fun resolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? = doResolveFromDefinitionModifier(config, name, typeExpression)

    private fun doResolve(config: CwtPropertyConfig, name: String): CwtModifierConfig? {
        // 从属性值解析修正的类别集合：支持 string | string[]
        val categories = config.stringValue?.let { setOf(it) }
            ?: config.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
            ?: return null
        return CwtModifierConfigImpl(config, name, categories.optimized())
    }

    private fun doResolveFromAlias(config: CwtAliasConfig): CwtModifierConfig {
        // 从别名派生修正规则：采用 subName 作为模板名（非最终修正名）
        return CwtModifierConfigImpl(config.config, config.subName)
    }

    private fun doResolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? {
        // 从定义内修正解析：模板名中将 $ 替换为 <typeExpression> 以形成可区分键
        // 支持 string | string[]
        val modifierName = name.replace("$", "<$typeExpression>").intern()
        val categories = config.stringValue?.let { setOf(it) }
            ?: config.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
            ?: return null
        return CwtModifierConfigImpl(config, modifierName, categories)
    }
}

private class CwtModifierConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String, // 模板名（非实际修正名）
    override val categories: Set<String> = emptySet() // 类别名集合
) : UserDataHolderBase(), CwtModifierConfig {
    override val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()

    // 解析模板表达式，用于后续根据上下文展开实际修正名
    override val template = CwtTemplateExpression.resolve(name)

    override val supportedScopes: Set<String> by lazy {
        if (categoryConfigMap.isNotEmpty()) {
            ParadoxScopeManager.getSupportedScopes(categoryConfigMap)
        } else {
            // 未提供类别映射时，从修正配置本身的 scopes 选项回退获取
            config.supportedScopes
        }
    }

    override fun toString() = "CwtModifierConfigImpl(name='$name')"
}
