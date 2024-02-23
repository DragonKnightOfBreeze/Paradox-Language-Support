package icu.windea.pls.config.config

import icu.windea.pls.config.expression.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*

//CWT规则文件中关于（生成的）修正的规则有多种写法
//* 写在 type[xxx] = {...} 子句中的 modifiers = { ... } 子句中，允许按子类型进行匹配，格式为 <template_string> = <categories>
//* 作为 alias[modifier:xxx] = x 中的 xxx ，这里PLS认为 xxx 需要表示一个常量字符串或者模版表达式（如 mod_<job>_desc）
//* 写在 modifier.cwt 中的 modifiers = { ... } 子句中，格式为 <template_string> == <categories>
//目前PLS支持以上所有三种写法

interface CwtModifierConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
    val name: String
    val categories: Set<String> //category names
    val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig>
    val template: CwtTemplateExpression
    val supportedScopes: Set<String>
    
    companion object {
        fun resolve(config: CwtPropertyConfig, name: String): CwtModifierConfig? = doResolve(config, name)
        
        fun resolveFromAlias(config: CwtAliasConfig): CwtModifierConfig = doResolveFromAlias(config)
        
        fun resolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? =
            doResolveFromDefinitionModifier(config, name, typeExpression)
    }
}

//Implementations (interned)

private fun doResolve(config: CwtPropertyConfig, name: String): CwtModifierConfig? {
    //string | string[]
    val categories = config.stringValue?.let { setOf(it) }
        ?: config.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
        ?: return null
    return CwtModifierConfigImpl(config, name, categories)
}

private fun doResolveFromAlias(config: CwtAliasConfig): CwtModifierConfig {
    return CwtModifierConfigImpl(config.config, config.subName)
}

private fun doResolveFromDefinitionModifier(config: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? {
    //string | string[]
    val modifierName = name.replace("$", "<$typeExpression>").intern()
    val categories = config.stringValue?.let { setOf(it) }
        ?: config.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
        ?: return null
    return CwtModifierConfigImpl(config, modifierName, categories)
}

private class CwtModifierConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String, //template name, not actual modifier name!
    override val categories: Set<String> = emptySet() //category names
) : CwtModifierConfig {
    override val categoryConfigMap: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
    
    override val template = CwtTemplateExpression.resolve(name)
    
    override val supportedScopes: Set<String> by lazy {
        if(categoryConfigMap.isNotEmpty()) {
            ParadoxScopeHandler.getSupportedScopes(categoryConfigMap)
        } else {
            //没有注明categories时从scopes选项中获取
            config.supportedScopes
        }
    }
}