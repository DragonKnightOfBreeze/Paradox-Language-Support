package icu.windea.pls

import com.intellij.codeInsight.lookup.*
import icu.windea.pls.config.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*

//Cwt Expression Extensions

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

fun matchesKey(expression: CwtExpression, key: ParadoxScriptPropertyKey, configGroup: CwtConfigGroup): Boolean {
	val (expressionType, expressionValue) = expression
	return when(expressionType) {
		CwtExpressionType.Any -> true
		CwtExpressionType.Bool -> {
			val value = key.value
			value.isBooleanYesNo()
		}
		CwtExpressionType.Int -> {
			key.value.isInt()
		}
		CwtExpressionType.IntExpression -> {
			val value = key.value
			value.isInt() && expressionValue?.toIntRange().let { it == null || value.toInt() in it }
		}
		CwtExpressionType.Float -> {
			key.value.isFloat()
		}
		CwtExpressionType.FloatExpression -> {
			val value = key.value
			value.isFloat() && expressionValue?.toFloatRange().let { it == null || value.toFloat() in it }
		}
		CwtExpressionType.Scalar -> {
			key.value.isString()
		}
		CwtExpressionType.PercentageField -> {
			key.value.isPercentageField()
		}
		CwtExpressionType.Color -> {
			key.value.isColor()
		}
		CwtExpressionType.Localisation -> {
			hasLocalisation(key.value, null, configGroup.project)
		}
		CwtExpressionType.SyncedLocalisation -> {
			hasSyncedLocalisation(key.value, null, configGroup.project)
		}
		CwtExpressionType.InlineLocalisation -> {
			if(key.isQuoted) true else hasLocalisation(key.value, null, configGroup.project)
		}
		CwtExpressionType.FilePath -> {
			true //TODO
		}
		CwtExpressionType.FilePathExpression -> {
			true //TODO
		}
		CwtExpressionType.IconExpression -> {
			true //TODO
		}
		CwtExpressionType.DateField -> {
			key.value.isDateField()
		}
		CwtExpressionType.TypeExpression -> {
			run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(key.value, typeExpression, configGroup.project)
			}
		}
		CwtExpressionType.TypeExpressionString -> {
			run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(key.value, typeExpression, configGroup.project)
			}
		}
		CwtExpressionType.EnumExpression -> {
			run {
				val enumExpression = expressionValue ?: return@run false
				val enumValues = configGroup.enums[enumExpression]?.values ?: return@run false
				key.value in enumValues
			}
		}
		CwtExpressionType.ScopeExpression -> {
			true //TODO
		}
		CwtExpressionType.ScopeField -> {
			true //TODO
		}
		CwtExpressionType.VariableField -> {
			key.value.isVariableField() //TODO
		}
		CwtExpressionType.VariableFieldExpression -> {
			key.value.isVariableField() //TODO
		}
		CwtExpressionType.IntVariableField -> {
			key.value.isVariableField() //TODO
		}
		CwtExpressionType.IntVariableFieldExpression -> {
			key.value.isVariableField() //TODO
		}
		CwtExpressionType.ValueField -> {
			true //TODO
		}
		CwtExpressionType.ValueFieldExpression -> {
			true //TODO
		}
		CwtExpressionType.IntValueField -> {
			true //TODO
		}
		CwtExpressionType.IntValueFieldExpression -> {
			true //TODO
		}
		CwtExpressionType.AliasNameExpression -> {
			true //TODO
		}
		CwtExpressionType.AliasMatchLeftExpression -> {
			true //TODO
		}
		CwtExpressionType.Constant -> {
			key.value == expressionValue
		}
	}
}


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

