package icu.windea.pls

import com.intellij.codeInsight.lookup.*
import icu.windea.pls.config.*
import icu.windea.pls.script.psi.*

//Expression Extensions

/**
 * 从表达式字符串得到cwtExpression。包含表达式的类型和值。
 */
fun String.resolveCwtExpression(): CwtExpression {
	return when {
		this == "any" -> {
			CwtExpression(CwtExpressionType.Any)
		}
		this == "bool" -> {
			CwtExpression(CwtExpressionType.Bool)
		}
		this == "int" -> {
			CwtExpression(CwtExpressionType.Int)
		}
		surroundsWith("int[", "]") -> {
			CwtExpression(CwtExpressionType.IntExpression, substring(4, length - 1))
		}
		this == "float" -> {
			CwtExpression(CwtExpressionType.Float)
		}
		surroundsWith("float[", "]") -> {
			CwtExpression(CwtExpressionType.FloatExpression, substring(6, length - 1))
		}
		this == "scalar" -> {
			CwtExpression(CwtExpressionType.Scalar)
		}
		this == "percentage_field" -> {
			CwtExpression(CwtExpressionType.PercentageField)
		}
		this == "color" -> {
			CwtExpression(CwtExpressionType.Color)
		}
		this == " localisation" -> {
			CwtExpression(CwtExpressionType.Localisation)
		}
		this == "localisation_synced" -> {
			CwtExpression(CwtExpressionType.SyncedLocalisation)
		}
		this == "localisation_inline" -> {
			CwtExpression(CwtExpressionType.InlineLocalisation)
		}
		this == "filepath" -> {
			CwtExpression(CwtExpressionType.FilePath)
		}
		surroundsWith("filepath[", "]") -> {
			CwtExpression(CwtExpressionType.FilePathExpression, substring(9, length - 1))
		}
		surroundsWith("icon[", "]") -> {
			CwtExpression(CwtExpressionType.IconExpression, substring(5, length - 1))
		}
		this == "date_field" -> {
			CwtExpression(CwtExpressionType.DateField)
		}
		surroundsWith('<', '>') -> {
			CwtExpression(CwtExpressionType.TypeExpression, substring(1, length - 1))
		}
		indexOf('<').let { it > 0 && it < indexOf('>') } -> {
			CwtExpression(CwtExpressionType.TypeExpressionString, substring(indexOf('<'), indexOf('>')))
		}
		surroundsWith("enum[", "]") -> {
			CwtExpression(CwtExpressionType.EnumExpression, substring(5, length - 1))
		}
		surroundsWith("scope[", "]") -> {
			CwtExpression(CwtExpressionType.ScopeExpression, substring(6, length - 1))
		}
		this == "scope_field" -> {
			CwtExpression(CwtExpressionType.ScopeField)
		}
		this == "variable_field" -> {
			CwtExpression(CwtExpressionType.VariableField)
		}
		this.surroundsWith("variable_field[", "]") -> {
			CwtExpression(CwtExpressionType.VariableFieldExpression, substring(15, length - 1))
		}
		this == "int_variable_field" -> {
			CwtExpression(CwtExpressionType.IntVariableField)
		}
		this.surroundsWith("int_variable_field[", "]") -> {
			CwtExpression(CwtExpressionType.IntVariableFieldExpression, substring(19, length - 1))
		}
		this == "value_field" -> {
			CwtExpression(CwtExpressionType.ValueField)
		}
		this.surroundsWith("value_field[", "]") -> {
			CwtExpression(CwtExpressionType.ValueFieldExpression, substring(12, length - 1))
		}
		this == "int_value_field" -> {
			CwtExpression(CwtExpressionType.IntValueField)
		}
		this.surroundsWith("int_value_field[", "]") -> {
			CwtExpression(CwtExpressionType.IntValueFieldExpression, substring(16, length - 1))
		}
		surroundsWith("alias_name[", "]") -> {
			CwtExpression(CwtExpressionType.AliasNameExpression, substring(11, length - 1))
		}
		surroundsWith("alias_match_left[", "]") -> {
			CwtExpression(CwtExpressionType.AliasNameExpression, substring(17, length - 1))
		}
		else -> {
			CwtExpression(CwtExpressionType.Constant, this)
		}
	}
}

/**
 * 匹配cwtExpression和propertyValue。
 */
