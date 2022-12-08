@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.core.handler

import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.CwtConfigHandler.matchesScriptExpression
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理定义信息。
 */
object ParadoxDefinitionHandler {
	@JvmStatic
	fun getInfo(element: ParadoxDefinitionProperty): ParadoxDefinitionInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedDefinitionInfoKey) {
			val file = element.containingFile
			val value = resolve(element, file)
			CachedValueProvider.Result.create(value, file) //invalidated on file modification
		}
	}
	
	@JvmStatic
	fun resolve(element: ParadoxDefinitionProperty, file: PsiFile = element.containingFile): ParadoxDefinitionInfo? {
		//排除带参数的情况
		if(element is ParadoxScriptProperty && element.propertyKey.isParameterAwareExpression()) return null
		
		val project = file.project
		
		//首先尝试直接基于stub进行解析
		val stub = runCatching { element.getStub() }.getOrNull()
		if(stub != null) {
			return resolveByStub(element, stub, project)
		}
		
		ProgressManager.checkCanceled()
		val fileInfo = file.fileInfo
		//当无法获取fileInfo时，尝试基于上一行的特殊注释（指定游戏类型和定义类型）、脚本文件开始的特殊注释（指定游戏类型、文件路径）进行解析
		if(fileInfo == null) {
			return resolveByTypeComment(element, project) ?: resolveByPathComment(element, file, project)
		}
		val elementPath = ParadoxElementPathHandler.resolveFromFile(element, PlsConstants.maxMayBeDefinitionDepth) ?: return null
		val rootKey = element.pathName //如果是文件名，不要包含扩展名
		val path = fileInfo.path
		val gameType = fileInfo.rootInfo.gameType //这里还是基于fileInfo获取gameType
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolve(configGroup, element, rootKey, path, elementPath)
	}
	
	private fun resolveByStub(element: ParadoxDefinitionProperty, stub: ParadoxDefinitionPropertyStub<out ParadoxDefinitionProperty>, project: Project): ParadoxDefinitionInfo? {
		val gameType = stub.gameType
		val type = stub.type
		val rootKey = stub.rootKey
		if(gameType == null || type == null || rootKey == null) return null
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolveWithKnownType(configGroup, element, type, rootKey)
			?.apply { sourceType = ParadoxDefinitionInfo.SourceType.Stub }
	}
	
	private fun resolveByPathComment(element: ParadoxDefinitionProperty, file: PsiFile, project: Project): ParadoxDefinitionInfo? {
		val (gameType, path) = ParadoxMagicCommentHandler.resolveFilePathComment(file) ?: return null
		val elementPath = ParadoxElementPathHandler.resolveFromFile(element, PlsConstants.maxMayBeDefinitionDepth) ?: return null
		val rootKey = element.pathName //如果是文件名，不要包含扩展名
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolve(configGroup, element, rootKey, path, elementPath)
			?.apply { sourceType = ParadoxDefinitionInfo.SourceType.PathComment }
	}
	
	private fun resolveByTypeComment(element: ParadoxDefinitionProperty, project: Project): ParadoxDefinitionInfo? {
		val (gameType, type) = ParadoxMagicCommentHandler.resolveDefinitionTypeComment(element) ?: return null
		val rootKey = element.pathName //如果是文件名，不要包含扩展名
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolveWithKnownType(configGroup, element, type, rootKey)
			?.apply { sourceType = ParadoxDefinitionInfo.SourceType.TypeComment }
	}
	
	private fun doResolve(configGroup: CwtConfigGroup, element: ParadoxDefinitionProperty, rootKey: String, path: ParadoxPath, elementPath: ParadoxElementPath): ParadoxDefinitionInfo? {
		val gameType = configGroup.gameType ?: return null
		for(typeConfig in configGroup.types.values) {
			if(matchesType(configGroup, typeConfig, element, rootKey, path, elementPath)) {
				//需要懒加载
				return ParadoxDefinitionInfo(rootKey, typeConfig, gameType, configGroup, element)
			}
		}
		return null
	}
	
	private fun doResolveWithKnownType(configGroup: CwtConfigGroup, element: ParadoxDefinitionProperty, type: String, rootKey: String): ParadoxDefinitionInfo? {
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
		configGroup: CwtConfigGroup,
		typeConfig: CwtTypeConfig,
		element: ParadoxDefinitionProperty,
		rootKey: String,
		path: ParadoxPath,
		elementPath: ParadoxElementPath
	): Boolean {
		//判断element.value是否需要是block
		val blockConfig = typeConfig.block
		val elementBlock = element.block
		if(blockConfig) {
			if(elementBlock == null) return false
		}
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
		//判断path_extension是否匹配
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
			val skipResult = skipRootKeyConfig.any { elementPath.matchEntire(it, useParentPath = true) }
			if(!skipResult) return false
		}
		//如果starts_with存在，则要求type_key匹配这个前缀（忽略大小写）
		val startsWithConfig = typeConfig.startsWith
		if(!startsWithConfig.isNullOrEmpty()) {
			if(!rootKey.startsWith(startsWithConfig, true)) return false
		}
		//如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
		val typeKeyFilterConfig = typeConfig.typeKeyFilter
		if(!typeKeyFilterConfig.isNullOrEmpty()) {
			val filterResult = typeKeyFilterConfig.contains(rootKey)
			if(!filterResult) return false
		}
		//到这里再次处理block为false的情况
		if(!blockConfig) {
			return elementBlock == null
		}
		return true
	}
	
	@JvmStatic
	fun matchesTypeWithKnownType(
		typeConfig: CwtTypeConfig,
		rootKey: String
	): Boolean {
		//如果starts_with存在，则要求type_key匹配这个前缀（忽略大小写）
		val startsWithConfig = typeConfig.startsWith
		if(!startsWithConfig.isNullOrEmpty()) {
			if(!rootKey.startsWith(startsWithConfig, true)) return false
		}
		//如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
		val typeKeyFilterConfig = typeConfig.typeKeyFilter
		if(!typeKeyFilterConfig.isNullOrEmpty()) {
			val filterResult = typeKeyFilterConfig.contains(rootKey)
			if(!filterResult) return false
		}
		return true
	}
	
	@JvmStatic
	fun matchesSubtype(
		configGroup: CwtConfigGroup,
		subtypeConfig: CwtSubtypeConfig,
		element: ParadoxDefinitionProperty,
		rootKey: String,
		result: MutableList<CwtSubtypeConfig>
	): Boolean {
		//如果only_if_not存在，且已经匹配指定的任意子类型，则不匹配
		val onlyIfNotConfig = subtypeConfig.onlyIfNot
		if(!onlyIfNotConfig.isNullOrEmpty()) {
			val matchesAny = result.any { it.name in onlyIfNotConfig }
			if(matchesAny) return false
		}
		//如果starts_with存在，则要求type_key匹配这个前缀（忽略大小写）
		val startsWithConfig = subtypeConfig.startsWith
		if(!startsWithConfig.isNullOrEmpty()) {
			if(!rootKey.startsWith(startsWithConfig, true)) return false
		}
		//如果type_key_filter存在，则通过type_key进行过滤（忽略大小写）
		val typeKeyFilterConfig = subtypeConfig.typeKeyFilter
		if(!typeKeyFilterConfig.isNullOrEmpty()) {
			val filterResult = typeKeyFilterConfig.contains(rootKey)
			if(!filterResult) return false
		}
		//根据config对property进行内容匹配
		val elementConfig = subtypeConfig.config
		return doMatchDefinitionProperty(element, elementConfig, configGroup)
	}
	
	private fun doMatchDefinitionProperty(propertyElement: ParadoxDefinitionProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
		when {
			//匹配属性列表
			!propertyConfig.properties.isNullOrEmpty() -> {
				val propConfigs = propertyConfig.properties
				val props = propertyElement.propertyList
				if(!doMatchProperties(props, propConfigs, configGroup)) return false //继续匹配
			}
			//匹配值列表
			!propertyConfig.values.isNullOrEmpty() -> {
				val valueConfigs = propertyConfig.values
				val values = propertyElement.valueList
				if(!doMatchValues(values, valueConfigs, configGroup)) return false //继续匹配
			}
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
					val matchType = 0
					return matchesScriptExpression(expression, propertyConfig.valueExpression, configGroup, matchType)
				}
				//匹配single_alias
				CwtConfigHandler.isSingleAlias(propertyConfig) -> {
					return doMatchSingleAlias(propertyElement, propertyConfig, configGroup)
				}
				//匹配alias
				CwtConfigHandler.isAlias(propertyConfig) -> {
					return doMatchAlias(propertyElement, propertyConfig, configGroup)
				}
				//匹配属性列表
				!propertyConfig.properties.isNullOrEmpty() -> {
					val propConfigs = propertyConfig.properties
					val props = propertyElement.propertyList
					if(!doMatchProperties(props, propConfigs, configGroup)) return false //继续匹配
				}
				//匹配值列表
				!propertyConfig.values.isNullOrEmpty() -> {
					val valueConfigs = propertyConfig.values
					val values = propertyElement.valueList
					if(!doMatchValues(values, valueConfigs, configGroup)) return false //继续匹配
				}
			}
		}
		return true
	}
	
	private fun doMatchProperties(propertyElements: List<ParadoxScriptProperty>, propertyConfigs: List<CwtPropertyConfig>, configGroup: CwtConfigGroup): Boolean {
		//properties为空的情况系认为匹配
		if(propertyElements.isEmpty()) return true
		
		//要求其中所有的value的值在最终都会小于等于0
		val minMap = propertyConfigs.associateByTo(mutableMapOf(), { it.key }, { it.cardinality?.min ?: 1 }) //默认为1
		
		//注意：propConfig.key可能有重复，这种情况下只要有其中一个匹配即可
		for(propertyElement in propertyElements) {
			val keyElement = propertyElement.propertyKey
			val expression = ParadoxDataExpression.resolve(keyElement)
			val matchType = 0
			val propConfigs = propertyConfigs.filter {
				matchesScriptExpression(expression, it.keyExpression, configGroup, matchType)
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
		//values为空的情况下认为匹配 
		if(valueElements.isEmpty()) return true
		
		//要求其中所有的value的值在最终都会小于等于0
		val minMap = valueConfigs.associateByTo(mutableMapOf(), { it.value }, { it.cardinality?.min ?: 1 }) //默认为1
		
		for(value in valueElements) {
			//如果没有匹配的规则则认为不匹配
			val expression = ParadoxDataExpression.resolve(value)
			val matchType = 0
			val matched = valueConfigs.any { valueConfig ->
				val matched = matchesScriptExpression(expression, valueConfig.valueExpression, configGroup, matchType)
				if(matched) minMap.compute(valueConfig.value) { _, v -> if(v == null) 1 else v - 1 }
				matched
			}
			if(!matched) return false
		}
		
		return minMap.values.any { it <= 0 }
	}
	
	private fun doMatchSingleAlias(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
		val singleAliasName = propertyConfig.valueExpression.value ?: return false
		val singleAliases = configGroup.singleAliases[singleAliasName] ?: return false
		return singleAliases.any { singleAlias ->
			doMatchProperty(propertyElement, singleAlias.config, configGroup)
		}
	}
	
	private fun doMatchAlias(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
		//aliasName和aliasSubName需要匹配
		val aliasName = propertyConfig.keyExpression.value ?: return false
		val key = propertyElement.name
		val quoted = propertyElement.propertyKey.text.isLeftQuoted()
		val aliasSubName = CwtConfigHandler.getAliasSubName(key, quoted, aliasName, configGroup) ?: return false
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
		val aliases = aliasGroup[aliasSubName] ?: return false
		return aliases.any { alias ->
			doMatchProperty(propertyElement, alias.config, configGroup)
		}
	}
	
	fun getName(element: ParadoxDefinitionProperty): String? {
		return runCatching { element.getStub() }.getOrNull()?.name ?: element.definitionInfo?.name
	}
	
	fun getType(element: ParadoxDefinitionProperty): String? {
		return runCatching { element.getStub() }.getOrNull()?.type ?: element.definitionInfo?.type
	}
	
	fun getSubtypes(element: ParadoxDefinitionProperty): List<String>? {
		return runCatching { element.getStub() }.getOrNull()?.subtypes ?: element.definitionInfo?.subtypes
	}
}