fun matchesValue(expression: CwtExpression, value: ParadoxScriptValue, configGroup: CwtConfigGroup): Boolean {
	val (expressionType, expressionValue) = expression
	return when(expressionType) {
		CwtExpressionType.Any -> true
		CwtExpressionType.Bool -> {
			value is ParadoxScriptBoolean
		}
		CwtExpressionType.Int -> {
			value is ParadoxScriptInt
		}
		CwtExpressionType.IntExpression -> {
			value is ParadoxScriptInt && expressionValue?.toIntRange().let { it == null || value.intValue in it }
		}
		CwtExpressionType.Float -> {
			value is ParadoxScriptFloat
		}
		CwtExpressionType.FloatExpression -> {
			value is ParadoxScriptFloat && expressionValue?.toFloatRange().let { it == null || value.floatValue in it }
		}
		CwtExpressionType.Scalar -> {
			value is ParadoxScriptString
		}
		CwtExpressionType.PercentageField -> {
			value is ParadoxScriptString && value.stringValue.isPercentageField()
		}
		CwtExpressionType.Color -> {
			value is ParadoxScriptString && value.stringValue.isColor()
		}
		CwtExpressionType.Localisation -> {
			value is ParadoxScriptString && hasLocalisation(value.stringValue, null, configGroup.project)
		}
		CwtExpressionType.SyncedLocalisation -> {
			value is ParadoxScriptString && hasSyncedLocalisation(value.stringValue, null, configGroup.project)
		}
		CwtExpressionType.InlineLocalisation -> {
			value is ParadoxScriptString && run {
				if(value.isQuoted) true else hasLocalisation(value.stringValue, null, configGroup.project)
			}
		}
		CwtExpressionType.FilePath -> {
			true //TODO
		}
		CwtExpressionType.FilePathExpression -> {
			true //TODO
		}
		CwtExpressionType.IconExpression -> {
			true //TODO
		}
		CwtExpressionType.DateField -> {
			value is ParadoxScriptString && value.stringValue.isDateField()
		}
		CwtExpressionType.TypeExpression -> {
			value is ParadoxScriptString && run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(value.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtExpressionType.TypeExpressionString -> {
			value is ParadoxScriptString && run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(value.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtExpressionType.EnumExpression -> {
			value is ParadoxScriptString && run {
				val enumExpression = expressionValue ?: return@run false
				val enumValues = configGroup.enums[enumExpression]?.values ?: return@run false
				value.stringValue in enumValues
			}
		}
		CwtExpressionType.ScopeExpression -> {
			true //TODO
		}
		CwtExpressionType.ScopeField -> {
			true //TODO
		}
		CwtExpressionType.VariableField -> {
			value is ParadoxScriptString && value.stringValue.isVariableField() //TODO
		}
		CwtExpressionType.VariableFieldExpression -> {
			value is ParadoxScriptString && value.stringValue.isVariableField() //TODO
		}
		CwtExpressionType.IntVariableField -> {
			value is ParadoxScriptString && value.stringValue.isVariableField() //TODO
		}
		CwtExpressionType.IntVariableFieldExpression -> {
			value is ParadoxScriptString && value.stringValue.isVariableField() //TODO
		}
		CwtExpressionType.ValueField -> {
			true //TODO
		}
		CwtExpressionType.ValueFieldExpression -> {
			true //TODO
		}
		CwtExpressionType.IntValueField -> {
			true //TODO
		}
		CwtExpressionType.IntValueFieldExpression -> {
			true //TODO
		}
		CwtExpressionType.AliasNameExpression -> {
			true //TODO
		}
		CwtExpressionType.AliasMatchLeftExpression -> {
			true //TODO
		}
		CwtExpressionType.Constant -> {
			value is ParadoxScriptString && value.stringValue == expressionValue
		}
	}
}

fun completeKey(
	expression: CwtExpression, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo,
	configGroup: CwtConfigGroup,result: MutableList<LookupElement>
){
	val (expressionType,expressionValue) = expression
	when(expressionType){
		CwtExpressionType.Any -> pass()
		CwtExpressionType.Bool -> pass()
		CwtExpressionType.Int -> pass()
		CwtExpressionType.IntExpression -> pass()
		CwtExpressionType.Float -> pass()
		CwtExpressionType.FloatExpression -> pass()
		CwtExpressionType.Scalar -> pass()
		CwtExpressionType.PercentageField -> pass()
		CwtExpressionType.Color -> pass()
		CwtExpressionType.Localisation -> TODO()
		CwtExpressionType.SyncedLocalisation -> TODO()
		CwtExpressionType.InlineLocalisation -> TODO()
		CwtExpressionType.FilePath -> TODO()
		CwtExpressionType.FilePathExpression -> TODO()
		CwtExpressionType.IconExpression -> TODO()
		CwtExpressionType.DateField -> TODO()
		CwtExpressionType.TypeExpression -> TODO()
		CwtExpressionType.TypeExpressionString -> TODO()
		CwtExpressionType.EnumExpression -> TODO()
		CwtExpressionType.ScopeExpression -> TODO()
		CwtExpressionType.ScopeField -> TODO()
		CwtExpressionType.VariableField -> TODO()
		CwtExpressionType.VariableFieldExpression -> TODO()
		CwtExpressionType.IntVariableField -> TODO()
		CwtExpressionType.IntVariableFieldExpression -> TODO()
		CwtExpressionType.ValueField -> TODO()
		CwtExpressionType.ValueFieldExpression -> TODO()
		CwtExpressionType.IntValueField -> TODO()
		CwtExpressionType.IntValueFieldExpression -> TODO()
		CwtExpressionType.AliasNameExpression -> TODO()
		CwtExpressionType.AliasMatchLeftExpression -> TODO()
		CwtExpressionType.Constant -> TODO()
	}
}

fun completeValue(
	expression: CwtExpression, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo,
	configGroup: CwtConfigGroup, result: MutableList<LookupElement>
) {
	
}

//Match Content Extensions

/**
 * 根据cwtConfigProperty配置对scriptProperty进行匹配。
 */
fun matchProperty(element: ParadoxDefinitionProperty, elementConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
	val propertiesConfig = elementConfig.properties.orEmpty() //不应该为null，转为emptyList
	if(propertiesConfig.isEmpty()) return true //config为空表示匹配
	val properties = element.properties
	return doMatchProperties(properties, propertiesConfig, configGroup)
}

private fun doMatchProperties(properties: List<ParadoxScriptProperty>, propertiesConfig: List<CwtPropertyConfig>, configGroup: CwtConfigGroup): Boolean {
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
		val prop = properties.find {
			//propertyConfig.key可以是一个表达式
			val expression = propertyConfig.key.resolveCwtExpression()
			matchesKey(expression, it.propertyKey, configGroup)
		}
		if(prop == null) {
			//如果对应的prop不存在但propConfig是可选的，那么跳过，否则说明不匹配
			//如果对应的propConfig有多个，这些都应该是可选的，这里不验证
			return propertyConfig.cardinality?.min == 0
		} else {
			//这里的propConfig可能有多个匹配的！
			tempConfigKey = propertyConfig.key
			tempResult = doMatchProperty(prop, propertyConfig, configGroup)
		}
	}
	return tempResult
}

private fun doMatchProperty(property: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
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
				if(!doMatchProperties(props, propsConfig, configGroup)) return false
			}
			propertyConfig.stringValue != null -> {
				//propertyConfig.stringValue可以是一个表达式
				val expression = propertyConfig.stringValue.resolveCwtExpression()
				return matchesValue(expression, propValue, configGroup)
			}
		}
	}
	return true
}

