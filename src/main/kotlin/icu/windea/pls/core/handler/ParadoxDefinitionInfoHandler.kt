@file:Suppress("UNUSED_PARAMETER")

package icu.windea.pls.core.handler

import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

/**
 * 用于处理定义信息。
 */
object ParadoxDefinitionInfoHandler {
	@JvmStatic
	fun get(element: ParadoxDefinitionProperty): ParadoxDefinitionInfo? {
		return CachedValuesManager.getCachedValue(element, PlsKeys.cachedDefinitionInfoKey) {
			val value = resolve(element)
			CachedValueProvider.Result.create(value, element)
		}
	}
	
	@JvmStatic
	fun resolve(element: ParadoxDefinitionProperty): ParadoxDefinitionInfo? {
		//首先尝试直接基于stub进行解析
		val stub = runCatching { element.getStub() }.getOrNull()
		if(stub != null) {
			val gameType = stub.gameType
			val type = stub.type
			val rootKey = stub.rootKey
			if(gameType != null && type != null && rootKey != null) {
				val configGroup = getCwtConfig(element.project).getValue(gameType) //这里需要指定project
				return doResolveWithKnownType(configGroup, element, type, rootKey)
					?.apply { sourceType = ParadoxDefinitionInfo.SourceType.Stub }
			}
		}
		//当无法获取fileInfo时，尝试基于上一行的特殊注释（指定游戏类型和定义类型）、脚本文件开始的特殊注释（指定游戏类型、文件路径）进行解析
		val file = element.containingFile
		val project = file.project
		val fileInfo = file.fileInfo
			?: return resolveByTypeComment(element, project) ?: resolveByPathComment(element, file, project)
		val elementPath = ParadoxElementPathHandler.resolveFromFile(element, maxMayBeDefinitionDepth) ?: return null
		val rootKey = element.pathName //如果是文件名，不要包含扩展名
		val path = fileInfo.path
		val gameType = fileInfo.rootInfo.gameType //这里还是基于fileInfo获取gameType
		val configGroup = getCwtConfig(project).getValue(gameType) //这里需要指定project
		return doResolve(configGroup, element, rootKey, path, elementPath)
	}
	
	private fun resolveByPathComment(element: ParadoxDefinitionProperty, file: PsiFile, project: Project): ParadoxDefinitionInfo? {
		val (gameType, path) = ParadoxMagicCommentHandler.resolveFilePathComment(file) ?: return null
		val elementPath = ParadoxElementPathHandler.resolveFromFile(element, maxMayBeDefinitionDepth) ?: return null
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
	
	private fun doResolve(
		configGroup: CwtConfigGroup,
		element: ParadoxDefinitionProperty,
		rootKey: String,
		path: ParadoxPath,
		elementPath: ParadoxElementPath
	): ParadoxDefinitionInfo? {
		for(typeConfig in configGroup.types.values) {
			if(matchesTypeConfig(configGroup, typeConfig, element, rootKey, path, elementPath)) {
				//需要懒加载
				return ParadoxDefinitionInfo(rootKey, typeConfig, configGroup.gameType, configGroup, element)
			}
		}
		return null
	}
	
	private fun doResolveWithKnownType(
		configGroup: CwtConfigGroup,
		element: ParadoxDefinitionProperty,
		type: String,
		rootKey: String
	): ParadoxDefinitionInfo? {
		val typeConfig = configGroup.types[type] ?: return null
		//仍然要求匹配rootKey
		if(matchesTypeConfigWithKnownType(typeConfig, rootKey)) {
			return ParadoxDefinitionInfo(rootKey, typeConfig, configGroup.gameType, configGroup, element)
		}
		return null
	}
	
	@JvmStatic
	fun matchesTypeConfig(
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
			if(pathExtensionConfig != "." + path.fileExtension) return false
		}
		//如果skip_root_key = any，则要判断是否需要跳过rootKey，如果为any，则任何情况都要跳过（忽略大小写）
		//skip_root_key可以为列表（如果是列表，其中的每一个root_key都要依次匹配）
		//skip_root_key可以重复（其中之一匹配即可）
		val skipRootKeyConfig = typeConfig.skipRootKey
		if(skipRootKeyConfig.isNullOrEmpty()) {
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
	fun matchesTypeConfigWithKnownType(
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
	fun matchesSubtypeConfig(
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
		return CwtConfigHandler.matchesDefinitionProperty(element, elementConfig, configGroup)
	}
}