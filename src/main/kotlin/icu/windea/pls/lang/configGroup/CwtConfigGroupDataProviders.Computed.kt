package icu.windea.pls.lang.configGroup

import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 用于初始CWT规则分组中需要经过计算的那些数据。
 */
class CwtConfigGroupComputedDataProvider : CwtConfigGroupDataProvider {
    override fun process(configGroup: CwtConfigGroup): Boolean {
        configGroup.progressIndicator?.apply {
            text = PlsBundle.message("configGroup.computeData")
            text2 = ""
            isIndeterminate = true
        }
        
        run {
            configGroup.modifiers.values
                .filter { it.template.isNotEmpty() }
                .sortedByDescending { it.template.snippetExpressions.size } //put xxx_<xxx>_xxx before xxx_<xxx>
                .associateByTo(configGroup.generatedModifiers.asMutable()) { it.name }
            configGroup.modifiers.values
                .filter { it.template.isEmpty() }
                .associateByTo(configGroup.predefinedModifiers.asMutable()) { it.name }
        }

        run {
            for(typeConfig in configGroup.types.values) {
                if(typeConfig.baseType == null) continue
                val typeName = typeConfig.name
                configGroup.swappedTypes.asMutable()[typeName] = typeConfig
                val baseTypeName = typeConfig.baseType.substringBefore('.')
                val baseDeclarationConfig = configGroup.declarations[baseTypeName] ?: continue
                val typeKey = typeConfig.typeKeyFilter?.takeIfTrue()?.singleOrNull() ?: continue
                val declarationConfig = baseDeclarationConfig.propertyConfig.configs
                    ?.find { it is CwtPropertyConfig && it.key.equals(typeKey, true) }?.castOrNull<CwtPropertyConfig>()
                    ?.let { resolveDeclarationConfig(it, typeName) }
                    ?: continue
                configGroup.declarations.asMutable()[typeName] = declarationConfig
            }
        }

        run {
            for((key, linkConfig) in configGroup.linksAsScopeNotData) {
                val localisationLinkConfig = CwtLocalisationLinkConfig(
                    linkConfig.pointer, linkConfig.info, linkConfig.config,
                    linkConfig.name, linkConfig.desc, linkConfig.inputScopes, linkConfig.outputScope
                )
                configGroup.localisationLinks.asMutable()[key] = localisationLinkConfig
            }
        }

        run {
            for(modifier in configGroup.modifiers.values) {
                //category可能是modifierCategory的name，也可能是modifierCategory的internalId
                for(category in modifier.categories) {
                    val categoryConfig = configGroup.modifierCategories[category] ?: continue
                    modifier.categoryConfigMap[categoryConfig.name] = categoryConfig
                }
            }
        }

        run {
            for((k, v) in configGroup.aliasGroups) {
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
                    configGroup.aliasKeysGroupConst.asMutable()[k] = keysConst
                }
                if(!keysNoConst.isNullOrEmpty()) {
                    configGroup.aliasKeysGroupNoConst.asMutable()[k] = keysNoConst.sortedByPriority({ CwtKeyExpression.resolve(it) }, { configGroup }).toSet()
                }
            }
        }

        run {
          configGroup.linksAsScopeWithPrefixSorted.asMutable() += configGroup.linksAsScopeWithPrefix.values.sortedByPriority({ it.dataSource!! }, { configGroup })
          configGroup.linksAsValueWithPrefixSorted.asMutable() += configGroup.linksAsValueWithPrefix.values.sortedByPriority({ it.dataSource!! }, { configGroup })
          configGroup.linksAsScopeWithoutPrefixSorted.asMutable() += configGroup.linksAsScopeWithoutPrefix.values.sortedByPriority({ it.dataSource!! }, { configGroup })
          configGroup.linksAsValueWithoutPrefixSorted.asMutable() += configGroup.linksAsValueWithoutPrefix.values.sortedByPriority({ it.dataSource!! }, { configGroup })
          configGroup.linksAsVariable.asMutable() += configGroup. linksAsValueWithoutPrefix["variable"].toSingletonListOrEmpty()
        }

        run {
            with(configGroup.aliasNamesSupportScope.asMutable()) {
                this += "modifier" //也支持，但不能切换作用域
                this += "trigger"
                this += "effect"
                configGroup.info.aliasNamesSupportScope.asMutable().forEach { this += it }
            }
            with(configGroup.definitionTypesSupportScope.asMutable()) {
                this += "scripted_effect"
                this += "scripted_trigger"
                this += "game_rule"
            }
            with(configGroup.definitionTypesIndirectSupportScope.asMutable()) {
                this += "on_action" //也支持，其中调用的事件的类型要匹配
                this += "event" //事件
            }
            with(configGroup.definitionTypesSkipCheckSystemLink.asMutable()) {
                this += "event"
                this += "scripted_trigger"
                this += "scripted_effect"
                this += "script_value"
                this += "game_rule"
            }
            with(configGroup.definitionTypesSupportParameters.asMutable()) {
                this += "script_value" //SV也支持参数
                //this += "inline_script" //内联脚本也支持参数（并且可以表示多条语句）（但不是定义）
                for(parameterConfig in configGroup.info.parameterConfigs) {
                    val propertyConfig = parameterConfig.parentConfig as? CwtPropertyConfig ?: continue
                    val aliasSubName = propertyConfig.key.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
                    val contextExpression = if(aliasSubName.isNullOrEmpty()) propertyConfig.keyExpression else CwtKeyExpression.resolve(aliasSubName)
                    if(contextExpression.type == CwtDataType.Definition && contextExpression.value != null) {
                        this += contextExpression.value
                    }
                }
            }
        }
        
        run {
            val definitionTypes = configGroup.definitionTypesSupportParameters
            val builder = StringBuilder()
            var isFirst = true
            for(definitionType in definitionTypes) {
                val typeConfig = configGroup.types.get(definitionType) ?: continue
                val filePath = typeConfig.pathFile ?: typeConfig.path ?: continue
                val fileExtension = typeConfig.pathExtension
                if(isFirst) isFirst = false else builder.append('|')
                builder.append(filePath)
                if(fileExtension != null) builder.append(':').append(fileExtension)
            }
            val modificationTracker = ParadoxPsiModificationTracker.getInstance(configGroup.project).ScriptFileTracker(builder.toString())
            configGroup.parameterModificationTracker = MergedModificationTracker(configGroup.modificationTracker, modificationTracker)
        }

        return true
    }

    private fun resolveDeclarationConfig(propertyConfig: CwtPropertyConfig, name: String): CwtDeclarationConfig {
        return CwtDeclarationConfig(propertyConfig.pointer, propertyConfig.info, name, propertyConfig)
    }
}