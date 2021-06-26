package icu.windea.pls

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.psi.*
import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.expression.*
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

//Match Extensions

fun matchesSubtype(subtypeExpression: String, subtypes: List<String>): Boolean {
	return when {
		subtypeExpression.startsWith('!') -> subtypeExpression.drop(1) !in subtypes
		else -> subtypeExpression in subtypes
	}
}

fun matchDefinitionProperty(propertyElement: ParadoxDefinitionProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
	when {
		propertyConfig.properties != null && propertyConfig.properties.isNotEmpty() -> {
			val propConfigs = propertyConfig.properties.orEmpty() //不应该为null，转为emptyList
			val props = propertyElement.properties
			if(!matchesProperties(props, propConfigs, configGroup)) return false //继续匹配
		}
		propertyConfig.values != null && propertyConfig.values.isNotEmpty() -> {
			val valueConfigs = propertyConfig.values.orEmpty() //不应该为null，转为emptyList
			val values = propertyElement.values
			if(!matchesValues(values, valueConfigs, configGroup)) return false //继续匹配
		}
	}
	return true
}

fun matchesProperties(propertyElements: List<ParadoxScriptProperty>, propertyConfigs: List<CwtPropertyConfig>, configGroup: CwtConfigGroup): Boolean {
	//properties为空的情况系认为匹配
	if(propertyElements.isEmpty()) return true
	
	//要求其中所有的value的值在最终都会小于等于0
	val minMap = propertyConfigs.associateByTo(mutableMapOf(), { it.key }, { it.cardinality?.min ?: 1 }) //默认为1
	
	//注意：propConfig.key可能有重复，这种情况下只要有其中一个匹配即可
	for(propertyElement in propertyElements) {
		val keyElement = propertyElement.propertyKey
		val propConfigs = propertyConfigs.filter { matchesKey(it.keyExpression, keyElement, configGroup) }
		//如果没有匹配的规则则忽略
		if(propConfigs.isNotEmpty()) {
			val matched = propConfigs.any { propConfig ->
				val matched = matchesProperty(propertyElement, propConfig, configGroup)
				if(matched) minMap.compute(propConfig.key) { _, v -> if(v == null) 1 else v - 1 }
				matched
			}
			if(!matched) return false
		}
	}
	
	return minMap.values.any { it <= 0 }
}

fun matchesValues(valueElements: List<ParadoxScriptValue>, valueConfigs: List<CwtValueConfig>, configGroup: CwtConfigGroup): Boolean {
	//values为空的情况下认为匹配 
	if(valueElements.isEmpty()) return true
	
	//要求其中所有的value的值在最终都会小于等于0
	val minMap = valueConfigs.associateByTo(mutableMapOf(), { it.value }, { it.cardinality?.min ?: 1 }) //默认为1
	
	for(value in valueElements) {
		//如果没有匹配的规则则认为不匹配
		val matched = valueConfigs.any { valueConfig ->
			val matched = matchesValue(valueConfig.valueExpression, value, configGroup)
			if(matched) minMap.compute(valueConfig.value) { _, v -> if(v == null) 1 else v - 1 }
			matched
		}
		if(!matched) return false
	}
	
	return minMap.values.any { it <= 0 }
}

fun matchesProperty(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
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
				if(!matchesProperties(props, propConfigs, configGroup)) return false //继续匹配
			}
			//匹配值列表
			propertyConfig.values != null && propertyConfig.values.isNotEmpty() -> {
				val valueConfigs = propertyConfig.values.orEmpty() //不应该为null，转为emptyList
				val values = propertyElement.values
				if(!matchesValues(values, valueConfigs, configGroup)) return false //继续匹配
			}
			//匹配值
			propertyConfig.stringValue != null -> {
				return matchesValue(propertyConfig.valueExpression, propValue, configGroup)
			}
		}
	}
	return true
}

