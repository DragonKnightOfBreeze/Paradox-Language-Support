package icu.windea.pls.lang.configGroup

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*

/**
 * 用于CWT规则分组的初始化的最后，对其进行一些必要的处理。
 */
abstract class PostCwtConfigGroupSupport : CwtConfigGroupSupportBase()

/**
 * 用于初始化需要经过计算的那些数据。
 */
class BasePostCwtConfigGroupSupport : PostCwtConfigGroupSupport() {
    override fun process(configGroup: CwtConfigGroup): Boolean = with(configGroup) {
        run {
            modifiers.values
                .filter { it.template.isNotEmpty() }
                .sortedByDescending { it.template.snippetExpressions.size } //put xxx_<xxx>_xxx before xxx_<xxx>
                .associateByTo(generatedModifiers.asMutable()) { it.name }
            modifiers.values
                .filter { it.template.isEmpty() }
                .associateByTo(predefinedModifiers.asMutable()) { it.name }
        }
        
        run {
            for(typeConfig in types.values) {
                if(typeConfig.baseType == null) continue
                val typeName = typeConfig.name
                swappedTypes.asMutable()[typeName] = typeConfig
                val baseTypeName = typeConfig.baseType.substringBefore('.')
                val baseDeclarationConfig = declarations[baseTypeName] ?: continue
                val typeKey = typeConfig.typeKeyFilter?.takeIfTrue()?.singleOrNull() ?: continue
                val declarationConfig = baseDeclarationConfig.propertyConfig.configs
                    ?.find { it is CwtPropertyConfig && it.key.equals(typeKey, true) }?.castOrNull<CwtPropertyConfig>()
                    ?.let { resolveDeclarationConfig(it, typeName) }
                    ?: continue
                declarations.asMutable()[typeName] = declarationConfig
            }
        }
        
        run {
            for((key, linkConfig) in linksAsScopeNotData) {
                val localisationLinkConfig = CwtLocalisationLinkConfig(
                    linkConfig.pointer, linkConfig.info, linkConfig.config,
                    linkConfig.name, linkConfig.desc, linkConfig.inputScopes, linkConfig.outputScope
                )
                localisationLinks.asMutable()[key] = localisationLinkConfig
            }
        }
        
        run {
            for(modifier in modifiers.values) {
                //category可能是modifierCategory的name，也可能是modifierCategory的internalId
                for(category in modifier.categories) {
                    val categoryConfig = modifierCategories[category] ?: continue
                    modifier.categoryConfigMap[categoryConfig.name] = categoryConfig
                }
            }
        }
        
        run {
            for((k, v) in aliasGroups) {
                var keysConst: MutableMap<String, String>? = null
                var keysNoConst: MutableSet<String>? = null
                for(key in v.keys) {
                    if(CwtKeyExpression.resolve(key).type == CwtDataType.Constant) {
                        if(keysConst == null) keysConst = caseInsensitiveStringKeyMap()
                        keysConst[key] = key
                    } else {
                        if(keysNoConst == null) keysNoConst = mutableSetOf()
                        keysNoConst += key
                    }
                }
                if(!keysConst.isNullOrEmpty()) {
                    aliasKeysGroupConst.asMutable()[k] = keysConst
                }
                if(!keysNoConst.isNullOrEmpty()) {
                    aliasKeysGroupNoConst.asMutable()[k] = keysNoConst.sortedByPriority({ CwtKeyExpression.resolve(it) }, { this }).toSet()
                }
            }
        }
        
        run {
            linksAsScopeWithPrefixSorted.asMutable() += linksAsScopeWithPrefix.values.sortedByPriority({ it.dataSource!! }, { this })
            linksAsValueWithPrefixSorted.asMutable() += linksAsValueWithPrefix.values.sortedByPriority({ it.dataSource!! }, { this })
            linksAsScopeWithoutPrefixSorted.asMutable() += linksAsScopeWithoutPrefix.values.sortedByPriority({ it.dataSource!! }, { this })
            linksAsValueWithoutPrefixSorted.asMutable() += linksAsValueWithoutPrefix.values.sortedByPriority({ it.dataSource!! }, { this })
            linksAsVariable.asMutable() +=  linksAsValueWithoutPrefix["variable"].toSingletonListOrEmpty()
        }
        
        run {
            with(aliasNamesSupportScope.asMutable()) {
                this += "modifier" //也支持，但不能切换作用域
                this += "trigger"
                this += "effect"
                info.aliasNamesSupportScope.asMutable().forEach { this += it }
            }
            with(definitionTypesSupportScope.asMutable()) {
                this += "scripted_effect"
                this += "scripted_trigger"
                this += "game_rule"
            }
            with(definitionTypesIndirectSupportScope.asMutable()) {
                this += "on_action" //也支持，其中调用的事件的类型要匹配
                this += "event" //事件
            }
            with(definitionTypesSkipCheckSystemLink.asMutable()) {
                this += "event"
                this += "scripted_trigger"
                this += "scripted_effect"
                this += "script_value"
                this += "game_rule"
            }
            with(definitionTypesSupportParameters.asMutable()) {
                this += "script_value" //SV也支持参数
                //this += "inline_script" //内联脚本也支持参数（并且可以表示多条语句）（但不是定义）
                for(parameterConfig in info.parameterConfigs) {
                    val propertyConfig = parameterConfig.parentConfig as? CwtPropertyConfig ?: continue
                    val aliasSubName = propertyConfig.key.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
                    val contextExpression = if(aliasSubName.isNullOrEmpty()) propertyConfig.keyExpression else CwtKeyExpression.resolve(aliasSubName)
                    if(contextExpression.type == CwtDataType.Definition && contextExpression.value != null) {
                        this += contextExpression.value
                    }
                }
            }
        }
        
        return true
    }
    
}