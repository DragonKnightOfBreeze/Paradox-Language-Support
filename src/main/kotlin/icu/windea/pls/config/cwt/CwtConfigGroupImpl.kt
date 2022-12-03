package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import com.intellij.util.*
import com.intellij.util.containers.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.config.ext.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.cwt.setting.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.model.*
import icu.windea.pls.cwt.*
import kotlin.collections.isNullOrEmpty
import kotlin.collections.mapNotNullTo

private const val s = "system_scopes"

class CwtConfigGroupImpl(
	override val project: Project,
	override val gameType: ParadoxGameType?,
	cwtFileConfigs: MutableMap<String, CwtFileConfig>
) : CwtConfigGroup {
	override val foldingSettings: MutableMap<String, MutableMap<String, CwtFoldingSetting>> = mutableMapOf()
	override val postfixTemplateSettings: MutableMap<String, MutableMap<String, CwtPostfixTemplateSetting>> = mutableMapOf()
	
	override val systemScopes: MutableMap<@CaseInsensitive String, CwtSystemScopeConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val localisationLocales: MutableMap<String, CwtLocalisationLocaleConfig> = mutableMapOf()
	override val localisationLocalesNoDefault: MutableMap<String, CwtLocalisationLocaleConfig> = mutableMapOf()
	override val localisationLocalesByCode: MutableMap<String, CwtLocalisationLocaleConfig> = mutableMapOf()
	override val localisationPredefinedParameters: MutableMap<String, CwtLocalisationPredefinedParameterConfig> = mutableMapOf()
	
	override val folders: MutableSet<String> = mutableSetOf()
	override val types: MutableMap<String, CwtTypeConfig> = mutableMapOf()
	override val values: MutableMap<String, CwtEnumConfig> = mutableMapOf()
	//enumValue可以是int、float、bool类型，统一用字符串表示
	override val enums: MutableMap<String, CwtEnumConfig> = mutableMapOf()
	//基于enum_name进行定位，对应的可能是key/value
	override val complexEnums: MutableMap<String, CwtComplexEnumConfig> = mutableMapOf()
	
	override val links: MutableMap<@CaseInsensitive String, CwtLinkConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val linksAsScopeNotData: MutableMap<@CaseInsensitive String, CwtLinkConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val linksAsScopeWithPrefix: MutableMap<@CaseInsensitive String, CwtLinkConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val linksAsScopeWithoutPrefix: MutableMap<@CaseInsensitive String, CwtLinkConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val linksAsValueNotData: MutableMap<@CaseInsensitive String, CwtLinkConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val linksAsValueWithPrefix: MutableMap<@CaseInsensitive String, CwtLinkConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val linksAsValueWithoutPrefix: MutableMap<@CaseInsensitive String, CwtLinkConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	
	override val localisationLinks: MutableMap<@CaseInsensitive String, CwtLocalisationLinkConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val localisationCommands: MutableMap<@CaseInsensitive String, CwtLocalisationCommandConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val modifierCategories: MutableMap<String, CwtModifierCategoryConfig> = mutableMapOf()
	override val modifiers: MutableMap<String, CwtModifierConfig> = mutableMapOf()
	override val scopes: MutableMap<@CaseInsensitive String, CwtScopeConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val scopeAliasMap: MutableMap<@CaseInsensitive String, CwtScopeConfig> = CollectionFactory.createCaseInsensitiveStringMap()
	override val scopeGroups: MutableMap<String, CwtScopeGroupConfig> = mutableMapOf()
	override val singleAliases: MutableMap<String, MutableList<CwtSingleAliasConfig>> = mutableMapOf()
	override val aliasGroups: MutableMap<String, MutableMap<String, MutableList<CwtAliasConfig>>> = mutableMapOf()
	override val declarations: MutableMap<String, CwtDeclarationConfig> = mutableMapOf()
	
	init {
		for(fileConfig in cwtFileConfigs.values) {
			fileConfig.info.configGroup = this
			val fileKey = fileConfig.key
			
			when(fileKey) {
				//解析代码折叠配置
				"folding_settings" -> {
					resolveFoldingSettings(fileConfig)
					continue
				}
				"postfix_template_settings" -> {
					resolvePostfixTemplateSettings(fileConfig)
				}
			}
			
			when(fileKey) {
				//解析系统作用域规则
				"system_scopes" -> {
					resolveSystemScopes(fileConfig)
					continue
				}
				//解析本地化语言区域规则
				"localisation_locales" -> {
					resolveLocalisationLocales(fileConfig)
					continue
				}
				//解析本地化预定义参数规则
				"localisation_predefined_parameters" -> {
					resolveLocalisationPredefinedParameters(fileConfig)
					continue
				}
			}
			
			//解析要识别为脚本文件的文件夹列表 
			if(fileKey == "folders") {
				resolveFolders(fileConfig)
				continue
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
					key == "links" -> {
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
					key == "localisation_links" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val linkName = prop.key
							val linkConfig = resolveLocalisationLinkConfig(prop, linkName) ?: continue
							localisationLinks[linkName] = linkConfig
						}
					}
					//找到配置文件中的顶级的key为"localisation_commands"的属性，然后解析它的子属性，添加到localisationCommands中
					key == "localisation_commands" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val commandName = prop.key
							val commandConfig = resolveLocalisationCommandConfig(prop, commandName)
							localisationCommands[commandName] = commandConfig
						}
					}
					//找到配置文件中的顶级的key为"modifier_categories"的属性，然后解析它的子属性，添加到modifierCategories中
					key == "modifier_categories" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val modifierCategoryName = prop.key
							val categoryConfig = resolveModifierCategoryConfig(prop, modifierCategoryName) ?: continue
							modifierCategories[modifierCategoryName] = categoryConfig
						}
					}
					//找到配置文件中的顶级的key为"modifiers"的属性，然后解析它的子属性，添加到modifiers中
					key == "modifiers" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val modifierName = prop.key
							val modifierConfig = resolveModifierConfig(prop, modifierName) ?: continue
							modifiers[modifierName] = modifierConfig
						}
					}
					//找到配置文件中的顶级的key为"scopes"的属性，然后解析它的子属性，添加到scopes中
					key == "scopes" -> {
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
					key == "scope_groups" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val scopeGroupName = prop.key
							val scopeGroupConfig = resolveScopeGroupConfig(prop, scopeGroupName) ?: continue
							scopeGroups[scopeGroupName] = scopeGroupConfig
						}
					}
					else -> {
						//判断配置文件中的顶级的key是否匹配"single_alias[?]"，如果匹配，则解析配置并添加到single_aliases中
						val singleAliasName = key.removeSurroundingOrNull("single_alias[", "]")
						if(singleAliasName != null) {
							val singleAliasConfig = resolveSingleAliasConfig(property, singleAliasName)
							val list = singleAliases.getOrPut(singleAliasName) { SmartList() }
							list.add(singleAliasConfig)
						}
						
						//判断配置文件中的顶级的key是否匹配"alias[?:?]"，如果匹配，则解析配置并添加到aliases中
						val aliasNamePair = key.removeSurroundingOrNull("alias[", "]")?.splitToPair(':')
						if(aliasNamePair != null) {
							val (aliasName, aliasSubName) = aliasNamePair
							val aliasConfig = resolveAliasConfig(property, aliasName, aliasSubName)
							val map = aliasGroups.getOrPut(aliasName) { mutableMapOf() }
							val list = map.getOrPut(aliasSubName) { SmartList() }
							list.add(aliasConfig)
						}
						
						//其他情况，放到definition中
						val declarationConfig = resolveDefinitionConfig(property, key)
						declarations[key] = declarationConfig
					}
				}
			}
		}
	}
	
	override val modifierCategoryIdMap: Map<String, CwtModifierCategoryConfig> = initModifierCategoryIdMap()
	
	init {
		bindModifierCategorySupportedScopeNames()
		bindModifierCategories()
	}
	
	override val aliasKeysGroupConst: MutableMap<String, Map<@CaseInsensitive String, String>> = mutableMapOf()
	override val aliasKeysGroupNoConst: MutableMap<String, Set<String>> = mutableMapOf()
	
	init {
		for((k, v) in aliasGroups) {
			var keysConst: MutableMap<String, String>? = null
			var keysNoConst: MutableSet<String>? = null
			for(key in v.keys) {
				if(CwtKeyExpression.resolve(key).type == CwtDataTypes.ConstantKey) {
					if(keysConst == null) keysConst = CollectionFactory.createCaseInsensitiveStringMap()
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
	
	//支持参数的定义类型
	override val definitionTypesSupportParameters: Set<String> by lazy {
		initDefinitionTypesSupportParameters()
	}
	
	//解析CWT配置
	
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
					when{
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
					when{
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
	
	private fun resolveSystemScopes(fileConfig: CwtFileConfig) {
		val configs = fileConfig.properties.find { it.key == "system_scopes" }?.properties ?: return
		configs.forEach { property ->
			val id = property.key
			val description = property.documentation.orEmpty()
			val name = property.stringValue ?: id
			val config = CwtSystemScopeConfig(property.pointer, fileConfig.info, id, description, name)
			systemScopes.put(id, config)
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
			codes.forEach { code -> localisationLocalesByCode.put(code, config) }
		}
	}
	
	private fun resolveLocalisationPredefinedParameters(fileConfig: CwtFileConfig) {
		val configs = fileConfig.properties.find { it.key == "localisation_predefined_parameters" }?.properties ?: return
		configs.forEach { property ->
			val id = property.key
			val description = property.documentation.orEmpty()
			val config = CwtLocalisationPredefinedParameterConfig(property.pointer, fileConfig.info, id, description)
			localisationPredefinedParameters.put(id, config)
		}
	}
	
	//解析CWT规则
	
	private fun resolveFolders(fileConfig: CwtFileConfig) {
		fileConfig.values.mapTo(folders) { it.value }
	}
	
	private fun resolveTypeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtTypeConfig {
		var block = true
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
		var typeKeyFilter: ReversibleSet<String>? = null
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
					//定义的值是否需要为代码块，默认为是
					"block" -> block = prop.booleanValue ?: continue //EXTENDED BY PLS
					//这里的path会以"game/"开始，需要忽略
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
							val k = p.key
							val subtypeName = k.removeSurroundingOrNull("subtype[", "]")
							if(subtypeName != null) {
								val pps = p.properties ?: continue
								for(pp in pps) {
									val locationConfig = resolveLocationConfig(pp, pp.key) ?: continue
									configs.add(subtypeName to locationConfig)
								}
							} else {
								val locationConfig = resolveLocationConfig(p, k) ?: continue
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
							val k = p.key
							val subtypeName = k.removeSurroundingOrNull("subtype[", "]")
							if(subtypeName != null) {
								val pps = p.properties ?: continue
								for(pp in pps) {
									val locationConfig = resolveLocationConfig(pp, pp.key) ?: continue
									configs.add(subtypeName to locationConfig)
								}
							} else {
								val locationConfig = resolveLocationConfig(p, k) ?: continue
								configs.add(null to locationConfig)
							}
						}
						images = CwtTypeImagesConfig(propPointer, propertyConfig.info, configs)
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
						val set = CollectionFactory.createCaseInsensitiveStringSet() //忽略大小写
						if(value != null) set.add(value)
						if(!values.isNullOrEmpty()) values.forEach { v -> v.stringValue?.let { sv -> set.add(sv) } }
						val notReversed = option.separatorType == CwtSeparatorType.EQUAL
						typeKeyFilter = set.toReversibleSet(notReversed)
					}
					"starts_with" -> startsWith = option.stringValue ?: continue //忽略大小写
					"graph_related_types" -> {
						graphRelatedTypes = option.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue }
					}
				}
			}
		}
		
		return CwtTypeConfig(
			propertyConfig.pointer, propertyConfig.info, name,
			block, path, pathStrict, pathFile, pathExtension,
			nameField, nameFromFile, typePerFile, unique, severity, skipRootKey,
			typeKeyFilter, startsWith, graphRelatedTypes, subtypes,
			localisation, images
		)
	}
	
	private fun resolveSubtypeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtSubtypeConfig {
		var typeKeyFilter: ReversibleSet<String>? = null
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
						val set = CollectionFactory.createCaseInsensitiveStringSet() //忽略大小写
						if(value != null) set.add(value)
						if(!values.isNullOrEmpty()) values.forEach { v -> v.stringValue?.let { sv -> set.add(sv) } }
						val notReversed = option.separatorType == CwtSeparatorType.EQUAL
						typeKeyFilter = set.toReversibleSet(notReversed)
					}
					"push_scope" -> pushScope = option.stringValue ?: continue
					"starts_with" -> startsWith = option.stringValue ?: continue //忽略大小写
					"display_name" -> displayName = option.stringValue ?: continue
					"abbreviation" -> abbreviation = option.stringValue ?: continue
					"only_if_not" -> onlyIfNot = option.optionValues?.mapNotNullTo(mutableSetOf()) { it.stringValue } ?: continue
				}
			}
		}
		return CwtSubtypeConfig(
			propertyConfig.pointer, propertyConfig.info, name, propertyConfig,
			typeKeyFilter, pushScope, startsWith, displayName, abbreviation, onlyIfNot
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
	
	private fun resolveEnumConfig(propertyConfig: CwtPropertyConfig, name: String): CwtEnumConfig? {
		val pointer = propertyConfig.pointer
		val info = propertyConfig.info
		val propertyConfigValues = propertyConfig.values ?: return null
		if(propertyConfigValues.isEmpty()) {
			return CwtEnumConfig(pointer, info, name, emptySet(), emptyMap())
		}
		val values = CollectionFactory.createCaseInsensitiveStringSet() //忽略大小写
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
				//这里的path会以"game/"开始，需要忽略
				"path" -> {
					prop.stringValue?.removePrefix("game")?.trimStart('/')?.let { path.add(it) }
				}
				"path_file" -> pathFile = prop.stringValue
				"path_strict" -> pathStrict = prop.booleanValue ?: false
				"start_from_root" -> startFromRoot = prop.booleanValue ?: false
				"search_scope" -> searchScope = prop.stringValue
				"name" -> nameConfig = prop
			}
		}
		if(path.isEmpty() || nameConfig == null) return null //invalid
		return CwtComplexEnumConfig(pointer, info, name, path, pathFile, pathStrict, startFromRoot, searchScope, nameConfig)
	}
	
	private fun resolveTagConfig(propertyConfig: CwtPropertyConfig, name: String): CwtTagConfig? {
		val props = propertyConfig.properties ?: return null
		val since = propertyConfig.options?.find { it.key == "since" }?.stringValue
		var supportedTypes: Set<String>? = null
		for(prop in props) {
			when(prop.key) {
				"supported_types" -> supportedTypes = prop.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
			}
		}
		if(supportedTypes == null) return null //排除
		return CwtTagConfig(propertyConfig.pointer, propertyConfig.info, name, since, supportedTypes)
	}
	
	private fun resolveLinkConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLinkConfig? {
		var desc: String? = null
		var fromData = false
		var type: String? = null
		var dataSource: CwtValueExpression? = null
		var prefix: String? = null
		var inputScopes: Set<String>? = null
		var outputScope: String? = null
		var forDefinition: String? = null
		val props = propertyConfig.properties ?: return null
		for(prop in props) {
			when(prop.key) {
				"desc" -> desc = prop.stringValue?.takeUnless { it.all { c -> c.isExactIdentifierChar() } }?.trim()?.trim() //排除占位码 & 去除首尾空白
				"from_data" -> fromData = prop.booleanValue ?: false
				"type" -> type = prop.stringValue
				"data_source" -> dataSource = prop.valueExpression //TODO 实际上也可能data（可重复），但是目前只有一处
				"prefix" -> prefix = prop.stringValue
				"input_scopes" -> inputScopes = prop.stringValue?.let { setOf(it) }
					?: prop.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
				"output_scope" -> outputScope = prop.stringValue
				"for_definition" -> forDefinition = prop.stringValue
			}
		}
		return CwtLinkConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name, desc, fromData, type, dataSource, prefix, inputScopes, outputScope, forDefinition)
	}
	
	private fun resolveLocalisationLinkConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLocalisationLinkConfig? {
		var desc: String? = null
		var inputScopes: Set<String>? = null
		var outputScope: String? = null
		val props = propertyConfig.properties ?: return null
		for(prop in props) {
			when(prop.key) {
				"desc" -> desc = prop.stringValue?.takeUnless { it.all { c -> c.isExactIdentifierChar() } }?.trim() //排除占位码 & 去除首尾空白
				"input_scopes" -> inputScopes = prop.stringValue?.let { setOf(it) }
					?: prop.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
				"output_scope" -> outputScope = prop.stringValue
			}
		}
		return CwtLocalisationLinkConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name, desc, inputScopes, outputScope)
	}
	
	private fun resolveLocalisationCommandConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLocalisationCommandConfig {
		val supportedScopes = propertyConfig.stringValue?.let { setOf(it) } ?: propertyConfig.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
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
				"supported_scopes" -> supportedScopes = prop.stringValue?.let { setOf(it) } ?: prop.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
			}
		}
		return CwtModifierCategoryConfig(propertyConfig.pointer, propertyConfig.info, name, internalId, supportedScopes)
	}
	
	private fun resolveModifierConfig(propertyConfig: CwtPropertyConfig, name: String): CwtModifierConfig? {
		//string | string[]
		val categories = propertyConfig.stringValue?.let { setOf(it) }
			?: propertyConfig.values?.mapNotNullTo(mutableSetOf()) { it.stringValue }
			?: return null
		return CwtModifierConfig(propertyConfig.pointer, propertyConfig.info, name, categories)
	}
	
	private fun resolveScopeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtScopeConfig? {
		var aliases: Set<String>? = null
		val props = propertyConfig.properties
		if(props.isNullOrEmpty()) return null
		for(prop in props) {
			if(prop.key == "aliases") aliases = prop.values?.mapNotNullTo(CollectionFactory.createCaseInsensitiveStringSet()) { it.stringValue }
		}
		if(aliases == null) aliases = emptySet()
		return CwtScopeConfig(propertyConfig.pointer, propertyConfig.info, name, aliases)
	}
	
	private fun resolveScopeGroupConfig(propertyConfig: CwtPropertyConfig, name: String): CwtScopeGroupConfig? {
		val pointer = propertyConfig.pointer
		val info = propertyConfig.info
		val propertyConfigValues = propertyConfig.values ?: return null
		if(propertyConfigValues.isEmpty()) return CwtScopeGroupConfig(pointer, info, name, emptySet(), emptyMap())
		val values = CollectionFactory.createCaseInsensitiveStringSet() //忽略大小写
		val valueConfigMap = CollectionFactory.createCaseInsensitiveStringMap<CwtValueConfig>() //忽略大小写
		for(propertyConfigValue in propertyConfigValues) {
			values.add(propertyConfigValue.value)
			valueConfigMap.put(propertyConfigValue.value, propertyConfigValue)
		}
		return CwtScopeGroupConfig(pointer, info, name, values, valueConfigMap)
	}
	
	private fun resolveSingleAliasConfig(propertyConfig: CwtPropertyConfig, name: String): CwtSingleAliasConfig {
		return CwtSingleAliasConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name)
	}
	
	private fun resolveAliasConfig(propertyConfig: CwtPropertyConfig, name: String, subName: String): CwtAliasConfig {
		return CwtAliasConfig(propertyConfig.pointer, propertyConfig.info, propertyConfig, name, subName)
	}
	
	private fun resolveDefinitionConfig(propertyConfig: CwtPropertyConfig, name: String): CwtDeclarationConfig {
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
	
	private fun initDefinitionTypesSupportParameters(): MutableSet<String> {
		val result = mutableSetOf<String>()
		for(aliasGroup in aliasGroups.values) {
			for(aliasList in aliasGroup.values) {
				for(aliasConfig in aliasList) {
					val props = aliasConfig.config.properties ?: continue
					if(props.isEmpty()) continue
					if(props.none { it.keyExpression.let { e -> e.type == CwtDataTypes.Enum && e.value == CwtConfigHandler.paramsEnumName } }) continue
					val definitionType = aliasConfig.subNameExpression.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: continue
					result.add(definitionType)
				}
			}
		}
		result.add("script_value") //SV也支持参数
		return result
	}
	
	//绑定CWT配置
	
	private fun bindModifierCategorySupportedScopeNames() {
		for(modifierCategory in modifierCategories.values) {
			if(modifierCategory.supportAnyScope) {
				modifierCategory.supportedScopeNames.add("Any")
			} else {
				modifierCategory.supportedScopes?.mapTo(modifierCategory.supportedScopeNames) { CwtConfigHandler.getScopeName(it, this) }
			}
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
}
