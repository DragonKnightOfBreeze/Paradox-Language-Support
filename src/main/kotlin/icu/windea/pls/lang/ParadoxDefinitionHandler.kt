@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.lang

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理定义信息。
 * 
 * @see ParadoxScriptDefinitionElement
 * @see ParadoxDefinitionInfo
 */
object ParadoxDefinitionHandler {
	val definitionNamePrefixKey = Key.create<String>("paradox.definition.prefix")
	
	@JvmStatic
	fun getInfo(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
		ProgressManager.checkCanceled()
		val notUseCache = element.getUserData(PlsKeys.isIncompleteKey) == true
		if(notUseCache) {
			val file = element.containingFile
			return resolve(element, file)
		}
		return getInfoFromCache(element)
	}
	
	private fun getInfoFromCache(element: ParadoxScriptDefinitionElement): ParadoxDefinitionInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedDefinitionInfoKey) {
			val file = element.containingFile
			val value = resolve(element, file)
			//invalidated on file modification
			CachedValueProvider.Result.create(value, file)
		}
	}
	
	@JvmStatic
	fun resolve(element: ParadoxScriptDefinitionElement, file: PsiFile = element.containingFile): ParadoxDefinitionInfo? {
		//排除带参数的情况
		if(element is ParadoxScriptProperty && element.propertyKey.isParameterAwareExpression()) return null
		
		ProgressManager.checkCanceled()
		val project = file.project
		
		//首先尝试直接基于stub进行解析
		val stub = runCatching { element.getStub() }.getOrNull()
		if(stub != null) {
			return resolveByStub(element, stub, project)
		}
		
		val fileInfo = file.fileInfo
		//当无法获取fileInfo时，尝试基于上一行的特殊注释（指定游戏类型和定义类型）、脚本文件开始的特殊注释（指定游戏类型、文件路径）进行解析
		if(fileInfo == null) {
			return resolveByTypeComment(element, project) ?: resolveByPathComment(element, file, project)
		}
		val elementPath = ParadoxElementPathHandler.getFromFile(element, PlsConstants.maxDefinitionDepth) ?: return null
		val rootKey = element.pathName //如果是文件名，不要包含扩展名
		val path = fileInfo.entryPath //这里使用entryPath
		val gameType = fileInfo.rootInfo.gameType //这里还是基于fileInfo获取gameType
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolve(element, rootKey, path, elementPath, configGroup)
	}
	
	private fun resolveByStub(element: ParadoxScriptDefinitionElement, stub: ParadoxScriptDefinitionElementStub<out ParadoxScriptDefinitionElement>, project: Project): ParadoxDefinitionInfo? {
		val gameType = stub.gameType
		val type = stub.type
		val rootKey = stub.rootKey
		if(gameType == null || type == null || rootKey == null) return null
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolveWithKnownType(element, type, rootKey, configGroup)
			?.apply { sourceType = ParadoxDefinitionInfo.SourceType.Stub }
	}
	
	private fun resolveByPathComment(element: ParadoxScriptDefinitionElement, file: PsiFile, project: Project): ParadoxDefinitionInfo? {
		val (gameType, path) = ParadoxMagicCommentHandler.resolveFilePathComment(file) ?: return null
		val elementPath = ParadoxElementPathHandler.getFromFile(element, PlsConstants.maxDefinitionDepth) ?: return null
		val rootKey = element.pathName //如果是文件名，不要包含扩展名
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolve(element, rootKey, path, elementPath, configGroup)
			?.apply { sourceType = ParadoxDefinitionInfo.SourceType.PathComment }
	}
	
	private fun resolveByTypeComment(element: ParadoxScriptDefinitionElement, project: Project): ParadoxDefinitionInfo? {
		val (gameType, type) = ParadoxMagicCommentHandler.resolveDefinitionTypeComment(element) ?: return null
		val rootKey = element.pathName //如果是文件名，不要包含扩展名
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolveWithKnownType(element, type, rootKey, configGroup)
			?.apply { sourceType = ParadoxDefinitionInfo.SourceType.TypeComment }
	}
	
	private fun doResolve(element: ParadoxScriptDefinitionElement, rootKey: String, path: ParadoxPath, elementPath: ParadoxElementPath, configGroup: CwtConfigGroup): ParadoxDefinitionInfo? {
		val gameType = configGroup.gameType ?: return null
		for(typeConfig in configGroup.types.values) {
			ProgressManager.checkCanceled()
			if(matchesType(element, typeConfig, path, elementPath, rootKey, configGroup)) {
				//需要懒加载
				return ParadoxDefinitionInfo(rootKey, typeConfig, gameType, configGroup, element)
			}
		}
		return null
	}
	
	private fun doResolveWithKnownType(element: ParadoxScriptDefinitionElement, type: String, rootKey: String, configGroup: CwtConfigGroup): ParadoxDefinitionInfo? {
		val gameType = configGroup.gameType ?: return null
		val typeConfig = configGroup.types[type] ?: return null
		//仍然要求匹配rootKey
		if(matchesTypeWithKnownType(typeConfig, rootKey)) {
			return ParadoxDefinitionInfo(rootKey, typeConfig, gameType, configGroup, element)
		}
		return null
	}
	
	@JvmStatic
	fun matchesType(
		element: ParadoxScriptDefinitionElement,
		typeConfig: CwtTypeConfig,
		path: ParadoxPath,
		elementPath: ParadoxElementPath,
		rootKey: String,
		configGroup: CwtConfigGroup
	): Boolean {
		//判断element是否需要是scriptFile还是scriptProperty
		val nameFromFileConfig = typeConfig.nameFromFile
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
		//判断path_extension是否匹配（默认为".txt"，CWT文件中可能未填写，此时直接留空）
		val pathExtensionConfig = typeConfig.pathExtension //String?
		if(pathExtensionConfig != null) {
			if(pathExtensionConfig != "." + path.fileExtension) return false
		}
		
		//如果skip_root_key = any，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过（忽略大小写）
		//skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
		//skip_root_key可以重复（其中之一匹配即可）
		val skipRootKeyConfig = typeConfig.skipRootKey
		if(skipRootKeyConfig.isNullOrEmpty()) {
			if(elementPath.length > 1) return false
		} else {
			val result = skipRootKeyConfig.any { elementPath.matchEntire(it, useParentPath = true) }
			if(!result) return false
		}
		//如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
		val startsWithConfig = typeConfig.startsWith
		if(!startsWithConfig.isNullOrEmpty()) {
			val result = rootKey.startsWith(startsWithConfig, false)
			if(!result) return false
		}
		//如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
		val typeKeyFilterConfig = typeConfig.typeKeyFilter
		if(!typeKeyFilterConfig.isNullOrEmpty()) {
			val result = typeKeyFilterConfig.contains(rootKey)
			if(!result) return false
		}
		//如果name_field存在，则要求type_key必须是指定的所有type_key之一，或者没有任何指定的type_key
		val nameFieldConfig = typeConfig.nameField
		if(nameFieldConfig != null) {
			val result = (typeConfig.typeKeyFilter == null && typeConfig.subtypes.values.all { it.typeKeyFilter == null })
				|| typeConfig.typeKeyFilter?.set?.contains(rootKey) == true
				|| typeConfig.subtypes.values.any { it.typeKeyFilter?.set?.contains(rootKey) == true }
			if(!result) return false
		}
		
		//判断element的propertyValue是否需要是block
		val declarationConfig = configGroup.declarations[typeConfig.name]?.propertyConfig
		//当进行代码补全时需要特殊处理
		val isBlock = if(element.getUserData(PlsKeys.isIncompleteKey) == true) null
		else element.castOrNull<ParadoxScriptProperty>()?.propertyValue?.let { it is ParadoxScriptBlock }
		if(declarationConfig != null && isBlock != null) {
			val isBlockConfig = declarationConfig.valueExpression.type == CwtDataType.Block
			if(isBlockConfig != isBlock) return false
		}
		
		return true
	}
	
	@JvmStatic
	fun matchesTypeWithKnownType(
		typeConfig: CwtTypeConfig,
		rootKey: String
	): Boolean {
		//如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
		val startsWithConfig = typeConfig.startsWith
		if(!startsWithConfig.isNullOrEmpty()) {
			val result = rootKey.startsWith(startsWithConfig, false)
			if(!result) return false
		}
		//如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
		val typeKeyFilterConfig = typeConfig.typeKeyFilter
		if(!typeKeyFilterConfig.isNullOrEmpty()) {
			val filterResult = typeKeyFilterConfig.contains(rootKey)
			if(!filterResult) return false
		}
		//如果name_field存在，则要求type_key必须是指定的所有type_key之一
		val nameFieldConfig = typeConfig.nameField
		if(nameFieldConfig != null) {
			val result = typeConfig.typeKeyFilter?.set?.contains(rootKey) == true
				|| typeConfig.subtypes.values.any { subtypeConfig -> subtypeConfig.typeKeyFilter?.set?.contains(rootKey) == true }
			if(!result) return false
		}
		return true
	}
	
	@JvmStatic
	fun matchesTypeWithUnknownDeclaration(
		typeConfig: CwtTypeConfig,
		path: ParadoxPath,
		elementPath: ParadoxElementPath?,
		rootKey: String?
	): Boolean {
		//判断element是否需要是scriptFile还是scriptProperty
		val nameFromFileConfig = typeConfig.nameFromFile
		if(nameFromFileConfig) return false
		
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
			if(pathExtensionConfig != "." + path.fileExtension) return false
		}
		
		if(elementPath != null) {
			//如果skip_root_key = any，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过（忽略大小写）
			//skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
			//skip_root_key可以重复（其中之一匹配即可）
			val skipRootKeyConfig = typeConfig.skipRootKey
			if(skipRootKeyConfig.isNullOrEmpty()) {
				if(elementPath.length > 1) return false
			} else {
				val skipResult = skipRootKeyConfig.any { elementPath.matchEntire(it, useParentPath = true) }
				if(!skipResult) return false
			}
		}
		
		if(rootKey != null) {
			//如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
			val startsWithConfig = typeConfig.startsWith
			if(!startsWithConfig.isNullOrEmpty()) {
				val result = rootKey.startsWith(startsWithConfig, false)
				if(!result) return false
			}
			//如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
			val typeKeyFilterConfig = typeConfig.typeKeyFilter
			if(!typeKeyFilterConfig.isNullOrEmpty()) {
				val filterResult = typeKeyFilterConfig.contains(rootKey)
				if(!filterResult) return false
			}
			//如果name_field存在，则要求type_key必须是指定的所有type_key之一
			val nameFieldConfig = typeConfig.nameField
			if(nameFieldConfig != null) {
				val result = typeConfig.typeKeyFilter?.set?.contains(rootKey) == true
					|| typeConfig.subtypes.values.any { subtypeConfig -> subtypeConfig.typeKeyFilter?.set?.contains(rootKey) == true }
				if(!result) return false
			}
		}
		
		return true
	}
	
	@JvmStatic
	fun matchesSubtype(
		element: ParadoxScriptDefinitionElement,
		subtypeConfig: CwtSubtypeConfig,
		rootKey: String,
		configGroup: CwtConfigGroup,
		subtypes: MutableList<CwtSubtypeConfig>
	): Boolean {
		//如果only_if_not存在，且已经匹配指定的任意子类型，则不匹配
		val onlyIfNotConfig = subtypeConfig.onlyIfNot
		if(!onlyIfNotConfig.isNullOrEmpty()) {
			val matchesAny = subtypes.any { it.name in onlyIfNotConfig }
			if(matchesAny) return false
		}
		//如果starts_with存在，则要求type_key匹配这个前缀（不忽略大小写）
		val startsWithConfig = subtypeConfig.startsWith
		if(!startsWithConfig.isNullOrEmpty()) {
			val result = rootKey.startsWith(startsWithConfig, false)
			if(!result) return false
		}
		//如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
		val typeKeyFilterConfig = subtypeConfig.typeKeyFilter
		if(!typeKeyFilterConfig.isNullOrEmpty()) {
			val filterResult = typeKeyFilterConfig.contains(rootKey)
			if(!filterResult) return false
		}
		//根据config对property进行内容匹配
		val elementConfig = subtypeConfig.config
		return doMatchDefinition(element, elementConfig, configGroup)
	}
	
	private fun doMatchDefinition(definitionElement: ParadoxScriptDefinitionElement, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
		val childValueConfigs = propertyConfig.values.orEmpty()
		if(childValueConfigs.isNotEmpty()) {
			//匹配值列表
			val values = definitionElement.valueList
			if(!doMatchValues(values, childValueConfigs, configGroup)) return false //继续匹配
		}
		val childPropertyConfigs = propertyConfig.properties.orEmpty()
		if(childPropertyConfigs.isNotEmpty()) {
			//匹配属性列表
			val properties = definitionElement.propertyList
			if(!doMatchProperties(properties, childPropertyConfigs, configGroup)) return false //继续匹配
		}
		return true
	}
	
	private fun doMatchProperty(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
		val propValue = propertyElement.propertyValue
		if(propValue == null) {
			//对于propertyValue同样这样判断（可能脚本没有写完）
			return propertyConfig.cardinality?.min == 0
		} else {
			when {
				//匹配布尔值
				propertyConfig.booleanValue != null -> {
					if(propValue !is ParadoxScriptBoolean || propValue.booleanValue != propertyConfig.booleanValue) return false
				}
				//匹配值
				propertyConfig.stringValue != null -> {
					val expression = ParadoxDataExpression.resolve(propValue)
					return CwtConfigHandler.matchesScriptExpression(propValue, expression, propertyConfig.valueExpression, propertyConfig, configGroup)
				}
				//匹配single_alias
				CwtConfigHandler.isSingleAlias(propertyConfig) -> {
					return doMatchSingleAlias(propertyElement, propertyConfig, configGroup)
				}
				//匹配alias
				CwtConfigHandler.isAlias(propertyConfig) -> {
					return doMatchAlias(propertyElement, propertyConfig, configGroup)
				}
				propertyConfig.configs.orEmpty().isNotEmpty() -> {
					//匹配值列表
					val values = propertyElement.valueList
					if(!doMatchValues(values, propertyConfig.values.orEmpty(), configGroup)) return false
					//匹配属性列表
					val props = propertyElement.propertyList
					if(!doMatchProperties(props, propertyConfig.properties.orEmpty(), configGroup)) return false
				}
			}
		}
		return true
	}
	
	private fun doMatchProperties(propertyElements: List<ParadoxScriptProperty>, propertyConfigs: List<CwtPropertyConfig>, configGroup: CwtConfigGroup): Boolean {
		if(propertyConfigs.isEmpty()) return true
		if(propertyElements.isEmpty()) return false
		
		//要求其中所有的value的值在最终都会小于等于指定值
		val minMap = propertyConfigs.associateByTo(mutableMapOf(), { it.key }, { it.cardinality?.min ?: 1 }) //默认为1
		
		//注意：propConfig.key可能有重复，这种情况下只要有其中一个匹配即可
		for(propertyElement in propertyElements) {
			val keyElement = propertyElement.propertyKey
			val expression = ParadoxDataExpression.resolve(keyElement)
			val propConfigs = propertyConfigs.filter {
				CwtConfigHandler.matchesScriptExpression(keyElement, expression, it.keyExpression, it, configGroup)
			}
			//如果没有匹配的规则则忽略
			if(propConfigs.isNotEmpty()) {
				val matched = propConfigs.any { propConfig ->
					val matched = doMatchProperty(propertyElement, propConfig, configGroup)
					if(matched) minMap.compute(propConfig.key) { _, v -> if(v == null) 1 else v - 1 }
					matched
				}
				if(!matched) return false
			}
		}
		
		return minMap.values.any { it <= 0 }
	}
	
	private fun doMatchValues(valueElements: List<ParadoxScriptValue>, valueConfigs: List<CwtValueConfig>, configGroup: CwtConfigGroup): Boolean {
		if(valueConfigs.isEmpty()) return true
		if(valueElements.isEmpty()) return false
		
		//要求其中所有的value的值在最终都会小于等于指定值
		val minMap = valueConfigs.associateByTo(mutableMapOf(), { it.value }, { it.cardinality?.min ?: 1 }) //默认为1
		
		for(value in valueElements) {
			//如果没有匹配的规则则认为不匹配
			val expression = ParadoxDataExpression.resolve(value)
			val matched = valueConfigs.any { valueConfig ->
				val matched = CwtConfigHandler.matchesScriptExpression(value, expression, valueConfig.valueExpression, valueConfig, configGroup)
				if(matched) minMap.compute(valueConfig.value) { _, v -> if(v == null) 1 else v - 1 }
				matched
			}
			if(!matched) return false
		}
		
		return minMap.values.any { it <= 0 }
	}
	
	private fun doMatchSingleAlias(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
		val singleAliasName = propertyConfig.valueExpression.value ?: return false
		val singleAlias = configGroup.singleAliases[singleAliasName] ?: return false
		return doMatchProperty(propertyElement, singleAlias.config, configGroup)
	}
	
	private fun doMatchAlias(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
		//aliasName和aliasSubName需要匹配
		val aliasName = propertyConfig.keyExpression.value ?: return false
		val key = propertyElement.name
		val quoted = propertyElement.propertyKey.text.isLeftQuoted()
		val aliasSubName = CwtConfigHandler.getAliasSubName(propertyElement, key, quoted, aliasName, configGroup) ?: return false
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
		val aliases = aliasGroup[aliasSubName] ?: return false
		return aliases.any { alias ->
			doMatchProperty(propertyElement, alias.config, configGroup)
		}
	}
	
	@JvmStatic
	fun getName(element: ParadoxScriptDefinitionElement): String? {
		return runCatching { element.getStub() }.getOrNull()?.name ?: element.definitionInfo?.name
	}
	
	@JvmStatic
	fun getType(element: ParadoxScriptDefinitionElement): String? {
		return runCatching { element.getStub() }.getOrNull()?.type ?: element.definitionInfo?.type
	}
	
	@JvmStatic
	fun getSubtypes(element: ParadoxScriptDefinitionElement): List<String>? {
		return runCatching { element.getStub() }.getOrNull()?.subtypes ?: element.definitionInfo?.subtypes
	}
	
	@JvmStatic
	fun getDefinitionNamePrefixOption(typeConfig: CwtTypeConfig) : String {
		//return config.getOrPutUserData(definitionNamePrefixKey) {
		//	val option = config.options?.find { it.key == "prefix" }
		//	return option?.stringValue.orEmpty()
		//}
		var prefix = ""
		typeConfig.subtypes.values.forEach { subtypeConfig ->
			val config = subtypeConfig.config
			prefix = config.getOrPutUserData(definitionNamePrefixKey) {
				val option = config.options?.find { it.key == "prefix" }
				return option?.stringValue.orEmpty()
			}
			if(prefix.isNotEmpty()) return prefix
		}
		val config = typeConfig.config
		prefix = config.getOrPutUserData(definitionNamePrefixKey) {
			val option = config.options?.find { it.key == "prefix" }
			return option?.stringValue.orEmpty()
		}
		return prefix
	}
}
