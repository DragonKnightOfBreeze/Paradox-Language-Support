package icu.windea.pls.ep.configGroup

import com.intellij.openapi.application.readAction
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtFileConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.delegated.CwtAliasConfig
import icu.windea.pls.config.config.delegated.CwtComplexEnumConfig
import icu.windea.pls.config.config.delegated.CwtDatabaseObjectTypeConfig
import icu.windea.pls.config.config.delegated.CwtDeclarationConfig
import icu.windea.pls.config.config.delegated.CwtDynamicValueTypeConfig
import icu.windea.pls.config.config.delegated.CwtEnumConfig
import icu.windea.pls.config.config.delegated.CwtExtendedComplexEnumValueConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDefinitionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedDynamicValueConfig
import icu.windea.pls.config.config.delegated.CwtExtendedGameRuleConfig
import icu.windea.pls.config.config.delegated.CwtExtendedInlineScriptConfig
import icu.windea.pls.config.config.delegated.CwtExtendedOnActionConfig
import icu.windea.pls.config.config.delegated.CwtExtendedParameterConfig
import icu.windea.pls.config.config.delegated.CwtExtendedScriptedVariableConfig
import icu.windea.pls.config.config.delegated.CwtInlineConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.config.delegated.CwtLocaleConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationCommandConfig
import icu.windea.pls.config.config.delegated.CwtLocalisationPromotionConfig
import icu.windea.pls.config.config.delegated.CwtModifierCategoryConfig
import icu.windea.pls.config.config.delegated.CwtModifierConfig
import icu.windea.pls.config.config.delegated.CwtRowConfig
import icu.windea.pls.config.config.delegated.CwtScopeConfig
import icu.windea.pls.config.config.delegated.CwtScopeGroupConfig
import icu.windea.pls.config.config.delegated.CwtSingleAliasConfig
import icu.windea.pls.config.config.delegated.CwtSystemScopeConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.config.config.internal.CwtFoldingSettingsConfig
import icu.windea.pls.config.config.internal.CwtPostfixTemplateSettingsConfig
import icu.windea.pls.config.config.internal.CwtSchemaConfig
import icu.windea.pls.config.config.properties
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.configGroup.aliasGroups
import icu.windea.pls.config.configGroup.complexEnums
import icu.windea.pls.config.configGroup.databaseObjectTypes
import icu.windea.pls.config.configGroup.declarations
import icu.windea.pls.config.configGroup.dynamicValueTypes
import icu.windea.pls.config.configGroup.enums
import icu.windea.pls.config.configGroup.extendedComplexEnumValues
import icu.windea.pls.config.configGroup.extendedDefinitions
import icu.windea.pls.config.configGroup.extendedDynamicValues
import icu.windea.pls.config.configGroup.extendedGameRules
import icu.windea.pls.config.configGroup.extendedInlineScripts
import icu.windea.pls.config.configGroup.extendedOnActions
import icu.windea.pls.config.configGroup.extendedParameters
import icu.windea.pls.config.configGroup.extendedScriptedVariables
import icu.windea.pls.config.configGroup.inlineConfigGroup
import icu.windea.pls.config.configGroup.links
import icu.windea.pls.config.configGroup.localisationCommands
import icu.windea.pls.config.configGroup.localisationLinks
import icu.windea.pls.config.configGroup.localisationLocalesByCode
import icu.windea.pls.config.configGroup.localisationLocalesById
import icu.windea.pls.config.configGroup.localisationPromotions
import icu.windea.pls.config.configGroup.modifierCategories
import icu.windea.pls.config.configGroup.modifiers
import icu.windea.pls.config.configGroup.priorities
import icu.windea.pls.config.configGroup.rows
import icu.windea.pls.config.configGroup.scopeAliasMap
import icu.windea.pls.config.configGroup.scopeGroups
import icu.windea.pls.config.configGroup.scopes
import icu.windea.pls.config.configGroup.singleAliases
import icu.windea.pls.config.configGroup.systemScopes
import icu.windea.pls.config.configGroup.type2ModifiersMap
import icu.windea.pls.config.configGroup.types
import icu.windea.pls.config.util.CwtConfigCollector
import icu.windea.pls.config.util.CwtConfigFileResolver
import icu.windea.pls.core.collections.getOrInit
import icu.windea.pls.core.normalizePath
import icu.windea.pls.core.orNull
import icu.windea.pls.core.toPsiFile
import icu.windea.pls.cwt.psi.CwtFile
import icu.windea.pls.ep.priority.ParadoxPriority

