package icu.windea.pls.ep.configGroup

import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.model.*

/**
 * 用于初始规则分组中需要经过计算的那些数据。
 */
class ComputedCwtConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override fun process(configGroup: CwtConfigGroup): Boolean {
        //compute `generatedModifiers` and `predefinedModifiers`
        run {
            configGroup.modifiers.values
                .filter { it.template.expressionString.isNotEmpty() }
                .sortedByDescending { it.template.snippetExpressions.size } //put xxx_<xxx>_xxx before xxx_<xxx>
                .associateByTo(configGroup.generatedModifiers) { it.name }
            configGroup.modifiers.values
                .filter { it.template.expressionString.isEmpty() }
                .associateByTo(configGroup.predefinedModifiers) { it.name }
        }

        //compute `swappedTypes` and add missing declarations with swapped type
        run {
            for (typeConfig in configGroup.types.values) {
                if (typeConfig.baseType == null) continue
                val typeName = typeConfig.name
                configGroup.swappedTypes[typeName] = typeConfig
                val baseTypeName = typeConfig.baseType!!.substringBefore('.')
                val baseDeclarationConfig = configGroup.declarations[baseTypeName] ?: continue
                val typeKey = typeConfig.typeKeyFilter?.takeWithOperator()?.singleOrNull() ?: continue
                val rootKeysList = typeConfig.skipRootKey?.takeIf { it.size > 1 }?.drop(1) ?: continue
                val configPaths = when {
                    rootKeysList.isEmpty() -> listOf(CwtConfigPath.resolve(typeKey))
                    else -> rootKeysList.map { CwtConfigPath.resolve(it + typeKey) }
                }
                val c0 = baseDeclarationConfig.configForDeclaration
                val c = configPaths.firstNotNullOfOrNull { c0.findPropertyByPath(it, ignoreCase = true) } ?: continue
                val declarationConfig = CwtDeclarationConfig.resolve(c, name = typeName) ?: continue
                configGroup.declarations[typeName] = declarationConfig
            }
        }

        //add missing localisation links from links
        run {
            val localisationLinksNotFromData = configGroup.localisationLinks.values.filter { !it.fromData }
            if (localisationLinksNotFromData.isNotEmpty()) return@run
            val linksNotFromData = configGroup.links.values.filter { !it.fromData }
            for (linkConfig in linksNotFromData) {
                configGroup.localisationLinks[linkConfig.name] = CwtLinkConfig.resolveForLocalisation(linkConfig)
            }
        }

        //bind specific links and localisation links
        run {
            configGroup.linksOfVariable += configGroup.links.values
                .filter { it.forValue() && it.fromData && it.name == "variable" }
        }

        //bind `categoryConfigMap` for modifier configs
        run {
            for (modifier in configGroup.modifiers.values) {
                for (category in modifier.categories) {
                    val categoryConfig = configGroup.modifierCategories[category] ?: continue
                    modifier.categoryConfigMap[categoryConfig.name] = categoryConfig
                }
            }
        }

        //compute `aliasKeysGroupConst` and `aliasKeysGroupNoConst`
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

        //compute `definitionTypesSupportParameters`
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

        //compute `definitionTypesMayWithTypeKeyPrefix`
        run {
            //按文件路径计算，更准确地说，按规则的文件路径模式是否有交集来计算
            //based on file paths, in detail, based on file path patterns (has any same file path patterns)
            with(configGroup.definitionTypesMayWithTypeKeyPrefix) {
                val types = configGroup.types.values.filter { c -> c.typeKeyPrefix != null }
                val filePathPatterns = types.flatMapTo(mutableSetOf()) { c -> c.filePathPatterns }
                val types1 = configGroup.types.values.filter { c ->
                    val filePathPatterns1 = c.filePathPatterns
                    filePathPatterns1.isNotEmpty() && filePathPatterns1.any { it in filePathPatterns }
                }
                types1.forEach { c -> this += c.name }
            }
        }

        //computer `relatedLocalisationPatterns`
        run {
            with(configGroup.relatedLocalisationPatterns) {
                val r = mutableSetOf<String>()
                configGroup.types.values.forEach { c ->
                    c.localisation?.locationConfigs?.forEach { (_, lc) -> r += lc.value }
                }
                r.forEach { s ->
                    val i = s.indexOf('$')
                    if (i == -1) return@forEach
                    this += tupleOf(s.substring(0, i), s.substring(i + 1))
                }
                this.sortedWith(compareBy({ it.first }, { it.second }))
            }
        }

        return true
    }
}
