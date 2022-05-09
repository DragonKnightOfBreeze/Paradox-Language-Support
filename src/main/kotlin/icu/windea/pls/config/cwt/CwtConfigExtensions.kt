@file:Suppress("unused")

package icu.windea.pls.config.cwt

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*
import kotlin.text.removeSurrounding

typealias CwtConfigMap = MutableMap<String, CwtFileConfig>
typealias CwtConfigMaps = MutableMap<String, CwtConfigMap>

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

fun isSingleAlias(propertyConfig: CwtPropertyConfig): Boolean {
	return propertyConfig.valueExpression.type == CwtValueExpression.Type.SingleAliasRight
}

fun matchesScope(alias: String, otherAlias: String, configGroup: CwtConfigGroup): Boolean {
	return alias == otherAlias || configGroup.scopeAliasMap[alias]?.aliases?.contains(otherAlias) ?: false
}

fun resolveAliasSubNameExpression(key: String, quoted: Boolean, aliasGroup: Map<String, List<CwtAliasConfig>>, configGroup: CwtConfigGroup): String? {
	return aliasGroup.keys.find {
		val expression = CwtKeyExpression.resolve(it)
		matchesKey(expression, key, quoted, configGroup)
	}
}

fun mergeScope(scopeMap: MutableMap<String, String>, thisScope: String?): MutableMap<String, String> {
	if(thisScope == null) return scopeMap
	val mergedScopeMap = scopeMap.toMutableMap()
	mergedScopeMap.put("this", thisScope)
	return scopeMap
}
//endregion

//region Matches Extensions
//NOTE 基于cwt规则文件的匹配方法不进一步匹配scope

