package icu.windea.pls.ep.configGroup

import com.intellij.openapi.vfs.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.config.extended.*
import icu.windea.pls.config.config.internal.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.ep.priority.*
import kotlin.collections.set

/**
 * 用于初始规则分组中基于文件内容的那些数据。
 */
class FileBasedCwtConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override fun process(configGroup: CwtConfigGroup): Boolean {
        //按照文件路径（相对于规则分组的根目录）正序读取所有规则文件
        //后加入的规则文件会覆盖先加入的同路径的规则文件
        //后加入的数据项会覆盖先加入的同名同类型的数据项
        
        val allInternalFiles = mutableMapOf<String, VirtualFile>()
        val allFiles = mutableMapOf<String, VirtualFile>()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.all f@{ fileProvider ->
            fileProvider.processFiles(configGroup) p@{ filePath, file ->
                //不允许覆盖内部的规则文件
                if(fileProvider.isBuiltIn() && filePath.startsWith("internal/")) {
                    allInternalFiles.putIfAbsent(filePath, file)
                    return@p true
                }
                
                allFiles.put(filePath, file)
                true
            }
        }
        allInternalFiles.forEach f@{ filePath, file ->
            val psiFile = file.toPsiFile(configGroup.project) as? CwtFile ?: return@f
            val fileConfig = CwtConfigResolver.resolve(psiFile, configGroup)
            processInternalFile(filePath, fileConfig, configGroup)
        }
        allFiles.forEach f@{ filePath, file ->
            val psiFile = file.toPsiFile(configGroup.project) as? CwtFile ?: return@f
            val fileConfig = CwtConfigResolver.resolve(psiFile, configGroup)
            processFile(fileConfig, configGroup)
            configGroup.files[filePath] = fileConfig
        }
        
