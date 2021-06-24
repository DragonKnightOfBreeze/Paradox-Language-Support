package icu.windea.pls

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

//Constants

private val separatorChars = charArrayOf('=', '<', '>', '!')

private val separatorInsertHandler = InsertHandler<LookupElement> { context, _ ->
	//如果后面没有分隔符，则要加上等号，并且根据代码格式设置来判断是否加上等号周围的空格
	val editor = context.editor
	val document = editor.document
	val chars = document.charsSequence
	val charsLength = chars.length
	val oldOffset = editor.selectionModel.selectionEnd
	var offset = oldOffset
	while(offset < charsLength && chars[offset].isWhitespace()) {
		offset++
	}
	if(offset < charsLength && chars[offset] !in separatorChars) {
		val customSettings = CodeStyle.getCustomSettings(context.file, ParadoxScriptCodeStyleSettings::class.java)
		val spaceAroundSeparator = customSettings.SPACE_AROUND_SEPARATOR
		val separator = if(spaceAroundSeparator) " = " else "="
		EditorModificationUtil.insertStringAtCaret(editor, separator)
	}
}

//Resolve Extensions

fun String.resolveKeyExpression(): CwtKeyExpression {
	return when {
		this == "any" -> {
			CwtKeyExpression(CwtKeyExpressionType.Any)
		}
		this == "bool" -> {
			CwtKeyExpression(CwtKeyExpressionType.Bool)
		}
		this == "int" -> {
			CwtKeyExpression(CwtKeyExpressionType.Int)
		}
		surroundsWith("int[", "]") -> {
			CwtKeyExpression(CwtKeyExpressionType.IntExpression, substring(4, length - 1))
		}
		this == "float" -> {
			CwtKeyExpression(CwtKeyExpressionType.Float)
		}
		surroundsWith("float[", "]") -> {
			CwtKeyExpression(CwtKeyExpressionType.FloatExpression, substring(6, length - 1))
		}
		this == "scalar" -> {
			CwtKeyExpression(CwtKeyExpressionType.Scalar)
		}
		this == " localisation" -> {
			CwtKeyExpression(CwtKeyExpressionType.Localisation)
		}
		this == "localisation_synced" -> {
			CwtKeyExpression(CwtKeyExpressionType.SyncedLocalisation)
		}
		this == "localisation_inline" -> {
			CwtKeyExpression(CwtKeyExpressionType.InlineLocalisation)
		}
		surroundsWith('<', '>') -> {
			CwtKeyExpression(CwtKeyExpressionType.TypeExpression, substring(1, length - 1))
		}
		indexOf('<').let { it > 0 && it < indexOf('>') } -> {
			CwtKeyExpression(CwtKeyExpressionType.TypeExpressionString, substring(indexOf('<'), indexOf('>')))
		}
		surroundsWith("enum[", "]") -> {
			CwtKeyExpression(CwtKeyExpressionType.EnumExpression, substring(5, length - 1))
		}
		surroundsWith("scope[", "]") -> {
			CwtKeyExpression(CwtKeyExpressionType.ScopeExpression, substring(6, length - 1))
		}
		surroundsWith("alias_name[", "]") -> {
			CwtKeyExpression(CwtKeyExpressionType.AliasNameExpression, substring(11, length - 1))
		}
		else -> {
			CwtKeyExpression(CwtKeyExpressionType.Constant, this)
		}
	}
}

