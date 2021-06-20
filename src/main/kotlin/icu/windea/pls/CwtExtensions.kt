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

val EmptyCwtConfig = CwtFileConfig(emptyPointer(), emptyList(), emptyList())

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

/**
 * 根据cwtConfigProperty配置对scriptProperty进行匹配。
 */
fun matchProperty(element: ParadoxDefinitionProperty, elementConfig: CwtPropertyConfig, configGroup: icu.windea.pls.cwt.config.CwtConfigGroup): Boolean {
	val propertiesConfig = elementConfig.properties.orEmpty() //不应该为null，转为emptyList
	if(propertiesConfig.isEmpty()) return true //config为空表示匹配
	val properties = element.properties
	return doMatchProperties(properties, propertiesConfig, configGroup)
}

private fun doMatchProperties(properties: List<ParadoxScriptProperty>, propertiesConfig: List<CwtPropertyConfig>, configGroup: icu.windea.pls.cwt.config.CwtConfigGroup): Boolean {
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
			matchKey(propertyConfig.key, it.propertyKey, configGroup)
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

private fun doMatchProperty(property: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, configGroup: icu.windea.pls.cwt.config.CwtConfigGroup): Boolean {
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
				return matchValue(propertyConfig.stringValue, propValue, configGroup)
			}
		}
	}
	return true
}


fun matchKey(expression: String, key: ParadoxScriptPropertyKey, configGroup: icu.windea.pls.cwt.config.CwtConfigGroup): Boolean {
	val (expressionType, expressionValue) = expression.resolveKeyExpression()
	return when(expressionType) {
		CwtKeyExpressionType.Any -> true
		CwtKeyExpressionType.Bool -> {
			val value = key.value
			value.isBooleanYesNo()
		}
		CwtKeyExpressionType.Int -> {
			key.value.isInt()
		}
		CwtKeyExpressionType.IntExpression -> {
			val value = key.value
			value.isInt() && expressionValue?.toIntRange().let { it == null || value.toInt() in it }
		}
		CwtKeyExpressionType.Float -> {
			key.value.isFloat()
		}
		CwtKeyExpressionType.FloatExpression -> {
			val value = key.value
			value.isFloat() && expressionValue?.toFloatRange().let { it == null || value.toFloat() in it }
		}
		CwtKeyExpressionType.Scalar -> {
			key.value.isString()
		}
		CwtKeyExpressionType.Localisation -> {
			hasLocalisation(key.value, null, configGroup.project)
		}
		CwtKeyExpressionType.SyncedLocalisation -> {
			hasSyncedLocalisation(key.value, null, configGroup.project)
		}
		CwtKeyExpressionType.InlineLocalisation -> {
			if(key.isQuoted()) true else hasLocalisation(key.value, null, configGroup.project)
		}
		CwtKeyExpressionType.TypeExpression -> {
			run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(key.value, typeExpression, configGroup.project)
			}
		}
		CwtKeyExpressionType.TypeExpressionString -> {
			run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(key.value, typeExpression, configGroup.project)
			}
		}
		CwtKeyExpressionType.EnumExpression -> {
			run {
				val enumExpression = expressionValue ?: return@run false
				val enumValues = configGroup.enums[enumExpression]?.values ?: return@run false
				key.value in enumValues
			}
		}
		CwtKeyExpressionType.ScopeExpression -> {
			true //TODO
		}
		CwtKeyExpressionType.AliasNameExpression -> {
			true //TODO
		}
		CwtKeyExpressionType.Constant -> {
			key.value == expressionValue
		}
	}
}