/**
 * 用于初始规则分组中基于文件内容的那些数据。
 */
class FileBasedCwtConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override suspend fun process(configGroup: CwtConfigGroup): Boolean {
        // 按照文件路径（相对于规则分组的根目录）正序读取所有规则文件
        // 后加入的规则文件会覆盖先加入的同路径的规则文件
        // 后加入的数据项会覆盖先加入的同名同类型的数据项

        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        val fileProvidersAndRootDirectories = mutableMapOf<CwtConfigGroupFileProvider, VirtualFile>()
        readAction {
            fileProviders.forEach f@{ fileProvider ->
                val rootDirectory = fileProvider.getRootDirectory(configGroup.project) ?: return@f
                fileProvidersAndRootDirectories.put(fileProvider, rootDirectory)
            }
        }

        val allInternalFiles = mutableMapOf<String, VirtualFile>()
        val allFiles = mutableMapOf<String, VirtualFile>()
        readAction {
            fileProvidersAndRootDirectories.all { (fileProvider, rootDirectory) ->
                fileProvider.processFiles(configGroup, rootDirectory) p@{ filePath, file ->
                    if (filePath.startsWith("internal/")) {
                        if (fileProvider.type != CwtConfigGroupFileProvider.Type.BuiltIn) return@p true //不允许覆盖内部规则文件
                        allInternalFiles.putIfAbsent(filePath, file)
                        return@p true
                    }
                    allFiles.put(filePath, file)
                    true
                }
            }
            allInternalFiles.forEach f@{ (filePath, file) ->
                val psiFile = file.toPsiFile(configGroup.project) as? CwtFile ?: return@f
                val fileConfig = CwtConfigFileResolver.resolve(psiFile, configGroup)
                processInternalFile(filePath, fileConfig, configGroup)
            }
            allFiles.forEach f@{ (_, file) ->
                val psiFile = file.toPsiFile(configGroup.project) as? CwtFile ?: return@f
                val fileConfig = CwtConfigFileResolver.resolve(psiFile, configGroup)
                processFile(fileConfig, configGroup)
                //configGroup.files[filePath] = fileConfig // TODO 2.0.0-dev+ 目前并不需要缓存文件规则
            }
        }

