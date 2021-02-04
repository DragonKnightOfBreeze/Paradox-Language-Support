package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

@Suppress("UNCHECKED_CAST", "UnnecessaryVariable")
class ParadoxRuleGroup(
	val data: Map<String, Map<String, Any>>
) {
	val types = data["types"]?.mapValues { (k, v) -> Type(k, v.cast()) } ?: emptyMap()
	val definitions = data["definitions"]?.mapValues { (k, v) -> Definition(k, v.cast()) } ?: emptyMap()
	val enums = data["enums"]?.mapValues { (k, v) -> Enum(k, v.cast()) } ?: emptyMap()
	
	class Type(val key: String, val data: Map<String, Any>) {
		//不要单纯地遍历列表进行匹配，需要先通过某种方式过滤不合法的scriptProperty
		//暂时认为3级的scriptProperty不再需要匹配
		//path和scriptPath不要重复获取
		
		fun matches(element: ParadoxScriptProperty, elementName: String, path: ParadoxPath, scriptPath: ParadoxPath): Boolean {
			//valueType必须匹配，默认是object
			val valueTypeData = data["value_type"] ?: "object"
			if(valueTypeData is String) {
				val value = element.propertyValue?.value ?: return false
				val isValueTypeMatched = value.checkType(valueTypeData)
				if(!isValueTypeMatched) return false
			}
			//判断路径是否匹配
			val pathData = data["path"] as String? ?: return false
			val pathStrictData = data["path_strict"] as Boolean? ?: true
			if(pathStrictData) {
				if(pathData != path.parent) return false
			} else {
				if(!pathData.matchesPath(path.parent)) return false
			}
			//判断文件名是否匹配
			val fileNameData = data["file_name"]
			if(fileNameData is String) {
				if(fileNameData != path.fileName) return false
			} else if(fileNameData is List<*>) {
				if(!fileNameData.contains(path.fileName)) return false
			}
			//判断文件扩展名是否匹配
			val fileExtensionData = data["file_extension"]
			if(fileExtensionData is String) {
				if(fileExtensionData != path.fileExtension) return false
			} else if(fileExtensionData is List<*>) {
				if(!fileExtensionData.contains(path.fileExtension)) return false
			}
			//处理是否需要跳过rootKey
			val skipRootKeyData = data["skip_root_key"]
			if(skipRootKeyData is String) {
				if(scriptPath.length != 2 || skipRootKeyData != scriptPath.root) return false
			} else if(skipRootKeyData is Boolean) {
				if(scriptPath.length != 2 || !skipRootKeyData) return false
			} else {
				if(scriptPath.length != 1) return false
			}
			//过滤key
			val keyFilterData = data["key_filter"]
			if(keyFilterData is String) {
				val keyConditions = keyFilterData.split(',').map { it.trim() }
				for(keyCondition in keyConditions) {
					if(keyCondition.isNotEmpty() && keyCondition[0] == '!') {
						if(elementName == keyCondition.drop(1)) return false
					} else {
						if(elementName != keyCondition) return false
					}
				}
			}
			//预测表达式
			val predicateData = data["predicate"]
			if(predicateData is Map<*, *>) {
				val predicateValue = element.propertyValue?.value
				if(predicateValue is ParadoxScriptBlock) {
					val propMap = predicateValue.propertyList.associateBy { it.name }
					for((k, v) in predicateData) {
						val (key, optional) = k.toString().toConditionalExpression()
						val prop = propMap[key] ?: if(optional) continue else return false
						when {
							v == true || v == "any" -> {
								val p = prop.propertyValue?.value
								val isNullLike = p == null || p.isNullLike()
								if(isNullLike) return false
							}
							v == false || v == "none" -> {
								val p = prop.propertyValue?.value
								val isNullLike = p == null || p.isNullLike()
								if(!isNullLike) return false
							}
							//TODO 判断类型、定义类型、数字范围等
							else -> continue
						}
					}
				}
			}
			return true
		}
		
		fun toTypeInfo(element: ParadoxScriptProperty, elementName: String): ParadoxTypeInfo {
			val subtypesData = filterSubtypesData(data, element, elementName)
			val name = getName(data, element)
			val type = key
			val subtypes = subtypesData.entries.map { (k, _) -> k }
			val localisation = getLocalisation(data, element, name).apply {
				for((_, v) in subtypesData) addAll(getLocalisation(v, element, name))
			}
			val scopes = getScopes(data).apply {
				for((_, v) in subtypesData) putAll(getScopes(v))
			}
			val fromVersion = getFromVersion(data)
			return ParadoxTypeInfo(name, type, subtypes, localisation, scopes, fromVersion)
		}
		
		private fun filterSubtypesData(data: Map<String, Any>, element: ParadoxScriptProperty, elementName: String): Map<String, Map<String, Any>> {
			val subtypes = data["subtypes"] as Map<String, Map<String, Any>>? ?: return emptyMap()
			return subtypes.filterValues { matchesSubtypesData(it, element, elementName) }
		}
		
		private fun matchesSubtypesData(data: Map<String, Any>, element: ParadoxScriptProperty, elementName: String): Boolean {
			//过滤key
			val keyFilterData = data["key_filter"]
			if(keyFilterData is String) {
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
			val predicateData = data["predicate"]
			if(predicateData is Map<*, *>) {
				val predicateValue = element.propertyValue?.value
				if(predicateValue is ParadoxScriptBlock) {
					val propMap = predicateValue.propertyList.associateBy { it.name }
					for((k, v) in predicateData) {
						val (key, optional) = k.toString().toConditionalExpression()
						val prop = propMap[key] ?: if(optional) continue else return false
						when {
							v == true || v == "any" -> {
								val p = prop.propertyValue?.value
								val isNullLike = p == null || p.isNullLike()
								if(isNullLike) return false
							}
							v == false || v == "none" -> {
								val p = prop.propertyValue?.value
								val isNullLike = p == null || p.isNullLike()
								if(!isNullLike) return false
							}
							//TODO 判断类型、定义类型、数字范围等
							else -> continue
						}
					}
				}
			}
			return true
		}
		
		private fun getName(data: Map<String, Any>, element: ParadoxScriptProperty): String {
			//几种情况：从value得到，从指定的key的value得到，完全匿名，直接使用elementName
			val nameFromValueData = data["name_from_value"] as Boolean? ?: false
			if(nameFromValueData) {
				val value = element.propertyValue?.value
				if(value is ParadoxScriptStringValue) return value.value
			}
			//是否指定了name_key，或者完全匿名
			val nameKeyData = data["name_key"]
			if(nameKeyData is String) {
				return element.findProperty(nameKeyData)?.value ?: return anonymousName
			} else if(nameKeyData is Boolean) {
				if(!nameKeyData) return anonymousName //完全匿名 
			}
			return element.name
		}
		
		private fun getLocalisation(data: Map<String, Any>, element: ParadoxScriptProperty, name: String): MutableList<Pair<ConditionalExpression, String>> {
			val localisationData = data["localisation"] as Map<String, String>? ?: return mutableListOf()
			val result = mutableListOf<Pair<ConditionalExpression, String>>()
			for((keyData, valueData) in localisationData) {
				when {
					//如果以.开始，表示对应的属性的值是localisation的key，值可以是string，也可以是string数组
					valueData.startsWith('.') -> {
						val k = keyData.toConditionalExpression()
						val value = element.findProperty(valueData.drop(1))?.propertyValue?.value ?: continue
						when {
							value is ParadoxScriptString -> result.add(k to value.value)
							value is ParadoxScriptBlock && value.isArray -> for(v in value.valueList) {
								if(v is ParadoxScriptString) result.add(k to v.value)
							}
						}
					}
					//如果包含占位符$，表示用name替换掉占位符后是localisation的key
					else -> {
						val k = keyData.toConditionalExpression()
						val v = buildString { for(c in valueData) if(c == '$') append(name) else append(c) }
						result.add(k to v)
					}
				}
			}
			return result
		}
		
		private fun getScopes(data: Map<String, Any>): MutableMap<String, String> {
			return data["replace_scopes"] as MutableMap<String, String>? ?: return mutableMapOf()
		}
		
		private fun getFromVersion(data: Map<String, Any>): String {
			return data["from_version"] as String? ?: ""
		}
	}
	
	class Definition(val key: String, val data: Map<String, Any>)
	
	class Enum(val name: String, val data: List<Any>)
}