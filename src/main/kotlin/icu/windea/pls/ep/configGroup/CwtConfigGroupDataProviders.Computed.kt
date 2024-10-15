package icu.windea.pls.ep.configGroup

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 用于初始规则分组中需要经过计算的那些数据。
 */
class ComputedCwtConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override fun process(configGroup: CwtConfigGroup): Boolean {
        run {
            configGroup.modifiers.values
                .filter { it.template.expressionString.isNotEmpty() }
                .sortedByDescending { it.template.snippetExpressions.size } //put xxx_<xxx>_xxx before xxx_<xxx>
                .associateByTo(configGroup.generatedModifiers) { it.name }
            configGroup.modifiers.values
                .filter { it.template.expressionString.isEmpty() }
                .associateByTo(configGroup.predefinedModifiers) { it.name }
        }

        run {
            for (typeConfig in configGroup.types.values) {
                if (typeConfig.baseType == null) continue
                val typeName = typeConfig.name
                configGroup.swappedTypes[typeName] = typeConfig
                val baseTypeName = typeConfig.baseType!!.substringBefore('.')
                val baseDeclarationConfig = configGroup.declarations[baseTypeName] ?: continue
                val typeKey = typeConfig.typeKeyFilter?.takeIfTrue()?.singleOrNull() ?: continue
                val declarationConfig = baseDeclarationConfig.config.configs
                    ?.find { it is CwtPropertyConfig && it.key.equals(typeKey, true) }?.castOrNull<CwtPropertyConfig>()
                    ?.let { CwtDeclarationConfig.resolve(it, name = typeName) }
                    ?: continue
                configGroup.declarations[typeName] = declarationConfig
            }
        }

        //add missing localisation links from links
        run {
            val localisationLinksNotFromData = configGroup.localisationLinks.values.filter { !it.fromData }
            if (localisationLinksNotFromData.isNotEmpty()) return@run
            val linksNotFromData = configGroup.links.values.filter { !it.fromData }
            for (linkConfig in linksNotFromData) {
                configGroup.localisationLinks[linkConfig.name] = linkConfig
            }
        }

        //bind specific links and localisation links
        run {
            configGroup.linksOfVariable += configGroup.links.values.filter { it.forValue() && it.fromData && it.name == "variable" }
            configGroup.localisationLinksOfEventTarget += configGroup.localisationLinks.values.filter { it.forScope() && it.fromData && it.prefix == "event_target:" }
        }

        run {
            for (modifier in configGroup.modifiers.values) {
                for (category in modifier.categories) {
                    val categoryConfig = configGroup.modifierCategories[category] ?: continue
                    modifier.categoryConfigMap[categoryConfig.name] = categoryConfig
                }
            }
        }

        run {
            for ((k, v) in configGroup.aliasGroups) {
                var keysConst: MutableMap<String, String>? = null
                var keysNoConst: MutableSet<String>? = null
                for (key in v.keys) {
                    if (CwtDataExpression.resolve(key, true).type == CwtDataTypes.Constant) {
                        if (keysConst == null) keysConst = caseInsensitiveStringKeyMap()
                        keysConst[key] = key
                    } else {
                        if (keysNoConst == null) keysNoConst = mutableSetOf()
                        keysNoConst += key
                    }
                }
                if (!keysConst.isNullOrEmpty()) {
                    configGroup.aliasKeysGroupConst[k] = keysConst
                }
                if (!keysNoConst.isNullOrEmpty()) {
                    configGroup.aliasKeysGroupNoConst[k] = keysNoConst.sortedByPriority({ CwtDataExpression.resolve(it, true) }, { configGroup }).toMutableSet()
                }
            }
        }

        run {
            with(configGroup.definitionTypesSupportParameters) {
                for (parameterConfig in configGroup.parameterConfigs) {
                    val propertyConfig = parameterConfig.parentConfig as? CwtPropertyConfig ?: continue
                    val aliasSubName = propertyConfig.key.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
                    val contextExpression = if (aliasSubName.isNullOrEmpty()) propertyConfig.keyExpression else CwtDataExpression.resolve(aliasSubName, true)
                    if (contextExpression.type == CwtDataTypes.Definition) {
                        contextExpression.value?.let { this += it }
                    }
                }
            }
        }

        return true
    }
}