        return true
    }

    private fun processInternalFile(filePath: String, fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        when (filePath) {
            "internal/schema.cwt" -> CwtSchemaConfig.resolveInFile(fileConfig, configGroup)
            "internal/folding_settings.cwt" -> CwtFoldingSettingsConfig.resolveInFile(fileConfig, configGroup)
            "internal/postfix_template_settings.cwt" -> CwtPostfixTemplateSettingsConfig.resolveInFile(fileConfig, configGroup)
        }
    }

    private fun processFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        for (property in fileConfig.properties) {
            val key = property.key
            when {
                key == "priorities" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val k = config.key.removePrefix("game/").normalizePath().orNull() ?: continue
                        val v = config.stringValue?.orNull()?.let { ParadoxPriority.resolve(it) } ?: continue
                        configGroup.priorities[k] = v
                    }
                }
                key == "system_scopes" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val systemScopeConfig = CwtSystemScopeConfig.resolve(config)
                        configGroup.systemScopes[systemScopeConfig.id] = systemScopeConfig
                    }
                }
                key == "locales" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val localeConfig = CwtLocaleConfig.resolve(config)
                        configGroup.localisationLocalesById[localeConfig.id] = localeConfig
                        localeConfig.codes.forEach { code ->
                            configGroup.localisationLocalesByCode[code] = localeConfig
                        }
                    }
                }
                key == "types" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val typeConfig = CwtTypeConfig.resolve(config) ?: continue
                        configGroup.types[typeConfig.name] = typeConfig
                    }
                }
                key == "rows" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val rowConfig = CwtRowConfig.resolve(config) ?: continue
                        configGroup.rows[rowConfig.name] = rowConfig
                    }
                }
                key == "enums" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
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
                    for (config in configs) {
                        val dynamicValueTypeConfig = CwtDynamicValueTypeConfig.resolve(config) ?: continue
                        configGroup.dynamicValueTypes[dynamicValueTypeConfig.name] = dynamicValueTypeConfig
                    }
                }
                key == "links" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val linkConfig = CwtLinkConfig.resolve(config) ?: continue
                        configGroup.links[linkConfig.name] = linkConfig
                    }
                }
                key == "localisation_links" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val linkConfig = CwtLinkConfig.resolveForLocalisation(config) ?: continue
                        configGroup.localisationLinks[linkConfig.name] = linkConfig
                    }
                }
                key == "localisation_promotions" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val localisationPromotionConfig = CwtLocalisationPromotionConfig.resolve(config)
                        configGroup.localisationPromotions[localisationPromotionConfig.name] = localisationPromotionConfig
                    }
                }
                key == "localisation_commands" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val localisationCommandConfig = CwtLocalisationCommandConfig.resolve(config)
                        configGroup.localisationCommands[localisationCommandConfig.name] = localisationCommandConfig
                    }
                }
                key == "modifier_categories" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val modifierCategoryConfig = CwtModifierCategoryConfig.resolve(config) ?: continue
                        configGroup.modifierCategories[modifierCategoryConfig.name] = modifierCategoryConfig
                    }
                }
                key == "modifiers" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val modifierName = config.key
                        val modifierConfig = CwtModifierConfig.resolve(config, modifierName) ?: continue
                        configGroup.modifiers[modifierName] = modifierConfig
                        for (snippetExpression in modifierConfig.template.snippetExpressions) {
                            if (snippetExpression.type == CwtDataTypes.Definition) {
                                val typeExpression = snippetExpression.value ?: continue
                                configGroup.type2ModifiersMap.getOrPut(typeExpression) { mutableMapOf() }[modifierName] = modifierConfig
                            }
                        }
                    }
                }
                key == "scopes" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val scopeConfig = CwtScopeConfig.resolve(config) ?: continue
                        configGroup.scopes[scopeConfig.name] = scopeConfig
                        for (alias in scopeConfig.aliases) {
                            configGroup.scopeAliasMap[alias] = scopeConfig
                        }
                    }
                }
                key == "scope_groups" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val scopeGroupConfig = CwtScopeGroupConfig.resolve(config) ?: continue
                        configGroup.scopeGroups[scopeGroupConfig.name] = scopeGroupConfig
                    }
                }
                key == "database_object_types" -> {
                    val configs = property.properties ?: continue
                    for (config in configs) {
                        val databaseObjectTypeConfig = CwtDatabaseObjectTypeConfig.resolve(config) ?: continue
                        configGroup.databaseObjectTypes[databaseObjectTypeConfig.name] = databaseObjectTypeConfig
                    }
                }
                key == "scripted_variables" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val scriptedVariableConfig = CwtExtendedScriptedVariableConfig.resolve(config) ?: continue
                        configGroup.extendedScriptedVariables[scriptedVariableConfig.name] = scriptedVariableConfig
                    }
                }
                key == "definitions" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val definitionConfig = CwtExtendedDefinitionConfig.resolve(config) ?: continue
                        configGroup.extendedDefinitions.getOrPut(definitionConfig.name) { mutableListOf() } += definitionConfig
                    }
                }
                key == "game_rules" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val gameRuleConfig = CwtExtendedGameRuleConfig.resolve(config)
                        configGroup.extendedGameRules[gameRuleConfig.name] = gameRuleConfig
                    }
                }
                key == "on_actions" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val onActionConfig = CwtExtendedOnActionConfig.resolve(config) ?: continue
                        configGroup.extendedOnActions[onActionConfig.name] = onActionConfig
                    }
                }
                key == "inline_scripts" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val inlineScriptConfig = CwtExtendedInlineScriptConfig.resolve(config)
                        configGroup.extendedInlineScripts[inlineScriptConfig.name] = inlineScriptConfig
                    }
                }
                key == "parameters" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        val parameterConfig = CwtExtendedParameterConfig.resolve(config) ?: continue
                        configGroup.extendedParameters.getOrInit(parameterConfig.name) += parameterConfig
                    }
                }
                key == "complex_enum_values" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        if (config !is CwtPropertyConfig) continue
                        val type = config.key
                        val configs1 = config.configs ?: continue
                        for (config1 in configs1) {
                            val complexEnumValueConfig = CwtExtendedComplexEnumValueConfig.resolve(config1, type)
                            configGroup.extendedComplexEnumValues.getOrInit(type)[complexEnumValueConfig.name] = complexEnumValueConfig
                        }
                    }
                }
                key == "dynamic_values" -> {
                    val configs = property.configs ?: continue
                    for (config in configs) {
                        if (config !is CwtPropertyConfig) continue
                        val type = config.key
                        val configs1 = config.configs ?: continue
                        for (config1 in configs1) {
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
                        CwtConfigCollector.processConfigWithConfigExpression(aliasConfig, aliasConfig.configExpression)
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