fun matchesKey(expression: CwtKeyExpression, keyElement: ParadoxScriptPropertyKey, configGroup: CwtConfigGroup): Boolean {
	//这里的key=keyElement.value, quoted=keyElement.isQuoted()使用懒加载
	return when(expression.type) {
		CwtKeyExpression.Type.Any -> true
		CwtKeyExpression.Type.Bool -> {
			val key = keyElement.value
			key.isBooleanYesNo()
		}
		CwtKeyExpression.Type.Int -> {
			val key = keyElement.value
			key.isInt()
		}
		CwtKeyExpression.Type.IntExpression -> {
			val key = keyElement.value
			key.isInt() && expression.extraValue.castOrNull<IntRange>()?.contains(key.toInt()) ?: true
		}
		CwtKeyExpression.Type.Float -> {
			val key = keyElement.value
			key.isFloat()
		}
		CwtKeyExpression.Type.FloatExpression -> {
			val key = keyElement.value
			key.isFloat() && expression.extraValue.castOrNull<FloatRange>()?.contains(key.toFloat()) ?: true
		}
		CwtKeyExpression.Type.Scalar -> {
			val key = keyElement.value
			key.isString()
		}
		CwtKeyExpression.Type.Localisation -> {
			val key = keyElement.value
			hasLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.SyncedLocalisation -> {
			val key = keyElement.value
			hasSyncedLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.InlineLocalisation -> {
			val quoted = keyElement.isQuoted()
			if(quoted) return true
			val key = keyElement.value
			hasLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.TypeExpression -> {
			val typeExpression = expression.value ?: return false
			val name = keyElement.value
			hasDefinition(name, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return false
			val key = keyElement.value
			hasDefinition(key, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.EnumExpression -> {
			val enumExpression = expression.value ?: return false
			val enumValues = configGroup.enums[enumExpression]?.values ?: return false
			val key = keyElement.value
			key in enumValues
		}
		CwtKeyExpression.Type.ScopeExpression -> {
			true //TODO
		}
		CwtKeyExpression.Type.AliasNameExpression -> {
			true //TODO
		}
		CwtKeyExpression.Type.Constant -> {
			val key = keyElement.value
			key == expression.value
		}
	}
}

fun matchesKey(expression: CwtKeyExpression, key: String, quoted: Boolean, configGroup: CwtConfigGroup): Boolean {
	return when(expression.type) {
		CwtKeyExpression.Type.Any -> true
		CwtKeyExpression.Type.Bool -> {
			key.isBooleanYesNo()
		}
		CwtKeyExpression.Type.Int -> {
			key.isInt()
		}
		CwtKeyExpression.Type.IntExpression -> {
			key.isInt() && expression.extraValue.castOrNull<IntRange>()?.contains(key.toInt()) ?: true
		}
		CwtKeyExpression.Type.Float -> {
			key.isFloat()
		}
		CwtKeyExpression.Type.FloatExpression -> {
			key.isFloat() && expression.extraValue.castOrNull<FloatRange>()?.contains(key.toFloat()) ?: true
		}
		CwtKeyExpression.Type.Scalar -> {
			key.isString()
		}
		CwtKeyExpression.Type.Localisation -> {
			hasLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.SyncedLocalisation -> {
			hasSyncedLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.InlineLocalisation -> {
			if(quoted) return true
			hasLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.TypeExpression -> {
			val typeExpression = expression.value ?: return false
			hasDefinition(key, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return false
			hasDefinition(key, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.EnumExpression -> {
			val enumExpression = expression.value ?: return false
			val enumValues = configGroup.enums[enumExpression]?.values ?: return false
			key in enumValues
		}
		CwtKeyExpression.Type.ScopeExpression -> {
			true //TODO
		}
		CwtKeyExpression.Type.AliasNameExpression -> {
			true //TODO
		}
		CwtKeyExpression.Type.Constant -> {
			key == expression.value
		}
	}
}

fun matchesValue(expression: CwtValueExpression, valueElement: ParadoxScriptValue, configGroup: CwtConfigGroup): Boolean {
	return when(expression.type) {
		CwtValueExpression.Type.Any -> true
		CwtValueExpression.Type.Bool -> {
			valueElement is ParadoxScriptBoolean
		}
		CwtValueExpression.Type.Int -> {
			valueElement is ParadoxScriptInt
		}
		CwtValueExpression.Type.IntExpression -> {
			val value = valueElement.value
			valueElement is ParadoxScriptInt && expression.extraValue.castOrNull<IntRange>()?.contains(value.toInt()) ?: true
		}
		CwtValueExpression.Type.Float -> {
			valueElement is ParadoxScriptFloat
		}
		CwtValueExpression.Type.FloatExpression -> {
			val value = valueElement.value
			valueElement is ParadoxScriptFloat && expression.extraValue.castOrNull<FloatRange>()?.contains(value.toFloat()) ?: true
		}
		CwtValueExpression.Type.Scalar -> {
			valueElement is ParadoxScriptString
		}
		CwtValueExpression.Type.PercentageField -> {
			val value = valueElement.value
			valueElement is ParadoxScriptString && value.isPercentageField()
		}
		CwtValueExpression.Type.ColorField -> {
			val value = valueElement.value
			valueElement is ParadoxScriptString && value.isColorField()
		}
		CwtValueExpression.Type.Localisation -> {
			val value = valueElement.value
			valueElement is ParadoxScriptString && hasLocalisation(value, null, configGroup.project)
		}
		CwtValueExpression.Type.SyncedLocalisation -> {
			val value = valueElement.value
			valueElement is ParadoxScriptString && hasSyncedLocalisation(value, null, configGroup.project)
		}
		CwtValueExpression.Type.InlineLocalisation -> {
			val quoted = valueElement.isQuoted()
			if(quoted) return true
			val value = valueElement.value
			valueElement is ParadoxScriptString && hasLocalisation(value, null, configGroup.project)
		}
		CwtValueExpression.Type.FilePath -> {
			true //TODO
		}
		CwtValueExpression.Type.FilePathExpression -> {
			true //TODO
		}
		CwtValueExpression.Type.IconExpression -> {
			true //TODO
		}
		CwtValueExpression.Type.DateField -> {
			val value = valueElement.value
			valueElement is ParadoxScriptString && value.isDateField()
		}
		CwtValueExpression.Type.TypeExpression -> {
			valueElement is ParadoxScriptString && run {
				val typeExpression = expression.value ?: return@run false
				hasDefinition(valueElement.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpression.Type.TypeExpressionString -> {
			valueElement is ParadoxScriptString && run {
				val typeExpression = expression.value ?: return@run false
				hasDefinition(valueElement.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpression.Type.EnumExpression -> {
			valueElement is ParadoxScriptString && run {
				val enumExpression = expression.value ?: return@run false
				val enumValues = configGroup.enums[enumExpression]?.values ?: return@run false
				valueElement.stringValue in enumValues
			}
		}
		CwtValueExpression.Type.ScopeExpression -> {
			true //TODO
		}
		CwtValueExpression.Type.ScopeField -> {
			true //TODO
		}
		CwtValueExpression.Type.VariableField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpression.Type.VariableFieldExpression -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpression.Type.IntVariableField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpression.Type.IntVariableFieldExpression -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpression.Type.ValueField -> {
			true //TODO
		}
		CwtValueExpression.Type.ValueFieldExpression -> {
			true //TODO
		}
		CwtValueExpression.Type.IntValueField -> {
			true //TODO
		}
		CwtValueExpression.Type.IntValueFieldExpression -> {
			true //TODO
		}
		CwtValueExpression.Type.AliasMatchLeftExpression -> {
			true //TODO
		}
		CwtValueExpression.Type.Constant -> {
			valueElement is ParadoxScriptString && valueElement.stringValue == expression.value
		}
	}
}

//Add Completions Extensions

fun addKeyCompletions(keyElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet) {
	val keyword = keyElement.keyword
	val quoted = keyElement.isQuoted()
	val project = propertyElement.project
	val definitionPropertyInfo = propertyElement.paradoxDefinitionPropertyInfo ?: return
	val gameType = definitionPropertyInfo.gameType
	val configGroup = getConfig(project).getValue(gameType)
	val childPropertyConfigs = definitionPropertyInfo.childPropertyConfigs
	if(childPropertyConfigs.isEmpty()) return
	val childPropertyOccurrence = definitionPropertyInfo.childPropertyOccurrence
	
	for(propConfig in childPropertyConfigs) {
		if(shouldComplete(propConfig, childPropertyOccurrence)) {
			completeKey(propConfig.keyExpression, keyword, quoted, propConfig.pointer, configGroup, result)
		}
	}
	
}

fun addValueCompletions(valueElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet) {
	val keyword = valueElement.keyword
	val quoted = valueElement.isQuoted()
	val project = propertyElement.project
	val definitionPropertyInfo = propertyElement.paradoxDefinitionPropertyInfo ?: return
	val gameType = definitionPropertyInfo.gameType
	val configGroup = getConfig(project).getValue(gameType)
	val propertyConfigs = definitionPropertyInfo.propertyConfigs
	if(propertyConfigs.isEmpty()) return
	
	for(propertyConfig in propertyConfigs) {
		completeValue(propertyConfig.valueExpression, keyword, quoted, propertyConfig.pointer, configGroup, result)
	}
}

fun addValueCompletionsInBlock(valueElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet) {
	val keyword = valueElement.keyword
	val quoted = valueElement.isQuoted()
	val project = propertyElement.project
	val definitionPropertyInfo = propertyElement.paradoxDefinitionPropertyInfo ?: return
	val gameType = definitionPropertyInfo.gameType
	val configGroup = getConfig(project).getValue(gameType)
	val childValueConfigs = definitionPropertyInfo.childValueConfigs
	if(childValueConfigs.isEmpty()) return
	val childValueOccurrence = definitionPropertyInfo.childValueOccurrence
	
	for(valueConfig in childValueConfigs) {
		if(shouldComplete(valueConfig, childValueOccurrence)) {
			completeValue(valueConfig.valueExpression, keyword, quoted, valueConfig.pointer, configGroup, result)
		}
	}
}

private fun shouldComplete(config: CwtPropertyConfig, occurrence: Map<CwtKeyExpression, Int>): Boolean {
	val expression = config.keyExpression
	val actualCount = occurrence[expression] ?: 0
	//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
	val maxCount = config.cardinality?.max ?: if(expression.type == CwtKeyExpression.Type.Constant) 1 else null
	return maxCount == null || actualCount < maxCount
}

private fun shouldComplete(config: CwtValueConfig, occurrence: Map<CwtValueExpression, Int>): Boolean {
	val expression = config.valueExpression
	val actualCount = occurrence[expression] ?: 0
	//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
	val maxCount = config.cardinality?.max ?: if(expression.type == CwtValueExpression.Type.Constant) 1 else null
	return maxCount == null || actualCount < maxCount
}

private fun completeKey(expression: CwtKeyExpression, keyword: String, quoted: Boolean, pointer: SmartPsiElementPointer<*>, configGroup: CwtConfigGroup, result: CompletionResultSet) {
	when(expression.type) {
		CwtKeyExpression.Type.Any -> pass()
		CwtKeyExpression.Type.Bool -> pass()
		CwtKeyExpression.Type.Int -> pass()
		CwtKeyExpression.Type.IntExpression -> pass()
		CwtKeyExpression.Type.Float -> pass()
		CwtKeyExpression.Type.FloatExpression -> pass()
		CwtKeyExpression.Type.Scalar -> pass()
		CwtKeyExpression.Type.Localisation -> {
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(localisation in localisations) {
				val name = localisation.name.quoteIf(quoted) //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.SyncedLocalisation -> {
			val syncedLocalisations = findSyncedLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(syncedLocalisation in syncedLocalisations) {
				val name = syncedLocalisation.name.quoteIf(quoted) //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = syncedLocalisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(syncedLocalisation, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.InlineLocalisation -> {
			if(quoted) return
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
		CwtKeyExpression.Type.TypeExpression -> {
			val typeExpression = expression.value ?: return
			val definitions = findDefinitions(typeExpression, configGroup.project)
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val name = definitionName.quoteIf(quoted)
				val icon = definitionIcon //使用特定图标
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return
			val (prefix, suffix) = expression.extraValue.castOrNull<Pair<String, String>>() ?: return
			val definitions = findDefinitions(typeExpression, configGroup.project)
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val name = "$prefix$definitionName$suffix".quoteIf(quoted)
				val tailText = "from definition $definitionName" //注明从哪个definition生成
				val icon = definitionIcon //使用特定图标
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true).withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.EnumExpression -> {
			val enumExpression = expression.value ?: return
			val enumConfig = configGroup.enums[enumExpression] ?: return
			val enumValues = enumConfig.values
			for(enumValue in enumValues) {
				val name = enumValue.quoteIf(quoted)
				val icon = enumIcon //使用特定图标
				val typeText = enumConfig.pointer.containingFile?.name
				val lookupElement = LookupElementBuilder.create(name).withIcon(icon)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.ScopeExpression -> pass() //TODO
		CwtKeyExpression.Type.AliasNameExpression -> pass() //TODO
		CwtKeyExpression.Type.Constant -> {
			val n = expression.value ?: return
			val name = n.quoteIf(quoted)
			val icon = scriptPropertyIcon //使用特定图标
			val typeText = pointer.containingFile?.name
			val lookupElement = LookupElementBuilder.create(name).withIcon(icon)
				.withTypeText(typeText, true)
				.withInsertHandler(separatorInsertHandler)
				.withPriority(propertyPriority)
			result.addElement(lookupElement)
		}
	}
}

private fun completeValue(expression: CwtValueExpression, keyword: String, quoted: Boolean, pointer: SmartPsiElementPointer<*>, configGroup: CwtConfigGroup, result: CompletionResultSet) {
	when(expression.type) {
		CwtValueExpression.Type.Any -> pass()
		CwtValueExpression.Type.Bool -> pass()
		CwtValueExpression.Type.Int -> pass()
		CwtValueExpression.Type.IntExpression -> pass()
		CwtValueExpression.Type.Float -> pass()
		CwtValueExpression.Type.FloatExpression -> pass()
		CwtValueExpression.Type.Scalar -> pass()
		CwtValueExpression.Type.PercentageField -> pass()
		CwtValueExpression.Type.ColorField -> pass()
		CwtValueExpression.Type.Localisation -> {
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(localisation in localisations) {
				val name = localisation.name.quoteIf(quoted) //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.SyncedLocalisation -> {
			val syncedLocalisations = findSyncedLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(syncedLocalisation in syncedLocalisations) {
				val name = syncedLocalisation.name.quoteIf(quoted) //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = syncedLocalisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(syncedLocalisation, name).withIcon(icon)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.InlineLocalisation -> {
			if(quoted) return
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project, maxSize = maxCompleteSize)
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.FilePath -> pass() //TODO
		CwtValueExpression.Type.FilePathExpression -> pass() //TODO
		CwtValueExpression.Type.IconExpression -> pass() //TODO
		CwtValueExpression.Type.DateField -> pass()
		CwtValueExpression.Type.TypeExpression -> {
			val typeExpression = expression.value ?: return
			val definitions = findDefinitions(typeExpression, configGroup.project)
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val name = definitionName.quoteIf(quoted)
				val icon = definitionIcon //使用特定图标
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return
			val (prefix, suffix) = expression.extraValue?.castOrNull<Pair<String, String>>() ?: return
			val definitions = findDefinitions(typeExpression, configGroup.project)
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val name = "$prefix$definitionName$suffix".quoteIf(quoted)
				val tailText = "from definition $definitionName" //注明从哪个definition生成
				val icon = definitionIcon //使用特定图标
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.EnumExpression -> {
			val enumExpression = expression.value ?: return
			val enumConfig = configGroup.enums[enumExpression] ?: return
			val enumValues = enumConfig.values
			for(enumValue in enumValues) {
				val name = enumValue.quoteIf(quoted)
				val icon = enumIcon //使用特定图标
				val typeText = enumConfig.pointer.containingFile?.name
				val lookupElement = LookupElementBuilder.create(name).withIcon(icon)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.ScopeExpression -> pass() //TODO
		CwtValueExpression.Type.ScopeField -> pass() //TODO
		CwtValueExpression.Type.VariableField -> pass() //TODO
		CwtValueExpression.Type.VariableFieldExpression -> pass() //TODO
		CwtValueExpression.Type.IntVariableField -> pass() //TODO
		CwtValueExpression.Type.IntVariableFieldExpression -> pass() //TODO
		CwtValueExpression.Type.ValueField -> pass() //TODO
		CwtValueExpression.Type.ValueFieldExpression -> pass() //TODO
		CwtValueExpression.Type.IntValueField -> pass() //TODO
		CwtValueExpression.Type.IntValueFieldExpression -> pass() //TODO
		CwtValueExpression.Type.AliasMatchLeftExpression -> pass() //TODO
		CwtValueExpression.Type.Constant -> {
			val n = expression.value ?: return
			val name = n.quoteIf(quoted)
			val icon = scriptValueIcon //使用特定图标
			val typeText = pointer.containingFile?.name
			val lookupElement = LookupElementBuilder.create(name).withIcon(icon)
				.withTypeText(typeText, true)
				.withInsertHandler(separatorInsertHandler).withPriority(propertyPriority)
			result.addElement(lookupElement)
		}
	}
}