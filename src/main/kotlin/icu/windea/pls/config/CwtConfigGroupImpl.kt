package icu.windea.pls.config

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.vfs.*
import com.intellij.util.*
import com.intellij.util.containers.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.config.setting.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.model.*
import kotlin.collections.isNullOrEmpty
import kotlin.collections.mapNotNullTo

class CwtConfigGroupImpl(
	override val project: Project,
	override val gameType: ParadoxGameType?,
	override val info: CwtConfigGroupInfo,
	fileGroup: MutableMap<String, VirtualFile>
) : CwtConfigGroup {
	override val foldingSettings: MutableMap<String, MutableMap<String, CwtFoldingSetting>> = mutableMapOf()
	override val postfixTemplateSettings: MutableMap<String, MutableMap<String, CwtPostfixTemplateSetting>> = mutableMapOf()
	
	override val systemLinks: MutableMap<@CaseInsensitive String, CwtSystemLinkConfig> = caseInsensitiveStringKeyMap()
	override val localisationLocales: MutableMap<String, CwtLocalisationLocaleConfig> = mutableMapOf()
	override val localisationLocalesNoDefault: MutableMap<String, CwtLocalisationLocaleConfig> = mutableMapOf()
	override val localisationLocalesNoDefaultNoPrefix: MutableMap<String, CwtLocalisationLocaleConfig> = mutableMapOf()
	override val localisationLocalesByCode: MutableMap<String, CwtLocalisationLocaleConfig> = mutableMapOf()
	override val localisationPredefinedParameters: MutableMap<String, CwtLocalisationPredefinedParameterConfig> = mutableMapOf()
	
	override val folders: MutableSet<String> = mutableSetOf()
	
	override val types: MutableMap<String, CwtTypeConfig> = mutableMapOf()
	override val typeToSwapTypeMap: BidirectionalMap<String, String> by lazy { 
		val map = BidirectionalMap<String, String>()
		for(typeConfig in types.values) {
			if(typeConfig.baseType != null) {
				map.put(typeConfig.baseType, typeConfig.name)
			}
		}
		map
	}
	override val typeToModifiersMap: MutableMap<String, MutableMap<String, CwtModifierConfig>> = mutableMapOf()
	override val declarations: MutableMap<String, CwtDeclarationConfig> = mutableMapOf()
	
	override val values: MutableMap<String, CwtEnumConfig> = mutableMapOf()
	//enumValue可以是int、float、bool类型，统一用字符串表示
	override val enums: MutableMap<String, CwtEnumConfig> = mutableMapOf()
	//基于enum_name进行定位，对应的可能是key/value
	override val complexEnums: MutableMap<String, CwtComplexEnumConfig> = mutableMapOf()
	
	override val links: MutableMap<@CaseInsensitive String, CwtLinkConfig> = caseInsensitiveStringKeyMap()
	override val linksAsScopeNotData: MutableMap<@CaseInsensitive String, CwtLinkConfig> = caseInsensitiveStringKeyMap()
	override val linksAsScopeWithPrefix: MutableMap<@CaseInsensitive String, CwtLinkConfig> = caseInsensitiveStringKeyMap()
	override val linksAsScopeWithoutPrefix: MutableMap<@CaseInsensitive String, CwtLinkConfig> = caseInsensitiveStringKeyMap()
	override val linksAsValueNotData: MutableMap<@CaseInsensitive String, CwtLinkConfig> = caseInsensitiveStringKeyMap()
	override val linksAsValueWithPrefix: MutableMap<@CaseInsensitive String, CwtLinkConfig> = caseInsensitiveStringKeyMap()
	override val linksAsValueWithoutPrefix: MutableMap<@CaseInsensitive String, CwtLinkConfig> = caseInsensitiveStringKeyMap()
	
	override val localisationLinks: MutableMap<@CaseInsensitive String, CwtLocalisationLinkConfig> = caseInsensitiveStringKeyMap()
	override val localisationCommands: MutableMap<@CaseInsensitive String, CwtLocalisationCommandConfig> = caseInsensitiveStringKeyMap()
	
	override val scopes: MutableMap<@CaseInsensitive String, CwtScopeConfig> = caseInsensitiveStringKeyMap()
	override val scopeAliasMap: MutableMap<@CaseInsensitive String, CwtScopeConfig> = caseInsensitiveStringKeyMap()
	override val scopeGroups: MutableMap<String, CwtScopeGroupConfig> = mutableMapOf()
	
	override val singleAliases: MutableMap<String, CwtSingleAliasConfig> = mutableMapOf()
	override val aliasGroups: MutableMap<String, MutableMap<String, MutableList<CwtAliasConfig>>> = mutableMapOf()
	override val inlineConfigGroup: MutableMap<String, MutableList<CwtInlineConfig>> = mutableMapOf()
	
	override val gameRules: MutableMap<String, CwtGameRuleConfig> = mutableMapOf()
	override val onActions: MutableMap<String, CwtOnActionConfig> = mutableMapOf()
	
	override val modifierCategories: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
	override val modifiers: MutableMap<@CaseInsensitive String, CwtModifierConfig> = caseInsensitiveStringKeyMap()
	override val generatedModifiers: Map<@CaseInsensitive String, CwtModifierConfig> by lazy {
		//put xxx_<xxx>_xxx before xxx_<xxx>
		modifiers.values
			.filter { it.template.isNotEmpty() }
			.sortedByDescending { it.template.snippetExpressions.size }
			.associateByTo(caseInsensitiveStringKeyMap()) { it.name }
	}
	override val predefinedModifiers: Map<@CaseInsensitive String, CwtModifierConfig> by lazy {
		modifiers.values
			.filter { it.template.isEmpty() }
			.associateByTo(caseInsensitiveStringKeyMap()) { it.name }
	}
	
	init {
		runReadAction {
			for(virtualFile in fileGroup.values) {
				var result: Boolean
				//val fileName = virtualFile.name
				val fileExtension = virtualFile.extension?.lowercase()
				when {
					fileExtension == "cwt" -> {
						val file = virtualFile.toPsiFile<CwtFile>(project) ?: continue
						val fileConfig =  CwtConfigResolver.resolve(file, info)
						
						result = resolveCwtSettingInCwtFile(fileConfig)
						if(!result) continue
						
						result = resolveExtendedCwtConfigInCwtFile(fileConfig)
						if(!result) continue
						
						resolveCwtConfigInCwtFile(fileConfig)
					}
				}
			}
		}
	}
	
	init {
		bindLinksToLocalisationLinks()
	}
	
	override val modifierCategoryIdMap: Map<String, CwtModifierCategoryConfig> = initModifierCategoryIdMap()
	
	init {
		bindModifierCategories()
	}
	
	override val aliasKeysGroupConst: MutableMap<String, Map<@CaseInsensitive String, String>> = mutableMapOf()
	override val aliasKeysGroupNoConst: MutableMap<String, Set<String>> = mutableMapOf()
	
	init {
		bindAliasKeysGroup()
	}
	
	override val linksAsScopeWithPrefixSorted: List<CwtLinkConfig> by lazy {
		linksAsScopeWithPrefix.values.sortedByPriority(this) { it.dataSource!! }
	}
	override val linksAsValueWithPrefixSorted: List<CwtLinkConfig> by lazy {
		linksAsValueWithPrefix.values.sortedByPriority(this) { it.dataSource!! }
	}
	override val linksAsScopeWithoutPrefixSorted: List<CwtLinkConfig> by lazy {
		linksAsScopeWithoutPrefix.values.sortedByPriority(this) { it.dataSource!! }
	}
	override val linksAsValueWithoutPrefixSorted: List<CwtLinkConfig> by lazy {
		linksAsValueWithoutPrefix.values.sortedByPriority(this) { it.dataSource!! }
	}
	override val linksAsVariable: List<CwtLinkConfig> by lazy { 
		linksAsValueWithoutPrefix["variable"].toSingletonListOrEmpty()
	}
	
	
	override val aliasNamesSupportScope: MutableSet<String> = mutableSetOf(
		"modifier", //也支持，但不能切换作用域
		"trigger",
		"effect",
		//其它的随后加入
	)
	override val definitionTypesSupportScope: MutableSet<String> = mutableSetOf(
		"scripted_effect",
		"scripted_trigger",
		"game_rule",
		"on_action", //也支持，其中调用的事件的类型要匹配
	)
	override val definitionTypesSkipCheckSystemLink: MutableSet<String> = mutableSetOf(
		"event",
		"scripted_trigger",
		"scripted_effect",
		"script_value",
		"game_rule",
	)
	override val definitionTypesSupportParameters: MutableSet<String> = mutableSetOf(
		"script_value", //SV也支持参数
		//"inline_script", //内联脚本也支持参数（并且可以表示多条语句）（但不是定义）
		//其它的随后加入
	)
	
	init {
		info.aliasNamesSupportScope.forEach { aliasNamesSupportScope.add(it) }
		info.parameterConfigs.forEach {
			val propertyConfig = it.parent as? CwtPropertyConfig ?: return@forEach
			val aliasSubName = propertyConfig.key.removeSurroundingOrNull("alias[", "]")?.substringAfter(':', "")
			val contextExpression = if(aliasSubName.isNullOrEmpty()) propertyConfig.keyExpression else CwtKeyExpression.resolve(aliasSubName)
			if(contextExpression.type == CwtDataType.Definition && contextExpression.value != null) {
				definitionTypesSupportParameters.add(contextExpression.value)
			}
		}
	}
	
	//解析CWT设置
	
	private fun resolveCwtSettingInCwtFile(fileConfig: CwtFileConfig): Boolean {
		when(fileConfig.key) {
			//解析代码折叠配置
			"folding_settings" -> {
				resolveFoldingSettings(fileConfig)
				return false
			}
			"postfix_template_settings" -> {
				resolvePostfixTemplateSettings(fileConfig)
				return false
			}
		}
		return true
	}
	
	private fun resolveFoldingSettings(fileConfig: CwtFileConfig) {
		val configs = fileConfig.properties
		configs.forEach { groupProperty ->
			val groupName = groupProperty.key
			val map = CollectionFactory.createCaseInsensitiveStringMap<CwtFoldingSetting>()
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
					val foldingSetting = CwtFoldingSetting(id, key, keys, placeholder!!)
					map.put(id, foldingSetting)
				}
			}
			foldingSettings.put(groupName, map)
		}
	}
	
	private fun resolvePostfixTemplateSettings(fileConfig: CwtFileConfig) {
		val configs = fileConfig.properties
		configs.forEach { groupProperty ->
			val groupName = groupProperty.key
			val map = CollectionFactory.createCaseInsensitiveStringMap<CwtPostfixTemplateSetting>()
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
								it.forEach { p ->
									if(p.stringValue != null) put(p.key, p.stringValue)
								}
							}
						}
						prop.key == "expression" -> expression = prop.stringValue
					}
				}
				if(key != null && expression != null) {
					val foldingSetting = CwtPostfixTemplateSetting(id, key!!, example, variables.orEmpty(), expression!!)
					map.put(id, foldingSetting)
				}
			}
			postfixTemplateSettings.put(groupName, map)
		}
	}
	
	//解析扩展的CWT规则
	
	private fun resolveExtendedCwtConfigInCwtFile(fileConfig: CwtFileConfig): Boolean {
		when(fileConfig.key) {
			//解析系统作用域规则
			"system_links" -> {
				resolveSystemLinks(fileConfig)
				return false
			}
			//解析本地化语言区域规则
			"localisation_locales" -> {
				resolveLocalisationLocales(fileConfig)
				return false
			}
			//解析本地化预定义参数规则
			"localisation_predefined_parameters" -> {
				resolveLocalisationPredefinedParameters(fileConfig)
				return false
			}
		}
		return true
	}
	
	private fun resolveSystemLinks(fileConfig: CwtFileConfig) {
		val configs = fileConfig.properties.find { it.key == "system_links" }?.properties ?: return
		configs.forEach { property ->
			val id = property.key
			val baseId = property.properties?.find { p -> p.key == "base_id" }?.stringValue ?: id
			val description = property.documentation.orEmpty()
			val name = property.stringValue ?: id
			val config = CwtSystemLinkConfig(property.pointer, fileConfig.info, id, baseId, description, name)
			systemLinks.put(id, config)
		}
	}
	
	private fun resolveLocalisationLocales(fileConfig: CwtFileConfig) {
		val configs = fileConfig.properties.find { it.key == "localisation_locales" }?.properties ?: return
		configs.forEach { property ->
			val id = property.key
			val description = property.documentation.orEmpty()
			val codes = property.properties?.find { p -> p.key == "codes" }?.values?.mapNotNull { v -> v.stringValue }.orEmpty()
			val config = CwtLocalisationLocaleConfig(property.pointer, fileConfig.info, id, description, codes)
			localisationLocales.put(id, config)
			if(id != "l_default") localisationLocalesNoDefault.put(id, config)
			if(id != "l_default" && id.startsWith("l_")) localisationLocalesNoDefaultNoPrefix.put(id.drop(2), config)
			codes.forEach { code -> localisationLocalesByCode.put(code, config) }
		}
	}
	
	private fun resolveLocalisationPredefinedParameters(fileConfig: CwtFileConfig) {
		val configs = fileConfig.properties.find { it.key == "localisation_predefined_parameters" }?.properties ?: return
		configs.forEach { property ->
			val id = property.key
			val mockValue = property.value
			val description = property.documentation.orEmpty()
			val config = CwtLocalisationPredefinedParameterConfig(property.pointer, fileConfig.info, id, mockValue, description)
			localisationPredefinedParameters.put(id, config)
		}
	}
	
	//解析CWT规则
	
	private fun resolveCwtConfigInCwtFile(fileConfig: CwtFileConfig): Boolean {
		val fileKey = fileConfig.key
		//解析要识别为脚本文件的文件夹列表
		if(fileKey == "folders") {
			resolveFolders(fileConfig)
			return false
		}
		//处理fileConfig的properties
		for(property in fileConfig.properties) {
			val key = property.key
			when {
				//找到配置文件中的顶级的key为"types"的属性，然后解析它的子属性，添加到types中
				key == "types" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val typeName = prop.key.removeSurroundingOrNull("type[", "]")
						if(!typeName.isNullOrEmpty()) {
							val typeConfig = resolveTypeConfig(prop, typeName)
							types[typeName] = typeConfig
						}
					}
				}
				//找到配置文件中的顶级的key为"values"的属性，然后解析它的子属性，添加到values中
				key == "values" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						//TODO valueName may be a template expression (e.g. xxx_<xxx>)
						val valueName = prop.key.removeSurroundingOrNull("value[", "]")
						if(!valueName.isNullOrEmpty()) {
							val valueConfig = resolveEnumConfig(prop, valueName) ?: continue
							values[valueName] = valueConfig
						}
					}
				}
				//找到配置文件中的顶级的key为"enums"的属性，然后解析它的子属性，添加到enums和complexEnums中
				key == "enums" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val enumName = prop.key.removeSurroundingOrNull("enum[", "]")
						if(!enumName.isNullOrEmpty()) {
							val enumConfig = resolveEnumConfig(prop, enumName) ?: continue
							enums[enumName] = enumConfig
						}
						val complexEnumName = prop.key.removeSurroundingOrNull("complex_enum[", "]")
						if(!complexEnumName.isNullOrEmpty()) {
							val complexEnumConfig = resolveComplexEnumConfig(prop, complexEnumName) ?: continue
							complexEnums[complexEnumName] = complexEnumConfig
						}
					}
				}
				//找到配置文件中的顶级的key为"links"的属性，然后解析它的子属性，添加到links中
				fileKey == "links" && key == "links" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val linkName = prop.key
						val linkConfig = resolveLinkConfig(prop, linkName) ?: continue
						links[linkName] = linkConfig
						//要求data_source存在
						val fromData = linkConfig.fromData && linkConfig.dataSource != null
						val withPrefix = linkConfig.prefix != null
						val type = linkConfig.type
						if(type == null || type == "scope" || type == "both") {
							when {
								!fromData -> linksAsScopeNotData[linkName] = linkConfig
								withPrefix -> linksAsScopeWithPrefix[linkName] = linkConfig
								else -> linksAsScopeWithoutPrefix[linkName] = linkConfig
							}
						}
						if(type == "value" || type == "both") {
							when {
								!fromData -> linksAsValueNotData[linkName] = linkConfig
								withPrefix -> linksAsValueWithPrefix[linkName] = linkConfig
								else -> linksAsValueWithoutPrefix[linkName] = linkConfig
							}
						}
					}
				}
				//找到配置文件中的顶级的key为"localisation_links"的属性，然后解析它的子属性，添加到localisationLinks中
				fileKey == "localisation" && key == "localisation_links" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val linkName = prop.key
						val linkConfig = resolveLocalisationLinkConfig(prop, linkName) ?: continue
						localisationLinks[linkName] = linkConfig
					}
				}
				//找到配置文件中的顶级的key为"localisation_commands"的属性，然后解析它的子属性，添加到localisationCommands中
				fileKey == "localisation" && key == "localisation_commands" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val commandName = prop.key
						val commandConfig = resolveLocalisationCommandConfig(prop, commandName)
						localisationCommands[commandName] = commandConfig
					}
				}
				//找到配置文件中的顶级的key为"modifier_categories"的属性，然后解析它的子属性，添加到modifierCategories中
				fileKey == "modifier_categories" && key == "modifier_categories" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val modifierCategoryName = prop.key
						val categoryConfig = resolveModifierCategoryConfig(prop, modifierCategoryName) ?: continue
						modifierCategories[modifierCategoryName] = categoryConfig
					}
				}
				//找到配置文件中的顶级的key为"modifiers"的属性，然后解析它的子属性，添加到modifiers中
				fileKey == "modifiers" && key == "modifiers" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val modifierName = prop.key
						val modifierConfig = resolveModifierConfig(prop, modifierName) ?: continue
						modifiers[modifierName] = modifierConfig
						for(snippetExpression in modifierConfig.template.snippetExpressions) {
							if(snippetExpression.type == CwtDataType.Definition) {
								val typeExpression = snippetExpression.value ?: continue
								typeToModifiersMap.getOrPut(typeExpression) { mutableMapOf() }.put(modifierName, modifierConfig)
							}
						}
					}
				}
				//找到配置文件中的顶级的key为"scopes"的属性，然后解析它的子属性，添加到scopes中
				fileKey == "scopes" && key == "scopes" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val scopeName = prop.key
						val scopeConfig = resolveScopeConfig(prop, scopeName) ?: continue
						scopes[scopeName] = scopeConfig
						for(alias in scopeConfig.aliases) {
							scopeAliasMap[alias] = scopeConfig
						}
					}
				}
				//找到配置文件中的顶级的key为"scope_groups"的属性，然后解析它的子属性，添加到scopeGroups中
				fileKey == "scopes" && key == "scope_groups" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val scopeGroupName = prop.key
						val scopeGroupConfig = resolveScopeGroupConfig(prop, scopeGroupName) ?: continue
						scopeGroups[scopeGroupName] = scopeGroupConfig
					}
				}
				fileKey == "game_rules" && key == "game_rules" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val onActionName = prop.key
						val onActionConfig = resolveGameRuleConfig(prop, onActionName) ?: continue
						gameRules[onActionName] = onActionConfig
					}
				}
				fileKey == "on_actions" && key == "on_actions" -> {
					val props = property.properties ?: continue
					for(prop in props) {
						val onActionName = prop.key
						val onActionConfig = resolveOnActionConfig(prop, onActionName) ?: continue
						onActions[onActionName] = onActionConfig
					}
				}
				else -> {
					//判断配置文件中的顶级的key是否匹配"single_alias[?]"，如果匹配，则解析配置并添加到single_aliases中
					val singleAliasName = key.removeSurroundingOrNull("single_alias[", "]")
					if(singleAliasName != null) {
						val singleAliasConfig = resolveSingleAliasConfig(property, singleAliasName)
						singleAliases[singleAliasName] = singleAliasConfig
					}
					
					//判断配置文件中的顶级的key是否匹配"alias[?:?]"，如果匹配，则解析配置并添加到aliases中
					val aliasNamePair = key.removeSurroundingOrNull("alias[", "]")?.splitToPair(':')
					if(aliasNamePair != null) {
						val (aliasName, aliasSubName) = aliasNamePair
						val aliasConfig = resolveAliasConfig(property, aliasName, aliasSubName)
						//目前不这样处理
						//if(aliasConfig.name == "modifier" && aliasConfig.expression.type.isConstantLikeType()) {
						//	val modifierConfig = resolveModifierConfigFromAliasConfig(aliasConfig)
						//	modifiers.put(modifierConfig.name, modifierConfig)
						//	continue
						//} 
						val map = aliasGroups.getOrPut(aliasName) { mutableMapOf() }
						val list = map.getOrPut(aliasSubName) { SmartList() }
						list.add(aliasConfig)
						
					}
					
					val inlineConfigName = key.removeSurroundingOrNull("inline[", "]")
					if(inlineConfigName != null) {
						val inlineConfig = resolveInlineConfig(property, inlineConfigName)
						val list = inlineConfigGroup.getOrPut(inlineConfigName) { SmartList() }
						list.add(inlineConfig)
					}
					
					//其他情况，放到definition中
					val declarationConfig = resolveDeclarationConfig(property, key)
					declarations[key] = declarationConfig
				}
			}
		}
		return true
	}
	
	private fun resolveFolders(fileConfig: CwtFileConfig) {
		fileConfig.values.mapTo(folders) { it.value }
	}
	
	private fun resolveTypeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtTypeConfig {
		var baseType: String? = null
		var path: String? = null
		var pathStrict = false
		var pathFile: String? = null
		var pathExtension: String? = null
		var nameField: String? = null
		var nameFromFile = false
		var typePerFile = false
		var unique = false
		var severity: String? = null
		var skipRootKey: MutableList<List<String>>? = null
		var typeKeyFilter: ReversibleValue<Set<String>>? = null
		var typeKeyRegex: Regex? = null
		var startsWith: String? = null
		var graphRelatedTypes: Set<String>? = null
		val subtypes: MutableMap<String, CwtSubtypeConfig> = mutableMapOf()
		var localisation: CwtTypeLocalisationConfig? = null
		var images: CwtTypeImagesConfig? = null
		
		val props = propertyConfig.properties
		if(!props.isNullOrEmpty()) {
			for(prop in props) {
				val key = prop.key
				when(key) {
					"base_type" -> baseType = prop.stringValue
					//这里的path一般"game/"开始，这里需要忽略
					"path" -> path = prop.stringValue?.removePrefix("game")?.trimStart('/') ?: continue
					"path_strict" -> pathStrict = prop.booleanValue ?: continue
					"path_file" -> pathFile = prop.stringValue ?: continue
					"path_extension" -> pathExtension = prop.stringValue ?: continue
					"name_field" -> nameField = prop.stringValue ?: continue
					"name_from_file" -> nameFromFile = prop.booleanValue ?: continue
					"type_per_file" -> typePerFile = prop.booleanValue ?: continue
					"unique" -> unique = prop.booleanValue ?: continue
					"severity" -> severity = prop.stringValue ?: continue
					"skip_root_key" -> {
						//值可能是string也可能是stringArray
						val list = prop.stringValue?.let { listOf(it) }
							?: prop.values?.mapNotNull { it.stringValue }
							?: continue
						if(skipRootKey == null) skipRootKey = SmartList()
						skipRootKey.add(list) //出于一点点的性能考虑，这里保留大小写，后面匹配路径时会忽略掉
					}
					"localisation" -> {
						val configs: MutableList<Pair<String?, CwtLocationConfig>> = SmartList()
						val propPointer = prop.pointer
						val propProps = prop.properties ?: continue
						for(p in propProps) {
							val subtypeName = p.key.removeSurroundingOrNull("subtype[", "]")
							if(subtypeName != null) {
								val pps = p.properties ?: continue
								for(pp in pps) {
									val locationConfig = resolveLocationConfig(pp, pp.key) ?: continue
									configs.add(subtypeName to locationConfig)
								}
							} else {
								val locationConfig = resolveLocationConfig(p, p.key) ?: continue
								configs.add(null to locationConfig)
							}
						}
						localisation = CwtTypeLocalisationConfig(propPointer, propertyConfig.info, configs)
					}
					"images" -> {
						val configs: MutableList<Pair<String?, CwtLocationConfig>> = SmartList()
						val propPointer = prop.pointer
						val propProps = prop.properties ?: continue
						for(p in propProps) {
							val subtypeName = p.key.removeSurroundingOrNull("subtype[", "]")
							if(subtypeName != null) {
								val pps = p.properties ?: continue
								for(pp in pps) {
									val locationConfig = resolveLocationConfig(pp, pp.key) ?: continue
									configs.add(subtypeName to locationConfig)
								}
							} else {
								val locationConfig = resolveLocationConfig(p, p.key) ?: continue
								configs.add(null to locationConfig)
							}
						}
						images = CwtTypeImagesConfig(propPointer, propertyConfig.info, configs)
					}
					"modifiers" -> {
						val propProps = prop.properties ?: continue
						for(p in propProps) {
							val subtypeName = p.key.removeSurroundingOrNull("subtype[", "]")
							if(subtypeName != null) {
								val pps = p.properties ?: continue
								for(pp in pps) {
									val typeExpression = "$name.$subtypeName"
									val modifierConfig = resolveDefinitionModifierConfig(pp, pp.key, typeExpression) ?: continue
									modifiers[modifierConfig.name] = modifierConfig
									typeToModifiersMap.getOrPut(typeExpression) { mutableMapOf() }.put(pp.key, modifierConfig)
								}
							} else {
								val typeExpression = name
								val modifierConfig = resolveDefinitionModifierConfig(p, p.key, typeExpression) ?: continue
								modifiers[modifierConfig.name] = modifierConfig
								typeToModifiersMap.getOrPut(typeExpression) { mutableMapOf() }.put(p.key, modifierConfig)
							}
						}
					}
				}
				
				val subtypeName = key.removeSurroundingOrNull("subtype[", "]")
				if(subtypeName != null) {
					val subtypeConfig = resolveSubtypeConfig(prop, subtypeName)
					subtypes[subtypeName] = subtypeConfig
				}
			}
		}
		
		val options = propertyConfig.options
		if(!options.isNullOrEmpty()) {
			for(option in options) {
				val key = option.key
				when(key) {
					"type_key_filter" -> {
						//值可能是string也可能是stringArray
						val value = option.stringValue
						val values = option.optionValues
						if(value == null && values == null) continue
						val set = caseInsensitiveStringSet() //忽略大小写
						if(value != null) set.add(value)
						if(!values.isNullOrEmpty()) values.forEach { v -> v.stringValue?.let { sv -> set.add(sv) } }
						val notReversed = option.separatorType == CwtSeparator.EQUAL
						val o = option.separatorType == CwtSeparator.EQUAL
						typeKeyFilter = set reverseIf o
					}
					"type_key_regex" -> {
						typeKeyRegex = option.stringValue?.toRegex(RegexOption.IGNORE_CASE)
					}
					"starts_with" -> startsWith = option.stringValue ?: continue //忽略大小写
					"graph_related_types" -> {
						graphRelatedTypes = option.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue }
					}
				}
			}
		}
		
		return CwtTypeConfig(
			propertyConfig.pointer, propertyConfig.info, propertyConfig,
			name, baseType,
			path, pathStrict, pathFile, pathExtension,
			nameField, nameFromFile, typePerFile, unique, severity, skipRootKey,
			typeKeyFilter, typeKeyRegex, startsWith, 
			graphRelatedTypes, subtypes,
			localisation, images
		)
	}
	
	private fun resolveSubtypeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtSubtypeConfig {
		var typeKeyFilter: ReversibleValue<Set<String>>? = null
		var typeKeyRegex: Regex? = null
		var pushScope: String? = null
		var startsWith: String? = null
		var displayName: String? = null
		var abbreviation: String? = null
		var onlyIfNot: Set<String>? = null
		
		val options = propertyConfig.options
		if(!options.isNullOrEmpty()) {
			for(option in options) {
				val key = option.key
				when(key) {
					"type_key_filter" -> {
						//值可能是string也可能是stringArray
						val value = option.stringValue
						val values = option.optionValues
						if(value == null && values == null) continue
						val set = caseInsensitiveStringSet() //忽略大小写
						if(value != null) set.add(value)
						if(!values.isNullOrEmpty()) values.forEach { v -> v.stringValue?.let { sv -> set.add(sv) } }
						val o = option.separatorType == CwtSeparator.EQUAL
						typeKeyFilter = set reverseIf o
					}
					"type_key_regex" -> {
						typeKeyRegex = option.stringValue?.toRegex(RegexOption.IGNORE_CASE)
					}
					"starts_with" -> startsWith = option.stringValue ?: continue //忽略大小写
					"push_scope" -> pushScope = option.stringValue ?: continue
					"display_name" -> displayName = option.stringValue ?: continue
					"abbreviation" -> abbreviation = option.stringValue ?: continue
					"only_if_not" -> onlyIfNot = option.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue } ?: continue
				}
			}
		}
		return CwtSubtypeConfig(
			propertyConfig.pointer, propertyConfig.info, propertyConfig,
			name, typeKeyFilter, typeKeyRegex, startsWith,
			pushScope, displayName, abbreviation, onlyIfNot
		)
	}
	
	private fun resolveLocationConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLocationConfig? {
		val expression = propertyConfig.stringValue ?: return null
		var required = false
		var primary = false
		val optionValues = propertyConfig.optionValues
		if(!optionValues.isNullOrEmpty()) {
			for(optionValue in optionValues) {
				val value = optionValue.stringValue ?: continue
				when(value) {
					"required" -> required = true
					"primary" -> primary = true
					//"optional" -> pass() //忽略（默认就是required = false）
				}
			}
		}
		return CwtLocationConfig(propertyConfig.pointer, propertyConfig.info, name, expression, required, primary)
	}
	
	private fun resolveDefinitionModifierConfig(propertyConfig: CwtPropertyConfig, name: String, typeExpression: String): CwtModifierConfig? {
		//string | string[]
		val modifierName = name.replace("$", "<$typeExpression>")
		val categories = propertyConfig.stringValue?.let { setOf(it) }
			?: propertyConfig.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
			?: return null
		return CwtModifierConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, modifierName, categories)
	}
	
	private fun resolveEnumConfig(propertyConfig: CwtPropertyConfig, name: String): CwtEnumConfig? {
		val pointer = propertyConfig.pointer
		val info = propertyConfig.info
		val propertyConfigValues = propertyConfig.values ?: return null
		if(propertyConfigValues.isEmpty()) {
			return CwtEnumConfig(pointer, info, name, emptySet(), emptyMap())
		}
		val values = caseInsensitiveStringSet() //忽略大小写
		val valueConfigMap = CollectionFactory.createCaseInsensitiveStringMap<CwtValueConfig>() //忽略大小写
		for(propertyConfigValue in propertyConfigValues) {
			values.add(propertyConfigValue.value)
			valueConfigMap.put(propertyConfigValue.value, propertyConfigValue)
		}
		return CwtEnumConfig(pointer, info, name, values, valueConfigMap)
	}
	
	private fun resolveComplexEnumConfig(propertyConfig: CwtPropertyConfig, name: String): CwtComplexEnumConfig? {
		val pointer = propertyConfig.pointer
		val info = propertyConfig.info
		val props = propertyConfig.properties ?: return null
		if(props.isEmpty()) return null //invalid
		val path: MutableSet<String> = mutableSetOf()
		var pathFile: String? = null
		var pathStrict = false
		var startFromRoot = false
		var searchScope: String? = null
		var nameConfig: CwtPropertyConfig? = null
		for(prop in props) {
			when(prop.key) {
				//这里的path一般"game/"开始，这里需要忽略
				"path" -> prop.stringValue?.removePrefix("game")?.trimStart('/')?.let { path.add(it) }
				"path_file" -> pathFile = prop.stringValue
				"path_strict" -> pathStrict = prop.booleanValue ?: false
				"start_from_root" -> startFromRoot = prop.booleanValue ?: false
				"search_scope_type" -> searchScope = prop.stringValue
				"name" -> nameConfig = prop
			}
		}
		if(path.isEmpty() || nameConfig == null) return null //invalid
		return CwtComplexEnumConfig(pointer, info, name, path, pathFile, pathStrict, startFromRoot, searchScope, nameConfig)
	}
	
	private fun resolveLinkConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLinkConfig? {
		var desc: String? = null
		var fromData = false
		var type: String? = null
		var dataSource: CwtValueExpression? = null
		var prefix: String? = null
		var inputScopes: Set<String>? = null
		var outputScope: String? = null
		var forDefinitionType: String? = null
		val props = propertyConfig.properties ?: return null
		for(prop in props) {
			when(prop.key) {
				"desc" -> desc = prop.stringValue?.trim() //去除首尾空白
				"from_data" -> fromData = prop.booleanValue ?: false
				"type" -> type = prop.stringValue
				"data_source" -> dataSource = prop.valueExpression //TODO 实际上也可能是data（可重复），但是目前只有一处
				"prefix" -> prefix = prop.stringValue
				"for_definition_type" -> forDefinitionType = prop.stringValue
				"input_scopes" -> inputScopes = buildSet {
					prop.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
					prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
				}
				"output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) }
			}
		}
		inputScopes = inputScopes.takeIfNotEmpty() ?: ParadoxScopeHandler.anyScopeIdSet
		return CwtLinkConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name, desc, fromData, type, dataSource, prefix, forDefinitionType, inputScopes, outputScope)
	}
	
	private fun resolveLocalisationLinkConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLocalisationLinkConfig? {
		var desc: String? = null
		var inputScopes: Set<String>? = null
		var outputScope: String? = null
		val props = propertyConfig.properties ?: return null
		for(prop in props) {
			when(prop.key) {
				"desc" -> desc = prop.stringValue?.trim() //排除占位码 & 去除首尾空白
				"input_scopes" -> inputScopes = buildSet {
					prop.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
					prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
				}
				"output_scope" -> outputScope = prop.stringValue?.let { v -> ParadoxScopeHandler.getScopeId(v) }
			}
		}
		inputScopes = inputScopes.takeIfNotEmpty() ?: ParadoxScopeHandler.anyScopeIdSet
		return CwtLocalisationLinkConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name, desc, inputScopes, outputScope)
	}
	
	private fun resolveLocalisationCommandConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLocalisationCommandConfig {
		val supportedScopes = buildSet {
			propertyConfig.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
			propertyConfig.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
		}.ifEmpty { ParadoxScopeHandler.anyScopeIdSet }
		return CwtLocalisationCommandConfig(propertyConfig.pointer, propertyConfig.info, name, supportedScopes)
	}
	
	private fun resolveModifierCategoryConfig(propertyConfig: CwtPropertyConfig, name: String): CwtModifierCategoryConfig? {
		var internalId: String? = null
		var supportedScopes: Set<String>? = null
		val props = propertyConfig.properties
		if(props.isNullOrEmpty()) return null
		for(prop in props) {
			when(prop.key) {
				"internal_id" -> internalId = prop.value //目前版本的CWT配置已经不再有这个属性
				"supported_scopes" -> supportedScopes =  buildSet {
					prop.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) }
					prop.values?.forEach { it.stringValue?.let { v -> add(ParadoxScopeHandler.getScopeId(v)) } }
				} //may be empty here (e.g. "AI Economy")
			}
		}
		supportedScopes = supportedScopes ?: ParadoxScopeHandler.anyScopeIdSet
		return CwtModifierCategoryConfig(propertyConfig.pointer, propertyConfig.info, name, internalId, supportedScopes)
	}
	
	private fun resolveModifierConfig(propertyConfig: CwtPropertyConfig, name: String): CwtModifierConfig? {
		//string | string[]
		val categories = propertyConfig.stringValue?.let { setOf(it) }
			?: propertyConfig.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
			?: return null
		return CwtModifierConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name, categories)
	}
	
	private fun resolveScopeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtScopeConfig? {
		var aliases: Set<String>? = null
		val props = propertyConfig.properties
		if(props.isNullOrEmpty()) return null
		for(prop in props) {
			if(prop.key == "aliases") aliases = prop.values?.mapNotNullTo(caseInsensitiveStringSet()) { it.stringValue }
		}
		if(aliases == null) aliases = emptySet()
		return CwtScopeConfig(propertyConfig.pointer, propertyConfig.info, name, aliases)
	}
	
	private fun resolveScopeGroupConfig(propertyConfig: CwtPropertyConfig, name: String): CwtScopeGroupConfig? {
		val pointer = propertyConfig.pointer
		val info = propertyConfig.info
		val propertyConfigValues = propertyConfig.values ?: return null
		if(propertyConfigValues.isEmpty()) return CwtScopeGroupConfig(pointer, info, name, emptySet(), emptyMap())
		val values = caseInsensitiveStringSet() //忽略大小写
		val valueConfigMap = CollectionFactory.createCaseInsensitiveStringMap<CwtValueConfig>() //忽略大小写
		for(propertyConfigValue in propertyConfigValues) {
			values.add(propertyConfigValue.value)
			valueConfigMap.put(propertyConfigValue.value, propertyConfigValue)
		}
		return CwtScopeGroupConfig(pointer, info, name, values, valueConfigMap)
	}
	
	private fun resolveGameRuleConfig(propertyConfig: CwtPropertyConfig, name: String) : CwtGameRuleConfig? {
		val pointer = propertyConfig.pointer
		val info = propertyConfig.info
		return CwtGameRuleConfig(pointer, info, propertyConfig, name)
	}
	
	private fun resolveOnActionConfig(propertyConfig: CwtPropertyConfig, name: String) : CwtOnActionConfig? {
		val pointer = propertyConfig.pointer
		val info = propertyConfig.info
		val eventType = propertyConfig.stringValue ?: return null
		return CwtOnActionConfig(pointer, info, propertyConfig, name, eventType)
	}
	
	private fun resolveSingleAliasConfig(propertyConfig: CwtPropertyConfig, name: String): CwtSingleAliasConfig {
		return CwtSingleAliasConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name)
	}
	
	private fun resolveAliasConfig(propertyConfig: CwtPropertyConfig, name: String, subName: String): CwtAliasConfig {
		return CwtAliasConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name, subName)
			.apply {
				info.acceptConfigExpression(subNameExpression, null)
			}
	}
	
	private fun resolveModifierConfigFromAliasConfig(aliasConfig: CwtAliasConfig): CwtModifierConfig {
		val propertyConfig = aliasConfig.config
		return CwtModifierConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, aliasConfig.subName)
	}
	
	private fun resolveInlineConfig(propertyConfig: CwtPropertyConfig, name: String): CwtInlineConfig {
		return CwtInlineConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name)
	}
	
	private fun resolveDeclarationConfig(propertyConfig: CwtPropertyConfig, name: String): CwtDeclarationConfig {
		return CwtDeclarationConfig(propertyConfig.pointer, propertyConfig.info, name, propertyConfig)
	}
	
	//初始化基于解析的CWT配置的额外配置
	
	private fun initModifierCategoryIdMap(): Map<String, CwtModifierCategoryConfig> {
		val result: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
		for(modifierCategory in modifierCategories.values) {
			val internalId = modifierCategory.internalId ?: continue
			result[internalId] = modifierCategory
		}
		return result
	}
	
	//绑定CWT配置
	
	private fun bindLinksToLocalisationLinks() {
		for((key, linkConfig) in linksAsScopeNotData) {
			val localisationLinkConfig = CwtLocalisationLinkConfig(linkConfig.pointer, linkConfig.info, linkConfig.config,
				linkConfig.name, linkConfig.desc, linkConfig.inputScopes, linkConfig.outputScope)
			localisationLinks.put(key, localisationLinkConfig)
		}
	}
	
	private fun bindModifierCategories() {
		for(modifier in modifiers.values) {
			//category可能是modifierCategory的name，也可能是modifierCategory的internalId
			for(category in modifier.categories) {
				val categoryConfig = modifierCategories[category] ?: modifierCategoryIdMap[category] ?: continue
				modifier.categoryConfigMap[categoryConfig.name] = categoryConfig
			}
		}
	}
	
	private fun bindAliasKeysGroup() {
		for((k, v) in aliasGroups) {
			var keysConst: MutableMap<String, String>? = null
			var keysNoConst: MutableSet<String>? = null
			for(key in v.keys) {
				if(CwtKeyExpression.resolve(key).type == CwtDataType.Constant) {
					if(keysConst == null) keysConst = caseInsensitiveStringKeyMap()
					keysConst.put(key, key)
				} else {
					if(keysNoConst == null) keysNoConst = mutableSetOf()
					keysNoConst.add(key)
				}
			}
			if(!keysConst.isNullOrEmpty()) {
				aliasKeysGroupConst.put(k, keysConst)
			}
			if(!keysNoConst.isNullOrEmpty()) {
				aliasKeysGroupNoConst.put(k, keysNoConst.sortedByPriority(this) { CwtKeyExpression.resolve(it) }.toSet())
			}
		}
	}
}
