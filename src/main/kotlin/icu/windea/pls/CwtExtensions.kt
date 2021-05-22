package icu.windea.pls

import com.intellij.openapi.project.*
import icu.windea.pls.config.*
import icu.windea.pls.script.psi.*

/**
 * 根据cwtConfigProperty配置对scriptProperty进行匹配。
 */
fun matchContent(element: ParadoxScriptProperty, config: CwtConfigProperty, project: Project): Boolean {
	val propertiesConfig = config.properties ?: emptyList() //不应该为null，转为emptyList
	if(propertiesConfig.isEmpty()) return true //config为空表示匹配
	val properties = (element.propertyValue?.value as? ParadoxScriptBlock)?.propertyList ?: return true //脚本未写完
	return doMatchContent(properties, propertiesConfig, project)
}

private fun doMatchContent(properties: List<ParadoxScriptProperty>, propertiesConfig: List<CwtConfigProperty>, project: Project): Boolean {
	//注意：propConfig.name可能有重复，这种情况下只要有其中一个匹配即可
	//递归对内容进行匹配
	for(propConfig in propertiesConfig) {
		val prop = properties.find { it.name == propConfig.key }
		if(prop == null) {
			//如果对应的property不存在但propertyConfig是可选的，那么跳过，否则说明不匹配
			if(propConfig.cardinality?.min == 0) continue else return false
		} else {
			val propValue = prop.propertyValue?.value
			if(propValue == null) {
				//对于propertyValue同样这样判断（可能脚本没有写完）
				if(propConfig.cardinality?.min == 0) continue else return false
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
						if(propsConfig.isEmpty()) continue
						val props = (prop.propertyValue?.value as? ParadoxScriptBlock)?.propertyList ?: continue //脚本未写完
						//继续递归匹配
						if(!doMatchContent(props, propsConfig, project)) return false
					}
					propConfig.stringValue != null -> {
						val stringValue = propConfig.stringValue ?: return false //不应该为null
						when {
							//字符串"any"表示匹配任意内容
							stringValue == "any" -> continue
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
								val expression = stringValue.resolveIntExpression()?: return false
								if(propValue.intValue !in expression) return false
							}
							//字符串"float"表示匹配任意scriptInt
							stringValue == "float" -> {
								if(propValue !is ParadoxScriptFloat) return false
							}
							//字符串格式是"float[m..n]"表示匹配范围内的scriptFloat
							stringValue.isFloatExpression() -> {
								if(propValue !is ParadoxScriptFloat) return false
								val expression = stringValue.resolveFloatExpression()?: return false
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
								val resolved = findLocalisation(key, null, project) ?: return false
								if(!resolved.isLocalisation()) return false
							}
							//TODO 不能保证，暂时弄成存在任意locale的都可以
							//字符串"localisation_synced"表示匹配任意localisation_synced的key
							stringValue == "localisation_synced" -> {
								if(propValue !is ParadoxScriptString) return false
								val key = propValue.stringValue.ifEmpty { return false }
								val resolved = findLocalisation(key, null, project) ?: return false
								if(!resolved.isLocalisationSynced()) return false
							}
							//TODO 不能保证，需要验证
							//字符串格式是"<type.subtype>"的情况，表示匹配对应类型的definition
							stringValue.isCwtTypeExpression() -> {
								if(propValue !is ParadoxScriptString) return false
								val typeExpression = stringValue.resolveCwtTypeExpression() //应该保证是合法的，这里不进行额外判断
								val key = propValue.stringValue.ifEmpty { return false }
								if(findDefinition(key, typeExpression, project) == null) return false
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
	}
	return true
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

private fun String.isCwtTypeExpression(): Boolean {
	return surroundsWith('<', '>')
}

private fun String.resolveCwtTypeExpression(): String {
	return substring(1, length - 1)
}