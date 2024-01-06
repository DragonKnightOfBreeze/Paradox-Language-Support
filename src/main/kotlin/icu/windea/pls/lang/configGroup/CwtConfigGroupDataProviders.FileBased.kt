package icu.windea.pls.lang.configGroup

import com.intellij.openapi.vfs.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.settings.*
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
        val fileConfig = CwtConfigResolver.resolve(psiFile, configGroup.info)
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
            val map = caseInsensitiveStringKeyMap<CwtFoldingSettings>()
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
                    val foldingSetting = CwtFoldingSettings(id, key, keys, placeholder!!)
                    map.put(id, foldingSetting)
                }
            }
            configGroup.foldingSettings.asMutable()[groupName] = map
        }
    }
    
    private fun resolvePostfixTemplateSettingsInFile(fileConfig: CwtFileConfig,configGroup: CwtConfigGroup ) {
        val configs = fileConfig.properties
        configs.forEach { groupProperty ->
            val groupName = groupProperty.key
            val map = caseInsensitiveStringKeyMap<CwtPostfixTemplateSettings>()
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
                    val foldingSetting = CwtPostfixTemplateSettings(id, key!!, example, variables.orEmpty(), expression!!)
                    map.put(id, foldingSetting)
                }
            }
            configGroup.postfixTemplateSettings.asMutable()[groupName] = map
        }
    }
    
    private fun resolveSystemLinks(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties.find { it.key == "system_links" }?.properties ?: return
        configs.forEach { property ->
            val systemLinkConfig = CwtSystemLinkConfig.resolve(property)
            configGroup.systemLinks.asMutable()[systemLinkConfig.id] = systemLinkConfig
        }
    }
    
    private fun resolveLocalisationLocalesInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties.find { it.key == "localisation_locales" }?.properties ?: return
        configs.forEach { property ->
            val localisationLocaleConfig = CwtLocalisationLocaleConfig.resolve(property)
            configGroup.localisationLocalesById.asMutable()[localisationLocaleConfig.id] = localisationLocaleConfig
            localisationLocaleConfig.codes.forEach { code ->
                configGroup.localisationLocalesByCode.asMutable()[code] = localisationLocaleConfig
            }
        }
    }
    
    private fun resolveLocalisationPredefinedParametersInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        val configs = fileConfig.properties.find { it.key == "localisation_predefined_parameters" }?.properties ?: return
        configs.forEach { property ->
            val localisationPredefinedParameterConfig = CwtLocalisationPredefinedParameterConfig.resolve(property)
            configGroup.localisationPredefinedParameters.asMutable()[localisationPredefinedParameterConfig.id] = localisationPredefinedParameterConfig
        }
    }
    
    private fun doProcessFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        when(fileConfig.key) {
            //解析要将其中的文件识别为脚本文件的目录列表 - 仅作记录，插件目前并不这个目录列表来判断是否要将文件识别为脚本文件
            "folders" -> resolveFoldersInFile(fileConfig, configGroup)
            //对于其他情况，不限制文件名，统一处理
            else -> resolveOthersInFile(fileConfig, configGroup)
        }
    }
    
    private fun resolveFoldersInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        fileConfig.values.mapTo(configGroup.folders.asMutable()) { it.value }
    }
    
    private fun resolveOthersInFile(fileConfig: CwtFileConfig, configGroup: CwtConfigGroup) {
        for(property in fileConfig.properties) {
            val key = property.key
            when {
                //找到配置文件中的顶级的key为"types"的属性，然后解析它的子属性，添加到types中
                key == "types" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val typeName = prop.key.removeSurroundingOrNull("type[", "]")
                        if(!typeName.isNullOrEmpty()) {
                            val typeConfig = CwtTypeConfig.resolve(prop, typeName)
                            configGroup.types.asMutable()[typeName] = typeConfig
                        }
                    }
                }
                //找到配置文件中的顶级的key为"enums"的属性，然后解析它的子属性，添加到enums和complexEnums中
                key == "enums" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        //TODO enumName may be a template expression (e.g. xxx_<xxx>)
                        val enumName = prop.key.removeSurroundingOrNull("enum[", "]")
                        if(!enumName.isNullOrEmpty()) {
                            val enumConfig = CwtEnumConfig.resolve(prop, enumName) ?: continue
                            configGroup.enums.asMutable()[enumName] = enumConfig
                        }
                        val complexEnumName = prop.key.removeSurroundingOrNull("complex_enum[", "]")
                        if(!complexEnumName.isNullOrEmpty()) {
                            val complexEnumConfig = CwtComplexEnumConfig.resolve(prop, complexEnumName) ?: continue
                            configGroup.complexEnums.asMutable()[complexEnumName] = complexEnumConfig
                        }
                    }
                }
                key == "links" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val linkName = prop.key
                        val linkConfig = CwtLinkConfig.resolve(prop, linkName) ?: continue
                        configGroup.links.asMutable()[linkName] = linkConfig
                        //要求data_source存在
                        val fromData = linkConfig.fromData && linkConfig.dataSource != null
                        val withPrefix = linkConfig.prefix != null
                        val type = linkConfig.type
                        if(type == null || type == "scope" || type == "both") {
                            when {
                                !fromData -> configGroup.linksAsScopeNotData.asMutable()[linkName] = linkConfig
                                withPrefix -> configGroup.linksAsScopeWithPrefix.asMutable()[linkName] = linkConfig
                                else -> configGroup.linksAsScopeWithoutPrefix.asMutable()[linkName] = linkConfig
                            }
                        }
                        if(type == "value" || type == "both") {
                            when {
                                !fromData -> configGroup.linksAsValueNotData.asMutable()[linkName] = linkConfig
                                withPrefix -> configGroup.linksAsValueWithPrefix.asMutable()[linkName] = linkConfig
                                else -> configGroup.linksAsValueWithoutPrefix.asMutable()[linkName] = linkConfig
                            }
                        }
                    }
                }
                key == "localisation_links" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val linkName = prop.key
                        val localisationLinkConfig = CwtLocalisationLinkConfig.resolve(prop, linkName) ?: continue
                        configGroup.localisationLinks.asMutable()[linkName] = localisationLinkConfig
                    }
                }
                key == "localisation_commands" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val commandName = prop.key
                        val localisationCommandConfig = CwtLocalisationCommandConfig.resolve(prop, commandName)
                        configGroup.localisationCommands.asMutable()[commandName] = localisationCommandConfig
                    }
                }
                key == "modifier_categories" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val modifierCategoryName = prop.key
                        val modifierCategoryConfig = CwtModifierCategoryConfig.resolve(prop, modifierCategoryName) ?: continue
                        configGroup.modifierCategories.asMutable()[modifierCategoryName] = modifierCategoryConfig
                    }
                }
                key == "modifiers" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val modifierName = prop.key
                        val modifierConfig = CwtModifierConfig.resolve(prop, modifierName) ?: continue
                        configGroup.modifiers.asMutable()[modifierName] = modifierConfig
                        for(snippetExpression in modifierConfig.template.snippetExpressions) {
                            if(snippetExpression.type == CwtDataTypes.Definition) {
                                val typeExpression = snippetExpression.value ?: continue
                                configGroup.type2ModifiersMap.asMutable().getOrPut(typeExpression) { mutableMapOf() }.asMutable()[modifierName] = modifierConfig
                            }
                        }
                    }
                }
                key == "scopes" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val scopeName = prop.key
                        val scopeConfig = CwtScopeConfig.resolve(prop, scopeName) ?: continue
                        configGroup.scopes.asMutable()[scopeName] = scopeConfig
                        for(alias in scopeConfig.aliases) {
                            configGroup.scopeAliasMap.asMutable()[alias] = scopeConfig
                        }
                    }
                }
                key == "scope_groups" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        val scopeGroupName = prop.key
                        val scopeGroupConfig = CwtScopeGroupConfig.resolve(prop, scopeGroupName) ?: continue
                        configGroup.scopeGroups.asMutable()[scopeGroupName] = scopeGroupConfig
                    }
                }
                key == "definitions" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val definitionConfig = CwtDefinitionConfig.resolve(config) ?: continue
                        configGroup.definitions.asMutable().getOrPut(definitionConfig.name) { mutableListOf() }.asMutable() += definitionConfig
                    }
                }
                key == "game_rules" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val gameRuleConfig = CwtGameRuleConfig.resolve(config)
                        configGroup.gameRules.asMutable()[gameRuleConfig.name] = gameRuleConfig
                    }
                }
                key == "on_actions" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val onActionConfig = CwtOnActionConfig.resolve(config) ?: continue
                        configGroup.onActions.asMutable()[onActionConfig.name] = onActionConfig
                    }
                }
                key == "values" -> {
                    val props = property.properties ?: continue
                    for(prop in props) {
                        //TODO valueName may be a template expression (e.g. xxx_<xxx>)
                        val dynamicValueName = prop.key.removeSurroundingOrNull("value[", "]")
                        if(!dynamicValueName.isNullOrEmpty()) {
                            val valueConfig = CwtDynamicValueConfig.resolve(prop, dynamicValueName) ?: continue
                            configGroup.dynamicValues.asMutable()[dynamicValueName] = valueConfig
                        }
                    }
                }
                key == "parameters" -> {
                    val configs = property.configs ?: continue
                    for(config in configs) {
                        val parameterConfig = CwtParameterConfig.resolve(config) ?: continue
                        configGroup.parameters.asMutable().getOrPut(parameterConfig.name) { mutableListOf() }.asMutable() += parameterConfig
                    }
                }
                else -> {
                    //判断配置文件中的顶级的key是否匹配"single_alias[?]"，如果匹配，则解析配置并添加到singleAliases中
                    val singleAliasName = key.removeSurroundingOrNull("single_alias[", "]")
                    if(singleAliasName != null) {
                        val singleAliasConfig = CwtSingleAliasConfig.resolve(property, singleAliasName)
                        configGroup.singleAliases.asMutable()[singleAliasName] = singleAliasConfig
                        continue
                    }
                    
                    //判断配置文件中的顶级的key是否匹配"alias[?:?]"，如果匹配，则解析配置并添加到aliasGroups中
                    val aliasTokens = key.removeSurroundingOrNull("alias[", "]")?.split(':', limit = 2)?.takeIf { it.size == 2 }
                    if(aliasTokens != null) {
                        val (aliasName, aliasSubName) = aliasTokens
                        val aliasConfig = CwtAliasConfig.resolve(property, aliasName, aliasSubName)
                        //目前不这样处理
                        //if(aliasConfig.name == "modifier" && aliasConfig.expression.type.isConstantLikeType()) {
                        //	val modifierConfig = resolveModifierConfigFromAliasConfig(aliasConfig)
                        //	modifiers.asMutable()[modifierConfig.name] = modifierConfig
                        //	continue
                        //} 
                        val map = configGroup.aliasGroups.asMutable().getOrPut(aliasName) { mutableMapOf() }
                        val list = map.asMutable().getOrPut(aliasSubName) { mutableListOf() }.asMutable()
                        list.add(aliasConfig)
                        continue
                    }
                    
                    //判断配置文件中的顶级的key是否匹配"inline[?]"，如果匹配，则解析配置并添加到inlineConfigGroup中
                    val inlineConfigName = key.removeSurroundingOrNull("inline[", "]")
                    if(inlineConfigName != null) {
                        val inlineConfig = CwtInlineConfig.resolve(property, inlineConfigName)
                        val list = configGroup.inlineConfigGroup.asMutable().getOrPut(inlineConfigName) { mutableListOf() }.asMutable()
                        list.add(inlineConfig)
                        continue
                    }
                    
                    //其他情况，放到declarations中
                    val declarationConfig = CwtDeclarationConfig.resolve(property, key)
                    configGroup.declarations.asMutable()[key] = declarationConfig
                }
            }
        }
    }
}
