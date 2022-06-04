package icu.windea.pls.config.cwt

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.*
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

/**
 * CWT规则的处理器。
 *
 * 提供基于CWT规则实现的匹配、校验、代码提示、引用解析等功能。
 */
object CwtConfigHandler {
	//region Common Extensions
	private fun isAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.keyExpression.type == CwtDataTypes.AliasName &&
			propertyConfig.valueExpression.type == CwtDataTypes.AliasMatchLeft
	}
	
	private fun isSingleAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.valueExpression.type == CwtDataTypes.SingleAliasRight
	}
	
	private fun matchesScope(alias: String, otherAlias: String, configGroup: CwtConfigGroup): Boolean {
		return alias == otherAlias || configGroup.scopeAliasMap[alias]?.aliases?.contains(otherAlias) ?: false
	}
	
	private fun resolveAliasSubNameExpression(key: String, quoted: Boolean, aliasGroup: Map<String, List<CwtAliasConfig>>, configGroup: CwtConfigGroup): String? {
		return aliasGroup.keys.find {
			val expression = CwtKeyExpression.resolve(it)
			matchesKey(expression, key, ParadoxValueType.infer(key), quoted, configGroup)
		}
	}
	
	fun mergeScope(scopeMap: MutableMap<String, String>, thisScope: String?): MutableMap<String, String> {
		if(thisScope == null) return scopeMap
		val mergedScopeMap = scopeMap.toMutableMap()
		mergedScopeMap.put("this", thisScope)
		return scopeMap
	}
	
	/**
	 * 内联类型为`single_alias_right`或`alias_match_left`的规则。以便后续的代码提示、引用解析和结构验证。
	 */
	fun inlineConfig(key: String, quoted: Boolean, config: CwtPropertyConfig, configGroup: CwtConfigGroup,
		result: MutableList<CwtPropertyConfig>): Boolean {
		val valueExpression = config.valueExpression
		return when(valueExpression.type) {
			CwtDataTypes.SingleAliasRight -> {
				val singleAliasName = valueExpression.value ?: return false
				val singleAliases = configGroup.singleAliases[singleAliasName] ?: return false
				for(singleAlias in singleAliases) {
					val c = singleAlias.config.copy(
						pointer = config.pointer, key = config.key,
						options = config.options, optionValues = config.optionValues, documentation = config.documentation
					)
					c.parent = config.parent
					result.add(c)
				}
				true
			}
			CwtDataTypes.AliasMatchLeft -> {
				val aliasName = valueExpression.value ?: return false
				val aliasGroup = configGroup.aliases[aliasName] ?: return false
				val aliasSubName = resolveAliasSubNameExpression(key, quoted, aliasGroup, configGroup) ?: return false
				val aliases = aliasGroup[aliasSubName] ?: return false
				for(alias in aliases) {
					val c = alias.config.copy(
						pointer = config.pointer, key = config.key,
						options = config.options, optionValues = config.optionValues, documentation = config.documentation
					)
					c.parent = config.parent
					result.add(c)
				}
				true
			}
			else -> false
		}
	}
	//endregion
	
	//region Matches Extensions
	//TODO 基于cwt规则文件的匹配方法需要进一步匹配scope
	//TODO 兼容variableReference inlineMath parameter  
	
	fun matchesDefinitionProperty(propertyElement: ParadoxDefinitionProperty, propertyConfig: CwtPropertyConfig, configGroup: CwtConfigGroup): Boolean {
		when {
			//匹配属性列表
			propertyConfig.properties != null && propertyConfig.properties.isNotEmpty() -> {
				val propConfigs = propertyConfig.properties
				val props = propertyElement.propertyList
				if(!matchesProperties(props, propConfigs, configGroup)) return false //继续匹配
			}
			//匹配值列表
			propertyConfig.values != null && propertyConfig.values.isNotEmpty() -> {
				val valueConfigs = propertyConfig.values
				val values = propertyElement.valueList
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
					val props = propertyElement.propertyList
					if(!matchesProperties(props, propConfigs, configGroup)) return false //继续匹配
				}
				//匹配值列表
				propertyConfig.values != null && propertyConfig.values.isNotEmpty() -> {
					val valueConfigs = propertyConfig.values
					val values = propertyElement.valueList
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
		return matchesKey(expression, keyElement.value, keyElement.valueType, keyElement.isQuoted(), configGroup)
	}
	
	fun matchesKey(expression: CwtKeyExpression, value: String, valueType: ParadoxValueType, quoted: Boolean, configGroup: CwtConfigGroup): Boolean {
		if(expression.isEmpty()) return false
		when(expression.type) {
			CwtDataTypes.Any -> {
				return true
			}
			CwtDataTypes.Int -> {
				return valueType.matchesIntType() && expression.extraValue?.cast<IntRange>()?.contains(value.toInt()) ?: true
			}
			CwtDataTypes.Float -> {
				return valueType.matchesFloatType() && expression.extraValue?.cast<FloatRange>()?.contains(value.toFloat()) ?: true
			}
			CwtDataTypes.Scalar -> {
				return valueType.matchesStringType()
			}
			CwtDataTypes.Localisation -> {
				if(!valueType.matchesStringType()) return false
				return existsLocalisation(value, null, configGroup.project)
			}
			CwtDataTypes.SyncedLocalisation -> {
				if(!valueType.matchesStringType()) return false
				existsSyncedLocalisation(value, null, configGroup.project)
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return true
				if(!valueType.matchesStringType()) return false
				return existsLocalisation(value, null, configGroup.project)
			}
			CwtDataTypes.TypeExpression -> {
				if(!valueType.matchesStringType()) return false
				val typeExpression = expression.value ?: return false
				return existsDefinitionByType(value, typeExpression, configGroup.project)
			}
			CwtDataTypes.TypeExpressionString -> {
				if(!valueType.matchesStringType()) return false
				val typeExpression = expression.value ?: return false
				return existsDefinitionByType(value, typeExpression, configGroup.project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return false
				val valueValues = configGroup.values[valueName]?.values ?: return false
				return value in valueValues
			}
			CwtDataTypes.ValueSet -> {
				return false //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return false
				val enumValues = configGroup.enums[enumName]?.values ?: return false
				return value in enumValues
			}
			CwtDataTypes.ComplexEnum -> {
				return false //TODO
			}
			CwtDataTypes.Scope -> {
				return false //TODO
			}
			CwtDataTypes.ScopeField -> {
				return false //TODO
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup)
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup)
			}
			CwtDataTypes.Constant -> {
				return value.equals(expression.value, true) //忽略大小写
			}
			CwtDataTypes.Other -> return true
		}
		return true
	}
	
	fun matchesValue(expression: CwtValueExpression, valueElement: ParadoxScriptValue, configGroup: CwtConfigGroup): Boolean {
		return matchesValue(expression, valueElement.value, valueElement.valueType, valueElement.isQuoted(), configGroup)
	}
	
	fun matchesValue(expression: CwtValueExpression, value: String, valueType: ParadoxValueType, quoted: Boolean, configGroup: CwtConfigGroup): Boolean {
		if(expression.isEmpty()) return false
		when(expression.type) {
			CwtDataTypes.Any -> {
				return true
			}
			CwtDataTypes.Bool -> {
				return valueType.matchesBooleanType()
			}
			CwtDataTypes.Int -> {
				return valueType.matchesIntType() && expression.extraValue?.cast<IntRange>()?.contains(value.toInt()) ?: true
			}
			CwtDataTypes.Float -> {
				return valueType.matchesFloatType() && expression.extraValue?.cast<FloatRange>()?.contains(value.toFloat()) ?: true
			}
			CwtDataTypes.Scalar -> {
				return valueType.matchesStringType()
			}
			CwtDataTypes.ColorField -> {
				return valueType.matchesColorType() && expression.value?.let { value.startsWith(it) } ?: true
			}
			CwtDataTypes.PercentageField -> {
				if(!valueType.matchesStringType()) return false
				return ParadoxValueType.isPercentageField(value)
			}
			CwtDataTypes.DateField -> {
				if(!valueType.matchesStringType()) return false
				return ParadoxValueType.isDateField(value)
			}
			CwtDataTypes.Localisation -> {
				if(!valueType.matchesStringType()) return false
				return existsLocalisation(value, null, configGroup.project)
			}
			CwtDataTypes.SyncedLocalisation -> {
				if(!valueType.matchesStringType()) return false
				return existsSyncedLocalisation(value, null, configGroup.project)
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return true
				if(!valueType.matchesStringType()) return false
				return existsLocalisation(value, null, configGroup.project)
			}
			CwtDataTypes.AbsoluteFilePath -> {
				if(!valueType.matchesStringType()) return false
				val path = value.toPathOrNull() ?: return false
				return VfsUtil.findFile(path, true) != null
			}
			CwtDataTypes.FilePath -> {
				if(!valueType.matchesStringType()) return false
				val resolvedPath = CwtFilePathExpressionTypes.FilePath.resolve(expression.value, value)
				return findFileByFilePath(resolvedPath, configGroup.project) != null
			}
			CwtDataTypes.Icon -> {
				if(!valueType.matchesStringType()) return false
				val resolvedPath = CwtFilePathExpressionTypes.Icon.resolve(expression.value, value) ?: return false
				return findFileByFilePath(resolvedPath, configGroup.project) != null
			}
			CwtDataTypes.TypeExpression -> {
				if(!valueType.matchesStringType()) return false
				val typeExpression = expression.value ?: return false
				return existsDefinitionByType(value, typeExpression, configGroup.project)
			}
			CwtDataTypes.TypeExpressionString -> {
				if(!valueType.matchesStringType()) return false
				val typeExpression = expression.value ?: return false
				return existsDefinitionByType(value, typeExpression, configGroup.project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return false
				val valueValues = configGroup.values[valueName]?.values ?: return false
				return value in valueValues
			}
			CwtDataTypes.ValueSet -> {
				return false //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return false
				val enumValues = configGroup.enums[enumName]?.values ?: return false
				return value in enumValues
			}
			CwtDataTypes.ComplexEnum -> {
				return false //TODO
			}
			CwtDataTypes.ScopeGroup -> {
				val scopeGroupName = expression.value ?: return false
				val scopeGroupValues = configGroup.scopeGroups[scopeGroupName]?.values ?: return false
				return value in scopeGroupValues
			}
			CwtDataTypes.Scope -> {
				return false //TODO
			}
			CwtDataTypes.ScopeField -> {
				return false //TODO
			}
			CwtDataTypes.VariableField -> {
				return false //TODO
			}
			CwtDataTypes.IntVariableField -> {
				return false //TODO
			}
			CwtDataTypes.ValueField -> {
				return false //TODO
			}
			CwtDataTypes.IntValueField -> {
				return false //TODO
			}
			CwtDataTypes.SingleAliasRight -> {
				return false //不在这里处理
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup)
			}
			CwtDataTypes.AliasMatchLeft -> {
				return false //不在这里处理
			}
			CwtDataTypes.Constant -> {
				return value.equals(expression.value, true) //忽略大小写
			}
			CwtDataTypes.Other -> {
				return true
			}
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
	
	fun matchesAliasName(name: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup): Boolean {
		//TODO 匹配scope
		val alias = configGroup.aliases[aliasName] ?: return false
		val aliasSubName = resolveAliasSubNameExpression(name, quoted, alias, configGroup)
		if(aliasSubName != null) {
			val expression = CwtKeyExpression.resolve(aliasSubName)
			if(matchesKey(expression, name, ParadoxValueType.infer(name), quoted, configGroup)) {
				return true
			}
		}
		//如果aliasName是modifier，则aliasSubName也可以是modifiers中的modifier
		if(aliasName == "modifier") {
			if(matchesModifier(name, configGroup)) return true
		}
		return false
	}
	
	fun matchesModifier(name: String, configGroup: CwtConfigGroup): Boolean {
		val modifiers = configGroup.modifiers
		return modifiers.containsKey(name)
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
	
	private fun shouldComplete(config: CwtPropertyConfig, definitionElementInfo: ParadoxDefinitionElementInfo): Boolean {
		val expression = config.keyExpression
		//如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
		if(expression.type == CwtDataTypes.AliasName) return true
		val actualCount = definitionElementInfo.childPropertyOccurrence[expression] ?: 0
		//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
		val cardinality = config.cardinality
		val maxCount = when {
			cardinality == null -> if(expression.type == CwtDataTypes.Constant) 1 else null
			else -> cardinality.max
		}
		return maxCount == null || actualCount < maxCount
	}
	
	private fun shouldComplete(config: CwtValueConfig, definitionElementInfo: ParadoxDefinitionElementInfo): Boolean {
		val expression = config.valueExpression
		val actualCount = definitionElementInfo.childValueOccurrence[expression] ?: 0
		//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
		val cardinality = config.cardinality
		val maxCount = when {
			cardinality == null -> if(expression.type == CwtDataTypes.Constant) 1 else null
			else -> cardinality.max
		}
		return maxCount == null || actualCount < maxCount
	}
	
	fun completeKey(expression: CwtKeyExpression, keyword: String, quoted: Boolean, config: CwtKvConfig<*>,
		configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null) {
		if(expression.isEmpty()) return
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeText = localisation.containingFile.name
					val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.SyncedLocalisation -> {
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				processSyncedLocalisationVariants(keyword, configGroup.project) { syncedLocalisation ->
					val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeText = syncedLocalisation.containingFile.name
					val lookupElement = LookupElementBuilder.create(syncedLocalisation, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeText = localisation.containingFile.name
					val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.TypeExpression -> {
				val typeExpression = expression.value ?: return
				val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
				if(definitions.isEmpty()) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				for(definition in definitions) {
					val n = definition.definitionInfo?.name ?: continue
					val name = n.quoteIf(quoted)
					val typeText = definition.containingFile.name
					val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = true)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpressionString -> {
				val typeExpression = expression.value ?: return
				val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
				if(definitions.isEmpty()) return
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				for(definition in definitions) {
					val definitionName = definition.definitionInfo?.name ?: continue
					val n = "$prefix$definitionName$suffix"
					val name = n.quoteIf(quoted)
					val typeText = definition.containingFile.name
					val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = true)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return
				val valueConfig = configGroup.values[valueName] ?: return
				val valueValueConfigs = valueConfig.valueConfigMap.values
				if(valueValueConfigs.isEmpty()) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
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
						.withNeededInsertHandler(isKey = true)
						.withCaseSensitivity(false) //忽略大小写
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.ValueSet -> {
				//TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return
				val enumConfig = configGroup.enums[enumName] ?: return
				val enumValueConfigs = enumConfig.valueConfigMap.values
				if(enumValueConfigs.isEmpty()) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
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
						.withNeededInsertHandler(isKey = true)
						.withCaseSensitivity(false) //忽略大小写
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.ComplexEnum -> {
				//TODO
			}
			CwtDataTypes.Scope -> pass() //TODO
			CwtDataTypes.ScopeField -> pass() //TODO
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return
				completeAliasName(keyword, quoted, aliasName, config, configGroup, result, scope, isKey = true)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return
				completeAliasName(keyword, quoted, aliasName, config, configGroup, result, scope, isKey = true)
			}
			CwtDataTypes.Constant -> {
				val n = expression.value ?: return
				//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = config.pointer.element ?: return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " in ${config.configFileName}"
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withNeededInsertHandler(isKey = true)
					.withCaseSensitivity(false) //忽略大小写
					.withPriority(PlsPriorities.propertyPriority)
				result.addElement(lookupElement)
			}
			else -> pass()
		}
	}
	
	fun completeValue(expression: CwtValueExpression, keyword: String, quoted: Boolean, config: CwtKvConfig<*>,
		configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null) {
		if(expression.isEmpty()) return
		when(expression.type) {
			CwtDataTypes.Bool -> {
				result.addAllElements(boolLookupElements)
			}
			CwtDataTypes.Localisation -> {
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeText = localisation.containingFile.name
					val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.SyncedLocalisation -> {
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				processSyncedLocalisationVariants(keyword, configGroup.project) { syncedLocalisation ->
					val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeText = syncedLocalisation.containingFile.name
					val lookupElement = LookupElementBuilder.create(syncedLocalisation, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeText = localisation.containingFile.name
					val lookupElement = LookupElementBuilder.create(localisation, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.AbsoluteFilePath -> pass() //不提示绝对路径
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val expressionValue = expression.value
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(configGroup.project, distinct = true)
				} else {
					findFilesByFilePath(expressionValue, configGroup.project, expressionType = expressionType, distinct = true)
				}
				if(virtualFiles.isEmpty()) return
				val tailText = " by $expression in ${config.configFileName}"
				for(virtualFile in virtualFiles) {
					val file = virtualFile.toPsiFile<PsiFile>(configGroup.project) ?: continue
					val filePath = virtualFile.fileInfo?.path?.path ?: continue
					val icon = virtualFile.fileType.icon
					val name = expressionType.extract(expressionValue, filePath) ?: continue
					val typeText = virtualFile.name
					val lookupElement = LookupElementBuilder.create(file, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val expressionValue = expression.value
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(configGroup.project, distinct = true)
				} else {
					findFilesByFilePath(expressionValue, configGroup.project, expressionType = expressionType, distinct = true)
				}
				if(virtualFiles.isEmpty()) return
				val tailText = " by $expression in ${config.configFileName}"
				for(virtualFile in virtualFiles) {
					val file = virtualFile.toPsiFile<PsiFile>(configGroup.project) ?: continue
					val filePath = virtualFile.fileInfo?.path?.path ?: continue
					val icon = virtualFile.fileType.icon
					val name = expressionType.extract(expressionValue, filePath) ?: continue
					val typeText = virtualFile.name
					val lookupElement = LookupElementBuilder.create(file, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpression -> {
				val typeExpression = expression.value ?: return
				val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
				if(definitions.isEmpty()) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				for(definition in definitions) {
					val n = definition.definitionInfo?.name ?: continue
					val name = n.quoteIf(quoted)
					val typeText = definition.containingFile.name
					val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpressionString -> {
				val typeExpression = expression.value ?: return
				val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
				if(definitions.isEmpty()) return
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				for(definition in definitions) {
					val definitionName = definition.definitionInfo?.name ?: continue
					val n = "$prefix$definitionName$suffix"
					val name = n.quoteIf(quoted)
					val typeText = definition.containingFile.name
					val lookupElement = LookupElementBuilder.create(definition, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return
				val valueConfig = configGroup.values[valueName] ?: return
				val valueValueConfigs = valueConfig.valueConfigMap.values
				if(valueValueConfigs.isEmpty()) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				val typeText = valueConfig.pointer.containingFile?.name ?: anonymousString
				for(valueValueConfig in valueValueConfigs) {
					if(quoted && valueValueConfig.stringValue == null) continue
					val n = valueValueConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = valueValueConfig.pointer.element ?: continue
					val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withCaseSensitivity(false) //忽略大小写
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.ValueSet -> {
				//TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return
				val enumConfig = configGroup.enums[enumName] ?: return
				val enumValueConfigs = enumConfig.valueConfigMap.values
				if(enumValueConfigs.isEmpty()) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				val typeText = enumConfig.pointer.containingFile?.name ?: anonymousString
				for(enumValueConfig in enumValueConfigs) {
					if(quoted && enumValueConfig.stringValue == null) continue
					val n = enumValueConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = enumValueConfig.pointer.element ?: continue
					val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withCaseSensitivity(false) //忽略大小写
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.ComplexEnum -> {
				//TODO
			}
			CwtDataTypes.ScopeGroup -> {
				val scopeGroupName = expression.value ?: return
				val scopeGroupConfig = configGroup.scopeGroups[scopeGroupName] ?: return
				val scopeConfigs = scopeGroupConfig.valueConfigMap.values
				if(scopeConfigs.isEmpty()) return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " by $expression in ${config.configFileName}"
				val typeText = scopeGroupConfig.pointer.containingFile?.name ?: anonymousString
				for(scopeConfig in scopeConfigs) {
					val n = scopeConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = scopeConfig.pointer.element ?: continue
					val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
						.withTailText(tailText, true)
						.withTypeText(typeText, true)
						.withCaseSensitivity(false) //忽略大小写
						.withNeededInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Scope -> {
				pass() //TODO
			}
			CwtDataTypes.ScopeField -> {
				pass() //TODO
			}
			CwtDataTypes.VariableField -> pass() //TODO
			CwtDataTypes.IntVariableField -> pass() //TODO
			CwtDataTypes.ValueField -> pass() //TODO
			CwtDataTypes.IntValueField -> pass() //TODO
			//规则会被内联，不应该被匹配到
			CwtDataTypes.SingleAliasRight -> pass()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return
				completeAliasName(keyword, quoted, aliasName, config, configGroup, result, scope, isKey = false)
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.AliasMatchLeft -> pass()
			CwtDataTypes.Constant -> {
				val n = expression.value ?: return
				//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = config.pointer.element ?: return
				val icon = service<CwtConfigIconProvider>().resolve(config, expression.type)
				val tailText = " in ${config.configFileName}"
				val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
					.withTailText(tailText, true)
					.withNeededInsertHandler(isKey = false)
					.withCaseSensitivity(false) //忽略大小写
					.withPriority(PlsPriorities.propertyPriority)
				result.addElement(lookupElement)
			}
			else -> pass()
		}
	}
	
	fun completeAliasName(keyword: String, quoted: Boolean, aliasName: String, config: CwtKvConfig<*>,
		configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String?, isKey: Boolean) {
		val alias = configGroup.aliases[aliasName] ?: return
		for(aliasConfigs in alias.values) {
			//aliasConfigs的名字是相同的 
			val aliasConfig = aliasConfigs.firstOrNull() ?: continue
			//TODO alias的scope需要匹配（推断得到的scope为null时，总是提示）
			if(scope != null && !aliasConfig.supportedScopes.any { matchesScope(scope, it, configGroup) }) continue
			//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
			val finalScope = config.parent?.scope ?: scope
			//aliasSubName是一个表达式
			if(isKey) {
				completeKey(aliasConfig.keyExpression, keyword, quoted, aliasConfig.config, configGroup, result, finalScope)
			} else {
				completeValue(aliasConfig.valueExpression, keyword, quoted, aliasConfig.config, configGroup, result, finalScope)
			}
		}
		//如果aliasName是effect或trigger，则aliasSubName也可以是links中的link，或者其嵌套格式（root.owner）
		//TODO
		//如果aliasName是modifier，则aliasSubName也可以是modifiers中的modifier
		if(aliasName == "modifier") {
			//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
			val finalScope = config.parent?.scope ?: scope
			completeModifier(quoted, configGroup, result, finalScope, isKey)
		}
	}
	
	fun completeModifier(quoted: Boolean, configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null, isKey: Boolean) {
		val modifiers = configGroup.modifiers
		if(modifiers.isEmpty()) return
		for(modifierConfig in modifiers.values) {
			val categoryConfigMap = modifierConfig.categoryConfigMap ?: continue
			if(categoryConfigMap.isEmpty()) continue
			if(scope != null && !categoryConfigMap.values.any { c -> c.supportedScopes.any { s -> matchesScope(scope, s, configGroup) } }) continue
			val n = modifierConfig.name
			//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
			val name = n.quoteIf(quoted)
			val element = modifierConfig.pointer.element ?: continue
			val icon = service<CwtConfigIconProvider>().resolve(modifierConfig)
			val tailText = " from modifiers"
			val typeText = modifierConfig.pointer.containingFile?.name ?: anonymousString
			val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
				.withTailText(tailText, true)
				.withTypeText(typeText, true)
				.withNeededInsertHandler(isKey)
				.withPriority(PlsPriorities.modifierPriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeLocalisationCommand(configGroup: CwtConfigGroup, result: CompletionResultSet) {
		//TODO 匹配scope
		//val keyword = commandField.keyword
		val localisationCommands = configGroup.localisationCommands
		if(localisationCommands.isEmpty()) return
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for(localisationCommand in localisationCommands) {
			val config = localisationCommand.value
			val name = config.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = config.pointer.element ?: continue
			//val scopes = localisationCommand
			val icon = service<CwtConfigIconProvider>().resolve(config)
			val typeText = config.pointer.containingFile?.name ?: anonymousString
			val lookupElement = LookupElementBuilder.create(element, name).withIcon(icon)
				.withTypeText(typeText, true)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	val boolLookupElements = booleanValues.map { value ->
		LookupElementBuilder.create(value).bold().withPriority(PlsPriorities.keywordPriority)
	}
	
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
			val separator = if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "="
			EditorModificationUtil.insertStringAtCaret(editor, separator)
		}
	}
	
	private fun LookupElementBuilder.withNeededInsertHandler(isKey: Boolean): LookupElementBuilder {
		if(isKey) return withInsertHandler(separatorInsertHandler)
		return this
	}
	//endregion
	
	//region Resolve Extensions
	//TODO 基于cwt规则文件的解析方法需要进一步匹配scope
	inline fun resolveKey(keyElement: ParadoxScriptPropertyKey, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): PsiNamedElement? {
		val propertyConfig = keyElement.getPropertyConfig() ?: return null
		val expression = propertyConfig.keyExpression
		if(!expressionPredicate(expression)) return null
		return doResolveKey(keyElement, expression, propertyConfig)
	}
	
	@PublishedApi
	internal fun doResolveKey(keyElement: ParadoxScriptPropertyKey, expression: CwtKeyExpression, propertyConfig: CwtPropertyConfig): PsiNamedElement? {
		val project = keyElement.project
		return when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = keyElement.value
				findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = keyElement.value
				findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.TypeExpression -> {
				val name = keyElement.value
				val typeExpression = expression.value ?: return null
				findDefinitionByType(name, typeExpression, project)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = keyElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return null
				findDefinitionByType(name, typeExpression, project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return null
				val name = keyElement.value
				val gameType = keyElement.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
				valueValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ValueSet -> {
				propertyConfig.pointer.element //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return null
				val name = keyElement.value
				val gameType = keyElement.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
				enumValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ComplexEnum -> {
				propertyConfig.pointer.element //TODO
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return null
				val gameType = keyElement.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return null
				val gameType = keyElement.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup)
			}
			CwtDataTypes.Constant -> {
				propertyConfig.pointer.element
			}
			else -> {
				propertyConfig.pointer.element //TODO
			}
		}
	}
	
	inline fun multiResolveKey(keyElement: ParadoxScriptPropertyKey, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): List<PsiNamedElement> {
		val propertyConfig = keyElement.getPropertyConfig() ?: return emptyList()
		val expression = propertyConfig.keyExpression
		if(!expressionPredicate(expression)) return emptyList()
		return doMultiResolveKey(keyElement, expression, propertyConfig)
	}
	
	@PublishedApi
	internal fun doMultiResolveKey(keyElement: ParadoxScriptPropertyKey, expression: CwtKeyExpression, propertyConfig: CwtPropertyConfig): List<PsiNamedElement> {
		val project = keyElement.project
		return when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = keyElement.value
				findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = keyElement.value
				findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.TypeExpression -> {
				val name = keyElement.value
				val typeExpression = expression.value ?: return emptyList()
				findDefinitionsByType(name, typeExpression, project)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = keyElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return emptyList()
				findDefinitionsByType(name, typeExpression, project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return emptyList()
				val name = keyElement.value
				val gameType = keyElement.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return emptyList()
				valueValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			CwtDataTypes.ValueSet -> {
				propertyConfig.pointer.element.toSingletonListOrEmpty() //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return emptyList()
				val name = keyElement.value
				val gameType = keyElement.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
				enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			CwtDataTypes.ComplexEnum -> {
				propertyConfig.pointer.element.toSingletonListOrEmpty() //TODO
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = keyElement.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup).toSingletonListOrEmpty()
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = keyElement.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup).toSingletonListOrEmpty()
			}
			CwtDataTypes.Constant -> {
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
		val valueConfig = valueElement.getValueConfig() ?: return fallbackResolveValue(valueElement)
		val expression = valueConfig.valueExpression
		if(!expressionPredicate(expression)) return null
		return doResolveValue(valueElement, expression, valueConfig)
	}
	
	@PublishedApi
	internal fun doResolveValue(valueElement: ParadoxScriptValue, expression: CwtValueExpression, valueConfig: CwtValueConfig): PsiNamedElement? {
		val project = valueElement.project
		return when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = valueElement.value
				findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = valueElement.value
				findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = valueElement.value
				val path = filePath.toPathOrNull() ?: return null
				VfsUtil.findFile(path, true)?.toPsiFile(project)
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(expression.value, valueElement.value)
				findFileByFilePath(filePath, project)?.toPsiFile(project)
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(expression.value, valueElement.value) ?: return null
				findFileByFilePath(filePath, project)?.toPsiFile(project)
			}
			CwtDataTypes.TypeExpression -> {
				val name = valueElement.value
				val typeExpression = expression.value ?: return null
				findDefinitionByType(name, typeExpression, project)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = valueElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return null
				findDefinitionByType(name, typeExpression, project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return null
				val name = valueElement.value
				val gameType = valueElement.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
				valueValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ValueSet -> {
				valueConfig.pointer.element.castOrNull<CwtString>() //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return null
				val name = valueElement.value
				val gameType = valueElement.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
				enumValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ComplexEnum -> {
				valueConfig.pointer.element.castOrNull<CwtString>() //TODO
			}
			CwtDataTypes.ScopeGroup -> {
				val scopeGroupName = expression.value ?: return null
				val name = valueElement.value
				val gameType = valueElement.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val scopeConfig = configGroup.scopeGroups.get(scopeGroupName)?.valueConfigMap?.get(name) ?: return null
				scopeConfig.pointer.element.castOrNull<CwtString>()
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.SingleAliasRight -> {
				valueConfig.pointer.element.castOrNull<CwtString>()
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return null
				val gameType = valueElement.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				resolveAliasName(valueElement.value, valueElement.isQuoted(), aliasName, configGroup)
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.AliasMatchLeft -> {
				valueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.Constant -> {
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
		val valueConfig = valueElement.getValueConfig() ?: return fallbackMultiResolveValue(valueElement)
		val expression = valueConfig.valueExpression
		if(!expressionPredicate(expression)) return emptyList()
		return doMultiResolveValue(valueElement, expression, valueConfig)
	}
	
	@PublishedApi
	internal fun doMultiResolveValue(valueElement: ParadoxScriptValue, expression: CwtValueExpression, valueConfig: CwtValueConfig): List<PsiNamedElement> {
		val project = valueElement.project
		return when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = valueElement.value
				findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = valueElement.value
				findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = valueElement.value
				val path = filePath.toPathOrNull() ?: return emptyList()
				VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(expression.value, valueElement.value)
				findFilesByFilePath(filePath, project).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(expression.value, valueElement.value) ?: return emptyList()
				findFilesByFilePath(filePath, project).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.TypeExpression -> {
				val name = valueElement.value
				val typeExpression = expression.value ?: return emptyList()
				findDefinitionsByType(name, typeExpression, project)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = valueElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return emptyList()
				findDefinitionsByType(name, typeExpression, project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return emptyList()
				val name = valueElement.value
				val gameType = valueElement.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return emptyList()
				valueValueConfig.pointer.element.castOrNull<CwtString>()?.toSingletonList() ?: return emptyList()
			}
			CwtDataTypes.ValueSet -> {
				return emptyList() //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return emptyList()
				val name = valueElement.value
				val gameType = valueElement.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
				enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			CwtDataTypes.ComplexEnum -> {
				return emptyList() //TODO
			}
			CwtDataTypes.ScopeGroup -> {
				val scopeGroupName = expression.value ?: return emptyList()
				val name = valueElement.value
				val gameType = valueElement.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val scopeConfig = configGroup.scopeGroups.get(scopeGroupName)?.valueConfigMap?.get(name) ?: return emptyList()
				scopeConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.SingleAliasRight -> emptyList()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = valueElement.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				resolveAliasName(valueElement.value, valueElement.isQuoted(), aliasName, configGroup).toSingletonListOrEmpty()
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.AliasMatchLeft -> emptyList()
			CwtDataTypes.Constant -> {
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
	
	fun resolveAliasName(name: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup): PsiNamedElement? {
		val project = configGroup.project
		val aliasGroup = configGroup.aliases[aliasName] ?: return null
		val aliasSubName = resolveAliasSubNameExpression(name, quoted, aliasGroup, configGroup)
		if(aliasSubName != null) {
			val expression = CwtKeyExpression.resolve(aliasSubName)
			val resolved = when(expression.type) {
				CwtDataTypes.Localisation -> {
					findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
				}
				CwtDataTypes.SyncedLocalisation -> {
					findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
				}
				CwtDataTypes.TypeExpression -> {
					val typeExpression = expression.value ?: return null
					findDefinitionByType(name, typeExpression, project)
				}
				CwtDataTypes.TypeExpressionString -> {
					val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
					val nameToUse = name.removeSurrounding(prefix, suffix)
					val typeExpression = expression.value ?: return null
					findDefinitionByType(nameToUse, typeExpression, project)
				}
				CwtDataTypes.Value -> {
					val valueName = expression.value ?: return null
					val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
					valueValueConfig.pointer.element.castOrNull<CwtString>()
				}
				CwtDataTypes.ValueSet -> {
					null //TODO
				}
				CwtDataTypes.Enum -> {
					val enumName = expression.value ?: return null
					val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
					enumValueConfig.pointer.element.castOrNull<CwtString>()
				}
				CwtDataTypes.ComplexEnum -> {
					null //TODO
				}
				CwtDataTypes.Constant -> {
					//同名的定义有多个，取第一个即可
					val aliases = aliasGroup[aliasSubName]
					if(aliases != null) {
						val alias = aliases.firstOrNull()
						val element = alias?.pointer?.element
						if(element != null) return element
					}
					null
				}
				else -> null //TODO
			}
			if(resolved != null) return resolved
		}
		//如果aliasName是modifier，则aliasSubName也可以是modifiers中的modifier
		if(aliasName == "modifier") {
			val resolvedModifier = resolveModifier(name, configGroup)
			if(resolvedModifier != null) return resolvedModifier
		}
		return null
	}
	
	fun resolveModifier(name: String, configGroup: CwtConfigGroup): CwtProperty? {
		val modifier = configGroup.modifiers[name] ?: return null
		return modifier.pointer.element
	}
	
	fun resolveLocalisationCommand(name: String, configGroup: CwtConfigGroup): CwtProperty? {
		//TODO 匹配scope
		val localisationCommands = configGroup.localisationCommands
		if(localisationCommands.isEmpty()) return null
		val commandConfig = localisationCommands[name] ?: return null
		return commandConfig.pointer.element
	}
//endregion
}