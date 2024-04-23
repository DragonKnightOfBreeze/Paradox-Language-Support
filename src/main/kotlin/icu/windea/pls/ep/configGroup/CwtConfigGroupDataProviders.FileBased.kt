package icu.windea.pls.ep.configGroup

import com.intellij.openapi.vfs.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.config.extended.*
import icu.windea.pls.config.config.settings.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 用于初始CWT规则分组中基于文件内容的那些数据。
 */
class FileBasedCwtConfigGroupDataProvider : CwtConfigGroupDataProvider {
    override fun process(configGroup: CwtConfigGroup): Boolean {
        //按照文件路径（相对于规则分组的根目录）正序读取所有规则文件
        //后加入的规则文件会覆盖先加入的同路径的规则文件
        //后加入的数据项会覆盖先加入的同名同类型的数据项
        
        val allFiles = mutableMapOf<String, Tuple2<VirtualFile, CwtConfigGroupFileProvider>>()
        val fileProviders = CwtConfigGroupFileProvider.EP_NAME.extensionList
        fileProviders.all f@{ fileProvider ->
            fileProvider.processFiles(configGroup) { filePath, file ->
                allFiles[filePath] = tupleOf(file, fileProvider)
                true
            }
        }
        
        allFiles.all f@{ (filePath, tuple) ->
            val (file, fileProcessor) = tuple
            processFile(filePath, file, fileProcessor, configGroup)
        }
        
        return true
    }
    
    private fun processFile(filePath: String, file: VirtualFile, fileProcessor: CwtConfigGroupFileProvider, configGroup: CwtConfigGroup): Boolean {
        val psiFile = file.toPsiFile(configGroup.project) as? CwtFile ?: return true
        val fileConfig = CwtConfigResolver.resolve(psiFile, configGroup)
        configGroup.files[filePath] = fileConfig
        if(fileProcessor.isBuiltIn()) doProcessBuiltInFile(filePath, fileConfig, configGroup)
        doProcessFile(fileConfig, configGroup)
        return true
    }
    
    private fun doProcessBuiltInFile(filePath: String, fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        when(filePath) {
            "settings/folding_settings.pls.cwt" -> resolveFoldingSettingsInFile(fileConfig, configGroup)
            "settings/postfix_template_settings.pls.cwt" -> resolvePostfixTemplateSettingsInFile(fileConfig, configGroup)
            "system_links.pls.cwt" -> resolveSystemLinks(fileConfig, configGroup)
            "localisation_locales.pls.cwt" -> resolveLocalisationLocalesInFile(fileConfig, configGroup)
            "localisation_predefined_parameters.pls.cwt" -> resolveLocalisationPredefinedParametersInFile(fileConfig, configGroup)
        }
    }
    
