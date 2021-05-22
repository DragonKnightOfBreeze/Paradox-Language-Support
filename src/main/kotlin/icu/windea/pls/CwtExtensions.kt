package icu.windea.pls

import icu.windea.pls.config.*
import icu.windea.pls.script.psi.*

fun resolveTypeName(key: String): String? {
	return key.resolveByRemoveSurrounding("type[", "]")
}

fun resolveSubtypeName(key: String): String? {
	return key.resolveByRemoveSurrounding("subtype[", "]")
}

fun resolveEnumName(key: String): String? {
	return key.resolveByRemoveSurrounding("enum[", "]")
}

fun resolveAliasName(key: String): String? {
	return key.resolveByRemoveSurrounding("alias[", "]")
}

private fun String.isIntExpression(): Boolean {
	return surroundsWith("int[", "]")
}

private fun String.resolveIntExpression(): IntRange? {
	try {
		val expression = substring(4, length - 1)
		return expression.split("..", limit = 2).let { (a, b) -> a.toInt()..b.toInt() }
	} catch(e: Exception) {
		return null
	}
}

private fun String.isFloatExpression(): Boolean {
	return surroundsWith("float[", "]")
}

private fun String.resolveFloatExpression(): ClosedRange<Float>? {
	try {
		val expression = substring(6, length - 1)
		return expression.split("..", limit = 2).let { (a, b) -> a.toFloat()..b.toFloat() }
	} catch(e: Exception) {
		return null
	}
}

private fun String.isTypeExpression(): Boolean {
	return surroundsWith('<', '>')
}

private fun String.resolveTypeExpression(): String {
	return substring(1, length - 1)
}

private fun String.isEnumExpression(): Boolean {
	return surroundsWith("enum[", "]")
}

private fun String.resolveEnumExpression(): String {
	return substring(5, length - 1)
}

/**
 * 根据cwtConfigProperty配置对scriptProperty进行匹配。
 */
fun matchContent(element: ParadoxScriptProperty, elementConfig: CwtConfigProperty, config: CwtConfigGroupCache): Boolean {
	val propertiesConfig = elementConfig.properties ?: emptyList() //不应该为null，转为emptyList
	if(propertiesConfig.isEmpty()) return true //config为空表示匹配
	val properties = (element.propertyValue?.value as? ParadoxScriptBlock)?.propertyList ?: return true //脚本未写完
	return doMatchContent(properties, propertiesConfig, config)
}

private fun doMatchContent(properties: List<ParadoxScriptProperty>, propertiesConfig: List<CwtConfigProperty>, config: CwtConfigGroupCache): Boolean {
	//注意：propConfig.name可能有重复，这种情况下只要有其中一个匹配即可
	//递归对内容进行匹配
	for(propConfig in propertiesConfig) {
		val result = doMatchConfig(properties, propConfig, config)
		if(!result) return false
	}
	return true
}

private fun doMatchConfig(properties: List<ParadoxScriptProperty>, propConfig: CwtConfigProperty, config: CwtConfigGroupCache): Boolean {
	val prop = properties.find { it.name == propConfig.key }
	if(prop == null) {
		//如果对应的property不存在但propertyConfig是可选的，那么跳过，否则说明不匹配
		return propConfig.cardinality?.min == 0
	} else {
		val propValue = prop.propertyValue?.value
		if(propValue == null) {
			//对于propertyValue同样这样判断（可能脚本没有写完）
			return propConfig.cardinality?.min == 0
		} else {
			when {
				//布尔值要完全匹配
				propConfig.booleanValue != null -> {
					if(propValue !is ParadoxScriptBoolean || propConfig.booleanValue != propConfig.booleanValue) return false
				}
				//块（数组/对象）需要匹配
				propConfig.properties != null && propConfig.values != null -> {
					if(propValue !is ParadoxScriptBlock) return false
					//如果这里的propsConfig是空的，那么就跳过继续匹配
					val propsConfig = propConfig.properties ?: emptyList() //不应该为null，转为emptyList
					if(propsConfig.isEmpty()) return true
					val props = (prop.propertyValue?.value as? ParadoxScriptBlock)?.propertyList ?: return true //脚本未写完
					//继续递归匹配
					if(!doMatchContent(props, propsConfig, config)) return false
				}
				propConfig.stringValue != null -> {
					val stringValue = propConfig.stringValue ?: return false //不应该为null
					when {
						//字符串"any"表示匹配任意内容
						stringValue == "any" -> return true
						//字符串"bool"表示匹配任意scriptBoolean
						stringValue == "bool" -> {
							if(propValue !is ParadoxScriptBoolean) return false
						}
						//字符串"int"表示匹配任意scriptInt
						stringValue == "int" -> {
							if(propValue !is ParadoxScriptInt) return false
						}
						//字符串格式是"int[m..n]"表示匹配范围内的scriptInt
						stringValue.isIntExpression() -> {
							if(propValue !is ParadoxScriptInt) return false
							val expression = stringValue.resolveIntExpression() ?: return false
							if(propValue.intValue !in expression) return false
						}
						//字符串"float"表示匹配任意scriptInt
						stringValue == "float" -> {
							if(propValue !is ParadoxScriptFloat) return false
						}
						//字符串格式是"float[m..n]"表示匹配范围内的scriptFloat
						stringValue.isFloatExpression() -> {
							if(propValue !is ParadoxScriptFloat) return false
							val expression = stringValue.resolveFloatExpression() ?: return false
							if(propValue.floatValue !in expression) return false
						}
						//字符串"scalar"表示匹配任意scriptString
						stringValue == "scalar" -> {
							if(propValue !is ParadoxScriptString) return false
						}
						//TODO 不能保证，暂时弄成存在任意locale的都可以
						//字符串"localisation"表示匹配任意localisation的key
						stringValue == "localisation" -> {
							if(propValue !is ParadoxScriptString) return false
							val key = propValue.stringValue.ifEmpty { return false }
							val resolved = findLocalisation(key, null, config.project) ?: return false
							if(!resolved.isInLocalisationRootDirectory()) return false
						}
						//TODO 不能保证，暂时弄成存在任意locale的都可以
						//字符串"localisation_synced"表示匹配任意localisation_synced的key
						stringValue == "localisation_synced" -> {
							if(propValue !is ParadoxScriptString) return false
							val key = propValue.stringValue.ifEmpty { return false }
							val resolved = findLocalisation(key, null, config.project) ?: return false
							if(!resolved.isInLocalisationSyncedRootDirectory()) return false
						}
						//字符串格式是"enum[...]"的情况，表示匹配对应的枚举
						stringValue.isEnumExpression() -> {
							if(propValue !is ParadoxScriptString) return false 
							val enumExpression = stringValue.resolveEnumExpression() //应该保证是合法的，这里不进行额外判断
							val enumValue = propValue.stringValue.ifEmpty { return false }
							val enumValues = config.enums[enumExpression]?: return false
							if(enumValue !in enumValues) return false
						}
						//TODO 不能保证，需要验证
						//字符串格式是"<type.subtype>"的情况，表示匹配对应类型的definition
						stringValue.isTypeExpression() -> {
							if(propValue !is ParadoxScriptString) return false
							val typeExpression = stringValue.resolveTypeExpression() //应该保证是合法的，这里不进行额外判断
							val key = propValue.stringValue.ifEmpty { return false }
							if(findDefinition(key, typeExpression, config.project) == null) return false
						}
						//其他的字符串表示需要完全匹配，即两者相等
						else -> {
							if(propValue !is ParadoxScriptString || propValue.stringValue != stringValue) return false
						}
					}
				}
			}
		}
	}
	return true
}