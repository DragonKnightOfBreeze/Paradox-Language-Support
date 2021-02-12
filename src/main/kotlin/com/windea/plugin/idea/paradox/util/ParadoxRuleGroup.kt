package com.windea.plugin.idea.paradox.util

import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.script.psi.*

@Suppress("UNCHECKED_CAST")
class ParadoxRuleGroup(
	val data: Map<String, Map<String, Any>>
) {
	val definitions = data["definitions"]?.mapValues { (k, v) -> Definition(k, v.cast()) } ?: emptyMap()
	val enums = data["enums"]?.mapValues { (k, v) -> Enum(k, v.cast()) } ?: emptyMap()
	
	class Definition(val key: String, val data: Map<String, Any>) {
		//不要单纯地遍历列表进行匹配，需要先通过某种方式过滤不合法的scriptProperty
		//暂时认为3级以及以下的scriptProperty不再需要匹配
		//path和propertyPath不要重复获取
		
		fun matches(element: ParadoxScriptProperty, elementName: String, path: ParadoxPath, propertyPath: ParadoxPath): Boolean {
			//valueType必须匹配，默认是object
			val valueTypeData = data["value_type"] ?: "object"
			if(valueTypeData is String) {
				val value = element.propertyValue?.value ?: return false
				val isValueTypeMatched = value.checkType(valueTypeData)
				if(!isValueTypeMatched) return false
			}
			//判断路径是否匹配
			val pathData = data["path"] as? String ?: return false
			val pathStrictData = data["path_strict"] as Boolean? ?: true
			if(pathStrictData) {
				if(pathData != path.parent) return false
			} else {
				if(!pathData.matchesPath(path.parent)) return false
			}
			//判断文件名是否匹配
			val fileNameData = data["file_name"]
			when(fileNameData) {
				is String -> if(fileNameData != path.fileName) return false
				is List<*> -> if(!fileNameData.contains(path.fileName)) return false
			}
			//判断文件扩展名是否匹配
			val fileExtensionData = data["file_extension"]
			when(fileExtensionData) {
				is String -> if(fileExtensionData != path.fileExtension) return false
				is List<*> -> if(!fileExtensionData.contains(path.fileExtension)) return false
			}
			//处理是否需要跳过rootKey
			val skipRootKeyData = data["skip_root_key"]
			when(skipRootKeyData) {
				is String -> if(propertyPath.length != 2 || skipRootKeyData != propertyPath.root) return false
				is Boolean -> if(propertyPath.length != 2 || !skipRootKeyData) return false
				else -> if(propertyPath.length != 1) return false
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
		
		fun toDefinitionInfo(element: ParadoxScriptProperty, elementName: String): ParadoxDefinitionInfo {
			val name = getName(data, element)
			val type = getType(data, key)
			val subtypes = getSubtypes(element, elementName)
			val localisation = getLocalisation(subtypes)
			val properties= getProperties(subtypes)
			val scopes = getScopes()
			val fromVersion = getFromVersion()
			val definitionInfo =  ParadoxDefinitionInfo(name, type, subtypes, localisation,properties, scopes, fromVersion)
			definitionInfo.resolveLocalisation(element)
			return definitionInfo
		}
		
		private fun getName(data: Map<String, Any>, element: ParadoxScriptProperty): String {
			//几种情况：从value得到，从指定的key的value得到，完全匿名，直接使用elementName
			val nameFromValueData = data["name_from_value"] as? Boolean ?: false
			if(nameFromValueData) {
				val value = element.propertyValue?.value
				if(value is ParadoxScriptStringValue) return value.value
			}
			//是否指定了name_key，或者完全匿名
			val nameKeyData = data["name_key"]
			//完全匿名 
			when(nameKeyData) {
				is String -> return element.findProperty(nameKeyData)?.value ?: return anonymousName
				is Boolean -> if(!nameKeyData) return anonymousName
			}
			return element.name
		}
		
		private fun getType(data: Map<String, Any>, name: String): ParadoxType {
			val aliasData = data["alias"]
			return when {
				aliasData is String -> return ParadoxType(name, listOf(aliasData))
				aliasData is List<*> -> return ParadoxType(name, aliasData.cast())
				else -> ParadoxType(name)
			}
		}
		
		private fun getSubtypes(element: ParadoxScriptProperty, elementName: String): List<ParadoxType> {
			val subtypesData = data["subtypes"] as? Map<String, Map<String, Any>> ?: return emptyList()
			val filteredSubtypesData = if(subtypesData.isEmpty()) subtypesData else subtypesData.filterValues {
				matchesSubtype(it, element, elementName)
			}
			return filteredSubtypesData.entries.map { (key, data) -> getType(data, key) }
		}
		
		private fun matchesSubtype(data: Map<String, Any>, element: ParadoxScriptProperty, elementName: String): Boolean {
			//过滤key
			val keyFilterData = data["key_filter"]
			if(keyFilterData is String) {
				val keyExpressions = keyFilterData.split(',').map { it.trim() }
				for(keyExpression in keyExpressions) {
					val expression = keyExpression.toPredicateExpression()
					return expression.matches(elementName)
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
		
		private fun getLocalisation(subtypes: List<ParadoxType>): Map<String,String> {
			val localisationData = data["localisation"] as? Map<String, Any?> ?: return emptyMap()
			val result = mutableMapOf<String,String>()
			for((keyData, valueData) in localisationData) {
				//如果key以subtype:开始，则是subtypeExpression，否则就是name
				when {
					keyData.startsWith("subtype:") -> {
						val expression = keyData.drop(8).toPredicateExpression()
						if(expression.matches(subtypes){ it.name }) {
							valueData as? Map<String, String> ?: continue
							result.putAll(valueData)
						}
					}
					else -> {
						valueData as? String ?: continue
						result.put(keyData,valueData)
					}
				}
			}
			return result
		}
		
		private fun getProperties(subtypes: List<ParadoxType>): Map<String,Any?> {
			val propertiesData = data["properties"] as? Map<String, Any?> ?: return emptyMap()
			val result = mutableMapOf<String,Any?>()
			for((keyData, valueData) in propertiesData) {
				//如果key以subtype:开始，则是subtypeExpression，否则就是name
				when {
					keyData.startsWith("subtype:") -> {
						val expression = keyData.drop(8).toPredicateExpression()
						if(expression.matches(subtypes){ it.name }) {
							valueData as? Map<String, Any?> ?: continue
							result.putAll(valueData)
						}
					}
					else -> {
						result.put(keyData,valueData)
					}
				}
			}
			return result
		}
		
		private fun getScopes(): Map<String, Map<String,String>> {
			return data["scopes"] as? Map<String, Map<String,String>> ?:  emptyMap()
		}
		
		private fun getFromVersion(): String {
			return data["from_version"] as? String ?: ""
		}
	}
	
	class Enum(val key: String, val data: List<Any>){
		val enumValues = data.mapNotNull { 
			when{
				it is String -> it
				it is Map<*,*> -> (it as Map<String, Any?>)["name"]?.toString()
				else -> null
			}
		}
	}
}