fun matchesDefinitionProperty(propertyElement: ParadoxDefinitionProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
	when {
		//匹配属性列表
		propertyConfig.properties != null && propertyConfig.properties.isNotEmpty() -> {
			val propConfigs = propertyConfig.properties
			val props = propertyElement.properties
			if(!matchesProperties(props, propConfigs, configGroup)) return false //继续匹配
		}
		//匹配值列表
		propertyConfig.values != null && propertyConfig.values.isNotEmpty() -> {
			val valueConfigs = propertyConfig.values
			val values = propertyElement.values
			if(!matchesValues(values, valueConfigs, configGroup)) return false //继续匹配
		}
	}
	return true
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
			//匹配值
			propertyConfig.stringValue != null -> {
				return matchesValue(propertyConfig.valueExpression, propValue, configGroup)
			}
			//匹配single_alias
			isSingleAlias(propertyConfig) -> {
				return matchesSingleAlias(propertyConfig, propertyElement, configGroup)
			}
			//匹配alias
			isAlias(propertyConfig) -> {
				return matchesAlias(propertyConfig, propertyElement, configGroup)
			}
			//匹配属性列表
			propertyConfig.properties != null && propertyConfig.properties.isNotEmpty() -> {
				val propConfigs = propertyConfig.properties
				val props = propertyElement.properties
				if(!matchesProperties(props, propConfigs, configGroup)) return false //继续匹配
			}
			//匹配值列表
			propertyConfig.values != null && propertyConfig.values.isNotEmpty() -> {
				val valueConfigs = propertyConfig.values
				val values = propertyElement.values
				if(!matchesValues(values, valueConfigs, configGroup)) return false //继续匹配
			}
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

fun matchesKey(expression: CwtKeyExpression, keyElement: ParadoxScriptPropertyKey, configGroup: CwtConfigGroup): Boolean {
	if(expression.isEmpty()) return false
	val key = keyElement.value
	val quoted = keyElement.isQuoted()
	return matchesKey(expression, key, quoted, configGroup)
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
			existsLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.SyncedLocalisation -> {
			existsSyncedLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.InlineLocalisation -> {
			if(quoted) return true
			existsLocalisation(key, null, configGroup.project)
		}
		CwtKeyExpression.Type.TypeExpression -> {
			val typeExpression = expression.value ?: return false
			existsDefinitionByType(key, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return false
			existsDefinitionByType(key, typeExpression, configGroup.project)
		}
		CwtKeyExpression.Type.Value -> {
			val valueExpression = expression.value ?: return false
			val valueValues = configGroup.values[valueExpression]?.values ?: return false
			key in valueValues
		}
		CwtKeyExpression.Type.ValueSet -> {
			true //TODO
		}
		CwtKeyExpression.Type.Enum -> {
			val enumExpression = expression.value ?: return false
			val enumValues = configGroup.enums[enumExpression]?.values ?: return false
			key in enumValues
		}
		CwtKeyExpression.Type.ComplexEnum -> {
			true //TODO
		}
		CwtKeyExpression.Type.Scope -> {
			true //TODO
		}
		CwtKeyExpression.Type.ScopeField -> {
			true //TODO
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return false
			matchesAliasName(key, quoted, aliasName, configGroup)
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return false
			matchesAliasName(key, quoted, aliasName, configGroup)
		}
		CwtKeyExpression.Type.Constant -> {
			key.equals(expression.value, true) //忽略大小写
		}
		CwtKeyExpression.Type.Other -> return true
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
			valueElement is ParadoxScriptString && existsLocalisation(value, null, configGroup.project)
		}
		CwtValueExpression.Type.SyncedLocalisation -> {
			val value = valueElement.value
			valueElement is ParadoxScriptString && existsSyncedLocalisation(value, null, configGroup.project)
		}
		CwtValueExpression.Type.InlineLocalisation -> {
			val quoted = valueElement.isQuoted()
			if(quoted) return true
			val value = valueElement.value
			valueElement is ParadoxScriptString && existsLocalisation(value, null, configGroup.project)
		}
		CwtValueExpression.Type.AbsoluteFilePath -> {
			valueElement is ParadoxScriptString && run {
				val filePath = valueElement.value
				val toPath = filePath.toPathOrNull() ?: return@run false
				VfsUtil.findFile(toPath, true) != null
			}
		}
		CwtValueExpression.Type.FilePath -> {
			valueElement is ParadoxScriptString && run {
				val resolvedPath = CwtFilePathExpressionType.FilePath.resolve(expression.value, valueElement.value)
				findFileByFilePath(resolvedPath, configGroup.project) != null
			}
		}
		CwtValueExpression.Type.Icon -> {
			valueElement is ParadoxScriptString && run {
				val resolvedPath = CwtFilePathExpressionType.Icon.resolve(expression.value, valueElement.value) ?: return@run false
				findFileByFilePath(resolvedPath, configGroup.project) != null
			}
		}
		CwtValueExpression.Type.DateField -> {
			val value = valueElement.value
			valueElement is ParadoxScriptString && value.isDateField()
		}
		CwtValueExpression.Type.TypeExpression -> {
			valueElement is ParadoxScriptString && run {
				val typeExpression = expression.value ?: return@run false
				existsDefinitionByType(valueElement.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpression.Type.TypeExpressionString -> {
			valueElement is ParadoxScriptString && run {
				val typeExpression = expression.value ?: return@run false
				existsDefinitionByType(valueElement.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpression.Type.Value -> {
			valueElement is ParadoxScriptString && run {
				val valueExpression = expression.value ?: return@run false
				val valueValues = configGroup.values[valueExpression]?.values ?: return@run false
				valueElement.value in valueValues
			}
		}
		CwtValueExpression.Type.ValueSet -> {
			true //TODO
		}
		CwtValueExpression.Type.Enum -> {
			valueElement is ParadoxScriptString && run {
				val enumExpression = expression.value ?: return@run false
				val enumValues = configGroup.enums[enumExpression]?.values ?: return@run false
				valueElement.value in enumValues
			}
		}
		CwtValueExpression.Type.ComplexEnum -> {
			true //TODO
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
		CwtValueExpression.Type.SingleAliasRight -> false //NOTE 不在这里处理
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtValueExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return false
			val key = valueElement.value
			val quoted = valueElement.isQuoted()
			matchesAliasName(key, quoted, aliasName, configGroup)
		}
		CwtValueExpression.Type.AliasMatchLeft -> false //NOTE 不在这里处理
		CwtValueExpression.Type.Constant -> {
			valueElement is ParadoxScriptString && valueElement.stringValue.equals(expression.value, true) //忽略大小写
		}
		CwtValueExpression.Type.Other -> return true
	}
}

fun matchesSingleAlias(propertyConfig: CwtPropertyConfig, propertyElement: ParadoxScriptProperty, configGroup: CwtConfigGroup): Boolean {
	val singleAliasName = propertyConfig.valueExpression.value ?: return false
	val singleAliases = configGroup.singleAliases[singleAliasName] ?: return false
	return singleAliases.any { singleAlias ->
		matchesProperty(propertyElement, singleAlias.config, configGroup)
	}
}

fun matchesAlias(propertyConfig: CwtPropertyConfig, propertyElement: ParadoxScriptProperty, configGroup: CwtConfigGroup): Boolean {
	//aliasName和aliasSubName需要匹配
	val aliasName = propertyConfig.keyExpression.value ?: return false
	val aliasGroup = configGroup.aliases[aliasName] ?: return false
	val key = propertyElement.name
	val quoted = propertyElement.propertyKey.isQuoted()
	val aliasSubName = resolveAliasSubNameExpression(key, quoted, aliasGroup, configGroup) ?: return false
	val aliases = aliasGroup[aliasSubName] ?: return false
	return aliases.any { alias ->
		matchesProperty(propertyElement, alias.config, configGroup)
	}
}

fun matchesAliasName(key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup): Boolean {
	val aliasGroup = configGroup.aliases[aliasName] ?: return false
	val aliasSubName = resolveAliasSubNameExpression(key, quoted, aliasGroup, configGroup)
	return aliasSubName != null || run {
		//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifiers中的modifier
		if(aliasName == "modifier") matchesModifier(key, configGroup) else false
	}
}

fun matchesModifier(key: String, configGroup: CwtConfigGroup): Boolean {
	val modifiers = configGroup.modifiers
	return modifiers.containsKey(key)
}
//endregion

//region Complete Extensions
fun addKeyCompletions(keyElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet) {
	val keyword = keyElement.keyword
	val quoted = keyElement.isQuoted()
	val project = propertyElement.project
	val definitionElementInfo = propertyElement.definitionElementInfo ?: return
	val scope = definitionElementInfo.scope
	val gameType = definitionElementInfo.gameType
	val configGroup = getCwtConfig(project).getValue(gameType)
	val childPropertyConfigs = definitionElementInfo.childPropertyConfigs
	if(childPropertyConfigs.isEmpty()) return
	
	for(propConfig in childPropertyConfigs) {
		if(shouldComplete(propConfig, definitionElementInfo)) {
			completeKey(propConfig.keyExpression, keyword, quoted, propConfig, configGroup, result, scope)
		}
	}
	
}

fun addValueCompletions(valueElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet) {
	val keyword = valueElement.keyword
	val quoted = valueElement.isQuoted()
	val project = propertyElement.project
	val definitionElementInfo = propertyElement.definitionElementInfo ?: return
	val scope = definitionElementInfo.scope
	val gameType = definitionElementInfo.gameType
	val configGroup = getCwtConfig(project).getValue(gameType)
	val propertyConfigs = definitionElementInfo.propertyConfigs
	if(propertyConfigs.isEmpty()) return
	
	for(propertyConfig in propertyConfigs) {
		completeValue(propertyConfig.valueExpression, keyword, quoted, propertyConfig, configGroup, result, scope)
	}
}

fun addValueCompletionsInBlock(valueElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet) {
	val keyword = valueElement.keyword
	val quoted = valueElement.isQuoted()
	val project = propertyElement.project
	val definitionElementInfo = propertyElement.definitionElementInfo ?: return
	val scope = definitionElementInfo.scope
	val gameType = definitionElementInfo.gameType
	val configGroup = getCwtConfig(project).getValue(gameType)
	val childValueConfigs = definitionElementInfo.childValueConfigs
	if(childValueConfigs.isEmpty()) return
	
	for(valueConfig in childValueConfigs) {
		if(shouldComplete(valueConfig, definitionElementInfo)) {
			completeValue(valueConfig.valueExpression, keyword, quoted, valueConfig, configGroup, result, scope)
		}
	}
}

private fun shouldComplete(config: CwtPropertyConfig, definitionElementInfo: ParadoxDefinitionPropertyInfo): Boolean {
	val expression = config.keyExpression
	//如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
	if(expression.type == CwtKeyExpression.Type.AliasName) return true
	val actualCount = definitionElementInfo.childPropertyOccurrence[expression] ?: 0
	//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
	val cardinality = config.cardinality
	val maxCount = when {
		cardinality == null -> if(expression.type == CwtKeyExpression.Type.Constant) 1 else null
		else -> cardinality.max
	}
	return maxCount == null || actualCount < maxCount
}

private fun shouldComplete(config: CwtValueConfig, definitionElementInfo: ParadoxDefinitionPropertyInfo): Boolean {
	val expression = config.valueExpression
	val actualCount = definitionElementInfo.childValueOccurrence[expression] ?: 0
	//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
	val cardinality = config.cardinality
	val maxCount = when {
		cardinality == null -> if(expression.type == CwtValueExpression.Type.Constant) 1 else null
		else -> cardinality.max
	}
	return maxCount == null || actualCount < maxCount
}

fun completeKey(expression: CwtKeyExpression, keyword: String, quoted: Boolean, config: CwtKvConfig<*>,
	configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null) {
	//NOTE 需要尽可能地预先过滤结果
	if(expression.isEmpty()) return
	when(expression.type) {
		CwtKeyExpression.Type.Localisation -> {
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project) //预先过滤结果
			if(localisations.isEmpty()) return
			val icon = PlsIcons.localisationIcon //使用特定图标
			val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
			for(localisation in localisations) {
				val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val name = n.quoteIf(quoted)
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
			if(syncedLocalisations.isEmpty()) return
			val icon = PlsIcons.localisationIcon //使用特定图标
			val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
			for(syncedLocalisation in syncedLocalisations) {
				val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
				val name = n.quoteIf(quoted)
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
			if(localisations.isEmpty()) return
			val icon = PlsIcons.localisationIcon //使用特定图标
			val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
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
			val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
			if(definitions.isEmpty()) return
			val icon = PlsIcons.definitionIcon //使用特定图标
			val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
			for(definition in definitions) {
				val n = definition.definitionInfo?.name ?: continue
				val name = n.quoteIf(quoted)
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
			val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
			if(definitions.isEmpty()) return
			val (prefix, suffix) = expression.extraValue.castOrNull<Pair<String, String>>() ?: return
			val icon = PlsIcons.definitionIcon //使用特定图标
			val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
			for(definition in definitions) {
				val definitionName = definition.definitionInfo?.name ?: continue
				val n = "$prefix$definitionName$suffix"
				val name = n.quoteIf(quoted)
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
			val valueValueConfigs = valueConfig.valueConfigMap.values
			if(valueValueConfigs.isEmpty()) return
			val icon = PlsIcons.valueIcon //使用特定图标
			val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
			for(valueValueConfig in valueValueConfigs) {
				if(quoted && valueValueConfig.stringValue == null) continue
				val n = valueValueConfig.value
				//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = valueValueConfig.pointer.element ?: continue
				val typeText = valueConfig.pointer.containingFile?.name ?: anonymousString
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.ValueSet -> {
			//TODO
		}
		CwtKeyExpression.Type.Enum -> {
			val enumExpression = expression.value ?: return
			val enumConfig = configGroup.enums[enumExpression] ?: return
			val enumValueConfigs = enumConfig.valueConfigMap.values
			if(enumValueConfigs.isEmpty()) return
			val icon = PlsIcons.enumIcon //使用特定图标
			val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
			for(enumValueConfig in enumValueConfigs) {
				if(quoted && enumValueConfig.stringValue == null) continue
				val n = enumValueConfig.value
				//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = enumValueConfig.pointer.element ?: continue
				val typeText = enumConfig.pointer.containingFile?.name ?: anonymousString
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
					.withInsertHandler(separatorInsertHandler)
				result.addElement(lookupElement)
			}
		}
		CwtKeyExpression.Type.ComplexEnum -> {
			//TODO
		}
		CwtKeyExpression.Type.Scope -> pass() //TODO
		CwtKeyExpression.Type.ScopeField -> pass() //TODO
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return
			completeAliasName(aliasName, keyword, quoted, config, configGroup, result, scope)
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return
			completeAliasName(aliasName, keyword, quoted, config, configGroup, result, scope)
		}
		CwtKeyExpression.Type.Constant -> {
			val n = expression.value ?: return
			//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
			val name = n.quoteIf(quoted)
			val element = config.pointer.element ?: return
			val icon = PlsIcons.propertyIcon //使用特定图标
			val tailText = " in ${config.pointer.containingFile?.name ?: anonymousString}"
			val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
				.withTailText(tailText, true)
				.withInsertHandler(separatorInsertHandler)
				.withPriority(propertyPriority)
			result.addElement(lookupElement)
		}
		else -> pass()
	}
}

fun completeValue(expression: CwtValueExpression, keyword: String, quoted: Boolean, config: CwtKvConfig<*>,
	configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null) {
	//NOTE 需要尽可能地预先过滤结果
	if(expression.isEmpty()) return
	val configFileName = config.pointer.containingFile?.name ?: anonymousString
	when(expression.type) {
		CwtValueExpression.Type.Localisation -> {
			val localisations = findLocalisationsByKeyword(keyword, configGroup.project) //预先过滤结果
			if(localisations.isEmpty()) return
			val icon = PlsIcons.localisationIcon //使用特定图标
			val tailText = " by $expression in $configFileName"
			for(localisation in localisations) {
				val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val name = n.quoteIf(quoted)
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.SyncedLocalisation -> {
			val syncedLocalisations = findSyncedLocalisationsByKeyword(keyword, configGroup.project)
			if(syncedLocalisations.isEmpty()) return
			val icon = PlsIcons.localisationIcon //使用特定图标
			val tailText = " by $expression in $configFileName"
			for(syncedLocalisation in syncedLocalisations) {
				val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
				val name = n.quoteIf(quoted)
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
			if(localisations.isEmpty()) return
			val icon = PlsIcons.localisationIcon //使用特定图标
			val tailText = " by $expression in $configFileName"
			for(localisation in localisations) {
				val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
				val typeText = localisation.containingFile.name
				val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.AbsoluteFilePath -> pass() //不提示绝对路径
		CwtValueExpression.Type.FilePath -> {
			val expressionType = CwtFilePathExpressionType.FilePath
			val expressionValue = expression.value
			val virtualFiles = if(expressionValue == null) {
				findAllFilesByFilePath(configGroup.project, distinct = true)
			} else {
				findFilesByFilePath(expressionValue, configGroup.project, expressionType = expressionType, distinct = true)
			}
			if(virtualFiles.isEmpty()) return
			val tailText = " by $expression in $configFileName"
			for(virtualFile in virtualFiles) {
				val file = virtualFile.toPsiFile<PsiFile>(configGroup.project) ?: continue
				val filePath = virtualFile.fileInfo?.path?.path ?: continue
				val icon = virtualFile.fileType.icon
				val name = expressionType.extract(expressionValue, filePath) ?: continue
				val typeText = virtualFile.name
				val lookupElement = LookupElementBuilder.create(file, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.Icon -> {
			val expressionType = CwtFilePathExpressionType.Icon
			val expressionValue = expression.value
			val virtualFiles = if(expressionValue == null) {
				findAllFilesByFilePath(configGroup.project, distinct = true)
			} else {
				findFilesByFilePath(expressionValue, configGroup.project, expressionType = expressionType, distinct = true)
			}
			if(virtualFiles.isEmpty()) return
			val tailText = " by $expression in $configFileName"
			for(virtualFile in virtualFiles) {
				val file = virtualFile.toPsiFile<PsiFile>(configGroup.project) ?: continue
				val filePath = virtualFile.fileInfo?.path?.path ?: continue
				val icon = virtualFile.fileType.icon
				val name = expressionType.extract(expressionValue, filePath) ?: continue
				val typeText = virtualFile.name
				val lookupElement = LookupElementBuilder.create(file, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.TypeExpression -> {
			val typeExpression = expression.value ?: return
			val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
			if(definitions.isEmpty()) return
			val icon = PlsIcons.definitionIcon //使用特定图标
			val tailText = " by $expression in $configFileName"
			for(definition in definitions) {
				val n = definition.definitionInfo?.name ?: continue
				val name = n.quoteIf(quoted)
				val typeText = definition.containingFile.name
				val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.TypeExpressionString -> {
			val typeExpression = expression.value ?: return
			val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
			if(definitions.isEmpty()) return
			val (prefix, suffix) = expression.extraValue?.castOrNull<Pair<String, String>>() ?: return
			val icon = PlsIcons.definitionIcon //使用特定图标
			val tailText = " by $expression in $configFileName"
			for(definition in definitions) {
				val definitionName = definition.definitionInfo?.name ?: continue
				val n = "$prefix$definitionName$suffix"
				val name = n.quoteIf(quoted)
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
			val valueValueConfigs = valueConfig.valueConfigMap.values
			if(valueValueConfigs.isEmpty()) return
			val icon = PlsIcons.valueIcon //使用特定图标
			val tailText = " by $expression in $configFileName"
			for(valueValueConfig in valueValueConfigs) {
				if(quoted && valueValueConfig.stringValue == null) continue
				val n = valueValueConfig.value
				//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = valueValueConfig.pointer.element ?: continue
				val typeText = valueConfig.pointer.containingFile?.name ?: anonymousString
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.ValueSet -> {
			//TODO
		}
		CwtValueExpression.Type.Enum -> {
			val enumExpression = expression.value ?: return
			val enumConfig = configGroup.enums[enumExpression] ?: return
			val enumValueConfigs = enumConfig.valueConfigMap.values
			if(enumValueConfigs.isEmpty()) return
			val icon = PlsIcons.enumIcon //使用特定图标
			val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
			for(enumValueConfig in enumValueConfigs) {
				if(quoted && enumValueConfig.stringValue == null) continue
				val n = enumValueConfig.value
				//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = enumValueConfig.pointer.element ?: continue
				val typeText = enumConfig.pointer.containingFile?.name ?: anonymousString
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withTypeText(typeText, true)
				result.addElement(lookupElement)
			}
		}
		CwtValueExpression.Type.ComplexEnum -> {
			//TODO
		}
		CwtValueExpression.Type.Scope -> pass() //TODO
		CwtValueExpression.Type.ScopeField -> pass() //TODO
		CwtValueExpression.Type.VariableField -> pass() //TODO
		CwtValueExpression.Type.IntVariableField -> pass() //TODO
		CwtValueExpression.Type.ValueField -> pass() //TODO
		CwtValueExpression.Type.IntValueField -> pass() //TODO
		//规则会被内联，不应该被匹配到
		CwtValueExpression.Type.SingleAliasRight -> pass()
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtValueExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return
			completeAliasName(aliasName, keyword, quoted, config, configGroup, result, scope)
		}
		//规则会被内联，不应该被匹配到
		CwtValueExpression.Type.AliasMatchLeft -> pass()
		CwtValueExpression.Type.Constant -> {
			val n = expression.value ?: return
			//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
			val name = n.quoteIf(quoted)
			val element = config.pointer.element ?: return
			val icon = PlsIcons.valueIcon //使用特定图标
			val tailText = " in ${config.pointer.containingFile?.name ?: anonymousString}"
			val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
				.withTailText(tailText, true)
				.withPriority(propertyPriority)
			result.addElement(lookupElement)
		}
		else -> pass()
	}
}

fun completeAliasName(aliasName: String, keyword: String, quoted: Boolean, config: CwtKvConfig<*>,
	configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String?) {
	val alias = configGroup.aliases[aliasName] ?: return
	for(aliasConfigs in alias.values) {
		//aliasConfigs的名字是相同的 
		val aliasConfig = aliasConfigs.firstOrNull() ?: continue
		//NOTE alias的scope需要匹配（推断得到的scope为null时，总是提示）
		if(scope != null && !aliasConfig.supportedScopes.any { matchesScope(scope, it, configGroup) }) continue
		//NOTE 需要推断scope并向下传递，注意首先需要取config.parent.scope
		val finalScope = config.parent?.scope ?: scope
		//aliasSubName是一个表达式
		completeKey(aliasConfig.expression, keyword, quoted, aliasConfig.config, configGroup, result, finalScope)
	}
	//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifiers中的modifier
	if(aliasName == "modifier") {
		//NOTE 需要推断scope并向下传递，注意首先需要取config.parent.scope
		val finalScope = config.parent?.scope ?: scope
		completeModifier(quoted, configGroup, result, finalScope)
	}
}

fun completeModifier(quoted: Boolean, configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null) {
	val modifiers = configGroup.modifiers
	if(modifiers.isEmpty()) return
	for(modifierConfig in modifiers.values) {
		val categoryConfig = modifierConfig.categoryConfig ?: continue
		//NOTE modifier的scope需要匹配（推断得到的scope为null时，总是提示）
		if(scope != null && !categoryConfig.supportedScopes.any { matchesScope(scope, it, configGroup) }) continue
		val n = modifierConfig.name
		//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
		val name = n.quoteIf(quoted)
		val element = modifierConfig.pointer.element ?: continue
		val icon = PlsIcons.modifierIcon //使用特定图标
		val tailText = " from modifiers"
		val typeText = modifierConfig.pointer.containingFile?.name ?: anonymousString
		val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
			.withTailText(tailText, true)
			.withTypeText(typeText, true)
			.withInsertHandler(separatorInsertHandler)
			.withPriority(modifierPriority)
		result.addElement(lookupElement)
	}
}

fun completeLocalisationCommand(configGroup: CwtConfigGroup,
	result: CompletionResultSet) {
	//val keyword = commandField.keyword
	val localisationCommands = configGroup.localisationCommands
	if(localisationCommands.isEmpty()) return
	for(localisationCommand in localisationCommands) {
		val config = localisationCommand.value
		val name = config.name
		//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
		val element = config.pointer.element ?: continue
		//val scopes = localisationCommand
		val icon = PlsIcons.localisationCommandFieldIcon
		val typeText = config.pointer.containingFile?.name ?: anonymousString
		val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
			.withTypeText(typeText, true)
		result.addElement(lookupElement)
	}
}
//endregion

//region Resolve Extensions
//NOTE 基于cwt规则文件的解析方法不进一步匹配scope
inline fun resolveKey(keyElement: ParadoxScriptPropertyKey, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): PsiNamedElement? {
	val propertyConfig = keyElement.propertyConfig ?: return null
	val expression = propertyConfig.keyExpression
	if(!expressionPredicate(expression)) return null
	return doResolveKey(keyElement, expression, propertyConfig)
}

@PublishedApi
internal fun doResolveKey(keyElement: ParadoxScriptPropertyKey, expression: CwtKeyExpression, propertyConfig: CwtPropertyConfig): PsiNamedElement? {
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
			val (prefix, suffix) = expression.extraValue.castOrNull<Pair<String, String>>() ?: return null
			val name = keyElement.value.removeSurrounding(prefix, suffix)
			val typeExpression = expression.value ?: return null
			findDefinitionByType(name, typeExpression, project)
		}
		CwtKeyExpression.Type.Value -> {
			val valueName = expression.value ?: return null
			val name = keyElement.value
			val gameType = keyElement.gameType ?: return null
			val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
			val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
			valueValueConfig.pointer.element.castOrNull<CwtString>()
		}
		CwtKeyExpression.Type.ValueSet -> {
			propertyConfig.pointer.element //TODO
		}
		CwtKeyExpression.Type.Enum -> {
			val enumName = expression.value ?: return null
			val name = keyElement.value
			val gameType = keyElement.gameType ?: return null
			val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
			val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
			enumValueConfig.pointer.element.castOrNull<CwtString>()
		}
		CwtKeyExpression.Type.ComplexEnum -> {
			propertyConfig.pointer.element //TODO
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return null
			val gameType = keyElement.gameType ?: return null
			val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
			resolveAliasName(aliasName, keyElement, configGroup)
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return null
			val gameType = keyElement.gameType ?: return null
			val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
			resolveAliasName(aliasName, keyElement, configGroup)
		}
		CwtKeyExpression.Type.Constant -> {
			propertyConfig.pointer.element
		}
		else -> {
			propertyConfig.pointer.element //TODO
		}
	}
}

inline fun multiResolveKey(keyElement: ParadoxScriptPropertyKey, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): List<PsiNamedElement> {
	val propertyConfig = keyElement.propertyConfig ?: return emptyList()
	val expression = propertyConfig.keyExpression
	if(!expressionPredicate(expression)) return emptyList()
	return doMultiResolveKey(keyElement, expression, propertyConfig)
}

@PublishedApi
internal fun doMultiResolveKey(keyElement: ParadoxScriptPropertyKey, expression: CwtKeyExpression, propertyConfig: CwtPropertyConfig): List<PsiNamedElement> {
	val project = keyElement.project
	return when(expression.type) {
		CwtKeyExpression.Type.Localisation -> {
			val name = keyElement.value
			findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
		}
		CwtKeyExpression.Type.SyncedLocalisation -> {
			val name = keyElement.value
			findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
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
			val gameType = keyElement.gameType ?: return emptyList()
			val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
			val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return emptyList()
			valueValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
		}
		CwtKeyExpression.Type.ValueSet -> {
			propertyConfig.pointer.element.toSingletonListOrEmpty() //TODO
		}
		CwtKeyExpression.Type.Enum -> {
			val enumName = expression.value ?: return emptyList()
			val name = keyElement.value
			val gameType = keyElement.gameType ?: return emptyList()
			val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
			val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
			enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
		}
		CwtKeyExpression.Type.ComplexEnum -> {
			propertyConfig.pointer.element.toSingletonListOrEmpty() //TODO
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtKeyExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return emptyList()
			val gameType = keyElement.gameType ?: return emptyList()
			val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
			resolveAliasName(aliasName, keyElement, configGroup).toSingletonListOrEmpty()
		}
		CwtKeyExpression.Type.AliasName -> {
			val aliasName = expression.value ?: return emptyList()
			val gameType = keyElement.gameType ?: return emptyList()
			val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
			resolveAliasName(aliasName, keyElement, configGroup).toSingletonListOrEmpty()
		}
		CwtKeyExpression.Type.Constant -> {
			propertyConfig.pointer.element.toSingletonListOrEmpty()
		}
		else -> {
			propertyConfig.pointer.element.toSingletonListOrEmpty() //TODO
		}
	}
}

inline fun resolveValue(valueElement: ParadoxScriptValue, expressionPredicate: (CwtValueExpression) -> Boolean = { true }): PsiNamedElement? {
	//根据对应的expression进行解析
	//由于目前引用支持不完善，如果expression为null时需要进行回调解析引用
	val valueConfig = valueElement.valueConfig ?: return fallbackResolveValue(valueElement)
	val expression = valueConfig.valueExpression
	if(!expressionPredicate(expression)) return null
	//val expression = element.expression?:return null
	return doResolveValue(valueElement, expression, valueConfig)
}

@PublishedApi
internal fun doResolveValue(valueElement: ParadoxScriptValue, expression: CwtValueExpression, valueConfig: CwtValueConfig): PsiNamedElement? {
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
		CwtValueExpression.Type.AbsoluteFilePath -> {
			val filePath = valueElement.value
			val path = filePath.toPathOrNull() ?: return null
			VfsUtil.findFile(path, true)?.toPsiFile(project)
		}
		CwtValueExpression.Type.FilePath -> {
			val expressionType = CwtFilePathExpressionType.FilePath
			val filePath = expressionType.resolve(expression.value, valueElement.value)
			findFileByFilePath(filePath, project)?.toPsiFile(project)
		}
		CwtValueExpression.Type.Icon -> {
			val expressionType = CwtFilePathExpressionType.Icon
			val filePath = expressionType.resolve(expression.value, valueElement.value) ?: return null
			findFileByFilePath(filePath, project)?.toPsiFile(project)
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
			val gameType = valueElement.gameType ?: return null
			val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
			val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
			valueValueConfig.pointer.element.castOrNull<CwtString>()
		}
		CwtValueExpression.Type.ValueSet -> {
			valueConfig.pointer.element.castOrNull<CwtString>() //TODO
		}
		CwtValueExpression.Type.Enum -> {
			val enumName = expression.value ?: return null
			val name = valueElement.value
			val gameType = valueElement.gameType ?: return null
			val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
			val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
			enumValueConfig.pointer.element.castOrNull<CwtString>()
		}
		CwtValueExpression.Type.ComplexEnum -> {
			valueConfig.pointer.element.castOrNull<CwtString>() //TODO
		}
		//规则会被内联，不应该被匹配到
		CwtValueExpression.Type.SingleAliasRight -> {
			valueConfig.pointer.element.castOrNull<CwtString>()
		}
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtValueExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return null
			val gameType = valueElement.gameType ?: return null
			val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
			resolveAliasName(aliasName, valueElement, configGroup)
		}
		//规则会被内联，不应该被匹配到
		CwtValueExpression.Type.AliasMatchLeft -> {
			valueConfig.pointer.element.castOrNull<CwtString>()
		}
		CwtValueExpression.Type.Constant -> {
			valueConfig.pointer.element.castOrNull<CwtString>()
		}
		//对于值，如果类型是scalar、int等，不进行解析
		else -> null //TODO
	}
}

@PublishedApi
internal fun fallbackResolveValue(valueElement: ParadoxScriptValue): PsiNamedElement? {
	//NOTE 目前的版本不做任何处理
	return null
	//val name = valueElement.value
	//val project = valueElement.project
	//return findDefinition(name, null, project)
	//	?: findLocalisation(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
	//	?: findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
}

inline fun multiResolveValue(valueElement: ParadoxScriptValue, expressionPredicate: (CwtValueExpression) -> Boolean = { true }): List<PsiNamedElement> {
	//根据对应的expression进行解析
	//由于目前引用支持不完善，如果expression为null时需要进行回调解析引用
	val valueConfig = valueElement.valueConfig ?: return fallbackMultiResolveValue(valueElement)
	val expression = valueConfig.valueExpression
	if(!expressionPredicate(expression)) return emptyList()
	//val expression = element.expression?:return emptyList()
	return doMultiResolveValue(valueElement, expression, valueConfig)
}

@PublishedApi
internal fun doMultiResolveValue(valueElement: ParadoxScriptValue, expression: CwtValueExpression, valueConfig: CwtValueConfig): List<PsiNamedElement> {
	val project = valueElement.project
	return when(expression.type) {
		CwtValueExpression.Type.Localisation -> {
			val name = valueElement.value
			findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
		}
		CwtValueExpression.Type.SyncedLocalisation -> {
			val name = valueElement.value
			findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
		}
		CwtValueExpression.Type.AbsoluteFilePath -> {
			val filePath = valueElement.value
			val path = filePath.toPathOrNull() ?: return emptyList()
			VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
		}
		CwtValueExpression.Type.FilePath -> {
			val expressionType = CwtFilePathExpressionType.FilePath
			val filePath = expressionType.resolve(expression.value, valueElement.value)
			findFilesByFilePath(filePath, project).mapNotNull { it.toPsiFile(project) }
		}
		CwtValueExpression.Type.Icon -> {
			val expressionType = CwtFilePathExpressionType.Icon
			val filePath = expressionType.resolve(expression.value, valueElement.value) ?: return emptyList()
			findFilesByFilePath(filePath, project).mapNotNull { it.toPsiFile(project) }
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
			val gameType = valueElement.gameType ?: return emptyList()
			val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
			val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return emptyList()
			valueValueConfig.pointer.element.castOrNull<CwtString>()?.toSingletonList() ?: return emptyList()
		}
		CwtValueExpression.Type.ValueSet -> {
			return emptyList() //TODO
		}
		CwtValueExpression.Type.Enum -> {
			val enumName = expression.value ?: return emptyList()
			val name = valueElement.value
			val gameType = valueElement.gameType ?: return emptyList()
			val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
			val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
			enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
		}
		CwtValueExpression.Type.ComplexEnum -> {
			return emptyList() //TODO
		}
		//规则会被内联，不应该被匹配到
		CwtValueExpression.Type.SingleAliasRight -> emptyList()
		//NOTE 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
		CwtValueExpression.Type.AliasKeysField -> {
			val aliasName = expression.value ?: return emptyList()
			val gameType = valueElement.gameType ?: return emptyList()
			val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
			resolveAliasName(aliasName, valueElement, configGroup).toSingletonListOrEmpty()
		}
		//规则会被内联，不应该被匹配到
		CwtValueExpression.Type.AliasMatchLeft -> emptyList()
		CwtValueExpression.Type.Constant -> {
			valueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
		}
		//对于值，如果类型是scalar、int等，不进行解析
		else -> emptyList() //TODO
	}
}

@PublishedApi
internal fun fallbackMultiResolveValue(valueElement: ParadoxScriptValue): List<PsiNamedElement> {
	//NOTE 目前的版本不做任何处理
	return emptyList()
	//val name = valueElement.value
	//val project = valueElement.project
	//return findDefinitions(name, null, project)
	//	.ifEmpty { findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) } //仅查找用户的语言区域或任意语言区域的 
	//	.ifEmpty { findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) } //仅查找用户的语言区域或任意语言区域的
}

fun resolveAliasName(aliasName: String, keyElement: ParadoxScriptPropertyKey, configGroup: CwtConfigGroup): PsiNamedElement? {
	val aliasGroup = configGroup.aliases[aliasName] ?: return null
	val key = keyElement.value
	val quoted = keyElement.isQuoted()
	val aliasSubName = resolveAliasSubNameExpression(key, quoted, aliasGroup, configGroup)
	if(aliasSubName != null) {
		//NOTE 如果aliasSubName作为表达式类型不是constant，则要解析为具体的引用
		val expression = CwtKeyExpression.resolve(aliasSubName)
		val project = keyElement.project
		if(expression.type != CwtKeyExpression.Type.Constant) {
			return when(expression.type) {
				CwtKeyExpression.Type.Localisation -> {
					val name = key
					findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
				}
				CwtKeyExpression.Type.SyncedLocalisation -> {
					val name = key
					findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
				}
				CwtKeyExpression.Type.TypeExpression -> {
					val name = key
					val typeExpression = expression.value ?: return null
					findDefinitionByType(name, typeExpression, project)
				}
				CwtKeyExpression.Type.TypeExpressionString -> {
					val (prefix, suffix) = expression.extraValue.castOrNull<Pair<String, String>>() ?: return null
					val name = key.removeSurrounding(prefix, suffix)
					val typeExpression = expression.value ?: return null
					findDefinitionByType(name, typeExpression, project)
				}
				CwtKeyExpression.Type.Value -> {
					val valueName = expression.value ?: return null
					val name = key
					val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
					valueValueConfig.pointer.element.castOrNull<CwtString>()
				}
				CwtKeyExpression.Type.ValueSet -> {
					null //TODO
				}
				CwtKeyExpression.Type.Enum -> {
					val enumName = expression.value ?: return null
					val name = key
					val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
					enumValueConfig.pointer.element.castOrNull<CwtString>()
				}
				CwtKeyExpression.Type.ComplexEnum -> {
					null //TODO
				}
				else -> null //TODO
			}
		} else {
			//同名的定义有多个，取第一个即可
			val aliases = aliasGroup[aliasSubName]
			if(aliases != null) {
				val alias = aliases.firstOrNull()
				val element = alias?.pointer?.element
				if(element != null) return element
			}
		}
	}
	//NOTE 如果aliasName是modifier，则aliasSubName也可以是modifiers中的modifier
	if(aliasName == "modifier") {
		return resolveModifier(key, configGroup)
	}
	return null
}

fun resolveAliasName(aliasName: String, valueElement: ParadoxScriptValue, configGroup: CwtConfigGroup): PsiNamedElement? {
	val keyElement = valueElement.parent?.parent.castOrNull<ParadoxScriptProperty>()?.propertyKey ?: return null
	return resolveAliasName(aliasName, keyElement, configGroup)
}

fun resolveModifier(name: String, configGroup: CwtConfigGroup): CwtProperty? {
	val modifier = configGroup.modifiers[name] ?: return null
	return modifier.pointer.element
}
//endregion