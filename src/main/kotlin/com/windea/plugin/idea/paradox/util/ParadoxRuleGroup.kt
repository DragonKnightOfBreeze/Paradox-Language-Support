package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

@Suppress("UNCHECKED_CAST", "UnnecessaryVariable")
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
			//valueType必须匹配，默认是object
			val valueTypeData = data["value_type"] as String? ?: "object"
			val value = element.propertyValue?.value?:return false
			val isValueTypeMatched = value.checkType(valueTypeData)
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
			//预测表达式
			val predicateData = data["predicate"] as Map<String,Any>?
			if(predicateData != null){
				val propValue = element.propertyValue
				if(propValue is ParadoxScriptBlock){
					val propMap = propValue.propertyList.associateBy { it.name }
					for((k,v) in predicateData){
						val (key,optional) = k.toConditionalKey()
						val prop = propMap[key] ?: if(optional) continue else return false
						when{
							//TODO always=yes
							v == true || v == "any" -> {
								val p = prop.propertyValue?.value
								return p != null && !p.isNullLike()
							}
							v == false || v == "none" -> {
								val p = prop.propertyValue?.value
								return p == null || p.isNullLike()
							}
							//TODO 
							else -> continue
						}
					}
				}
			}
			return true
		}
		
		fun toDefinitionInfo(element: ParadoxScriptProperty, elementName: String): ParadoxDefinitionInfo {
			val subtypesData = getSubtypesData(data,element,elementName)
			val name = getName(data, element)
			val type = getType(data,key)
			val subtypes = subtypesData.entries.map { (k,v)-> getType(v,k) }
			val localisation = getLocalisation(data,element, name).apply {
				for((_, v) in subtypesData) addAll(getLocalisation(v,element,name))
			}
			val scopes = getScopes(data).apply { 
				for((_,v) in subtypesData) putAll(getScopes(v))
			}
			val fromVersion = getFromVersion(data)
			return ParadoxDefinitionInfo(name, type,subtypes, localisation, scopes, fromVersion)
		}
		
		private fun getSubtypesData(data:Map<String,Any>,element:ParadoxScriptProperty,elementName:String):Map<String,Map<String,Any>>{
			val subtypes = data["subtypes"] as Map<String,Map<String,Any>>? ?: return emptyMap()
			return subtypes.filterValues { matchesSubtypesData(it,element,elementName) }
		}
		
		private fun matchesSubtypesData(data:Map<String,Any>,element:ParadoxScriptProperty,elementName:String):Boolean{
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
			//预测表达式
			val predicateData = data["predicate"] as Map<String,Any>?
			if(predicateData != null){
				val value = element.propertyValue?.value
				if(value is ParadoxScriptBlock){
					val propMap = value.propertyList.associateBy { it.name }
					for((k,v) in predicateData){
						val (key,optional) = k.toConditionalKey()
						val prop = propMap[key] ?: if(optional) continue else return false
						when{
							//TODO always=yes
							v == true || v == "any" -> {
								val p = prop.propertyValue?.value
								return p != null && !p.isNullLike()
							}
							v == false || v == "none" -> {
								val p = prop.propertyValue?.value
								return p == null || p.isNullLike()
							}
							//TODO 判断类型、定义类型、数字范围等
							else -> continue
						}
					}
				}
			}
			return true
		}
		
		private fun getName(data:Map<String,Any>,element: ParadoxScriptProperty): String {
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
		
		private fun getType(data:Map<String,Any>,name:String):String{
			val alias = data["alias"] as String? ?: return name
			return "$name ($alias)"
		}
		
		private fun getLocalisation(data:Map<String,Any>,element:ParadoxScriptProperty,name: String): MutableList<Pair<ConditionalKey, String>> {
			val localisationData = data["localisation"] as Map<String, String>? ?: return mutableListOf()
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
		
		private fun getScopes(data:Map<String,Any>): MutableMap<String, String> {
			return data["replace_scopes"] as MutableMap<String, String>? ?: return mutableMapOf()
		}
		
		private fun getFromVersion(data:Map<String,Any>): String {
			return data["from_version"] as String? ?: ""
		}
	}
	
	class Definition(val key:String,val data:Map<String,Any>)
	
	class Enum(val name:String,val data:List<Any>)
}