fun String.resolveValueExpression(): CwtValueExpression {
	return when {
		this == "any" -> {
			CwtValueExpression(CwtValueExpressionType.Any)
		}
		this == "bool" -> {
			CwtValueExpression(CwtValueExpressionType.Bool)
		}
		this == "int" -> {
			CwtValueExpression(CwtValueExpressionType.Int)
		}
		surroundsWith("int[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.IntExpression, substring(4, length - 1))
		}
		this == "float" -> {
			CwtValueExpression(CwtValueExpressionType.Float)
		}
		surroundsWith("float[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.FloatExpression, substring(6, length - 1))
		}
		this == "scalar" -> {
			CwtValueExpression(CwtValueExpressionType.Scalar)
		}
		this == "percentage_field" -> {
			CwtValueExpression(CwtValueExpressionType.PercentageField)
		}
		this == "color_field" -> {
			CwtValueExpression(CwtValueExpressionType.ColorField)
		}
		this == " localisation" -> {
			CwtValueExpression(CwtValueExpressionType.Localisation)
		}
		this == "localisation_synced" -> {
			CwtValueExpression(CwtValueExpressionType.SyncedLocalisation)
		}
		this == "localisation_inline" -> {
			CwtValueExpression(CwtValueExpressionType.InlineLocalisation)
		}
		this == "filepath" -> {
			CwtValueExpression(CwtValueExpressionType.FilePath)
		}
		surroundsWith("filepath[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.FilePathExpression, substring(9, length - 1))
		}
		surroundsWith("icon[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.IconExpression, substring(5, length - 1))
		}
		this == "date_field" -> {
			CwtValueExpression(CwtValueExpressionType.DateField)
		}
		surroundsWith('<', '>') -> {
			CwtValueExpression(CwtValueExpressionType.TypeExpression, substring(1, length - 1))
		}
		indexOf('<').let { it > 0 && it < indexOf('>') } -> {
			CwtValueExpression(CwtValueExpressionType.TypeExpressionString, substring(indexOf('<'), indexOf('>')))
		}
		surroundsWith("enum[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.EnumExpression, substring(5, length - 1))
		}
		surroundsWith("scope[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.ScopeExpression, substring(6, length - 1))
		}
		this == "scope_field" -> {
			CwtValueExpression(CwtValueExpressionType.ScopeField)
		}
		this == "variable_field" -> {
			CwtValueExpression(CwtValueExpressionType.VariableField)
		}
		this.surroundsWith("variable_field[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.VariableFieldExpression, substring(15, length - 1))
		}
		this == "int_variable_field" -> {
			CwtValueExpression(CwtValueExpressionType.IntVariableField)
		}
		this.surroundsWith("int_variable_field[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.IntVariableFieldExpression, substring(19, length - 1))
		}
		this == "value_field" -> {
			CwtValueExpression(CwtValueExpressionType.ValueField)
		}
		this.surroundsWith("value_field[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.ValueFieldExpression, substring(12, length - 1))
		}
		this == "int_value_field" -> {
			CwtValueExpression(CwtValueExpressionType.IntValueField)
		}
		this.surroundsWith("int_value_field[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.IntValueFieldExpression, substring(16, length - 1))
		}
		surroundsWith("alias_match_left[", "]") -> {
			CwtValueExpression(CwtValueExpressionType.AliasMatchLeftExpression, substring(17, length - 1))
		}
		else -> {
			CwtValueExpression(CwtValueExpressionType.Constant, this)
		}
	}
}

//Match Extensions

fun matchDefinitionProperty(propertyElement: ParadoxDefinitionProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
	when {
		propertyConfig.properties != null && propertyConfig.properties.isNotEmpty() -> {
			val propConfigs = propertyConfig.properties.orEmpty() //不应该为null，转为emptyList
			val props = propertyElement.properties
			if(!matchProperties(props, propConfigs, configGroup)) return false //继续匹配
		}
		propertyConfig.values != null && propertyConfig.values.isNotEmpty() -> {
			val valueConfigs = propertyConfig.values.orEmpty() //不应该为null，转为emptyList
			val values = propertyElement.values
			if(!matchValues(values, valueConfigs, configGroup)) return false //继续匹配
		}
	}
	return true
}

private fun matchProperties(propertyElements: List<ParadoxScriptProperty>, propertyConfigs: List<CwtPropertyConfig>, configGroup: CwtConfigGroup): Boolean {
	//properties为空的情况系认为匹配
	if(propertyElements.isEmpty()) return true
	
	//要求其中所有的value的值在最终都会小于等于0
	val minMap = propertyConfigs.associateByTo(mutableMapOf(), { it.key }, { it.cardinality?.min ?: 1 }) //默认为1
	
	//注意：propConfig.key可能有重复，这种情况下只要有其中一个匹配即可
	for(property in propertyElements) {
		val propertyKey = property.propertyKey
		val propConfigs = propertyConfigs.filter { propConfig -> matchKey(propConfig.key, propertyKey, configGroup) }
		//如果没有匹配的规则则忽略
		if(propConfigs.isNotEmpty()) {
			val matched = propConfigs.any { propConfig ->
				val matched = matchProperty(property, propConfig, configGroup)
				if(matched) minMap.compute(propConfig.key) { _, v -> if(v == null) 1 else v - 1 }
				matched
			}
			if(!matched) return false
		}
	}
	
	return minMap.values.any { it <= 0 }
}