//Add Completions Extensions

fun addKeyCompletions(property: ParadoxDefinitionProperty): List<LookupElement> {
	val project = property.project
	val definitionPropertyInfo = property.paradoxDefinitionPropertyInfo ?: return emptyList()
	val path = definitionPropertyInfo.path
	val definitionInfo = definitionPropertyInfo.definitionInfo
	val type = definitionInfo.type
	val subtypes = definitionInfo.subtypes
	val gameType = definitionInfo.fileInfo.gameType
	val configGroup = getConfig(project).getValue(gameType)
	val propertiesConfig = configGroup.definitions.getValue(type).mergeConfig(subtypes)
	val result = mutableListOf<LookupElement>()
	
	//如果path是空的，表示需要补全definition的顶级属性		
	if(path.isEmpty()) {
		doAddRootKeyCompletions(propertiesConfig, definitionPropertyInfo, configGroup, result)
	}
	return result
}

private fun doAddRootKeyCompletions(propertiesConfig: List<CwtPropertyConfig>, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, configGroup: CwtConfigGroup, result: MutableList<LookupElement>) {
	for(propertyConfig in propertiesConfig) {
		//如果cardinality的最大值为1，则需要过滤掉已存在的属性名
		//propertyConfig.key可以是一个表达式
		val expression = propertyConfig.key.resolveCwtExpression()
		val r = completeKey(expression, propertyConfig, definitionPropertyInfo, configGroup,result)
	}
}