fun CwtExpression.matchesPropertyValue(value:ParadoxScriptValue,config: CwtConfigGroupCache):Boolean{
	val (expressionType,expressionValue) = this
	when(expressionType) {
		CwtExpressionType.Any -> pass()
		CwtExpressionType.Bool -> {
			if(value !is ParadoxScriptBoolean) return false
		}
		CwtExpressionType.Int -> {
			if(value !is ParadoxScriptInt) return false
		}
		CwtExpressionType.IntExpression -> {
			if(value !is ParadoxScriptInt) return false
			val intRange = expressionValue?.toIntRange() ?: return false
			if(value.intValue !in intRange) return false
		}
		CwtExpressionType.Float -> {
			if(value !is ParadoxScriptFloat) return false
		}
		CwtExpressionType.FloatExpression -> {
			if(value !is ParadoxScriptFloat) return false
			val floatRange = expressionValue?.toFloatRange() ?: return false
			if(value.floatValue !in floatRange) return false
		}
		CwtExpressionType.Scalar -> {
			if(value !is ParadoxScriptString) return false
		}
		CwtExpressionType.PercentageField -> {
			if(value !is ParadoxScriptString) return false
			if(!value.stringValue.isPercentageField()) return false
		}
		CwtExpressionType.Color -> {
			if(value !is ParadoxScriptString) return false
			if(!value.stringValue.isColor()) return false
		}
		CwtExpressionType.Localisation -> {
			if(value !is ParadoxScriptString) return false
			val key = value.stringValue.ifEmpty { return false }
			val resolved = findLocalisation(key, null, config.project) ?: return false //任意locale都可以
			if(!resolved.isLocalisation()) return false
		}
		CwtExpressionType.SyncedLocalisation -> {
			if(value !is ParadoxScriptString) return false
			val key = value.stringValue.ifEmpty { return false }
			val resolved = findSyncedLocalisation(key, null, config.project) ?: return false //任意locale都可以
			if(!resolved.isLocalisationSynced()) return false
		}
		CwtExpressionType.InlineLocalisation -> {
			if(value !is ParadoxScriptString) return false
			if(value.isQuoted) return true //如果用引号括起，则可以是任意字符串，否则必须是localisation
			val key = value.stringValue.ifEmpty { return false }
			val resolved = findLocalisation(key, null, config.project) ?: return false //任意locale都可以
			if(!resolved.isLocalisation()) return false
		}
		CwtExpressionType.FilePath -> {
			//TODO
		}
		CwtExpressionType.FilePathExpression -> {
			//TODO
		}
		CwtExpressionType.IconExpression -> {
			//TODO
		}
		CwtExpressionType.DateField -> {
			if(value !is ParadoxScriptString) return false
			if(!value.stringValue.isDateField()) return false
		}
		CwtExpressionType.TypeExpression -> {
			if(value !is ParadoxScriptString) return false
			val key = value.stringValue.ifEmpty { return false }
			val typeExpression = expressionValue ?: return false
			if(findDefinition(key, typeExpression, config.project) == null) return false
		}
		CwtExpressionType.TypeExpressionString -> {
			if(value !is ParadoxScriptString) return false
			val key = value.stringValue.ifEmpty { return false }
			val typeExpression = expressionValue ?: return false
			if(findDefinition(key, typeExpression, config.project) == null) return false
		}
		CwtExpressionType.EnumExpression -> {
			if(value !is ParadoxScriptString) return false
			val enumExpression = expressionValue ?: return false
			val enumValue = value.stringValue.ifEmpty { return false }
			val enumValues = config.enums[enumExpression]?.values ?: return false
			if(enumValue !in enumValues) return false
		}
		CwtExpressionType.ScopeExpression -> {
			//TODO
		}
		CwtExpressionType.ScopeField -> {
			//TODO
		}
		CwtExpressionType.VariableField -> {
			if(value !is ParadoxScriptString) return false
			if(!value.stringValue.isVariableField()) return false
			//TODO
		}
		CwtExpressionType.VariableFieldExpression -> {
			if(value !is ParadoxScriptString) return false
			if(!value.stringValue.isVariableField()) return false
			//TODO
		}
		CwtExpressionType.IntVariableField -> {
			if(value !is ParadoxScriptString) return false
			if(!value.stringValue.isVariableField()) return false
			//TODO
		}
		CwtExpressionType.IntVariableFieldExpression -> {
			if(value !is ParadoxScriptString) return false
			if(!value.stringValue.isVariableField()) return false
			//TODO
		}
		CwtExpressionType.ValueField -> {
			//TODO
		}
		CwtExpressionType.ValueFieldExpression -> {
			//TODO
		}
		CwtExpressionType.IntValueField -> {
			//TODO
		}
		CwtExpressionType.IntValueFieldExpression -> {
			//TODO
		}
		CwtExpressionType.AliasNameExpression -> {
			//TODO
		}
		CwtExpressionType.AliasMatchLeftExpression -> {
			//TODO
		}
		CwtExpressionType.Constant -> {
			if(value !is ParadoxScriptString) return false
			if(value.stringValue != expressionValue) return false
		}
	}
	return true
}

//Match Content Extensions

/**
 * 根据cwtConfigProperty配置对scriptProperty进行匹配。
 */
fun matchContent(element: ParadoxDefinitionProperty, elementConfig: CwtConfigProperty, config: CwtConfigGroupCache): Boolean {
	val propertiesConfig = elementConfig.properties.orEmpty() //不应该为null，转为emptyList
	if(propertiesConfig.isEmpty()) return true //config为空表示匹配
	val properties = element.properties
	return doMatchContent(properties, propertiesConfig, config)
}