fun matchValue(expression: String, value: ParadoxScriptValue, configGroup: icu.windea.pls.cwt.config.CwtConfigGroup): Boolean {
	val (expressionType, expressionValue) = expression.resolveValueExpression()
	return when(expressionType) {
		CwtValueExpressionType.Any -> true
		CwtValueExpressionType.Bool -> {
			value is ParadoxScriptBoolean
		}
		CwtValueExpressionType.Int -> {
			value is ParadoxScriptInt
		}
		CwtValueExpressionType.IntExpression -> {
			value is ParadoxScriptInt && expressionValue?.toIntRange().let { it == null || value.intValue in it }
		}
		CwtValueExpressionType.Float -> {
			value is ParadoxScriptFloat
		}
		CwtValueExpressionType.FloatExpression -> {
			value is ParadoxScriptFloat && expressionValue?.toFloatRange().let { it == null || value.floatValue in it }
		}
		CwtValueExpressionType.Scalar -> {
			value is ParadoxScriptString
		}
		CwtValueExpressionType.PercentageField -> {
			value is ParadoxScriptString && value.stringValue.isPercentageField()
		}
		CwtValueExpressionType.ColorField -> {
			value is ParadoxScriptString && value.stringValue.isColorField()
		}
		CwtValueExpressionType.Localisation -> {
			value is ParadoxScriptString && hasLocalisation(value.stringValue, null, configGroup.project)
		}
		CwtValueExpressionType.SyncedLocalisation -> {
			value is ParadoxScriptString && hasSyncedLocalisation(value.stringValue, null, configGroup.project)
		}
		CwtValueExpressionType.InlineLocalisation -> {
			value is ParadoxScriptString && run {
				if(value.isQuoted()) true else hasLocalisation(value.stringValue, null, configGroup.project)
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
			value is ParadoxScriptString && value.stringValue.isDateField()
		}
		CwtValueExpressionType.TypeExpression -> {
			value is ParadoxScriptString && run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(value.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpressionType.TypeExpressionString -> {
			value is ParadoxScriptString && run {
				val typeExpression = expressionValue ?: return@run false
				hasDefinition(value.stringValue, typeExpression, configGroup.project)
			}
		}
		CwtValueExpressionType.EnumExpression -> {
			value is ParadoxScriptString && run {
				val enumExpression = expressionValue ?: return@run false
				val enumValues = configGroup.enums[enumExpression]?.values ?: return@run false
				value.stringValue in enumValues
			}
		}
		CwtValueExpressionType.ScopeExpression -> {
			true //TODO
		}
		CwtValueExpressionType.ScopeField -> {
			true //TODO
		}
		CwtValueExpressionType.VariableField -> {
			value is ParadoxScriptString && value.stringValue.isVariableField() //TODO
		}
		CwtValueExpressionType.VariableFieldExpression -> {
			value is ParadoxScriptString && value.stringValue.isVariableField() //TODO
		}
		CwtValueExpressionType.IntVariableField -> {
			value is ParadoxScriptString && value.stringValue.isVariableField() //TODO
		}
		CwtValueExpressionType.IntVariableFieldExpression -> {
			value is ParadoxScriptString && value.stringValue.isVariableField() //TODO
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
			value is ParadoxScriptString && value.stringValue == expressionValue
		}
	}
}

//Add Completions Extensions

private val separatorInsertHandler = InsertHandler<LookupElement> { context, _ ->
	val customSettings = CodeStyle.getCustomSettings(context.file, ParadoxScriptCodeStyleSettings::class.java)
	val spaceAroundSeparator = customSettings.SPACE_AROUND_SEPARATOR
	val separator = if(spaceAroundSeparator) " = " else "="
	EditorModificationUtil.insertStringAtCaret(context.editor, separator)
}

fun addKeyCompletions(key: PsiElement, property: ParadoxDefinitionProperty, result: CompletionResultSet) {
	val project = property.project
	val definitionPropertyInfo = property.paradoxDefinitionPropertyInfo ?: return
	val path = definitionPropertyInfo.path
	val definitionInfo = definitionPropertyInfo.definitionInfo
	val type = definitionInfo.type
	val subtypes = definitionInfo.subtypes
	val gameType = definitionInfo.fileInfo.gameType
	val configGroup = getConfig(project).getValue(gameType)
	val propertiesConfig = configGroup.definitions.getValue(type).mergeConfig(subtypes)
	
	//如果path是空的，表示需要补全definition的顶级属性		
	if(path.isEmpty()) {
		doAddRootKeyCompletions(key, propertiesConfig, definitionPropertyInfo, configGroup, result)
	}
}

private fun doAddRootKeyCompletions(key: PsiElement, propertiesConfig: List<CwtPropertyConfig>, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, configGroup: CwtConfigGroup, result: CompletionResultSet) {
	for(propertyConfig in propertiesConfig) {
		//propertyConfig.key可以是一个表达式
		completeKey(propertyConfig.key, key, propertyConfig, definitionPropertyInfo, configGroup, result)
	}
}


fun completeKey(expression: String, key: PsiElement, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, configGroup: CwtConfigGroup, result: CompletionResultSet) {
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
			val keyword = key.keyword
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
			val keyword = key.keyword
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
			if(key.isQuoted()) return
			//NOTE 总是提示，不好统计数量
			val keyword = key.keyword
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
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withInsertHandler(separatorInsertHandler).withPriority(propertyPriority)
				result.addElement(lookupElement)
			}
		}
	}
}

fun completeValue(expression: String, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, configGroup: icu.windea.pls.cwt.config.CwtConfigGroup, result: MutableList<LookupElement>) {
	
}

inline fun completeIfNeed(name: String, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, action: () -> Unit) {
	//如果指定名字的已有属性数量大于等于要补全的属性的最大数量，则忽略补全（未指定最大数量则不忽略，至少对于alias是这样的）
	val count = propertyConfig.cardinality?.max
	val currentCount = definitionPropertyInfo.propertiesCardinality[name] ?: 0
	if(count != null && count > currentCount) {
		action()
	}
}

inline fun completeIfNeed(names: List<String>, propertyConfig: CwtPropertyConfig, definitionPropertyInfo: ParadoxDefinitionPropertyInfo, action: () -> Unit) {
	//如果指定名字的已有属性数量大于等于要补全的属性的最大数量，则忽略补全（未指定最大数量则不忽略，至少对于alias是这样的）
	val count = propertyConfig.cardinality?.max
	val currentCount = names.sumOf { name -> definitionPropertyInfo.propertiesCardinality[name] ?: 0 }
	if(count != null && count > currentCount) {
		action()
	}
}