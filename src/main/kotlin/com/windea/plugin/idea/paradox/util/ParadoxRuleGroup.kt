package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

@Suppress("UNCHECKED_CAST")
class ParadoxRuleGroup(
	val data: Map<String, Map<String, Any>>
){
	val locations = data.getOrDefault("locations",emptyMap()).mapValues { (k, v) -> Location(k, v.cast()) }
	val types = data.getOrDefault("types",emptyMap()).mapValues { (k, v) -> Type(k, v.cast()) }
	val definitions = data.getOrDefault("definitions",emptyMap()).mapValues { (k, v) -> Definition(k, v.cast()) }
	val enums = data.getOrDefault("enums",emptyMap()).mapValues { (k, v) -> Enum(k, v.cast()) }
	
	class Location(val key:String, val data:Map<String,Any>){
		fun matches(path:ParadoxPath,checkPath:Boolean = true):Boolean{
			if(checkPath) {
				val pathStrictData = data["path_strict"] as Boolean? ?: true
				if(pathStrictData) {
					if(key != path.parent) return false
				} else {
					if(!key.matchesPath(path.parent)) return false
				}
			}
			val fileExtensionsData = data["file_extensions"] as List<String>?
			if(fileExtensionsData != null && !fileExtensionsData.contains(path.fileExtension)) return false
			val fileExtensionData = data["file_extension"] as String? ?: "txt"
			if(fileExtensionData != path.fileExtension) return false
			val fileNameData = data["file_name"] as String?
			if(fileNameData != null && fileNameData != path.fileName) return false
			val fileNamesData = data["file_names"] as List<String>?
			if(fileNamesData != null && !fileNamesData.contains(path.fileName)) return false
			return true
		}
	}
	
	class Type(val key: String,val data: Map<String, Any>){
		//不要单纯地遍历列表进行匹配，需要先通过某种方式过滤不合法的scriptProperty
		//暂时认为3级的scriptProperty不再需要匹配
		//path和scriptPath不要重复获取
		
		fun matches(element:ParadoxScriptProperty, elementName: String, path: ParadoxPath, scriptPath: ParadoxPath): Boolean {
			//valueType必须匹配，默认是block
			val valueTypeData = data["value_type"] as String? ?: "block"
			val value = element.propertyValue?.value?:return false
			val isValueTypeMatched = checkScriptValueType(value,valueTypeData)
			if(!isValueTypeMatched) return false
			//判断文件扩展名是否匹配
			val fileExtensionData = data["file_extension"] as String? ?: "txt"
			if(fileExtensionData != path.fileExtension) return false
			//判断路径是否匹配
			val pathData = data["path"] as String? ?: return false
			val pathStrictData = data["path_strict"] as Boolean? ?: true
			if(pathStrictData) {
				if(pathData != path.parent) return false
			} else {
				if(!pathData.matchesPath(path.parent)) return false
			}
			//判断文件名是否匹配
			val fileNameData = data["file_name"] as String?
			if(fileNameData != null) {
				if(fileNameData != path.fileName) return false
			}
			//处理是否需要跳过rootKey
			val skipRootKeyData = data["skip_root_key"] as String?
			if(skipRootKeyData != null) {
				//skip_root_key=any表示跳过任意的rootKey
				if(scriptPath.length != 2 || skipRootKeyData != "any" && skipRootKeyData != scriptPath.root) return false
			} else {
				if(scriptPath.length != 1) return false
			}
			//过滤key
			val keyFilterData = data["key_filter"] as String?
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
		
		fun toDefinitionInfo(element: ParadoxScriptProperty, elementName: String): ParadoxDefinitionInfo {
			val name = getName(element)
			val type = getType()
			val rootKey = getRootKey(elementName)
			val localisation = getLocalisation(element,name)
			val scopes = getScopes()
			val fromVersion = getFromVersion()
			return ParadoxDefinitionInfo(name, type, rootKey, localisation, scopes, fromVersion)
		}
		
		private fun getName(element: ParadoxScriptProperty): String {
			//几种情况：从value得到，从指定的key的value得到，完全匿名，直接使用elementName
			val nameFromValueData = data["name_from_value"] as Boolean? ?: false
			if(nameFromValueData){
				val value = element.propertyValue?.value
				if(value is ParadoxScriptStringValue) return value.value
			}
			val nameKeyData = data["name_key"] as String? ?: return element.name
			if(nameKeyData == "none") return anonymousName //完全匿名 
			val nameProperty = element.findProperty(nameKeyData) ?: return anonymousName
			return nameProperty.value ?: anonymousName
		}
		
		private fun getType():String{
			return key
		}
		
		private fun getLocalisation(element:ParadoxScriptProperty,name: String): List<Pair<ConditionalKey, String>> {
			val localisationData = data["localisation"] as Map<String, String>? ?: return emptyList()
			val result = mutableListOf<Pair<ConditionalKey, String>>()
			for((keyData, valueData) in localisationData) {
				when {
					//如果以.开始，表示对应的属性的值是localisation的key，值可以是string，也可以是string数组
					valueData.startsWith('.') -> {
						val k = keyData.toConditionalKey()
						val value = element.findProperty(valueData.drop(1))?.propertyValue?.value?: continue
						when{
							value is ParadoxScriptString -> result.add(k to value.value)
							value is ParadoxScriptBlock && value.isArray -> for(v in value.valueList) {
								if(v is ParadoxScriptString) result.add(k to v.value)
							}
						}
					}
					//如果包含占位符$，表示用name替换掉占位符后是localisation的key
					else -> {
						val k = keyData.toConditionalKey()
						val v = buildString { for(c in valueData) if(c == '$') append(name) else append(c) }
						result.add(k to v)
					}
				}
			}
			return result
		}
		
		private fun getScopes(): Map<String, String> {
			return data["replace_scopes"] as Map<String, String>? ?: return emptyMap()
		}
		
		private fun getRootKey(elementName:String):String{
			return elementName
		}
		
		private fun getFromVersion(): String {
			return data["from_version"] as String? ?: ""
		}
	}
	
	class Definition(val key:String,val data:Map<String,Any>)
	
	class Enum(val name:String,val data:List<Any>)
}