private fun matchValues(valueElements: List<ParadoxScriptValue>, valueConfigs: List<CwtValueConfig>, configGroup: CwtConfigGroup): Boolean {
	//values为空的情况下认为匹配 
	if(valueElements.isEmpty()) return true
	
	//要求其中所有的value的值在最终都会小于等于0
	val minMap = valueConfigs.associateByTo(mutableMapOf(), { it.value }, { it.cardinality?.min ?: 1 }) //默认为1
	
	for(value in valueElements) {
		//如果没有匹配的规则则认为不匹配
		val matched = valueConfigs.any { valueConfig ->
			val matched = matchValue(valueConfig.value, value, configGroup)
			if(matched) minMap.compute(valueConfig.value) { _, v -> if(v == null) 1 else v - 1 }
			matched
		}
		if(!matched) return false
	}
	
	return minMap.values.any { it <= 0 }
}

fun matchProperty(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
	val propValue = propertyElement.propertyValue?.value
	if(propValue == null) {
		//对于propertyValue同样这样判断（可能脚本没有写完）
		return propertyConfig.cardinality?.min == 0
	} else {
		when {
			//匹配布尔值
			propertyConfig.booleanValue != null -> {
				if(propValue !is ParadoxScriptBoolean || propValue.booleanValue != propertyConfig.booleanValue) return false
			}
			//匹配属性列表
			propertyConfig.properties != null && propertyConfig.properties.isNotEmpty() -> {
				val propConfigs = propertyConfig.properties.orEmpty() //不应该为null，转为emptyList
				val props = propertyElement.properties
				if(!matchProperties(props, propConfigs, configGroup)) return false //继续匹配
			}
			//匹配值列表
			propertyConfig.values != null && propertyConfig.values.isNotEmpty() -> {
				val valueConfigs = propertyConfig.values.orEmpty() //不应该为null，转为emptyList
				val values = propertyElement.values
				if(!matchValues(values, valueConfigs, configGroup)) return false //继续匹配
			}
			//匹配值
			propertyConfig.stringValue != null -> {
				//propertyConfig.stringValue可以是一个表达式
				return matchValue(propertyConfig.stringValue, propValue, configGroup)
			}
		}
	}
	return true
}