private fun doMatchContent(properties: List<ParadoxScriptProperty>, propertiesConfig: List<CwtConfigProperty>, config: CwtConfigGroupCache): Boolean {
	//注意：propConfig.key可能有重复，这种情况下只要有其中一个匹配即可
	//注意：这里只兼容连续的相同key的propConfig，重复的情况下只有其中一个匹配即可
	//递归对内容进行匹配
	var tempConfigKey = ""
	var tempResult = true
	for(propertyConfig in propertiesConfig) {
		//如果上一个配置名字相同但不匹配，这一次继续匹配，不中断，如果匹配的话那么无论如何都直接跳过，不再匹配
		if(tempConfigKey.isNotEmpty()) {
			if(!tempResult) {
				if(tempConfigKey != propertyConfig.key) return false
			} else {
				continue
			}
		}
		//TODO 这里的propertyConfig.key也是一个表达式
		val prop = properties.find { it.name == propertyConfig.key }
		if(prop == null) {
			//如果对应的prop不存在但propConfig是可选的，那么跳过，否则说明不匹配
			//如果对应的propConfig有多个，这些都应该是可选的，这里不验证
			return propertyConfig.cardinality?.min == 0
		} else {
			//这里的propConfig可能有多个匹配的！
			tempConfigKey = propertyConfig.key
			tempResult = doMatchConfig(prop, propertyConfig, config)
		}
	}
	return tempResult
}

private fun doMatchConfig(property: ParadoxScriptProperty, propertyConfig: CwtConfigProperty, config: CwtConfigGroupCache): Boolean {
	val propValue = property.propertyValue?.value
	if(propValue == null) {
		//对于propertyValue同样这样判断（可能脚本没有写完）
		return propertyConfig.cardinality?.min == 0
	} else {
		when {
			//布尔值要完全匹配
			propertyConfig.booleanValue != null -> {
				if(propValue !is ParadoxScriptBoolean || propValue.booleanValue != propertyConfig.booleanValue) return false
			}
			//块（数组/对象）需要匹配
			propertyConfig.properties != null && propertyConfig.values != null -> {
				if(propValue !is ParadoxScriptBlock) return false
				//如果这里的propsConfig是空的，那么就跳过继续匹配
				val propsConfig = propertyConfig.properties.orEmpty() //不应该为null，转为emptyList
				if(propsConfig.isEmpty()) return true
				val props = (property.propertyValue?.value as? ParadoxScriptBlock)?.propertyList ?: return true //脚本未写完
				//继续递归匹配
				if(!doMatchContent(props, propsConfig, config)) return false
			}
			propertyConfig.stringValue != null -> {
				val stringValue = propertyConfig.stringValue
				if(stringValue.isEmpty()) return false //不应该为空
				val expression = stringValue.resolveCwtExpression()
				return expression.matchesPropertyValue(propValue,config)
			}
		}
	}
	return true
}

fun addPropertyNameCompletions(property: ParadoxDefinitionProperty): List<LookupElement> {
	val project = property.project
	val propertyInfo = property.paradoxDefinitionPropertyInfo ?: return emptyList()
	val path = propertyInfo.path
	val existPropertyNames = propertyInfo.existPropertyNames
	val definitionInfo = propertyInfo.definitionInfo
	val type = definitionInfo.type
	val subtypes = definitionInfo.subtypes
	val gameType = definitionInfo.fileInfo.gameType
	val definitionConfig = getConfig(project).getValue(gameType).definitions.getValue(type).mergeConfig(subtypes)
	val result = mutableListOf<LookupElement>()
	
	//如果path是空的，表示需要补全definition的顶级属性		
	if(path.isEmpty()) {
		doAddRootPropertyNameCompletions(definitionConfig, existPropertyNames, result)
	}
	return result
}

private fun doAddRootPropertyNameCompletions(definitionConfig: List<CwtConfigProperty>, existPropertyNames: List<String>, result: MutableList<LookupElement>) {
	for(propConfig in definitionConfig) {
		val expression = propConfig.key
		//如果cardinality的最小值为0，则提示（cardinality未声明也提示）
		if(propConfig.cardinality?.min != 0) {
			//when {
			//	//字符串"bool"表示匹配任意boolean
			//	expression == "bool" -> pass() //不提示
			//	//字符串"int"表示匹配任意int
			//	expression == "int" -> pass() //不提示
			//	//字符串格式是"int[m..n]"表示匹配范围内int
			//	isIntExpression(expression) -> pass() //不提示
			//	//字符串"float"表示匹配任意float
			//	expression == "float" -> pass() //不提示
			//	
			//}
			//
			//val typeName = resolveTypeName(expression)
			//if(typeName != null) {
			//	//TODO
			//	continue
			//}
			////propConfigName可能表示alias_name，如：alias_name[trigger]
			//val aliasNameName = resolveAliasNameName(expression)
			//if(aliasNameName != null) {
			//	//TODO
			//	continue
			//}
			//
			////其他情况都不匹配，那么pattern就表示需要提示的propName
			////如果pattern对应的propName并非已存在，则提示
			//if(expression !in existPropertyNames) {
			//	val propElement = propConfig.pointer.element ?: continue
			//	val lookupElement = LookupElementBuilder.create(propElement, expression)
			//	result.add(lookupElement)
			//}
		}
	}
}