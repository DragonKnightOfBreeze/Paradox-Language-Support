package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

@Suppress("UNCHECKED_CAST")
class ParadoxRuleGroup(
	data: Map<String, Map<String, Any>>
) : Map<String, Map<String, Any>> by data {
	val locations = get("locations")?.let { 
		(it as Map<String, Map<String, Any>>).mapValues { (k, v) -> Location(k, v) }
	} ?: emptyMap()
	val types = get("types")?.let { 
		(it as Map<String, Map<String, Any>>).mapValues { (k, v) -> Type(k, v) }
	} ?: emptyMap()
	val definitions = get("definitions") ?: emptyMap()
	val enums = get("enums") ?: emptyMap()
	
	class Location(
		key:String,data:Map<String,Any>
	):Map<String,Any> by data{
		val name = key
		
		fun matches(path:ParadoxPath,checkPath:Boolean = true):Boolean{
			if(checkPath) {
				val pathStrictData = get("path_strict") as Boolean? ?: true
				if(pathStrictData) {
					if(name != path.parent) return false
				} else {
					if(!name.matchesPath(path.parent)) return false
				}
			}
			val fileExtensionData = get("file_extension") as String? ?: "txt"
			if(fileExtensionData != path.fileExtension) return false
			val fileExtensionsData = get("file_extensions") as List<String>?
			if(fileExtensionsData != null && !fileExtensionsData.contains(path.fileExtension)) return false
			val fileNameData = get("file_name") as String?
			if(fileNameData != null && fileNameData != path.fileName) return false
			val fileNamesData = get("file_names") as List<String>?
			if(fileNamesData != null && !fileNamesData.contains(path.fileName)) return false
			return true
		}
	}
	//TODO subtypes以及subtypes的predicate
	class Type(
		key: String, data: Map<String, Any>
	) : Map<String, Any> by data {
		val name = key
		
		//不要单纯地遍历列表进行匹配，需要先通过某种方式过滤不合法的scriptProperty
		//暂时认为3级的scriptProperty不再需要匹配
		//path和propertyPath不要重复获取
		
		fun matches(elementName: String, path: ParadoxPath, scriptPath: ParadoxPath): Boolean {
			//判断文件扩展名是否匹配
			val fileExtensionData = get("file_extension") as String? ?: "txt"
			if(fileExtensionData != path.fileExtension) return false
			//判断路径是否匹配
			val pathData = get("path") as String? ?: return false
			val pathStrictData = get("path_strict") as Boolean? ?: true
			if(pathStrictData) {
				if(pathData != path.parent) return false
			} else {
				if(!pathData.matchesPath(path.parent)) return false
			}
			//判断文件名是否匹配
			val fileNameData = get("file_name") as String?
			if(fileNameData != null) {
				if(fileNameData != path.fileName) return false
			}
			//处理是否需要跳过rootKey
			val skipRootKeyData = get("skip_root_key") as String?
			if(skipRootKeyData != null) {
				//skip_root_key=any表示跳过任意的rootKey
				if(scriptPath.length != 2 || skipRootKeyData != "any" && skipRootKeyData != scriptPath.root) return false
			} else {
				if(scriptPath.length != 1) return false
			}
			//过滤key
			val keyFilterData = get("key_filter") as String?
			if(keyFilterData != null) {
				val keyConditions = keyFilterData.split(',')
				for(keyCondition in keyConditions) {
					if(keyCondition.isNotEmpty() && keyCondition[0] == '!') {
						if(elementName == keyCondition.drop(1)) return false
					} else {
						if(elementName != keyCondition) return false
					}
				}
			}
			return true
		}
		
		fun getName(element: ParadoxScriptProperty): String {
			val nameKeyData = get("name_key") as String? ?: return element.name
			if(nameKeyData == "none") return anonymousName //完全匿名 
			val nameProperty = element.findProperty(nameKeyData) ?: return anonymousName
			return nameProperty.value ?: anonymousName
		}
		
		fun getLocalisation(name: String): Map<ConditionalString, String> {
			val localisationData = get("localisation") as Map<String, String>? ?: return emptyMap()
			val result = mutableMapOf<ConditionalString, String>()
			for((k, v) in localisationData) {
				when {
					//如果以.开始，表示对应的属性的值是localisation的key
					v.startsWith('.') -> { }
					//如果包含占位符$，表示用name替换掉占位符后是localisation的key
					else -> result[k.toConditionalKey()] = replacePlaceholder(v, name)
				}
			}
			return result
		}
		
		private fun replacePlaceholder(placeholder: String, name: String): String {
			return buildString {
				for(c in placeholder) {
					if(c == '$') append(name) else append(c)
				}
			}
		}
		
		fun getScopes(): Map<String, String> {
			return get("replace_scopes") as Map<String, String>? ?: return emptyMap()
		}
		
		fun getFromVersion(): String {
			return get("from_version") as String? ?: ""
		}
		
		fun toMetadata(element: ParadoxScriptProperty, elementName: String): ParadoxDefinitionInfo {
			val name = getName(element)
			val type = this.name
			val localisation = getLocalisation(name)
			val scopes = getScopes()
			val fromVersion = getFromVersion()
			return ParadoxDefinitionInfo(name, type, elementName, localisation, scopes, fromVersion)
		}
	}
}