        return true
    }
    
    private fun processInternalFile(filePath: String, fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        when(filePath) {
            "internal/schema.cwt" -> CwtSchemaConfig.resolveInFile(fileConfig, configGroup)
            "internal/folding_settings.cwt" -> CwtFoldingSettingsConfig.resolveInFile(fileConfig, configGroup)
            "internal/postfix_template_settings.cwt" -> CwtPostfixTemplateSettingsConfig.resolveInFile(fileConfig, configGroup)
        }
    }
    
    private fun processFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        for(property in fileConfig.properties) {
            val key = property.key
            when {
                key == "priorities" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val k = config.key.removePrefix("game/").normalizePath().orNull() ?: continue
                        val v = config.stringValue?.orNull()?.let { ParadoxPriority.resolve(it) } ?: continue
                        configGroup.priorities[k] = v
                    }
                }
                key == "system_scopes" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val systemScopeConfig = CwtSystemScopeConfig.resolve(config)
                        configGroup.systemScopes[systemScopeConfig.id] = systemScopeConfig
                    }
                }
                key == "localisation_locales" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val localisationLocaleConfig = CwtLocalisationLocaleConfig.resolve(config)
                        configGroup.localisationLocalesById[localisationLocaleConfig.id] = localisationLocaleConfig
                        localisationLocaleConfig.codes.forEach { code ->
                            configGroup.localisationLocalesByCode[code] = localisationLocaleConfig
                        }
                    }
                }
                key == "types" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val typeConfig = CwtTypeConfig.resolve(config) ?: continue
                        configGroup.types[typeConfig.name] = typeConfig
                    }
                }
                key == "enums" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        run {
                            val enumConfig = CwtEnumConfig.resolve(config) ?: return@run
                            configGroup.enums[enumConfig.name] = enumConfig
                        }
                        run {
                            val complexEnumConfig = CwtComplexEnumConfig.resolve(config) ?: return@run
                            configGroup.complexEnums[complexEnumConfig.name] = complexEnumConfig
                        }
                    }
                }
                key == "values" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val dynamicValueTypeConfig = CwtDynamicValueTypeConfig.resolve(config) ?: continue
                        configGroup.dynamicValueTypes[dynamicValueTypeConfig.name] = dynamicValueTypeConfig
                    }
                }
                key == "links" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val linkConfig = CwtLinkConfig.resolve(config) ?: continue
                        configGroup.links[linkConfig.name] = linkConfig
                        
                        val fromData = linkConfig.fromData && linkConfig.dataSourceExpression != null
                        val withPrefix = linkConfig.prefix != null
                        val type = linkConfig.type
                        if(type == null || type == "scope" || type == "both") {
                            when {
                                !fromData -> configGroup.linksAsScopeNotData[linkConfig.name] = linkConfig
                                withPrefix -> configGroup.linksAsScopeWithPrefix[linkConfig.name] = linkConfig
                                else -> configGroup.linksAsScopeWithoutPrefix[linkConfig.name] = linkConfig
                            }
                        }
                        if(type == "value" || type == "both") {
                            when {
                                !fromData -> configGroup.linksAsValueNotData[linkConfig.name] = linkConfig
                                withPrefix -> configGroup.linksAsValueWithPrefix[linkConfig.name] = linkConfig
                                else -> configGroup.linksAsValueWithoutPrefix[linkConfig.name] = linkConfig
                            }
                        }
                    }
                }
                key == "localisation_links" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val localisationLinkConfig = CwtLocalisationLinkConfig.resolve(config) ?: continue
                        configGroup.localisationLinks[localisationLinkConfig.name] = localisationLinkConfig
                    }
                }
                key == "localisation_commands" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val localisationCommandConfig = CwtLocalisationCommandConfig.resolve(config)
                        configGroup.localisationCommands[localisationCommandConfig.name] = localisationCommandConfig
                    }
                }
                key == "modifier_categories" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val modifierCategoryConfig = CwtModifierCategoryConfig.resolve(config) ?: continue
                        configGroup.modifierCategories[modifierCategoryConfig.name] = modifierCategoryConfig
                    }
                }
                key == "modifiers" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val modifierName = config.key
                        val modifierConfig = CwtModifierConfig.resolve(config, modifierName) ?: continue
                        configGroup.modifiers[modifierName] = modifierConfig
                        for(snippetExpression in modifierConfig.template.snippetExpressions) {
                            if(snippetExpression.type == CwtDataTypes.Definition) {
                                val typeExpression = snippetExpression.value ?: continue
                                configGroup.type2ModifiersMap.getOrPut(typeExpression) { mutableMapOf() }[modifierName] = modifierConfig
                            }
                        }
                    }
                }
                key == "scopes" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val scopeConfig = CwtScopeConfig.resolve(config) ?: continue
                        configGroup.scopes[scopeConfig.name] = scopeConfig
                        for(alias in scopeConfig.aliases) {
                            configGroup.scopeAliasMap[alias] = scopeConfig
                        }
                    }
                }
                key == "scope_groups" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val scopeGroupConfig = CwtScopeGroupConfig.resolve(config) ?: continue
                        configGroup.scopeGroups[scopeGroupConfig.name] = scopeGroupConfig
                    }
                }
                key == "database_object_types" -> {
                    val configs = property.properties ?: continue
                    for(config in configs) {
                        val databaseObjectTypeConfig = CwtDatabaseObjectTypeConfig.resolve(config) ?: continue
                        configGroup.databaseObjectTypes[databaseObjectTypeConfig.name] = databaseObjectTypeConfig
                    }
                }
                key == "scripted_variables" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val scriptedVariableConfig = CwtExtendedScriptedVariableConfig.resolve(config) ?: continue
                        configGroup.extendedScriptedVariables[scriptedVariableConfig.name] = scriptedVariableConfig
                    }
                }
                key == "definitions" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val definitionConfig = CwtExtendedDefinitionConfig.resolve(config) ?: continue
                        configGroup.extendedDefinitions.getOrPut(definitionConfig.name) { mutableListOf() } += definitionConfig
                    }
                }
                key == "game_rules" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val gameRuleConfig = CwtExtendedGameRuleConfig.resolve(config)
                        configGroup.extendedGameRules[gameRuleConfig.name] = gameRuleConfig
                    }
                }
                key == "on_actions" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val onActionConfig = CwtExtendedOnActionConfig.resolve(config) ?: continue
                        configGroup.extendedOnActions[onActionConfig.name] = onActionConfig
                    }
                }
                key == "inline_scripts" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val inlineScriptConfig = CwtExtendedInlineScriptConfig.resolve(config)
                        configGroup.extendedInlineScripts[inlineScriptConfig.name] = inlineScriptConfig
                    }
                }
                key == "parameters" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val parameterConfig = CwtExtendedParameterConfig.resolve(config) ?: continue
                        configGroup.extendedParameters.getOrInit(parameterConfig.name) += parameterConfig
                    }
                }
                key == "complex_enum_values" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        if(config !is CwtPropertyConfig) continue
                        val type = config.key
                        val configs1 = config.configs ?: continue
                        for(config1 in configs1) {
                            val complexEnumValueConfig = CwtExtendedComplexEnumValueConfig.resolve(config1, type)
                            configGroup.extendedComplexEnumValues.getOrInit(type)[complexEnumValueConfig.name] = complexEnumValueConfig
                        }
                    }
                }
                key == "dynamic_values" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        if(config !is CwtPropertyConfig) continue
                        val type = config.key
                        val configs1 = config.configs ?: continue
                        for(config1 in configs1) {
                            val dynamicValueConfig = CwtExtendedDynamicValueConfig.resolve(config1, type)
                            configGroup.extendedDynamicValues.getOrInit(type)[dynamicValueConfig.name] = dynamicValueConfig
                        }
                    }
                }
                else -> {
                    run {
                        val singleAliasConfig = CwtSingleAliasConfig.resolve(property) ?: return@run
                        configGroup.singleAliases[singleAliasConfig.name] = singleAliasConfig
                    }
                    run {
                        val aliasConfig = CwtAliasConfig.resolve(property) ?: return@run
                        CwtConfigCollector.processConfigWithConfigExpression(aliasConfig, aliasConfig.expression)
                        configGroup.aliasGroups.getOrInit(aliasConfig.name).getOrInit(aliasConfig.subName) += aliasConfig
                    }
                    run {
                        val inlineConfig = CwtInlineConfig.resolve(property) ?: return@run
                        configGroup.inlineConfigGroup.getOrInit(inlineConfig.name) += inlineConfig
                    }
                    run {
                        val declarationConfig = CwtDeclarationConfig.resolve(property) ?: return@run
                        configGroup.declarations[key] = declarationConfig
                    }
                }
            }
        }
    }
}