    private fun resolveFoldingSettingsInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties
        configs.forEach { groupProperty ->
            val groupName = groupProperty.key
            val map = caseInsensitiveStringKeyMap<CwtFoldingSettingsConfig>()
            groupProperty.properties?.forEach { property ->
                val id = property.key
                var key: String? = null
                var keys: List<String>? = null
                var placeholder: String? = null
                property.properties?.forEach { prop ->
                    when {
                        prop.key == "key" -> key = prop.stringValue
                        prop.key == "keys" -> keys = prop.values?.mapNotNull { it.stringValue }
                        prop.key == "placeholder" -> placeholder = prop.stringValue
                    }
                }
                if(placeholder != null) {
                    val foldingSetting = CwtFoldingSettingsConfig(id, key, keys, placeholder!!)
                    map.put(id, foldingSetting)
                }
            }
            configGroup.foldingSettings[groupName] = map
        }
    }
    
    private fun resolvePostfixTemplateSettingsInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties
        configs.forEach { groupProperty ->
            val groupName = groupProperty.key
            val map = caseInsensitiveStringKeyMap<CwtPostfixTemplateSettingsConfig>()
            groupProperty.properties?.forEach { property ->
                val id = property.key
                var key: String? = null
                var example: String? = null
                var variables: Map<String, String>? = null
                var expression: String? = null
                property.properties?.forEach { prop ->
                    when {
                        prop.key == "key" -> key = prop.stringValue
                        prop.key == "example" -> example = prop.stringValue
                        prop.key == "variables" -> variables = prop.properties?.let {
                            buildMap {
                                it.forEach { p -> put(p.key, p.value) }
                            }
                        }
                        prop.key == "expression" -> expression = prop.stringValue
                    }
                }
                if(key != null && expression != null) {
                    val foldingSetting = CwtPostfixTemplateSettingsConfig(id, key!!, example, variables.orEmpty(), expression!!)
                    map.put(id, foldingSetting)
                }
            }
            configGroup.postfixTemplateSettings[groupName] = map
        }
    }
    
    private fun resolveSystemLinks(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties.find { it.key == "system_links" }?.properties ?: return
        configs.forEach { property ->
            val systemLinkConfig = CwtSystemLinkConfig.resolve(property)
            configGroup.systemLinks[systemLinkConfig.id] = systemLinkConfig
        }
    }
    
    private fun resolveLocalisationLocalesInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties.find { it.key == "localisation_locales" }?.properties ?: return
        configs.forEach { property ->
            val localisationLocaleConfig = CwtLocalisationLocaleConfig.resolve(property)
            configGroup.localisationLocalesById[localisationLocaleConfig.id] = localisationLocaleConfig
            localisationLocaleConfig.codes.forEach { code ->
                configGroup.localisationLocalesByCode[code] = localisationLocaleConfig
            }
        }
    }
    
    private fun resolveLocalisationPredefinedParametersInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties.find { it.key == "localisation_predefined_parameters" }?.properties ?: return
        configs.forEach { property ->
            val localisationPredefinedParameterConfig = CwtLocalisationPredefinedParameterConfig.resolve(property)
            configGroup.localisationPredefinedParameters[localisationPredefinedParameterConfig.id] = localisationPredefinedParameterConfig
        }
    }
    
    private fun doProcessFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        when(fileConfig.name) {
            //解析要将其中的文件识别为脚本文件的目录列表 - 仅作记录，插件目前并不这个目录列表来判断是否要将文件识别为脚本文件
            "folders.cwt" -> resolveFoldersInFile(fileConfig, configGroup)
            //对于其他情况，不限制文件名，统一处理
            else -> resolveOthersInFile(fileConfig, configGroup)
        }
    }
    
    private fun resolveFoldersInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        fileConfig.values.mapTo(configGroup.folders) { it.value }
    }
    
    private fun resolveOthersInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        for(property in fileConfig.properties) {
            val key = property.key
            when {
                //找到配置文件中的顶级的key为"types"的属性，然后解析它的子属性，添加到types中
                key == "types" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val typeConfig = CwtTypeConfig.resolve(prop) ?: continue
                        configGroup.types[typeConfig.name] = typeConfig
                    }
                }
                //找到配置文件中的顶级的key为"enums"的属性，然后解析它的子属性，添加到enums和complexEnums中
                key == "enums" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        run {
                            val enumConfig = CwtEnumConfig.resolve(prop) ?: return@run
                            configGroup.enums[enumConfig.name] = enumConfig
                        }
                        
                        run {
                            val complexEnumConfig = CwtComplexEnumConfig.resolve(prop) ?: return@run
                            configGroup.complexEnums[complexEnumConfig.name] = complexEnumConfig
                        }
                    }
                }
                key == "values" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val dynamicValueTypeConfig = CwtDynamicValueTypeConfig.resolve(prop) ?: continue
                        configGroup.dynamicValueTypes[dynamicValueTypeConfig.name] = dynamicValueTypeConfig
                    }
                }
                key == "links" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val linkConfig = CwtLinkConfig.resolve(prop) ?: continue
                        configGroup.links[linkConfig.name] = linkConfig
                        
                        //要求data_source存在
                        val fromData = linkConfig.fromData && linkConfig.dataSource != null
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
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val localisationLinkConfig = CwtLocalisationLinkConfig.resolve(prop) ?: continue
                        configGroup.localisationLinks[localisationLinkConfig.name] = localisationLinkConfig
                    }
                }
                key == "localisation_commands" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val localisationCommandConfig = CwtLocalisationCommandConfig.resolve(prop)
                        configGroup.localisationCommands[localisationCommandConfig.name] = localisationCommandConfig
                    }
                }
                key == "modifier_categories" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val modifierCategoryConfig = CwtModifierCategoryConfig.resolve(prop) ?: continue
                        configGroup.modifierCategories[modifierCategoryConfig.name] = modifierCategoryConfig
                    }
                }
                key == "modifiers" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val modifierName = prop.key
                        val modifierConfig = CwtModifierConfig.resolve(prop, modifierName) ?: continue
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
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val scopeConfig = CwtScopeConfig.resolve(prop) ?: continue
                        configGroup.scopes[scopeConfig.name] = scopeConfig
                        for(alias in scopeConfig.aliases) {
                            configGroup.scopeAliasMap[alias] = scopeConfig
                        }
                    }
                }
                key == "scope_groups" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val scopeGroupName = prop.key
                        val scopeGroupConfig = CwtScopeGroupConfig.resolve(prop) ?: continue
                        configGroup.scopeGroups[scopeGroupName] = scopeGroupConfig
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
                        val gameRuleConfig = CwtGameRuleConfig.resolve(config)
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
