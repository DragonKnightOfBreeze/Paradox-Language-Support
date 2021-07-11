package icu.windea.pls

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.cwt.config.*
import icu.windea.pls.cwt.expression.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

//region Constants
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
//endregion

//region Misc Extensions
fun isAlias(propertyConfig: CwtPropertyConfig): Boolean {
	return propertyConfig.keyExpression.type == CwtKeyExpression.Type.AliasName &&
		propertyConfig.valueExpression.type == CwtValueExpression.Type.AliasMatchLeft
}
//endregion

//region Match Extensions
fun matchesDefinitionProperty(propertyElement: ParadoxDefinitionProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
	when {
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
	}
	return true
}

private fun matchesProperty(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
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
			//匹配值
			propertyConfig.stringValue != null -> {
				return matchesValue(propertyConfig.valueExpression, propValue, configGroup)
			}
			//匹配alias
			isAlias(propertyConfig) -> {
				return matchesAlias(propertyConfig, propertyElement, configGroup)
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
		}
	}
	return true
}

private fun matchesProperties(propertyElements: List<ParadoxScriptProperty>, propertyConfigs: List<CwtPropertyConfig>, configGroup: CwtConfigGroup): Boolean {
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

private fun matchesValues(valueElements: List<ParadoxScriptValue>, valueConfigs: List<CwtValueConfig>, configGroup: CwtConfigGroup): Boolean {
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

fun matchesKey(expression: CwtKeyExpression, keyElement: ParadoxScriptPropertyKey, configGroup: CwtConfigGroup): Boolean {
	//这里的key=keyElement.value, quoted=keyElement.isQuoted()使用懒加载
	if(expression.isEmpty()) return false
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
		CwtKeyExpression.Type.IntRange -> {
			val key = keyElement.value
			key.isInt() && expression.extraValue.castOrNull<IntRange>()?.contains(key.toInt()) ?: true
		}
		CwtKeyExpression.Type.Float -> {
			val key = keyElement.value
			key.isFloat()
		}
		CwtKeyExpression.Type.FloatRange -> {
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
			hasDefinitionByType(name, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return false
			val key = keyElement.value
			hasDefinitionByType(key, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.Value -> {
			val valueExpression = expression.value ?: return false
			val valueValues = configGroup.values[valueExpression]?.values ?: return false
			val key = keyElement.value
			key in valueValues
		}
		CwtKeyExpression.Type.Enum -> {
			val enumExpression = expression.value ?: return false
			val enumValues = configGroup.enums[enumExpression]?.values ?: return false
			val key = keyElement.value
			key in enumValues
		}
		CwtKeyExpression.Type.Scope -> {
			true //TODO
		}
		CwtKeyExpression.Type.ScopeField -> {
			true //TODO
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return false
			val alias = configGroup.aliases[aliasName] ?: return false
			val key = keyElement.value
			alias.containsKey(key) || run {
				//NOTE 如果aliasName是modifier，则key也可以是modifierDefinition的tag
				if(aliasName == "modifier") matchesModifierDefinition(configGroup, key) else false
			}
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return false
			val alias = configGroup.aliases[aliasName] ?: return false
			val key = keyElement.value
			alias.containsKey(key) || run {
				//NOTE 如果aliasName是modifier，则key也可以是modifierDefinition的tag
				if(aliasName == "modifier") matchesModifierDefinition(configGroup, key) else false
			}
		}
		CwtKeyExpression.Type.Constant -> {
			val key = keyElement.value
			key == expression.value
		}
	}
}

fun matchesKey(expression: CwtKeyExpression, key: String, quoted: Boolean, configGroup: CwtConfigGroup): Boolean {
	if(expression.isEmpty()) return false
	return when(expression.type) {
		CwtKeyExpression.Type.Any -> true
		CwtKeyExpression.Type.Bool -> {
			key.isBooleanYesNo()
		}
		CwtKeyExpression.Type.Int -> {
			key.isInt()
		}
		CwtKeyExpression.Type.IntRange -> {
			key.isInt() && expression.extraValue.castOrNull<IntRange>()?.contains(key.toInt()) ?: true
		}
		CwtKeyExpression.Type.Float -> {
			key.isFloat()
		}
		CwtKeyExpression.Type.FloatRange -> {
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
			hasDefinitionByType(key, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return false
			hasDefinitionByType(key, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.Value -> {
			val valueExpression = expression.value ?: return false
			val valueValues = configGroup.values[valueExpression]?.values ?: return false
			key in valueValues
		}
		CwtKeyExpression.Type.Enum -> {
			val enumExpression = expression.value ?: return false
			val enumValues = configGroup.enums[enumExpression]?.values ?: return false
			key in enumValues
		}
		CwtKeyExpression.Type.Scope -> {
			true //TODO
		}
		CwtKeyExpression.Type.ScopeField -> {
			true //TODO
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return false
			val alias = configGroup.aliases[aliasName] ?: return false
			alias.containsKey(key) || run {
				//NOTE 如果aliasName是modifier，则key也可以是modifierDefinition的tag
				if(aliasName == "modifier") matchesModifierDefinition(configGroup, key) else false
			}
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return false
			val alias = configGroup.aliases[aliasName] ?: return false
			alias.containsKey(key) || run {
				//NOTE 如果aliasName是modifier，则key也可以是modifierDefinition的tag
				if(aliasName == "modifier") matchesModifierDefinition(configGroup, key) else false
			}
		}
		CwtKeyExpression.Type.Constant -> {
			key == expression.value
		}
	}
}

fun matchesValue(expression: CwtValueExpression, valueElement: ParadoxScriptValue, configGroup: CwtConfigGroup): Boolean {
	if(expression.isEmpty()) return false
	return when(expression.type) {
		CwtValueExpression.Type.Any -> true
		CwtValueExpression.Type.Bool -> {
			valueElement is ParadoxScriptBoolean
		}
		CwtValueExpression.Type.Int -> {
			valueElement is ParadoxScriptInt
		}
		CwtValueExpression.Type.IntRange -> {
			val value = valueElement.value
			valueElement is ParadoxScriptInt && expression.extraValue.castOrNull<IntRange>()?.contains(value.toInt()) ?: true
		}
		CwtValueExpression.Type.Float -> {
			valueElement is ParadoxScriptFloat
		}
		CwtValueExpression.Type.FloatRange -> {
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
		CwtValueExpression.Type.Icon -> {
			true //TODO
		}
		CwtValueExpression.Type.DateField -> {
			val value = valueElement.value
			valueElement is ParadoxScriptString && value.isDateField()
		}
		CwtValueExpression.Type.TypeExpression -> {
			valueElement is ParadoxScriptString && run {
				val typeExpression = expression.value ?: return@run false
				hasDefinitionByType(valueElement.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpression.Type.TypeExpressionString -> {
			valueElement is ParadoxScriptString && run {
				val typeExpression = expression.value ?: return@run false
				hasDefinitionByType(valueElement.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpression.Type.Value -> {
			valueElement is ParadoxScriptString && run {
				val valueExpression = expression.value ?: return@run false
				val valueValues = configGroup.values[valueExpression]?.values ?: return@run false
				valueElement.stringValue in valueValues
			}
		}
		CwtValueExpression.Type.Enum -> {
			valueElement is ParadoxScriptString && run {
				val enumExpression = expression.value ?: return@run false
				val enumValues = configGroup.enums[enumExpression]?.values ?: return@run false
				valueElement.stringValue in enumValues
			}
		}
		CwtValueExpression.Type.Scope -> {
			true //TODO
		}
		CwtValueExpression.Type.ScopeField -> {
			true //TODO
		}
		CwtValueExpression.Type.VariableField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpression.Type.IntVariableField -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.isVariableField() //TODO
		}
		CwtValueExpression.Type.ValueField -> {
			true //TODO
		}
		CwtValueExpression.Type.IntValueField -> {
			true //TODO
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtValueExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return false
			val alias = configGroup.aliases[aliasName] ?: return false
			val key = valueElement.value
			alias.containsKey(key) || run {
				//NOTE 如果aliasName是modifier，则key也可以是modifierDefinition的tag
				if(aliasName == "modifier") matchesModifierDefinition(configGroup, key) else false
			}
		}
		//NOTE 不在这里处理
		CwtValueExpression.Type.AliasMatchLeft -> false 
		CwtValueExpression.Type.Constant -> {
			valueElement is ParadoxScriptString && valueElement.stringValue == expression.value
		}
	}
}

private fun matchesAlias(propertyConfig: CwtPropertyConfig, propertyElement: ParadoxScriptProperty, configGroup: CwtConfigGroup): Boolean {
	//aliasName和aliasSubName需要匹配
	val keyExpression = propertyConfig.keyExpression
	val aliasName = keyExpression.value ?: return false
	val alias = configGroup.aliases[aliasName] ?: return false
	if(aliasName !in alias.keys) return false
	
	//匹配其中一个规则即可
	val aliasSubName = propertyElement.name
	val aliasConfigs = alias[aliasSubName] ?: return false
	return aliasConfigs.any { config ->
		matchesProperty(propertyElement, config.config, configGroup)
	}
}

private fun matchesModifierDefinition(configGroup: CwtConfigGroup, key: String): Boolean {
	val modifierDefinitions = configGroup.modifierDefinitions
	return modifierDefinitions.containsKey(key) //期望比key in list更快
}
//endregion

//region Complete Extensions
fun addKeyCompletions(keyElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet) {
	val keyword = keyElement.keyword
	val quoted = keyElement.isQuoted()
	val project = propertyElement.project
	val definitionPropertyInfo = propertyElement.paradoxDefinitionPropertyInfo ?: return
	val gameType = definitionPropertyInfo.gameType
	val configGroup = getConfig(project).getValue(gameType)
	val childPropertyConfigs = definitionPropertyInfo.childPropertyConfigs
	if(childPropertyConfigs.isEmpty()) return
	
	for(propConfig in childPropertyConfigs) {
		if(shouldComplete(propConfig, definitionPropertyInfo)) {
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
	
	for(valueConfig in childValueConfigs) {
		if(shouldComplete(valueConfig, definitionPropertyInfo)) {
			completeValue(valueConfig.valueExpression, keyword, quoted, valueConfig.pointer, configGroup, result)
		}
	}
}

private fun shouldComplete(config: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo): Boolean {
	val expression = config.keyExpression
	//NOTE 如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
	if(expression.type == CwtKeyExpression.Type.AliasName) return true
	val actualCount = definitionPropertyInfo.childPropertyOccurrence[expression] ?: 0
	//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
	val cardinality = config.cardinality
	val maxCount = when {
		cardinality == null -> if(expression.type == CwtKeyExpression.Type.Constant) 1 else null
		else -> cardinality.max
	}
	return maxCount == null || actualCount < maxCount
}

private fun shouldComplete(config: CwtValueConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo): Boolean {
	val expression = config.valueExpression
	val actualCount = definitionPropertyInfo.childValueOccurrence[expression] ?: 0
	//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
	val cardinality = config.cardinality
	val maxCount = when {
		cardinality == null -> if(expression.type == CwtValueExpression.Type.Constant) 1 else null
		else -> cardinality.max
	}
	return maxCount == null || actualCount < maxCount
}

fun completeKey(expression: CwtKeyExpression, keyword: String, quoted: Boolean, pointer: SmartPsiElementPointer<*>, configGroup: CwtConfigGroup, result: CompletionResultSet) {
	//NOTE 需要尽可能地预先过滤结果
	if(expression.isEmpty()) return
	when(expression.type) {
		CwtKeyExpression.Type.Localisation -> {
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project) //预先过滤结果
			for(localisation in localisations) {
				val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val name = n.quoteIf(quoted)
				val icon = localisationIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.SyncedLocalisation -> {
			val syncedLocalisations = findSyncedLocalisationsByKeyword(keyword, configGroup.project) //预先过滤结果
			for(syncedLocalisation in syncedLocalisations) {
				val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
				val name = n.quoteIf(quoted)
				val icon = localisationIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = syncedLocalisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(syncedLocalisation, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.InlineLocalisation -> {
			if(quoted) return
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project) //预先过滤结果
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.TypeExpression -> {
			val typeExpression = expression.value ?: return
			val definitions = findDefinitionsByKeywordByType(keyword, typeExpression, configGroup.project) //预先过滤结果
			for(definition in definitions) {
				val n = definition.paradoxDefinitionInfo?.name ?: continue
				val name = n.quoteIf(quoted)
				val icon = definitionIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return
			val (prefix, suffix) = expression.extraValue.castOrNull<Tuple2<String, String>>() ?: return
			val definitions = findDefinitionsByKeywordByType(keyword, typeExpression, configGroup.project) //预先过滤结果
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val n = "$prefix$definitionName$suffix"
				val name = n.quoteIf(quoted)
				val icon = definitionIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.Value -> {
			val valueExpression = expression.value ?: return
			val valueConfig = configGroup.values[valueExpression] ?: return
			val valueValueConfigs = valueConfig.valueConfigs
			for(valueValueConfig in valueValueConfigs) {
				if(quoted && valueValueConfig.stringValue == null) continue
				val n = valueValueConfig.value
				if(!n.matchesKeyword(keyword)) continue //预先过滤结果
				val name = n.quoteIf(quoted)
				val element = valueValueConfig.pointer.element ?: continue
				val icon = valueIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = valueConfig.pointer.containingFile?.name ?: anonymousString
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.Enum -> {
			val enumExpression = expression.value ?: return
			val enumConfig = configGroup.enums[enumExpression] ?: return
			val enumValueConfigs = enumConfig.valueConfigs
			for(enumValueConfig in enumValueConfigs) {
				if(quoted && enumValueConfig.stringValue == null) continue
				val n = enumValueConfig.value
				if(!n.matchesKeyword(keyword)) continue //预先过滤结果
				val name = n.quoteIf(quoted)
				val element = enumValueConfig.pointer.element ?: continue
				val icon = enumIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = enumConfig.pointer.containingFile?.name ?: anonymousString
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.Scope -> pass() //TODO
		CwtKeyExpression.Type.ScopeField -> pass() //TODO
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return
			val alias = configGroup.aliases[aliasName] ?: return
			for(aliasConfigs in alias.values) {
				//aliasConfigs的名字是相同的 
				val aliasConfig = aliasConfigs.firstOrNull() ?: continue
				//aliasSubName是一个表达式
				completeKey(aliasConfig.expression, keyword, quoted, aliasConfig.pointer, configGroup, result)
			}
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
			if(aliasName == "modifier") {
				completeModifierDefinition(configGroup, keyword, quoted, result)
			}
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return
			val alias = configGroup.aliases[aliasName] ?: return
			for(aliasConfigs in alias.values) {
				//aliasConfigs的名字是相同的 
				val aliasConfig = aliasConfigs.firstOrNull() ?: continue
				//aliasSubName是一个表达式
				completeKey(aliasConfig.expression, keyword, quoted, aliasConfig.pointer, configGroup, result)
			}
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
			if(aliasName == "modifier") {
				completeModifierDefinition(configGroup, keyword, quoted, result)
			}
		}
		CwtKeyExpression.Type.Constant -> {
			val n = expression.value ?: return
			if(!n.matchesKeyword(keyword)) return //预先过滤结果
			val name = n.quoteIf(quoted)
			val element = pointer.element ?: return
			val icon = propertyIcon //使用特定图标
			val tailText = " in ${pointer.containingFile?.name ?: anonymousString}"
			val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
				.withTailText(tailText, true)
				.withInsertHandler(separatorInsertHandler)
				.withPriority(propertyPriority)
			result.addElement(lookupElement)
		}
		else -> pass()
	}
}

fun completeValue(expression: CwtValueExpression, keyword: String, quoted: Boolean, pointer: SmartPsiElementPointer<*>, configGroup: CwtConfigGroup, result: CompletionResultSet) {
	//NOTE 需要尽可能地预先过滤结果
	if(expression.isEmpty()) return
	when(expression.type) {
		CwtValueExpression.Type.Localisation -> {
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project) //预先过滤结果
			for(localisation in localisations) {
				val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val name = n.quoteIf(quoted)
				val icon = localisationIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.SyncedLocalisation -> {
			val syncedLocalisations = findSyncedLocalisationsByKeyword(keyword, configGroup.project)
			for(syncedLocalisation in syncedLocalisations) {
				val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
				val name = n.quoteIf(quoted)
				val icon = localisationIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = syncedLocalisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(syncedLocalisation, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.InlineLocalisation -> {
			if(quoted) return
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project)
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val icon = localisationIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.FilePath -> pass() //TODO
		CwtValueExpression.Type.FilePathExpression -> pass() //TODO
		CwtValueExpression.Type.Icon -> pass() //TODO
		CwtValueExpression.Type.DateField -> pass()
		CwtValueExpression.Type.TypeExpression -> {
			val typeExpression = expression.value ?: return
			val definitions = findDefinitionsByKeywordByType(keyword, typeExpression, configGroup.project) //预先过滤结果
			for(definition in definitions) {
				val n = definition.paradoxDefinitionInfo?.name ?: continue
				val name = n.quoteIf(quoted)
				val icon = definitionIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return
			val (prefix, suffix) = expression.extraValue?.castOrNull<Tuple2<String, String>>() ?: return
			val definitions = findDefinitionsByKeywordByType(keyword, typeExpression, configGroup.project) //预先过滤结果
			for(definition in definitions) {
				val definitionName = definition.paradoxDefinitionInfo?.name ?: continue
				val n = "$prefix$definitionName$suffix"
				val name = n.quoteIf(quoted)
				val icon = definitionIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.Value -> {
			val valueExpression = expression.value ?: return
			val valueConfig = configGroup.values[valueExpression] ?: return
			val valueValueConfigs = valueConfig.valueConfigs
			for(valueValueConfig in valueValueConfigs) {
				if(quoted && valueValueConfig.stringValue == null) continue
				val n = valueValueConfig.value
				if(!n.matchesKeyword(keyword)) continue //预先过滤结果
				val name = n.quoteIf(quoted)
				val element = valueValueConfig.pointer.element ?: continue
				val icon = valueIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = valueConfig.pointer.containingFile?.name ?: anonymousString
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.Enum -> {
			val enumExpression = expression.value ?: return
			val enumConfig = configGroup.enums[enumExpression] ?: return
			val enumValueConfigs = enumConfig.valueConfigs
			for(enumValueConfig in enumValueConfigs) {
				if(quoted && enumValueConfig.stringValue == null) continue
				val n = enumValueConfig.value
				if(!n.matchesKeyword(keyword)) continue //预先过滤结果
				val name = n.quoteIf(quoted)
				val element = enumValueConfig.pointer.element ?: continue
				val icon = enumIcon //使用特定图标
				val tailText = " by $expression in ${pointer.containingFile?.name ?: anonymousString}"
				val typeText = enumConfig.pointer.containingFile?.name ?: anonymousString
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.Scope -> pass() //TODO
		CwtValueExpression.Type.ScopeField -> pass() //TODO
		CwtValueExpression.Type.VariableField -> pass() //TODO
		CwtValueExpression.Type.IntVariableField -> pass() //TODO
		CwtValueExpression.Type.ValueField -> pass() //TODO
		CwtValueExpression.Type.IntValueField -> pass() //TODO
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtValueExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return
			val alias = configGroup.aliases[aliasName] ?: return
			for(aliasConfigs in alias.values) {
				//aliasConfigs的名字是相同的 
				val aliasConfig = aliasConfigs.firstOrNull() ?: continue
				//aliasSubName是一个表达式
				completeKey(aliasConfig.expression, keyword, quoted, aliasConfig.pointer, configGroup, result)
			}
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
			if(aliasName == "modifier") {
				completeModifierDefinition(configGroup, keyword, quoted, result)
			}
		}
		CwtValueExpression.Type.AliasMatchLeft -> pass() //TODO
		CwtValueExpression.Type.Constant -> {
			val n = expression.value ?: return
			if(!n.matchesKeyword(keyword)) return //预先过滤结果
			val name = n.quoteIf(quoted)
			val element = pointer.element ?: return
			val icon = valueIcon //使用特定图标
			val tailText = " in ${pointer.containingFile?.name ?: anonymousString}"
			val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
				.withTailText(tailText, true)
				.withPriority(propertyPriority)
			result.addElement(lookupElement)
		}
		else -> pass()
	}
}

private fun completeModifierDefinition(configGroup: CwtConfigGroup, keyword: String, quoted: Boolean, result: CompletionResultSet) {
	val modifierDefinitions = configGroup.modifierDefinitions
	if(modifierDefinitions.isEmpty()) return
	var size = 0
	for(modifierDefinition in modifierDefinitions.values) {
		val n = modifierDefinition.tag
		if(!n.matchesKeyword(keyword)) continue //预先过滤结果
		val name = n.quoteIf(quoted)
		val icon = propertyIcon //使用特定图标
		val tailText = " from modifier_definitions"
		val lookupElement = LookupElementBuilder.create(name).withIcon(icon)
			.withTailText(tailText, true)
			.withInsertHandler(separatorInsertHandler)
			.withPriority(hardCodedConfigPriority)
		result.addElement(lookupElement)
		size++
		if(size == maxCompleteSize) return //限制补全项的数量
	}
}

fun completeLocalisationCommand(commandField: ParadoxLocalisationCommandField, configGroup: CwtConfigGroup, result: CompletionResultSet) {
	val keyword = commandField.keyword
	val localisationCommands = configGroup.localisationCommands
	if(localisationCommands.isEmpty()) return
	var size = 0
	for(localisationCommand in localisationCommands) {
		val config = localisationCommand.value
		val name = config.name
		if(!name.matchesKeyword(keyword)) continue
		val element = config.pointer.element ?: continue
		//val scopes = localisationCommand
		val icon = localisationCommandFieldIcon
		val typeText = config.pointer.containingFile?.name ?: anonymousString
		val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
			.withTypeText(typeText, true)
		result.addElement(lookupElement)
		size++
		if(size == maxCompleteSize) return //限制补全项的数量
	}
}
//endregion

//region Resolve Extensions
fun resolveKey(keyElement: ParadoxScriptPropertyKey): PsiNamedElement? {
	val propertyConfig = keyElement.propertyConfig ?: return null
	val expression = propertyConfig.keyExpression
	val project = keyElement.project
	return when(expression.type) {
		CwtKeyExpression.Type.Localisation -> {
			val name = keyElement.value
			findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
		}
		CwtKeyExpression.Type.SyncedLocalisation -> {
			val name = keyElement.value
			findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
		}
		CwtKeyExpression.Type.TypeExpression -> {
			val name = keyElement.value
			val typeExpression = expression.value ?: return null
			findDefinitionByType(name, typeExpression, project)
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val (prefix, suffix) = expression.extraValue.castOrNull<Tuple2<String, String>>() ?: return null
			val name = keyElement.value.removeSurrounding(prefix, suffix)
			val typeExpression = expression.value ?: return null
			findDefinitionByType(name, typeExpression, project)
		}
		CwtKeyExpression.Type.Value -> {
			val valueName = expression.value ?: return null
			val name = keyElement.value
			val gameType = keyElement.paradoxFileInfo?.gameType ?: return null
			val configGroup = getConfig(keyElement.project).getValue(gameType)
			val valueValueConfig = configGroup.values.get(valueName)?.valueConfigs?.find { it.value == name }
			valueValueConfig?.pointer?.element?.castOrNull<CwtString>()
		}
		CwtKeyExpression.Type.Enum -> {
			val enumName = expression.value ?: return null
			val name = keyElement.value
			val gameType = keyElement.paradoxFileInfo?.gameType ?: return null
			val configGroup = getConfig(keyElement.project).getValue(gameType)
			val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigs?.find { it.value == name } ?: return null
			enumValueConfig.pointer.element.castOrNull<CwtString>()
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return null
			val aliasSubName = keyElement.value
			val gameType = keyElement.paradoxFileInfo?.gameType ?: return null
			val configGroup = getConfig(keyElement.project).getValue(gameType)
			//同名的定义有多个，需要匹配
			val aliasConfig = configGroup.aliases.get(aliasName)?.get(aliasSubName)?.find {
				val propertyElement = keyElement.parent.castOrNull<ParadoxScriptProperty>() ?: return@find false
				matchesProperty(propertyElement, it.config, configGroup)
			} ?: return null
			aliasConfig.pointer.element
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return null
			val aliasSubName = keyElement.value
			val gameType = keyElement.paradoxFileInfo?.gameType ?: return null
			val configGroup = getConfig(keyElement.project).getValue(gameType)
			//同名的定义有多个，需要匹配
			val aliasConfig = configGroup.aliases.get(aliasName)?.get(aliasSubName)?.find {
				val propertyElement = keyElement.parent.castOrNull<ParadoxScriptProperty>() ?: return@find false
				matchesProperty(propertyElement, it.config, configGroup)
			} ?: return null
			aliasConfig.pointer.element
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
		}
		CwtKeyExpression.Type.Constant -> {
			propertyConfig.pointer.element
		}
		else -> null //TODO
	}
}

fun multiResolveKey(keyElement: ParadoxScriptPropertyKey): List<PsiNamedElement> {
	val propertyConfig = keyElement.propertyConfig ?: return emptyList()
	val expression = propertyConfig.keyExpression
	val project = keyElement.project
	return when(expression.type) {
		CwtKeyExpression.Type.Localisation -> {
			val name = keyElement.value
			findLocalisations(name, inferParadoxLocale(), project, hasDefault = true)
		}
		CwtKeyExpression.Type.SyncedLocalisation -> {
			val name = keyElement.value
			findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true)
		}
		CwtKeyExpression.Type.TypeExpression -> {
			val name = keyElement.value
			val typeExpression = expression.value ?: return emptyList()
			findDefinitionsByType(name, typeExpression, project)
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val (prefix, suffix) = expression.extraValue.castOrNull<Pair<String, String>>() ?: return emptyList()
			val name = keyElement.value.removeSurrounding(prefix, suffix)
			val typeExpression = expression.value ?: return emptyList()
			findDefinitionsByType(name, typeExpression, project)
		}
		CwtKeyExpression.Type.Value -> {
			val valueName = expression.value ?: return emptyList()
			val name = keyElement.value
			val gameType = keyElement.paradoxFileInfo?.gameType ?: return emptyList()
			val configGroup = getConfig(keyElement.project).getValue(gameType)
			val valueValueConfig = configGroup.values.get(valueName)?.valueConfigs?.find { it.value == name }
			valueValueConfig?.pointer?.element?.castOrNull<CwtString>()?.toSingletonList() ?: return emptyList()
		}
		CwtKeyExpression.Type.Enum -> {
			val enumName = expression.value ?: return emptyList()
			val name = keyElement.value
			val gameType = keyElement.paradoxFileInfo?.gameType ?: return emptyList()
			val configGroup = getConfig(keyElement.project).getValue(gameType)
			val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigs?.find { it.value == name } ?: return emptyList()
			enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return emptyList()
			val aliasSubName = keyElement.value
			val gameType = keyElement.paradoxFileInfo?.gameType ?: return emptyList()
			val configGroup = getConfig(keyElement.project).getValue(gameType)
			//同名的定义有多个，需要匹配
			val aliasConfig = configGroup.aliases.get(aliasName)?.get(aliasSubName)?.find {
				val propertyElement = keyElement.parent.castOrNull<ParadoxScriptProperty>() ?: return@find false
				matchesProperty(propertyElement, it.config, configGroup)
			} ?: return emptyList()
			aliasConfig.pointer.element.toSingletonListOrEmpty()
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return emptyList()
			val aliasSubName = keyElement.value
			val gameType = keyElement.paradoxFileInfo?.gameType ?: return emptyList()
			val configGroup = getConfig(keyElement.project).getValue(gameType)
			//同名的定义有多个，需要匹配
			val aliasConfig = configGroup.aliases.get(aliasName)?.get(aliasSubName)?.find {
				val propertyElement = keyElement.parent.castOrNull<ParadoxScriptProperty>() ?: return@find false
				matchesProperty(propertyElement, it.config, configGroup)
			} ?: return emptyList()
			aliasConfig.pointer.element.toSingletonListOrEmpty()
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
		}
		CwtKeyExpression.Type.Constant -> {
			propertyConfig.pointer.element.toSingletonListOrEmpty()
		}
		else -> return emptyList() //TODO
	}
}

fun resolveValue(valueElement: ParadoxScriptValue): PsiNamedElement? {
	//根据对应的expression进行解析
	//val expression = element.expression?:return null
	//NOTE 由于目前引用支持不完善，如果expression为null时需要进行回调解析引用
	val valueConfig = valueElement.valueConfig ?: return fallbackResolveValue(valueElement)
	val expression = valueConfig.valueExpression
	val project = valueElement.project
	return when(expression.type) {
		CwtValueExpression.Type.Localisation -> {
			val name = valueElement.value
			findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
		}
		CwtValueExpression.Type.SyncedLocalisation -> {
			val name = valueElement.value
			findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
		}
		CwtValueExpression.Type.TypeExpression -> {
			val name = valueElement.value
			val typeExpression = expression.value ?: return null
			findDefinitionByType(name, typeExpression, project)
		}
		CwtValueExpression.Type.TypeExpressionString -> {
			val (prefix, suffix) = expression.extraValue.castOrNull<Pair<String, String>>() ?: return null
			val name = valueElement.value.removeSurrounding(prefix, suffix)
			val typeExpression = expression.value ?: return null
			findDefinitionByType(name, typeExpression, project)
		}
		CwtValueExpression.Type.Value -> {
			val valueName = expression.value ?: return null
			val name = valueElement.value
			val gameType = valueElement.paradoxFileInfo?.gameType ?: return null
			val configGroup = getConfig(valueElement.project).getValue(gameType)
			val valueValueConfig = configGroup.values.get(valueName)?.valueConfigs?.find { it.value == name }
			valueValueConfig?.pointer?.element?.castOrNull<CwtString>()
		}
		CwtValueExpression.Type.Enum -> {
			val enumName = expression.value ?: return null
			val name = valueElement.value
			val gameType = valueElement.paradoxFileInfo?.gameType ?: return null
			val configGroup = getConfig(valueElement.project).getValue(gameType)
			val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigs?.find { it.value == name } ?: return null
			enumValueConfig.pointer.element.castOrNull<CwtString>()
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtValueExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return null
			val aliasSubName = valueElement.value
			val gameType = valueElement.paradoxFileInfo?.gameType ?: return null
			val configGroup = getConfig(valueElement.project).getValue(gameType)
			//同名的定义有多个，取第一个即可
			val aliasConfig = configGroup.aliases.get(aliasName)?.get(aliasSubName)?.firstOrNull() ?: return null
			aliasConfig.pointer.element
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
		}
		CwtValueExpression.Type.AliasMatchLeft -> fallbackResolveValue(valueElement) //TODO
		CwtValueExpression.Type.Constant -> {
			valueConfig.pointer.element.castOrNull<CwtString>()
		}
		else -> null //TODO
	}
}

private fun fallbackResolveValue(valueElement: ParadoxScriptValue): PsiNamedElement? {
	val name = valueElement.value
	val project = valueElement.project
	return findDefinition(name, null, project)
		?: findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
		?: findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
}

fun multiResolveValue(valueElement: ParadoxScriptValue): List<PsiNamedElement> {
	//根据对应的expression进行解析
	//val expression = element.expression?:return emptyArray()
	//NOTE 由于目前引用支持不完善，如果expression为null时需要进行回调解析引用
	val valueConfig = valueElement.valueConfig ?: return fallbackMultiResolveValue(valueElement)
	val expression = valueConfig.valueExpression
	val project = valueElement.project
	return when(expression.type) {
		CwtValueExpression.Type.Localisation -> {
			val name = valueElement.value
			findLocalisations(name, inferParadoxLocale(), project, hasDefault = true)
		}
		CwtValueExpression.Type.SyncedLocalisation -> {
			val name = valueElement.value
			findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true)
		}
		CwtValueExpression.Type.TypeExpression -> {
			val name = valueElement.value
			val typeExpression = expression.value ?: return emptyList()
			findDefinitionsByType(name, typeExpression, project)
		}
		CwtValueExpression.Type.TypeExpressionString -> {
			val (prefix, suffix) = expression.extraValue.castOrNull<Pair<String, String>>() ?: return emptyList()
			val name = valueElement.value.removeSurrounding(prefix, suffix)
			val typeExpression = expression.value ?: return emptyList()
			findDefinitionsByType(name, typeExpression, project)
		}
		CwtValueExpression.Type.Value -> {
			val valueName = expression.value ?: return emptyList()
			val name = valueElement.value
			val gameType = valueElement.paradoxFileInfo?.gameType ?: return emptyList()
			val configGroup = getConfig(valueElement.project).getValue(gameType)
			val valueValueConfig = configGroup.values.get(valueName)?.valueConfigs?.find { it.value == name }
			valueValueConfig?.pointer?.element.castOrNull<CwtString>()?.toSingletonList() ?: return emptyList()
		}
		CwtValueExpression.Type.Enum -> {
			val enumName = expression.value ?: return emptyList()
			val name = valueElement.value
			val gameType = valueElement.paradoxFileInfo?.gameType ?: return emptyList()
			val configGroup = getConfig(valueElement.project).getValue(gameType)
			val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigs?.find { it.value == name } ?: return emptyList()
			enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtValueExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return emptyList()
			val aliasSubName = valueElement.value
			val gameType = valueElement.paradoxFileInfo?.gameType ?: return emptyList()
			val configGroup = getConfig(valueElement.project).getValue(gameType)
			//同名的定义有多个，取第一个即可
			val aliasConfig = configGroup.aliases.get(aliasName)?.get(aliasSubName)?.firstOrNull() ?: return emptyList()
			aliasConfig.pointer.element.toSingletonListOrEmpty()
			//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifierDefinition的tag（在modifiers.log中定义）
		}
		CwtValueExpression.Type.AliasMatchLeft -> return fallbackMultiResolveValue(valueElement) //TODO
		CwtValueExpression.Type.Constant -> {
			valueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
		}
		else -> return emptyList() //TODO
	}
}

private fun fallbackMultiResolveValue(valueElement: ParadoxScriptValue): List<PsiNamedElement> {
	val name = valueElement.value
	val project = valueElement.project
	return findDefinitions(name, null, project)
		.ifEmpty { findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) }
		.ifEmpty { findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) }
}
//endregion