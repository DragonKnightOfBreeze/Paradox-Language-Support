package icu.windea.pls.config.cwt

import com.intellij.openapi.project.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
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
							val typeName = prop.key.removeSurroundingOrNull("type[", "]")
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
							val valueName = prop.key.removeSurroundingOrNull("value[", "]")
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
							val enumName = prop.key.removeSurroundingOrNull("enum[", "]")
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
							with(categoryConfig) {
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
		var graphRelatedTypes: List<String>? = null
		val subtypes: MutableMap<String, CwtSubtypeConfig> = mutableMapOf()
		var localisation: CwtTypeLocalisationConfig? = null
		var pictures: CwtTypePicturesConfig? = null
		
		val props = propertyConfig.properties
		if(props != null && props.isNotEmpty()) {
			for(prop in props) {
				val key = prop.key
				when(key) {
					//定义的值是否需要为代码块，默认为是
					"block" -> block = prop.booleanValue ?: continue //EXTENDED BY PLS
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
						val list = prop.stringValueOrValues ?: continue
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
						localisation = CwtTypeLocalisationConfig(propPointer, configs)
					}
					"pictures" -> {
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
						pictures = CwtTypePicturesConfig(propPointer, configs)
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
		if(options != null && options.isNotEmpty()) {
			for(option in options) {
				val key = option.key
				when(key) {
					"type_key_filter" -> {
						//值可能是string也可能是stringArray
						val set = option.stringValueOrValues?.mapTo(mutableSetOf()) { it.lowercase() } ?: continue
						val reversed = option.separatorType == CwtSeparatorType.NOT_EQUAL
						typeKeyFilter = set.toReversibleSet(reversed)
					}
					"starts_with" -> startsWith = option.stringValue?.lowercase() ?: continue
					"graph_related_types" -> {
						val optionValues = option.values ?: continue
						val list = optionValues.map { it.value }
						graphRelatedTypes = list
					}
				}
			}
		}
		
		return CwtTypeConfig(
			propertyConfig.pointer, name,
			block, path, pathStrict, pathFile, pathExtension,
			nameField, nameFromFile, typePerFile, unique, severity, skipRootKey,
			typeKeyFilter, startsWith, graphRelatedTypes, subtypes,
			localisation, pictures
		)
	}
	
	private fun resolveSubtypeConfig(propertyConfig: CwtPropertyConfig, name: String): CwtSubtypeConfig {
		var typeKeyFilter: ReversibleSet<String>? = null
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
						//值可能是string也可能是stringArray
						val set = option.stringValueOrValues?.mapTo(mutableSetOf()) { it.lowercase() } ?: continue
						val reversed = option.separatorType == CwtSeparatorType.NOT_EQUAL
						typeKeyFilter = set.toReversibleSet(reversed)
					}
					"push_scope" -> pushScope = option.stringValue ?: continue
					"starts_with" -> startsWith = option.stringValue?.lowercase() ?: continue
					"display_name" -> displayName = option.stringValue ?: continue
					"abbreviation" -> abbreviation = option.stringValue ?: continue
					"only_if_not" -> onlyIfNot = option.stringValues ?: continue
				}
			}
		}
		return CwtSubtypeConfig(
			propertyConfig.pointer, name, propertyConfig,
			typeKeyFilter, pushScope, startsWith, displayName, abbreviation, onlyIfNot
		)
	}
	
	private fun resolveLocationConfig(propertyConfig: CwtPropertyConfig, name: String): CwtLocationConfig? {
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
					//"optional" -> pass() //忽略（默认就是required = false）
				}
			}
		}
		return CwtLocationConfig(propertyConfig.pointer, name, expression, required, primary)
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
			val subtypeName = prop.key.removeSurroundingOrNull("subtype[", "]")
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
	
	fun resolveDefinitionInfo(element: ParadoxDefinitionProperty, elementName: String, path: ParadoxPath, elementPath: ParadoxDefinitionPath): ParadoxDefinitionInfo? {
		for(typeConfig in types.values) {
			if(matchesType(typeConfig, element, elementName, path, elementPath)) {
				return toDefinitionInfo(typeConfig, element, elementName, elementPath)
			}
		}
		return null
	}
	
	private fun matchesType(typeConfig: CwtTypeConfig, element: ParadoxDefinitionProperty, elementName: String, path: ParadoxPath, elementPath: ParadoxDefinitionPath): Boolean {
		val typeKey = elementName.lowercase()
		//判断element.value是否需要是block
		val blockConfig = typeConfig.block
		val elementBlock = element.block
		if(blockConfig) {
			if(elementBlock == null) return false
		} else {
			return true //直接认为匹配
		}
		//if(elementBlock == null) return false
		//判断element是否需要是scriptFile还是scriptProperty
		//TODO nameFromFile和typePerFile有什么区别？
		val nameFromFileConfig = typeConfig.nameFromFile || typeConfig.typePerFile
		if(nameFromFileConfig) {
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
		//如果skip_root_key = any，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过（忽略大小写）
		//skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
		//skip_root_key可以重复（其中之一匹配即可）
		val skipRootKeyConfig = typeConfig.skipRootKey
		if(skipRootKeyConfig == null || skipRootKeyConfig.isEmpty()) {
			if(elementPath.length > 1) return false
		} else {
			var skipResult = false
			for(keys in skipRootKeyConfig) {
				if(keys.matchEntirePath(elementPath.subPaths, matchesParent = true)) {
					skipResult = true
					break
				}
			}
			if(!skipResult) return false
		}
		//如果starts_with存在，则要求type_key匹配这个前缀（忽略大小写）
		val startsWithConfig = typeConfig.startsWith
		if(startsWithConfig != null && startsWithConfig.isNotEmpty()) {
			if(!typeKey.startsWith(startsWithConfig)) return false
		}
		//如果starts_with存在，则要求type_key匹配这个前缀（忽略大小写）
		val typeKeyFilterConfig = typeConfig.typeKeyFilter
		if(typeKeyFilterConfig != null && typeKeyFilterConfig.isNotEmpty()) {
			val filterResult = typeKeyFilterConfig.contains(typeKey)
			if(!filterResult) return false
		}
		return true
	}
	
	private fun matchesSubtype(subtypeConfig: CwtSubtypeConfig, element: ParadoxDefinitionProperty, elementName: String, result: MutableList<CwtSubtypeConfig>): Boolean {
		val typeKey = elementName.lowercase()
		//如果only_if_not存在，且已经匹配指定的任意子类型，则不匹配
		val onlyIfNotConfig = subtypeConfig.onlyIfNot
		if(onlyIfNotConfig != null && onlyIfNotConfig.isNotEmpty()) {
			val matchesAny = result.any { it.name in onlyIfNotConfig }
			if(matchesAny) return false
		}
		//如果starts_with存在，则要求type_key匹配这个前缀（忽略大小写）
		val startsWithConfig = subtypeConfig.startsWith
		if(startsWithConfig != null && startsWithConfig.isNotEmpty()) {
			if(!typeKey.startsWith(startsWithConfig)) return false
		}
		//如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
		val typeKeyFilterConfig = subtypeConfig.typeKeyFilter
		if(typeKeyFilterConfig != null && typeKeyFilterConfig.isNotEmpty()) {
			val filterResult = typeKeyFilterConfig.contains(typeKey)
			if(!filterResult) return false
		}
		//根据config对property进行内容匹配
		val elementConfig = subtypeConfig.config
		return matchesDefinitionProperty(element, elementConfig, this)
	}
	
	private fun toDefinitionInfo(typeConfig: CwtTypeConfig, element: ParadoxDefinitionProperty, elementName: String, elementPath: ParadoxDefinitionPath): ParadoxDefinitionInfo {
		val name = getName(typeConfig, element, elementName)
		val type = typeConfig.name
		val subtypeConfigs = getSubtypeConfigs(typeConfig, element, elementName)
		val subtypes = subtypeConfigs.map { it.name }
		val localisation = getLocalisation(typeConfig, subtypes, element, name)
		val localisationConfig = typeConfig.localisation
		val pictures = getPictures(typeConfig, subtypes, element, name)
		val picturesConfig = typeConfig.pictures
		val definition = getDefinition(type, subtypes)
		val definitionConfig = definitions.get(type)
		val rootKey = elementName
		return ParadoxDefinitionInfo(
			name, type, typeConfig, subtypes, subtypeConfigs,
			localisation, localisationConfig, pictures, picturesConfig,
			definition, definitionConfig, rootKey, elementPath, gameType
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
	
	private fun getLocalisation(typeConfig: CwtTypeConfig, subtypes: List<String>, element: ParadoxDefinitionProperty, name: String): List<ParadoxRelatedLocalisationInfo> {
		val mergedLocalisationConfig = typeConfig.localisation?.getMergedConfigs(subtypes) ?: return emptyList()
		val result = SmartList<ParadoxRelatedLocalisationInfo>()
		//从已有的cwt规则
		for(config in mergedLocalisationConfig) {
			val expression = CwtLocationExpression.resolve(config.expression)
			val location = expression.inferLocation(name, element) ?: continue //跳过无效的位置表达式
			val info = ParadoxRelatedLocalisationInfo(config.key, location, config.required, config.primary)
			result.add(info)
		}
		return result
	}
	
	private fun getPictures(typeConfig: CwtTypeConfig, subtypes: List<String>, element: ParadoxDefinitionProperty, name: String): List<ParadoxRelatedPicturesInfo> {
		val mergedPicturesConfig = typeConfig.pictures?.getMergedConfigs(subtypes) ?: return emptyList()
		val result = SmartList<ParadoxRelatedPicturesInfo>()
		//从已有的cwt规则
		for(config in mergedPicturesConfig) {
			val expression = CwtLocationExpression.resolve(config.expression)
			val location = expression.inferLocation(name, element) ?: continue //跳过无效的位置表达式
			val info = ParadoxRelatedPicturesInfo(config.key, location, config.required, config.primary)
			result.add(info)
		}
		return result
	}
	
	private fun getDefinition(type: String, subtypes: List<String>): List<CwtPropertyConfig> {
		return definitions.get(type)?.getMergeConfigs(subtypes) ?: emptyList()
	}
}