private fun matchKey(expression: String, keyElement: ParadoxScriptPropertyKey, configGroup: CwtConfigGroup): Boolean {
	//这里的key=keyElement.value, quoted=keyElement.isQuoted()使用懒加载
	val (expressionType, expressionValue) = expression.resolveKeyExpression()
	return when(expressionType) {
		CwtKeyExpressionType.Any -> true
		CwtKeyExpressionType.Bool -> {
			val key = keyElement.value
			key.isBooleanYesNo()
		}
		CwtKeyExpressionType.Int -> {
			val key = keyElement.value
			key.isInt()
		}
		CwtKeyExpressionType.IntExpression -> {
			val key = keyElement.value
			key.isInt() && expressionValue?.toIntRange().let { it == null || key.toInt() in it }
		}
		CwtKeyExpressionType.Float -> {
			val key = keyElement.value
			key.isFloat()
		}
		CwtKeyExpressionType.FloatExpression -> {
			val key = keyElement.value
			key.isFloat() && expressionValue?.toFloatRange().let { it == null || key.toFloat() in it }
		}
		CwtKeyExpressionType.Scalar -> {
			val key = keyElement.value
			key.isString()
		}
		CwtKeyExpressionType.Localisation -> {
			val key = keyElement.value
			hasLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpressionType.SyncedLocalisation -> {
			val key = keyElement.value
			hasSyncedLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpressionType.InlineLocalisation -> {
			val quoted = keyElement.isQuoted()
			if(quoted) return true
			val key = keyElement.value
			hasLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpressionType.TypeExpression -> {
			val typeExpression = expressionValue ?: return false
			val name = keyElement.value
			hasDefinition(name, typeExpression, configGroup.project)
		}
		CwtKeyExpressionType.TypeExpressionString -> {
			val typeExpression = expressionValue ?: return false
			val key = keyElement.value
			hasDefinition(key, typeExpression, configGroup.project)
		}
		CwtKeyExpressionType.EnumExpression -> {
			val enumExpression = expressionValue ?: return false
			val enumValues = configGroup.enums[enumExpression]?.values ?: return false
			val key = keyElement.value
			key in enumValues
		}
		CwtKeyExpressionType.ScopeExpression -> {
			true //TODO
		}
		CwtKeyExpressionType.AliasNameExpression -> {
			true //TODO
		}
		CwtKeyExpressionType.Constant -> {
			val key = keyElement.value
			key == expressionValue
		}
	}
}

private fun matchKey(expression: String, key: String, quoted:Boolean,configGroup: CwtConfigGroup): Boolean {
	val (expressionType, expressionValue) = expression.resolveKeyExpression()
	return when(expressionType) {
		CwtKeyExpressionType.Any -> true
		CwtKeyExpressionType.Bool -> {
			key.isBooleanYesNo()
		}
		CwtKeyExpressionType.Int -> {
			key.isInt()
		}
		CwtKeyExpressionType.IntExpression -> {
			key.isInt() && expressionValue?.toIntRange().let { it == null || key.toInt() in it }
		}
		CwtKeyExpressionType.Float -> {
			key.isFloat()
		}
		CwtKeyExpressionType.FloatExpression -> {
			key.isFloat() && expressionValue?.toFloatRange().let { it == null || key.toFloat() in it }
		}
		CwtKeyExpressionType.Scalar -> {
			key.isString()
		}
		CwtKeyExpressionType.Localisation -> {
			hasLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpressionType.SyncedLocalisation -> {
			hasSyncedLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpressionType.InlineLocalisation -> {
			if(quoted) return true 
			hasLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpressionType.TypeExpression -> {
			val typeExpression = expressionValue ?: return false
			hasDefinition(key, typeExpression, configGroup.project)
		}
		CwtKeyExpressionType.TypeExpressionString -> {
			val typeExpression = expressionValue ?: return false
			hasDefinition(key, typeExpression, configGroup.project)
		}
		CwtKeyExpressionType.EnumExpression -> {
			val enumExpression = expressionValue ?: return false
			val enumValues = configGroup.enums[enumExpression]?.values ?: return false
			key in enumValues
		}
		CwtKeyExpressionType.ScopeExpression -> {
			true //TODO
		}
		CwtKeyExpressionType.AliasNameExpression -> {
			true //TODO
		}
		CwtKeyExpressionType.Constant -> {
			key == expressionValue
		}
	}
}

private fun matchValue(expression: String, valueElement: ParadoxScriptValue, configGroup: CwtConfigGroup): Boolean {
	val (expressionType, expressionValue) = expression.resolveValueExpression()
	return when(expressionType) {
		CwtValueExpressionType.Any -> true
		CwtValueExpressionType.Bool -> {
			valueElement is ParadoxScriptBoolean
		}
		CwtValueExpressionType.Int -> {
			valueElement is ParadoxScriptInt
		}
		CwtValueExpressionType.IntExpression -> {
			valueElement is ParadoxScriptInt && expressionValue?.toIntRange().let { it == null || valueElement.intValue in it }
		}
		CwtValueExpressionType.Float -> {
			valueElement is ParadoxScriptFloat
		}
		CwtValueExpressionType.FloatExpression -> {
			valueElement is ParadoxScriptFloat && expressionValue?.toFloatRange().let { it == null || valueElement.floatValue in it }
		}
		CwtValueExpressionType.Scalar -> {
			valueElement is ParadoxScriptString
		}
		CwtValueExpressionType.PercentageField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isPercentageField()
		}
		CwtValueExpressionType.ColorField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isColorField()
		}
		CwtValueExpressionType.Localisation -> {
			valueElement is ParadoxScriptString && hasLocalisation(valueElement.stringValue, null, configGroup.project)
		}
		CwtValueExpressionType.SyncedLocalisation -> {
			valueElement is ParadoxScriptString && hasSyncedLocalisation(valueElement.stringValue, null, configGroup.project)
		}
		CwtValueExpressionType.InlineLocalisation -> {
			valueElement is ParadoxScriptString && run {
				if(valueElement.isQuoted()) true else hasLocalisation(valueElement.stringValue, null, configGroup.project)
			}
		}
		CwtValueExpressionType.FilePath -> {
			true //TODO
		}
		CwtValueExpressionType.FilePathExpression -> {
			true //TODO
		}
		CwtValueExpressionType.IconExpression -> {
			true //TODO
		}
		CwtValueExpressionType.DateField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isDateField()
		}
		CwtValueExpressionType.TypeExpression -> {
			valueElement is ParadoxScriptString && run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(valueElement.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpressionType.TypeExpressionString -> {
			valueElement is ParadoxScriptString && run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(valueElement.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpressionType.EnumExpression -> {
			valueElement is ParadoxScriptString && run {
				val enumExpression = expressionValue ?: return@run false
				val enumValues = configGroup.enums[enumExpression]?.values ?: return@run false
				valueElement.stringValue in enumValues
			}
		}
		CwtValueExpressionType.ScopeExpression -> {
			true //TODO
		}
		CwtValueExpressionType.ScopeField -> {
			true //TODO
		}
		CwtValueExpressionType.VariableField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpressionType.VariableFieldExpression -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpressionType.IntVariableField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpressionType.IntVariableFieldExpression -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpressionType.ValueField -> {
			true //TODO
		}
		CwtValueExpressionType.ValueFieldExpression -> {
			true //TODO
		}
		CwtValueExpressionType.IntValueField -> {
			true //TODO
		}
		CwtValueExpressionType.IntValueFieldExpression -> {
			true //TODO
		}
		CwtValueExpressionType.AliasMatchLeftExpression -> {
			true //TODO
		}
		CwtValueExpressionType.Constant -> {
			valueElement is ParadoxScriptString && valueElement.stringValue == expressionValue
		}
	}
}

//Add Completions Extensions

fun addKeyCompletions(keyElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet) {
	//key: propertyKey | value
	val project = propertyElement.project
	val definitionPropertyInfo = propertyElement.paradoxDefinitionPropertyInfo ?: return
	val path = definitionPropertyInfo.path
	val definitionInfo = definitionPropertyInfo.definitionInfo
	val type = definitionInfo.type
	val subtypes = definitionInfo.subtypes
	val gameType = definitionInfo.fileInfo.gameType
	val configGroup = getConfig(project).getValue(gameType)
	
	//rootPropertiesConfig需要经过合并且确保名字唯一
	val rootPropertiesConfig = configGroup.definitions.getValue(type).mergeAndDistinctConfig(subtypes)
	
	//如果path是空的，表示需要补全definition的顶级属性
	val propertyConfigs = getSubPropertiesConfig(rootPropertiesConfig,path,configGroup)
	if(propertyConfigs.isNotEmpty()) {
		for(propertyConfig in propertyConfigs) {
			//propertyConfig.key可以是一个表达式
			completeKey(propertyConfig.key, keyElement, propertyConfig, definitionPropertyInfo, configGroup, result)
		}
	}
}

fun addValueCompletions(keyElement:PsiElement,propertyElement:ParadoxScriptValue,result:CompletionResultSet){
	
}

private fun getSubPropertiesConfig(propertyConfigs: List<CwtPropertyConfig>, path: ParadoxPropertyPath, configGroup: CwtConfigGroup):List<CwtPropertyConfig>{
	if(path.isEmpty()) return propertyConfigs
	var result = propertyConfigs
	for((key,quoted) in path.subPathInfos) {
		result = result.find { matchKey(it.key,key,quoted,configGroup) }?.properties ?:return emptyList()
	}
	return result
}

private fun completeKey(expression: String, keyElement: PsiElement, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, configGroup: CwtConfigGroup, result: CompletionResultSet) {
	val (expressionType, expressionValue) = expression.resolveKeyExpression()
	when(expressionType) {
		CwtKeyExpressionType.Any -> pass()
		CwtKeyExpressionType.Bool -> pass()
		CwtKeyExpressionType.Int -> pass()
		CwtKeyExpressionType.IntExpression -> pass()
		CwtKeyExpressionType.Float -> pass()
		CwtKeyExpressionType.FloatExpression -> pass()
		CwtKeyExpressionType.Scalar -> pass()
		CwtKeyExpressionType.Localisation -> {
			//NOTE 总是提示，不好统计数量
			val keyword = keyElement.keyword
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpressionType.SyncedLocalisation -> {
			//NOTE 总是提示，不好统计数量
			val keyword = keyElement.keyword
			val syncedLocalisations = findSyncedLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(syncedLocalisation in syncedLocalisations) {
				val name = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = syncedLocalisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(syncedLocalisation, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpressionType.InlineLocalisation -> {
			if(keyElement.isQuoted()) return
			//NOTE 总是提示，不好统计数量
			val keyword = keyElement.keyword
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpressionType.TypeExpression -> {
			//NOTE 总是提示，不好统计数量
			val typeExpression = expressionValue ?: return
			val definitions = findDefinitions(typeExpression, configGroup.project)
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val name = definitionName
				val icon = definitionIcon //使用特定图标
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpressionType.TypeExpressionString -> {
			//NOTE 总是提示，不好统计数量
			val typeExpression = expressionValue ?: return
			val prefix = expression.substringBefore('<')
			val suffix = expression.substringAfterLast('>')
			val definitions = findDefinitions(typeExpression, configGroup.project)
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val name = prefix + definitionName + suffix
				val tailText = "from definition $definitionName" //注明从哪个definition生成
				val icon = definitionIcon //使用特定图标
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true).withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpressionType.EnumExpression -> {
			//NOTE 总是提示，不好统计数量
			val enumExpression = expressionValue ?: return
			val enumConfig = configGroup.enums[enumExpression] ?: return
			val enumValues = enumConfig.values
			for(enumValue in enumValues) {
				val name = enumValue
				val icon = enumIcon //使用特定图标
				val typeText = enumConfig.pointer.containingFile?.name
				val lookupElement = LookupElementBuilder.create(name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpressionType.ScopeExpression -> pass() //TODO
		CwtKeyExpressionType.AliasNameExpression -> pass() //TODO
		CwtKeyExpressionType.Constant -> {
			val name = expressionValue ?: return
			completeIfNeed(name, propertyConfig, definitionPropertyInfo) {
				val element = propertyConfig.pointer.element ?: return
				val icon = scriptPropertyIcon //使用特定图标
				val typeText = propertyConfig.pointer.containingFile?.name
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler).withPriority(propertyPriority)
				result.addElement(lookupElement)
			}
		}
	}
}

private fun completeValue(expression: String, valueElement: PsiElement, valueConfig: CwtValueConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, configGroup: CwtConfigGroup, result: CompletionResultSet) {
	val (expressionType, expressionValue) = expression.resolveValueExpression()
	when(expressionType) {
		CwtValueExpressionType.Any -> pass()
		CwtValueExpressionType.Bool -> pass()
		CwtValueExpressionType.Int -> pass()
		CwtValueExpressionType.IntExpression -> pass()
		CwtValueExpressionType.Float -> pass()
		CwtValueExpressionType.FloatExpression -> pass()
		CwtValueExpressionType.Scalar -> pass()
		CwtValueExpressionType.PercentageField -> pass()
		CwtValueExpressionType.ColorField -> pass()
		CwtValueExpressionType.Localisation -> {
			//NOTE 总是提示，不好统计数量
			val keyword = valueElement.keyword
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpressionType.SyncedLocalisation -> {
			//NOTE 总是提示，不好统计数量
			val keyword = valueElement.keyword
			val syncedLocalisations = findSyncedLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(syncedLocalisation in syncedLocalisations) {
				val name = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = syncedLocalisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(syncedLocalisation, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpressionType.InlineLocalisation -> {
			if(valueElement.isQuoted()) return
			//NOTE 总是提示，不好统计数量
			val keyword = valueElement.keyword
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpressionType.FilePath -> pass() //TODO
		CwtValueExpressionType.FilePathExpression -> pass() //TODO
		CwtValueExpressionType.IconExpression -> pass() //TODO
		CwtValueExpressionType.DateField -> pass()
		CwtValueExpressionType.TypeExpression -> {
			//NOTE 总是提示，不好统计数量
			val typeExpression = expressionValue ?: return
			val definitions = findDefinitions(typeExpression, configGroup.project)
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val name = definitionName
				val icon = definitionIcon //使用特定图标
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpressionType.TypeExpressionString -> {
			//NOTE 总是提示，不好统计数量
			val typeExpression = expressionValue ?: return
			val prefix = expression.substringBefore('<')
			val suffix = expression.substringAfterLast('>')
			val definitions = findDefinitions(typeExpression, configGroup.project)
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val name = prefix + definitionName + suffix
				val tailText = "from definition $definitionName" //注明从哪个definition生成
				val icon = definitionIcon //使用特定图标
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true).withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpressionType.EnumExpression -> {
			//NOTE 总是提示，不好统计数量
			val enumExpression = expressionValue ?: return
			val enumConfig = configGroup.enums[enumExpression] ?: return
			val enumValues = enumConfig.values
			for(enumValue in enumValues) {
				val name = enumValue
				val icon = enumIcon //使用特定图标
				val typeText = enumConfig.pointer.containingFile?.name
				val lookupElement = LookupElementBuilder.create(name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpressionType.ScopeExpression -> pass() //TODO
		CwtValueExpressionType.ScopeField -> pass() //TODO
		CwtValueExpressionType.VariableField -> pass() //TODO
		CwtValueExpressionType.VariableFieldExpression -> pass() //TODO
		CwtValueExpressionType.IntVariableField -> pass() //TODO
		CwtValueExpressionType.IntVariableFieldExpression -> pass() //TODO
		CwtValueExpressionType.ValueField -> pass() //TODO
		CwtValueExpressionType.ValueFieldExpression -> pass() //TODO
		CwtValueExpressionType.IntValueField -> pass() //TODO
		CwtValueExpressionType.IntValueFieldExpression -> pass() //TODO
		CwtValueExpressionType.AliasMatchLeftExpression -> pass() //TODO
		CwtValueExpressionType.Constant -> {
			val name = expressionValue ?: return
			completeIfNeed(name, valueConfig, definitionPropertyInfo) {
				val element = valueConfig.pointer.element ?: return
				val icon = scriptPropertyIcon //使用特定图标
				val typeText = valueConfig.pointer.containingFile?.name
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler).withPriority(propertyPriority)
				result.addElement(lookupElement)
			}
		}
	}
}

inline fun completeIfNeed(name: String, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, action: () -> Unit) {
	//如果指定名字的已有属性数量大于等于要补全的属性的最大数量，则忽略补全（未执行也忽略）
	val count = propertyConfig.cardinality?.max
	if(count == null || count > definitionPropertyInfo.propertiesCardinality[name] ?: 0) {
		action()
	}
}

inline fun completeIfNeed(names: List<String>, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, action: () -> Unit) {
	//如果指定名字的已有属性数量大于等于要补全的属性的最大数量，则忽略补全（未执行也忽略）
	val count = propertyConfig.cardinality?.max
	if(count == null || count > names.sumOf { name -> definitionPropertyInfo.propertiesCardinality[name] ?: 0 }) {
		action()
	}
}

inline fun completeIfNeed(name: String, valueConfig: CwtValueConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, action: () -> Unit) {
	//如果指定名字的已有属性数量大于等于要补全的属性的最大数量，则忽略补全（未执行也忽略）
	val count = valueConfig.cardinality?.max
	if(count == null || count > definitionPropertyInfo.propertiesCardinality[name] ?: 0) {
		action()
	}
}

inline fun completeIfNeed(names: List<String>, valueConfig: CwtValueConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, action: () -> Unit) {
	//如果指定名字的已有属性数量大于等于要补全的属性的最大数量，则忽略补全（未执行也忽略）
	val count = valueConfig.cardinality?.max
	if(count == null || count > names.sumOf { name -> definitionPropertyInfo.propertiesCardinality[name] ?: 0 }) {
		action()
	}
}