package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

class CwtConfigGroup(
	val gameType: ParadoxGameType,
	val project: Project,
	cwtFileConfigs: Map<String, CwtFileConfig>
) {
	val folders: Set<String>
	val types: Map<String, CwtTypeConfig>
	val values: Map<String, CwtEnumConfig>
	val enums: Map<String, CwtEnumConfig> //enumValue可以是int、float、bool类型，统一用字符串表示
	val links: Map<String, CwtLinkConfig>
	val localisationLinks: Map<String, CwtLinkConfig>
	val localisationCommands: Map<String, CwtLocalisationCommandConfig>
	val modifierCategories: Map<String, CwtModifierCategoryConfig>
	val modifierCategoryIdMap: Map<String, CwtModifierCategoryConfig> //目前版本的CWT配置已经不再使用
	val modifiers: Map<String, CwtModifierConfig>
	val scopes: Map<String, CwtScopeConfig>
	val scopeAliasMap: Map<String, CwtScopeConfig>
	val scopeGroups: Map<String, CwtScopeGroupConfig>
	val singleAliases: Map<String, List<CwtSingleAliasConfig>> //同名的single_alias可以有多个
	val aliases: Map<String, Map<String, List<CwtAliasConfig>>> //同名的alias可以有多个 
	val definitions: Map<String, CwtDefinitionConfig>
	
	init {
		folders = mutableSetOf()
		types = mutableMapOf()
		values = mutableMapOf()
		enums = mutableMapOf()
		links = mutableMapOf()
		localisationLinks = mutableMapOf()
		localisationCommands = mutableMapOf()
		modifierCategories = mutableMapOf()
		modifierCategoryIdMap = mutableMapOf()
		modifiers = mutableMapOf()
		scopes = mutableMapOf()
		scopeAliasMap = mutableMapOf()
		scopeGroups = mutableMapOf()
		singleAliases = mutableMapOf<String, MutableList<CwtSingleAliasConfig>>()
		aliases = mutableMapOf<String, MutableMap<String, MutableList<CwtAliasConfig>>>()
		definitions = mutableMapOf()
		
		for((filePath, fileConfig) in cwtFileConfigs) {
			//如果存在folders.cwt，则将其中的相对路径列表添加到folders中 
			if(filePath == "folders.cwt") {
				resolveFoldersCwt(fileConfig, folders)
			}
			
			//处理fileConfig的properties
			for(property in fileConfig.properties) {
				val key = property.key
				when(key) {
					//找到配置文件中的顶级的key为"types"的属性，然后解析它的子属性，添加到types中
					"types" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val typeName = resolveTypeName(prop.key)
							if(typeName != null && typeName.isNotEmpty()) {
								val typeConfig = resolveTypeConfig(prop, typeName)
								types[typeName] = typeConfig
							}
						}
					}
					//找到配置文件中的顶级的key为"values"的属性，然后解析它的子属性，添加到values中
					"values" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val valueName = resolveValueName(prop.key)
							if(valueName != null && valueName.isNotEmpty()) {
								val valueConfig = resolveEnumConfig(prop, valueName) ?: continue
								values[valueName] = valueConfig
							}
						}
					}
					//找到配置文件中的顶级的key为"enums"的属性，然后解析它的子属性，添加到enums中
					"enums" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val enumName = resolveEnumName(prop.key)
							if(enumName != null && enumName.isNotEmpty()) {
								val enumConfig = resolveEnumConfig(prop, enumName) ?: continue
								enums[enumName] = enumConfig
							}
						}
					}
					//找到配置文件中的顶级的key为"links"的属性，然后解析它的子属性，添加到links中
					"links" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val linkName = prop.key
							val linkConfig = resolveLinkConfig(prop, linkName) ?: continue
							links[linkName] = linkConfig
						}
					}
					//找到配置文件中的顶级的key为"localisation_links"的属性，然后解析它的子属性，添加到localisationLinks中
					"localisation_links" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val linkName = prop.key
							val linkConfig = resolveLinkConfig(prop, linkName) ?: continue
							localisationLinks[linkName] = linkConfig
						}
					}
					//找到配置文件中的顶级的key为"localisation_commands"的属性，然后解析它的子属性，添加到localisationCommands中
					"localisation_commands" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val commandName = prop.key
							val commandConfig = resolveLocalisationCommandConfig(prop, commandName) ?: continue
							localisationCommands[commandName] = commandConfig
						}
					}
					//找到配置文件中的顶级的key为"modifier_categories"的属性，然后解析它的子属性，添加到modifierCategories中
					"modifier_categories" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val modifierCategoryName = prop.key
							val categoryConfig = resolveModifierCategoryConfig(prop, modifierCategoryName) ?: continue
							with(categoryConfig){
								modifierCategories[name] = categoryConfig
								if(internalId != null) modifierCategoryIdMap[internalId] = categoryConfig
							}
						}
					}
					//找到配置文件中的顶级的key为"modifiers"的属性，然后解析它的子属性，添加到modifiers中
					"modifiers" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val modifierName = prop.key
							val modifierConfig = resolveModifierConfig(prop, modifierName)
							modifiers[modifierName] = modifierConfig
						}
					}
					//找到配置文件中的顶级的key为"scopes"的属性，然后解析它的子属性，添加到scopes中
					"scopes" -> {
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
					"scope_groups" -> {
						val props = property.properties ?: continue
						for(prop in props) {
							val scopeGroupName = prop.key
							val scopeGroupConfig = resolveScopeGroupConfig(prop, scopeGroupName) ?: continue
							scopeGroups[scopeGroupName] = scopeGroupConfig
						}
					}
					else -> {
						//判断配置文件中的顶级的key是否匹配"single_alias[?]"，如果匹配，则解析配置并添加到single_aliases中
						val singleAliasName = resolveSingleAliasName(key)
						if(singleAliasName != null) {
							val singleAliasConfig = resolveSingleAliasConfig(property, singleAliasName)
							val list = singleAliases.getOrPut(singleAliasName) { SmartList() }
							list.add(singleAliasConfig)
						}
						
						//判断配置文件中的顶级的key是否匹配"alias[?:?]"，如果匹配，则解析配置并添加到aliases中
						val aliasNamePair = resolveAliasName(key)
						if(aliasNamePair != null) {
							val (aliasName, aliasSubName) = aliasNamePair
							val aliasConfig = resolveAliasConfig(property, aliasName, aliasSubName)
							val map = aliases.getOrPut(aliasName) { mutableMapOf() }
							val list = map.getOrPut(aliasSubName) { SmartList() }
							list.add(aliasConfig)
						}
						
						//其他情况，放到definition中
						val definitionName = key
						val definitionConfig = resolveDefinitionConfig(property, definitionName) ?: continue
						definitions[definitionName] = definitionConfig
					}
				}
			}
		}
		
		bindModifierCategories()
	}
	
	//解析cwt配置文件
	
	private fun resolveFoldersCwt(fileConfig: CwtFileConfig, folders: MutableSet<String>) {
		fileConfig.values.mapTo(folders) { it.value }
	}
	
	private fun resolveTypeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtTypeConfig {
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
		var typeKeyFilter: ReversibleList<String>? = null
		var startsWith: String? = null
		var graphRelatedTypes: List<String>? = null
		val subtypes: MutableMap<String, CwtSubtypeConfig> = mutableMapOf()
		var localisation: CwtTypeLocalisationConfig? = null
		
		val props = propertyConfig.properties
		if(props != null && props.isNotEmpty()) {
			for(prop in props) {
				val key = prop.key
				when(key) {
					//这里path需要移除前缀"game/"，这个插件会忽略它
					"path" -> path = prop.stringValue?.removePrefix("game/") ?: continue
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
						val list = prop.stringValueOrValues
						if(list != null) {
							if(skipRootKey == null) skipRootKey = SmartList()
							skipRootKey.add(list)
						}
					}
					"localisation" -> {
						val configs: MutableList<Pair<String?, CwtTypeLocalisationInfoConfig>> = SmartList()
						val propPointer = prop.pointer
						val propProps = prop.properties
						if(propProps != null) {
							for(p in propProps) {
								val k = p.key
								val subtypeName = resolveSubtypeName(k)
								if(subtypeName != null) {
									val pps = p.properties ?: continue
									for(pp in pps) {
										val kk = pp.key
										val localisationConfig = resolveTypeLocalisationConfig(pp, kk) ?: continue
										configs.add(subtypeName to localisationConfig)
									}
								} else {
									val localisationConfig = resolveTypeLocalisationConfig(p, k) ?: continue
									configs.add(null to localisationConfig)
								}
							}
						}
						localisation = CwtTypeLocalisationConfig(propPointer, configs)
					}
				}
				
				val subtypeName = resolveSubtypeName(key)
				if(subtypeName != null) {
					val subtypeConfig = resolveSubtypeConfig(prop, subtypeName)
					subtypes[subtypeName] = subtypeConfig
				}
			}
		}
		
		val options = propertyConfig.options
		if(options != null && options.isNotEmpty()) {
			for(option in options) {
				val key = option.key
				when(key) {
					"starts_with" -> startsWith = option.stringValue ?: continue
					"type_key_filter" -> {
						val reversed = option.separatorType == CwtSeparatorType.NOT_EQUAL
						//值可能是string也可能是stringArray
						val list = option.stringValueOrValues
						typeKeyFilter = list?.toReversibleList(reversed)
					}
					"graph_related_types" -> {
						val optionValues = option.values ?: continue
						val list = optionValues.map { it.value }
						graphRelatedTypes = list
					}
				}
			}
		}
		
		return CwtTypeConfig(
			propertyConfig.pointer, name, path, pathStrict, pathFile, pathExtension,
			nameField, nameFromFile, typePerFile, unique, severity, skipRootKey,
			typeKeyFilter, startsWith, graphRelatedTypes, subtypes, localisation
		)
	}
	
	private fun resolveSubtypeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtSubtypeConfig {
		var typeKeyFilter: ReversibleList<String>? = null
		var pushScope: String? = null
		var startsWith: String? = null
		var displayName: String? = null
		var abbreviation: String? = null
		var onlyIfNot: List<String>? = null
		
		val options = propertyConfig.options
		if(options != null && options.isNotEmpty()) {
			for(option in options) {
				val key = option.key
				when(key) {
					"type_key_filter" -> {
						val reversed = option.separatorType == CwtSeparatorType.NOT_EQUAL
						//值可能是string也可能是stringArray
						val list = option.stringValueOrValues
						typeKeyFilter = list?.toReversibleList(reversed)
					}
					"push_scope" -> pushScope = option.stringValue
					"starts_with" -> startsWith = option.stringValue
					"display_name" -> displayName = option.stringValue
					"abbreviation" -> abbreviation = option.stringValue
					"only_if_not" -> onlyIfNot = option.stringValues
				}
			}
		}
		return CwtSubtypeConfig(
			propertyConfig.pointer, name, propertyConfig,
			typeKeyFilter, pushScope, startsWith, displayName, abbreviation, onlyIfNot
		)
	}
	
	private fun resolveTypeLocalisationConfig(propertyConfig: CwtPropertyConfig, name: String): CwtTypeLocalisationInfoConfig? {
		val expression = propertyConfig.stringValue ?: return null
		var required = false
		var primary = false
		val optionValues = propertyConfig.optionValues
		if(optionValues != null && optionValues.isNotEmpty()) {
			for(optionValue in optionValues) {
				val value = optionValue.stringValue ?: continue
				when(value) {
					"required" -> required = true
					"primary" -> primary = true
				}
			}
		}
		return CwtTypeLocalisationInfoConfig(propertyConfig.pointer, name, expression, required, primary)
	}
	
	private fun resolveEnumConfig(propertyConfig: CwtPropertyConfig, name: String): CwtEnumConfig? {
		val values = propertyConfig.values?.map { it.value } ?: return null
		val valueConfigs = propertyConfig.values
		return CwtEnumConfig(propertyConfig.pointer, name, values, valueConfigs)
	}
	
	private fun resolveLinkConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLinkConfig? {
		var desc: String? = null
		var fromData = false
		var type: String? = null
		var dataSource: CwtValueExpression? = null
		var prefix: String? = null
		var inputScopes: List<String>? = null
		var outputScope: String? = null
		val props = propertyConfig.properties ?: return null
		for(prop in props) {
			when(prop.key) {
				"desc" -> desc = prop.value
				"from_data" -> fromData = prop.booleanValue ?: false
				"type" -> type = prop.value
				"data_source" -> dataSource = prop.valueExpression
				"prefix" -> prefix = prop.value
				"input_scopes" -> inputScopes = prop.stringValues
				"output_scope" -> outputScope = prop.value
			}
		}
		if(inputScopes == null) inputScopes = emptyList()
		if(outputScope == null) return null //排除
		return CwtLinkConfig(propertyConfig.pointer, name, desc, fromData, type, dataSource, prefix, inputScopes, outputScope)
	}
	
	private fun resolveLocalisationCommandConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLocalisationCommandConfig? {
		val values = propertyConfig.stringValueOrValues ?: return null
		return CwtLocalisationCommandConfig(propertyConfig.pointer, name, values)
	}
	
	private fun resolveModifierCategoryConfig(propertyConfig: CwtPropertyConfig, name: String): CwtModifierCategoryConfig? {
		var internalId: String? = null
		var supportedScopes: List<String>? = null
		val props = propertyConfig.properties
		if(props == null || props.isEmpty()) return null
		for(prop in props) {
			when(prop.key) {
				"internal_id" -> internalId = prop.value //目前版本的CWT配置已经不再有这个属性
				"supported_scopes" -> supportedScopes = prop.stringValues
			}
		}
		if(supportedScopes == null) supportedScopes = emptyList()
		return CwtModifierCategoryConfig(propertyConfig.pointer, name, internalId, supportedScopes)
	}
	
	private fun resolveModifierConfig(propertyConfig: CwtPropertyConfig, name: String): CwtModifierConfig {
		return CwtModifierConfig(propertyConfig.pointer, name, propertyConfig.value)
	}
	
	private fun resolveScopeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtScopeConfig? {
		var aliases: List<String>? = null
		val props = propertyConfig.properties
		if(props == null || props.isEmpty()) return null
		for(prop in props) {
			if(prop.key == "aliases") aliases = prop.stringValues
		}
		if(aliases == null) aliases = emptyList()
		return CwtScopeConfig(propertyConfig.pointer, name, aliases)
	}
	
	private fun resolveScopeGroupConfig(propertyConfig: CwtPropertyConfig, name: String): CwtScopeGroupConfig? {
		val values = propertyConfig.stringValueOrValues ?: return null
		return CwtScopeGroupConfig(propertyConfig.pointer, name, values)
	}
	
	private fun resolveSingleAliasConfig(propertyConfig: CwtPropertyConfig, name: String): CwtSingleAliasConfig {
		return CwtSingleAliasConfig(propertyConfig.pointer, name, propertyConfig)
	}
	
	private fun resolveAliasConfig(propertyConfig: CwtPropertyConfig, name: String, subName: String): CwtAliasConfig {
		return CwtAliasConfig(propertyConfig.pointer, name, subName, propertyConfig)
	}
	
	private fun resolveDefinitionConfig(propertyConfig: CwtPropertyConfig, name: String): CwtDefinitionConfig? {
		val props = propertyConfig.properties ?: return null
		val propertyConfigs = SmartList<Pair<String?, CwtPropertyConfig>>()
		for(prop in props) {
			//这里需要进行合并
			val subtypeName = resolveSubtypeName(prop.key)
			if(subtypeName != null) {
				val propProps = prop.properties
				if(propProps != null) {
					for(propProp in propProps) {
						propertyConfigs.add(subtypeName to propProp)
					}
				}
			} else {
				propertyConfigs.add(null to prop)
			}
		}
		return CwtDefinitionConfig(propertyConfig.pointer, name, propertyConfigs)
	}
	
	//绑定CWT配置
	
	private fun bindModifierCategories() {
		for(modifier in modifiers.values) {
			//categories可能是modifierCategory的name，也可能是modifierCategory的internalId
			val categories = modifier.categories
			val categoryConfig = modifierCategories[categories] ?: modifierCategoryIdMap[categories]
			modifier.categoryConfig = categoryConfig
		}
	}
	
	//解析得到definitionInfo
	
	fun resolveDefinitionInfo(element: ParadoxDefinitionProperty, elementName: String, path: ParadoxPath, propertyPath: ParadoxPropertyPath): ParadoxDefinitionInfo? {
		for(typeConfig in types.values) {
			if(matchesType(typeConfig, element, elementName, path, propertyPath)) {
				return toDefinitionInfo(typeConfig, element, elementName)
			}
		}
		return null
	}
	
	private fun matchesType(typeConfig: CwtTypeConfig, element: ParadoxDefinitionProperty, elementName: String, path: ParadoxPath, propertyPath: ParadoxPropertyPath): Boolean {
		//判断value是否是block
		if(element.block == null) return false
		//判断element是否需要是scriptFile还是scriptProperty
		//TODO nameFromFile和typePerFile有什么区别？
		val nameFromFile = typeConfig.nameFromFile || typeConfig.typePerFile
		if(nameFromFile) {
			if(element !is ParadoxScriptFile) return false
		} else {
			if(element !is ParadoxScriptProperty) return false
		}
		//判断path是否匹配
		val pathConfig = typeConfig.path ?: return false
		val pathStrictConfig = typeConfig.pathStrict
		if(pathStrictConfig) {
			if(pathConfig != path.parent) return false
		} else {
			if(!pathConfig.matchesPath(path.parent)) return false
		}
		//判断path_name是否匹配
		val pathFileConfig = typeConfig.pathFile //String?
		if(pathFileConfig != null) {
			if(pathFileConfig != path.fileName) return false
		}
		//判断path_extension是否匹配
		val pathExtensionConfig = typeConfig.pathExtension //String?
		if(pathExtensionConfig != null) {
			if(pathExtensionConfig != path.fileExtension) return false
		}
		//如果skip_root_key = <any>，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过
		//skip_root_key可以为列表（多级），可以重复（其中之一匹配即可）
		val skipRootKeyConfig = typeConfig.skipRootKey //String? | "any"
		if(skipRootKeyConfig == null || skipRootKeyConfig.isEmpty()) {
			if(propertyPath.length > 1) return false
		} else {
			var skipResult = false
			for(keys in skipRootKeyConfig) {
				if(keys.relaxMatchesPath(propertyPath.parentSubPaths)) {
					skipResult = true
					break
				}
			}
			if(!skipResult) return false
		}
		//如果type_key_filter存在，则过滤key
		val typeKeyFilterConfig = typeConfig.typeKeyFilter
		if(typeKeyFilterConfig != null && typeKeyFilterConfig.isNotEmpty()) {
			val filterResult = if(typeKeyFilterConfig.reverse) {
				elementName !in typeKeyFilterConfig
			} else {
				elementName in typeKeyFilterConfig
			}
			if(!filterResult) return false
		}
		return true
	}
	
	private fun matchesSubtype(subtypeConfig: CwtSubtypeConfig, element: ParadoxDefinitionProperty, elementName: String, result: MutableList<CwtSubtypeConfig>): Boolean {
		//如果only_if_not存在，且已经匹配指定的任意子类型，则不匹配
		val onlyIfNotConfig = subtypeConfig.onlyIfNot
		if(onlyIfNotConfig != null && onlyIfNotConfig.isNotEmpty()) {
			val matchesAny = result.any { it.name in onlyIfNotConfig }
			if(matchesAny) return false
		}
		//如果type_key_filter存在，则过滤key
		val typeKeyFilterConfig = subtypeConfig.typeKeyFilter
		if(typeKeyFilterConfig != null && typeKeyFilterConfig.isNotEmpty()) {
			val filterResult = if(typeKeyFilterConfig.reverse) {
				elementName !in typeKeyFilterConfig
			} else {
				elementName in typeKeyFilterConfig
			}
			if(!filterResult) return false
		}
		//如果starts_with存在，则要求elementName匹配这个前缀
		val startsWithConfig = subtypeConfig.startsWith
		if(startsWithConfig != null && startsWithConfig.isNotEmpty()) {
			if(!elementName.startsWith(startsWithConfig)) return false
		}
		//根据config对property进行内容匹配
		val elementConfig = subtypeConfig.config
		return matchesDefinitionProperty(element, elementConfig, this)
	}
	
	private fun toDefinitionInfo(typeConfig: CwtTypeConfig, element: ParadoxDefinitionProperty, elementName: String): ParadoxDefinitionInfo {
		val name = getName(typeConfig, element, elementName)
		val typeKey = elementName
		val type = typeConfig.name
		val subtypeConfigs = getSubtypeConfigs(typeConfig, element, elementName)
		val subtypes = getSubtypes(subtypeConfigs)
		val localisation = getLocalisation(typeConfig, subtypes, element, name)
		val localisationConfig = getLocalisationConfig(typeConfig)
		val definition = getDefinition(type, subtypes)
		val definitionConfig = getDefinitionConfig(type)
		val graphRelatedTypes = typeConfig.graphRelatedTypes.orEmpty()
		val unique = typeConfig.unique
		val severity = typeConfig.severity
		return ParadoxDefinitionInfo(
			name, type, typeConfig, subtypes, subtypeConfigs, localisation, localisationConfig,
			definition, definitionConfig, typeKey, graphRelatedTypes, unique, severity, gameType
		)
	}
	
	private fun getName(typeConfig: CwtTypeConfig, element: ParadoxDefinitionProperty, elementName: String): String {
		//如果name_from_file = yes，则返回文件名（不包含扩展）
		val nameFromFileConfig = typeConfig.nameFromFile
		if(nameFromFileConfig) return element.containingFile.name.substringBeforeLast('.')
		//如果name_field = <any>，则返回对应名字的property的value
		val nameFieldConfig = typeConfig.nameField
		if(nameFieldConfig != null) return element.findProperty(nameFieldConfig, true)?.value.orEmpty()
		//如果有一个子属性的propertyKey为name，那么取这个子属性的值，这是为了兼容cwt规则文件尚未考虑到的一些需要名字的情况
		val nameProperty = element.findProperty("name", true)
		if(nameProperty != null) return nameProperty.value.orEmpty()
		//否则直接返回elementName
		return elementName
	}
	
	private fun getSubtypeConfigs(typeConfig: CwtTypeConfig, element: ParadoxDefinitionProperty, elementName: String): List<CwtSubtypeConfig> {
		val subtypesConfig = typeConfig.subtypes
		val result = SmartList<CwtSubtypeConfig>()
		for(subtypeConfig in subtypesConfig.values) {
			if(matchesSubtype(subtypeConfig, element, elementName, result)) result.add(subtypeConfig)
		}
		return result
	}
	
	private fun getSubtypes(subtypeConfigs: List<CwtSubtypeConfig>): List<String> {
		return subtypeConfigs.map { it.name }
	}
	
	private fun getLocalisation(typeConfig: CwtTypeConfig, subtypes: List<String>, element: ParadoxDefinitionProperty, name: String): List<ParadoxRelatedLocalisationInfo> {
		val localisationConfig = typeConfig.localisation?.mergeConfigs(subtypes) ?: return emptyList()
		val result = SmartList<ParadoxRelatedLocalisationInfo>()
		//从已有的cwt规则
		for(config in localisationConfig) {
			//如果name为空，则keyName也为空 
			//如果expression包含"$"，则keyName为将expression中的"$"替换为name后得到的字符串
			//否则，keyName为expression对应的definition的同名子属性（不区分大小写）的值对应的字符串
			val expression = config.expression
			val keyName = resolveKeyName(name, expression, element)
			val info = ParadoxRelatedLocalisationInfo(config.name, keyName, config.required, config.primary)
			result.add(info)
		}
		//从推断的cwt规则
		val names = localisationConfig.map { it.name.lowercase() }
		for(inferredName in relatedLocalisationNamesToInfer) {
			val inferredKeyName = inferKeyName(inferredName, names, element)
			if(inferredKeyName != null) {
				val info = ParadoxRelatedLocalisationInfo(inferredName, inferredKeyName)
				result.add(info)
			}
		}
		return result
	}
	
	private fun getLocalisationConfig(typeConfig: CwtTypeConfig): CwtTypeLocalisationConfig? {
		return typeConfig.localisation
	}
	
	private fun getDefinition(type: String, subtypes: List<String>): List<CwtPropertyConfig> {
		return definitions.get(type)?.mergeConfigs(subtypes) ?: emptyList()
	}
	
	private fun getDefinitionConfig(type: String): CwtDefinitionConfig? {
		return definitions.get(type)
	}
	
	private fun resolveKeyName(name: String, expression: String, element: ParadoxDefinitionProperty): String {
		return when {
			name.isEmpty() -> ""
			expression.contains('$') -> buildString { for(c in expression) if(c == '$') append(name) else append(c) }
			else -> element.findProperty(expression, ignoreCase = true)?.propertyValue?.value
				.castOrNull<ParadoxScriptString>()?.stringValue ?: ""
		}
	}
	
	private fun inferKeyName(name: String, names: List<String>, element: ParadoxDefinitionProperty): String? {
		if(name !in names) {
			return element.findProperty(name, ignoreCase = true)?.propertyValue?.value
				.castOrNull<ParadoxScriptString>()?.stringValue
		}
		return null
	}
}

private fun resolveTypeName(expression: String): String? {
	return expression.resolveByRemoveSurrounding("type[", "]")
}

private fun resolveSubtypeName(expression: String): String? {
	return expression.resolveByRemoveSurrounding("subtype[", "]")
}

private fun resolveValueName(expression: String): String? {
	return expression.resolveByRemoveSurrounding("value[", "]")
}

private fun resolveEnumName(expression: String): String? {
	return expression.resolveByRemoveSurrounding("enum[", "]")
}

private fun resolveSingleAliasName(expression: String): String? {
	return expression.resolveByRemoveSurrounding("single_alias[", "]")
}

private fun resolveAliasName(expression: String): Pair<String, String>? {
	return expression.resolveByRemoveSurrounding("alias[", "]")?.let {
		val index = it.indexOf(':')
		it.substring(0, index) to it.substring(index + 1)
	}
}