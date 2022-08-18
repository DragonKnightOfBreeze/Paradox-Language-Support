@file:Suppress("NAME_SHADOWING")

package icu.windea.pls.config.cwt

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.selector.*
import javax.swing.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.text.removeSurrounding

/**
 * CWT规则的处理器。
 *
 * 提供基于CWT规则实现的匹配、校验、代码提示、引用解析等功能。
 */
object CwtConfigHandler {
	const val paramsEnumName = "scripted_effect_params"
	//const val modifierAliasName = "modifier"
	
	//region Misc Methods
	fun getAliasSubName(key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup): String? {
		val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
		if(constKey != null) return constKey
		val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
		return keys.find {
			val expression = CwtKeyExpression.resolve(it)
			matchesKey(expression, key, ParadoxValueType.infer(key), quoted, configGroup)
		}
	}
	
	fun getScopeName(scopeNameOrAlias: String, configGroup: CwtConfigGroup): String {
		//handle "any" and "all" scope 
		if(scopeNameOrAlias.equals("any", true)) return "Any"
		if(scopeNameOrAlias.equals("all", true)) return "All"
		//a scope may not have aliases, or not defined in scopes.cwt
		return configGroup.scopes[scopeNameOrAlias]?.name
			?: configGroup.scopeAliasMap[scopeNameOrAlias]?.name
			?: scopeNameOrAlias.toCapitalizedWords()
	}
	
	fun matchScope(scope: String?, scopesToMatch: Collection<String>?, configGroup: CwtConfigGroup): Boolean {
		if(scope == null || scope.equals("any", true) || scope.equals("all", true)) return true
		if(scopesToMatch.isNullOrEmpty()) return true
		return scopesToMatch.any { s ->
			if(s.equals("any", true) || s.equals("all", true)) return@any true
			scope.equals(s, true) || configGroup.scopeAliasMap[scope]?.aliases?.contains(s) ?: false
		}
	}
	
	//fun matchScope(scopes: Collection<String>?, scopesToMatch: Collection<String>?, configGroup: CwtConfigGroup): Boolean {
	//	if(scopes.isNullOrEmpty()) return true
	//	return scopes.any { scope -> matchScope(scope, scopesToMatch, configGroup) }
	//}
	
	private fun isAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.keyExpression.type == CwtDataTypes.AliasName &&
			propertyConfig.valueExpression.type == CwtDataTypes.AliasMatchLeft
	}
	
	private fun isSingleAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.valueExpression.type == CwtDataTypes.SingleAliasRight
	}
	
	//fun mergeScope(scopeMap: MutableMap<String, String>, thisScope: String?): MutableMap<String, String> {
	//	if(thisScope == null) return scopeMap
	//	val mergedScopeMap = scopeMap.toMutableMap()
	//	mergedScopeMap.put("this", thisScope)
	//	return scopeMap
	//}
	//endregion
	
	//region Matches Methods
	//TODO 基于cwt规则文件的匹配方法需要进一步匹配scope
	//DONE 兼容variableReference inlineMath parameter 
	
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
		return matchesScriptExpression(expression, value, valueType, quoted, configGroup, true)
	}
	
	fun matchesValue(expression: CwtValueExpression, valueElement: ParadoxScriptValue, configGroup: CwtConfigGroup): Boolean {
		//当valueElement是scriptBlock时，expression必须是emptyExpression
		if(valueElement is IParadoxScriptBlock) return expression == CwtValueExpression.EmptyExpression
		
		return matchesValue(expression, valueElement.value, valueElement.valueType, valueElement.isQuoted(), configGroup)
	}
	
	fun matchesValue(expression: CwtValueExpression, value: String, valueType: ParadoxValueType, quoted: Boolean, configGroup: CwtConfigGroup): Boolean {
		return matchesScriptExpression(expression, value, valueType, quoted, configGroup, false)
	}
	
	fun matchesScriptExpression(expression: CwtKvExpression, value: String, valueType: ParadoxValueType, quoted: Boolean, configGroup: CwtConfigGroup, isKey: Boolean? = null): Boolean {
		if(expression.isEmpty() && value.isEmpty()) return true //匹配空字符串
		when(expression.type) {
			CwtDataTypes.Any -> {
				return true
			}
			CwtDataTypes.Bool -> {
				return valueType.matchesBooleanType()
			}
			CwtDataTypes.Int -> {
				//注意：用括号括起的整数（作为scalar）也匹配这个规则
				return valueType.matchesIntType() || ParadoxValueType.infer(value).matchesIntType() && expression.extraValue?.cast<IntRange>()?.contains(value.toIntOrNull()) ?: true
			}
			CwtDataTypes.Float -> {
				//注意：用括号括起的浮点数（作为scalar）也匹配这个规则
				return valueType.matchesFloatType() || ParadoxValueType.infer(value).matchesFloatType() && expression.extraValue?.cast<FloatRange>()?.contains(value.toFloatOrNull()) ?: true
			}
			CwtDataTypes.Scalar -> {
				if(value.isParameterAwareExpression()) return true
				return true
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
				if(value.isParameterAwareExpression()) return true
				val selector = localisationSelector().gameType(configGroup.gameType)
				return findLocalisation(value, configGroup.project, preferFirst = true, selector = selector) != null
				//return true
			}
			CwtDataTypes.SyncedLocalisation -> {
				if(value.isParameterAwareExpression()) return true
				val selector = localisationSelector().gameType(configGroup.gameType)
				return findSyncedLocalisation(value, configGroup.project, preferFirst = true, selector = selector) != null
				//return true
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return true
				if(value.isParameterAwareExpression()) return true
				val selector = localisationSelector().gameType(configGroup.gameType)
				return findLocalisation(value, configGroup.project, preferFirst = true, selector = selector) != null
				//return true
			}
			CwtDataTypes.AbsoluteFilePath -> {
				if(value.isParameterAwareExpression()) return true
				val path = value.toPathOrNull() ?: return false
				return VfsUtil.findFile(path, true) != null
			}
			CwtDataTypes.FilePath -> {
				if(value.isParameterAwareExpression()) return true
				val resolvedPath = CwtFilePathExpressionTypes.FilePath.resolve(expression.value, value.normalizePath())
				val selector = fileSelector().gameType(configGroup.gameType)
				return findFileByFilePath(resolvedPath, configGroup.project, selector = selector) != null
			}
			CwtDataTypes.Icon -> {
				if(value.isParameterAwareExpression()) return true
				val resolvedPath = CwtFilePathExpressionTypes.Icon.resolve(expression.value, value.normalizePath()) ?: return false
				val selector = fileSelector().gameType(configGroup.gameType)
				return findFileByFilePath(resolvedPath, configGroup.project, selector = selector) != null
			}
			CwtDataTypes.TypeExpression -> {
				if(value.isParameterAwareExpression()) return true
				val typeExpression = expression.value ?: return false
				val selector = definitionSelector().gameType(configGroup.gameType)
				return findDefinitionByType(value, typeExpression, configGroup.project, preferFirst = true, selector = selector) != null
				//return true
			}
			CwtDataTypes.TypeExpressionString -> {
				if(value.isParameterAwareExpression()) return true
				val typeExpression = expression.value ?: return false
				val selector = definitionSelector().gameType(configGroup.gameType)
				return findDefinitionByType(value, typeExpression, configGroup.project, preferFirst = true, selector = selector) != null
				//return true
			}
			CwtDataTypes.Enum -> {
				//TODO 支持complex_enum
				if(value.isParameterAwareExpression()) return true
				val enumName = expression.value ?: return false
				//匹配参数名（即使对应的定义声明中不存在对应名字的参数，也总是匹配）
				if(isKey == true && enumName == paramsEnumName) return true
				val enumValues = configGroup.enums[enumName]?.values ?: return false
				return value in enumValues
			}
			CwtDataTypes.Value -> {
				if(value.isParameterAwareExpression()) return true
				//val valueSetName = expression.value ?: return false
				//val valueValues = configGroup.values[valueSetName]?.values ?: return false
				//return value in valueValues
				return true //任意不带参数，不为复杂表达式的字符串
			}
			CwtDataTypes.ValueSet -> {
				if(value.isParameterAwareExpression()) return true
				return true //任意不带参数，不为复杂表达式的字符串
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope -> {
				if(quoted) return false //不允许用引号括起
				if(value.isParameterAwareExpression()) return true
				return matchesScopeFieldExpression(value, configGroup)
			}
			CwtDataTypes.ScopeGroup -> {
				if(quoted) return false //不允许用引号括起
				if(value.isParameterAwareExpression()) return true
				return matchesScopeFieldExpression(value, configGroup)
			}
			CwtDataTypes.ValueField -> {
				if(value.isParameterAwareExpression()) return true
				//也可以是整数，注意：用括号括起的整数（作为scalar）也匹配这个规则
				if(valueType.matchesFloatType() || ParadoxValueType.infer(value).matchesFloatType()) return true
				if(quoted) return false //接下来的匹配不允许用引号括起
				return matchesValueFieldExpression(value, configGroup)
			}
			CwtDataTypes.IntValueField -> {
				if(value.isParameterAwareExpression()) return true
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(valueType.matchesIntType() || ParadoxValueType.infer(value).matchesIntType()) return true
				if(quoted) return false //接下来的匹配不允许用引号括起
				return matchesValueFieldExpression(value, configGroup)
			}
			CwtDataTypes.VariableField -> {
				if(value.isParameterAwareExpression()) return true
				return false //TODO
			}
			CwtDataTypes.IntVariableField -> {
				if(value.isParameterAwareExpression()) return true
				return false //TODO
			}
			CwtDataTypes.Modifier -> {
				//匹配预定义的modifier
				if(value.isParameterAwareExpression()) return true
				return matchesModifier(value, configGroup)
			}
			CwtDataTypes.SingleAliasRight -> {
				if(value.isParameterAwareExpression()) return true
				return false //不在这里处理
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				if(value.isParameterAwareExpression()) return true
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup, isKey = true)
			}
			CwtDataTypes.AliasName -> {
				if(value.isParameterAwareExpression()) return true
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup, isKey = true)
			}
			CwtDataTypes.AliasMatchLeft -> {
				if(value.isParameterAwareExpression()) return true
				return false //不在这里处理
			}
			CwtDataTypes.Constant -> {
				return value.equals(expression.value, true) //忽略大小写
			}
			CwtDataTypes.Other -> return true
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
		val key = propertyElement.name
		val quoted = propertyElement.propertyKey.isQuoted()
		val aliasSubName = getAliasSubName(key, quoted, aliasName, configGroup) ?: return false
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
		val aliases = aliasGroup[aliasSubName] ?: return false
		return aliases.any { alias ->
			matchesProperty(propertyElement, alias.config, configGroup)
		}
	}
	
	private fun matchesAliasName(name: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, isKey: Boolean): Boolean {
		//TODO 匹配scope
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return false
		val aliasSubName = getAliasSubName(name, quoted, aliasName, configGroup) ?: return false
		val expression = CwtKeyExpression.resolve(aliasSubName)
		return matchesKey(expression, name, ParadoxValueType.infer(name), quoted, configGroup)
	}
	
	fun matchesModifier(name: String, configGroup: CwtConfigGroup): Boolean {
		val modifiers = configGroup.modifiers
		return modifiers.containsKey(name)
	}
	
	fun matchesScopeFieldExpression(expression: String, configGroup: CwtConfigGroup): Boolean {
		return ParadoxScriptScopeFieldExpression.resolve(expression, configGroup).isMatched()
	}
	
	fun matchesValueFieldExpression(expression: String, configGroup: CwtConfigGroup): Boolean {
		return ParadoxScriptValueFieldExpression.resolve(expression, configGroup).isMatched()
	}
	
	/**
	 * 当已经匹配时，是否可认为精确匹配。如果精确匹配，需要考虑忽略余下的非精确匹配的表达式。
	 */
	fun matchesExactly(expression: CwtKvExpression, value: String, configGroup: CwtConfigGroup): Boolean {
		//对于整数和数字，已匹配的情况下认为精确匹配
		if(ParadoxValueType.infer(value).matchesFloatType()) return true
		return when(expression.type) {
			CwtDataTypes.Any -> false
			CwtDataTypes.Bool -> true
			CwtDataTypes.Int -> true
			CwtDataTypes.Float -> true
			CwtDataTypes.Scalar -> false
			CwtDataTypes.ColorField -> true
			CwtDataTypes.PercentageField -> true
			CwtDataTypes.DateField -> true
			CwtDataTypes.Localisation -> false
			CwtDataTypes.SyncedLocalisation -> false
			CwtDataTypes.InlineLocalisation -> false
			CwtDataTypes.AbsoluteFilePath -> false
			CwtDataTypes.FilePath -> false
			CwtDataTypes.Icon -> false
			CwtDataTypes.TypeExpression -> false
			CwtDataTypes.TypeExpressionString -> false
			CwtDataTypes.Enum -> configGroup.enums.containsKey(expression.value!!)
			CwtDataTypes.Value -> configGroup.enums.containsKey(expression.value!!)
			CwtDataTypes.ValueSet -> false
			CwtDataTypes.ScopeField -> false
			CwtDataTypes.Scope -> false
			CwtDataTypes.ScopeGroup -> false
			CwtDataTypes.ValueField -> false
			CwtDataTypes.IntValueField -> false
			CwtDataTypes.VariableField -> false
			CwtDataTypes.IntVariableField -> false
			CwtDataTypes.Modifier -> configGroup.modifiers.containsKey(value)
			CwtDataTypes.SingleAliasRight -> false
			CwtDataTypes.AliasName -> false
			CwtDataTypes.AliasKeysField -> false
			CwtDataTypes.Constant -> true
			CwtDataTypes.Other -> false
			CwtDataTypes.AliasMatchLeft -> false
		}
	}
	//endregion
	
	//region Complete Methods
	fun addKeyCompletions(keyElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet, context: ProcessingContext): Boolean {
		val project = propertyElement.project
		val definitionElementInfo = propertyElement.definitionElementInfo ?: return true
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val childPropertyConfigs = definitionElementInfo.childPropertyConfigs
		if(childPropertyConfigs.isEmpty()) return true
		
		context.put(PlsCompletionKeys.isKeyKey, true)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		
		for(propConfig in childPropertyConfigs) {
			if(shouldComplete(propConfig, definitionElementInfo)) {
				context.completeKey(keyElement, propConfig.keyExpression, propConfig, result, scope)
			}
		}
		return true
	}
	
	fun addValueCompletions(valueElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet, context: ProcessingContext): Boolean {
		val project = propertyElement.project
		val definitionElementInfo = propertyElement.definitionElementInfo ?: return true
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val configs = definitionElementInfo.configs
		if(configs.isEmpty()) return true
		
		context.put(PlsCompletionKeys.isKeyKey, false)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		
		for(config in configs) {
			if(config is CwtPropertyConfig) {
				context.completeValue(valueElement, config.valueExpression, config, result, scope)
			}
		}
		return true
	}
	
	fun addValueCompletionsInBlock(valueElement: PsiElement, blockElement: ParadoxScriptBlock, result: CompletionResultSet, context: ProcessingContext): Boolean {
		val project = blockElement.project
		val definitionElementInfo = blockElement.definitionElementInfo ?: return true
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val childValueConfigs = definitionElementInfo.childValueConfigs
		if(childValueConfigs.isEmpty()) return true
		
		context.put(PlsCompletionKeys.isKeyKey, false)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		
		for(valueConfig in childValueConfigs) {
			if(shouldComplete(valueConfig, definitionElementInfo)) {
				context.completeValue(valueElement, valueConfig.valueExpression, valueConfig, result, scope)
			}
		}
		return true
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
	
	fun ProcessingContext.completeKey(contextElement: PsiElement, expression: CwtKeyExpression, config: CwtPropertyConfig, result: CompletionResultSet, scope: String?) {
		completeScriptExpression(contextElement, expression, config, result, scope)
	}
	
	fun ProcessingContext.completeValue(contextElement: PsiElement, expression: CwtValueExpression, config: CwtKvConfig<*>, result: CompletionResultSet, scope: String?) {
		completeScriptExpression(contextElement, expression, config, result, scope)
	}
	
	fun ProcessingContext.completeScriptExpression(contextElement: PsiElement, expression: CwtKvExpression, config: CwtKvConfig<*>, result: CompletionResultSet, scope: String?) {
		if(expression.isEmpty()) return
		if(keyword.isParameterAwareExpression()) return //排除带参数或者的情况
		when(expression.type) {
			CwtDataTypes.Bool -> {
				result.addAllElements(boolLookupElements)
			}
			CwtDataTypes.Localisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processLocalisationVariants(keyword, configGroup.project, selector = selector) { localisation ->
					val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withExpectedIcon(PlsIcons.Localisation)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.SyncedLocalisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processSyncedLocalisationVariants(keyword, configGroup.project, selector = selector) { syncedLocalisation ->
					val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = syncedLocalisation.containingFile
					val lookupElement = LookupElementBuilder.create(syncedLocalisation, name)
						.withExpectedIcon(PlsIcons.Localisation)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withExpectedIcon(PlsIcons.Localisation)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.AbsoluteFilePath -> pass() //不提示绝对路径
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val expressionValue = expression.value
				val selector = fileSelector().gameTypeFrom(contextElement).preferRootFrom(contextElement)
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(configGroup.project, distinct = true, selector = selector)
				} else {
					findFilesByFilePath(expressionValue, configGroup.project, expressionType = expressionType, distinct = true, selector = selector)
				}
				if(virtualFiles.isEmpty()) return
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				for(virtualFile in virtualFiles) {
					val file = virtualFile.toPsiFile<PsiFile>(configGroup.project) ?: continue
					val filePath = virtualFile.fileInfo?.path?.path ?: continue
					val name = expressionType.extract(expressionValue, filePath) ?: continue
					val lookupElement = LookupElementBuilder.create(file, name) //没有图标
						.withTailText(tailText, true)
						.withTypeText(file.name, file.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val expressionValue = expression.value
				val selector = fileSelector().gameTypeFrom(contextElement).preferRootFrom(contextElement)
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(configGroup.project, distinct = true, selector = selector)
				} else {
					findFilesByFilePath(expressionValue, configGroup.project, expressionType = expressionType, distinct = true, selector = selector)
				}
				if(virtualFiles.isEmpty()) return
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				for(virtualFile in virtualFiles) {
					val file = virtualFile.toPsiFile<PsiFile>(configGroup.project) ?: continue
					val filePath = virtualFile.fileInfo?.path?.path ?: continue
					val name = expressionType.extract(expressionValue, filePath) ?: continue
					val lookupElement = LookupElementBuilder.create(file, name) //没有图标
						.withTailText(tailText, true)
						.withTypeText(file.name, file.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpression -> {
				val typeExpression = expression.value ?: return
				val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(contextElement)
				val definitions = findAllDefinitionsByType(typeExpression, configGroup.project, distinct = true, selector = selector) //不预先过滤结果
				if(definitions.isEmpty()) return
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				for(definition in definitions) {
					val n = definition.definitionInfo?.name ?: continue
					val name = n.quoteIf(quoted)
					val typeFile = definition.containingFile
					val lookupElement = LookupElementBuilder.create(definition, name)
						.withExpectedIcon(PlsIcons.Definition)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpressionString -> {
				val typeExpression = expression.value ?: return
				val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(contextElement)
				val definitions = findAllDefinitionsByType(typeExpression, configGroup.project, distinct = true, selector = selector) //不预先过滤结果
				if(definitions.isEmpty()) return
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				for(definition in definitions) {
					val definitionName = definition.definitionInfo?.name ?: continue
					val n = "$prefix$definitionName$suffix"
					val name = n.quoteIf(quoted)
					val typeFile = definition.containingFile
					val lookupElement = LookupElementBuilder.create(definition, name)
						.withExpectedIcon(PlsIcons.Definition)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Enum -> {
				//TODO 支持complex_enum
				val enumName = expression.value ?: return
				//提示参数名（仅限key）
				if(isKey && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val propertyElement = contextElement.findParentDefinitionProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return
					completeParameters(propertyElement, config, result)
					return
				}
				val enumConfig = configGroup.enums[enumName] ?: return
				val enumValueConfigs = enumConfig.valueConfigMap.values
				if(enumValueConfigs.isEmpty()) return
				val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
				val typeFile = enumConfig.pointer.containingFile
				for(enumValueConfig in enumValueConfigs) {
					if(quoted && enumValueConfig.stringValue == null) continue
					val n = enumValueConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = enumValueConfig.pointer.element ?: continue
					val lookupElement = LookupElementBuilder.create(element, name)
						.withExpectedIcon(PlsIcons.EnumValue)
						.withTailText(tailText, true)
						.withTypeText(typeFile?.name, typeFile?.icon, true)
						.withCaseSensitivity(false) //忽略大小写
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				//提示来自脚本文件的value
				run {
					val selector = valueSetValueSelector().gameType(configGroup.gameType)
					val valuesInValueSet = findAllValueSetValues(valueSetName, configGroup.project, distinct = true, selector = selector)
					for(valueSetValue in valuesInValueSet) {
						val n = runCatching { valueSetValue.stub?.castOrNull<ParadoxValueSetValueStub>()?.name }.getOrNull() ?: valueSetValue.value
						val name = n.quoteIf(quoted)
						val element = valueSetValue
						//不显示typeText
						val lookupElement = LookupElementBuilder.create(element, name)
							.withExpectedIcon(PlsIcons.ValueSetValue)
							.withTailText(tailText, true)
							.withExpectedInsertHandler(isKey)
							.withCaseSensitivity(false) //忽略大小写
						result.addElement(lookupElement)
					}
				}
				//提示预定义的value
				run {
					val valueConfig = configGroup.values[valueSetName] ?: return@run
					val valueSetValueConfigs = valueConfig.valueConfigMap.values
					if(valueSetValueConfigs.isEmpty()) return@run
					for(valueSetValueConfig in valueSetValueConfigs) {
						if(quoted && valueSetValueConfig.stringValue == null) continue
						val n = valueSetValueConfig.value
						//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
						val name = n.quoteIf(quoted)
						val element = valueSetValueConfig.pointer.element ?: continue
						val typeFile = valueConfig.pointer.containingFile
						val lookupElement = LookupElementBuilder.create(element, name)
							.withExpectedIcon(PlsIcons.HardCodedValueSetValue)
							.withTailText(tailText, true)
							.withTypeText(typeFile?.name, typeFile?.icon, true)
							.withExpectedInsertHandler(isKey)
							.withCaseSensitivity(false) //忽略大小写
						result.addElement(lookupElement)
					}
				}
			}
			CwtDataTypes.ValueSet -> {
				return //不需要进行提示
			}
			CwtDataTypes.ScopeField -> {
				completeScopeFieldExpression(result)
			}
			CwtDataTypes.Scope -> {
				put(PlsCompletionKeys.scopeNameKey, expression.value)
				completeScopeFieldExpression(result)
				put(PlsCompletionKeys.scopeNameKey, null)
			}
			CwtDataTypes.ScopeGroup -> {
				put(PlsCompletionKeys.scopeGroupNameKey, expression.value)
				completeScopeFieldExpression(result)
				put(PlsCompletionKeys.scopeGroupNameKey, null)
			}
			CwtDataTypes.ValueField -> {
				completeValueFieldExpression(result)
			}
			CwtDataTypes.IntValueField -> {
				completeValueFieldExpression(result, isInt = true)
			}
			CwtDataTypes.VariableField -> pass() //TODO
			CwtDataTypes.IntVariableField -> pass() //TODO
			CwtDataTypes.Modifier -> {
				//提示预定义的modifier
				//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
				val nextScope = config.parent?.scope ?: scope
				completeModifier(result, nextScope)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> pass()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return
				completeAliasName(contextElement, aliasName, config, result, scope)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return
				completeAliasName(contextElement, aliasName, config, result, scope)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> pass()
			CwtDataTypes.Constant -> {
				val n = expression.value ?: return
				//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = config.resolved.pointer.element ?: return
				val typeFile = config.resolved.pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withExpectedIcon(if(isKey) PlsIcons.Property else PlsIcons.Value, config)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withExpectedInsertHandler(isKey)
					.withCaseSensitivity(false) //忽略大小写
					.withPriority(if(isKey) PlsCompletonPriorities.propertyPriority else PlsCompletonPriorities.valuePriority)
				result.addElement(lookupElement)
			}
			else -> pass()
		}
	}
	
	fun ProcessingContext.completeAliasName(contextElement: PsiElement, aliasName: String, config: CwtKvConfig<*>, result: CompletionResultSet, scope: String?) {
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
		for(aliasConfigs in aliasGroup.values) {
			//aliasConfigs的名字是相同的 
			val aliasConfig = aliasConfigs.firstOrNull() ?: continue
			//TODO alias的scope需要匹配（推断得到的scope为null时，总是提示）
			val isScopeMatched = matchScope(scope, aliasConfig.supportedScopes, configGroup)
			if(!isScopeMatched) continue
			
			//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
			val nextScope = config.parent?.scope ?: scope
			//aliasSubName是一个表达式
			if(isKey) {
				completeKey(contextElement, aliasConfig.keyExpression, aliasConfig.config, result, nextScope)
			} else {
				completeValue(contextElement, aliasConfig.valueExpression, aliasConfig.config, result, nextScope)
			}
		}
	}
	
	fun ProcessingContext.completeModifier(result: CompletionResultSet, scope: String?) {
		val modifiers = configGroup.modifiers
		if(modifiers.isEmpty()) return
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for(modifierConfig in modifiers.values) {
			//排除不匹配modifier的supported_scopes的情况
			val isScopeMatched = scope == null || modifierConfig.categoryConfigMap.values.any { c -> matchScope(scope, c.supportedScopes, configGroup) }
			if(!isScopeMatched) continue
			
			val n = modifierConfig.name
			//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
			val name = n.quoteIf(quoted)
			val element = modifierConfig.pointer.element ?: continue
			val tailText = " from modifiers"
			val typeFile = modifierConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				//.apply { if(!scopeMatched) withItemTextForeground(Color.GRAY) }
				.withExpectedIcon(PlsIcons.Modifier)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withPriority(PlsCompletonPriorities.modifierPriority)
				//.withPriority(PlsCompletonPriorities.modifierPriority, scopeMatched)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeScopeFieldExpression(result: CompletionResultSet) {
		//基于当前位置的代码补全
		val expression = ParadoxScriptScopeFieldExpression.resolve(keyword, configGroup)
		expression.complete(result, this)
	}
	
	fun ProcessingContext.completeValueFieldExpression(result: CompletionResultSet, isInt: Boolean = false) {
		//基于当前位置的代码补全
		val expression = ParadoxScriptValueFieldExpression.resolve(keyword, configGroup)
		expression.complete(result, this)
	}
	
	fun ProcessingContext.completeScope(result: CompletionResultSet) {
		//TODO 进一步匹配scope
		val keyword = keyword
		val lookupElements = mutableSetOf<LookupElement>()
		val systemScopeConfigs = InternalConfigHandler.getSystemScopeMap().values
		val linkConfigs = configGroup.linksAsScopeNotData
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		
		for(systemScopeConfig in systemScopeConfigs) {
			val name = systemScopeConfig.id
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = systemScopeConfig.pointer.element ?: continue
			val tailText = " from system scopes"
			val typeFile = systemScopeConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.SystemScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletonPriorities.systemScopePriority)
			lookupElements.add(lookupElement)
		}
		for(linkConfig in linkConfigs.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val isScopeMatched = matchScope(outputScope, linkConfig.inputScopes, configGroup)
			if(!isScopeMatched) continue
			
			val name = linkConfig.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from scopes"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.Scope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletonPriorities.scopePriority)
			lookupElements.add(lookupElement)
		}
		result.withPrefixMatcher(keyword).addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeScopeFieldPrefixOrDataSource(result: CompletionResultSet) {
		//TODO 进一步匹配scope
		val keyword = keyword
		val linkConfigs = configGroup.linksAsScope
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		//合法的表达式需要匹配scopeName或者scopeGroupName，来自scope[xxx]或者scope_group[xxx]中的xxx，但进行提示时不匹配
		
		val prefixLinkConfigs = linkConfigs.values
			.filter { it.prefix != null && it.dataSource != null }
		val prefixLinkConfigsToUse = prefixLinkConfigs.filter { keyword.startsWith(it.prefix!!) }
		if(prefixLinkConfigsToUse.isNotEmpty()){
			//有前缀，基于匹配前缀的dataSource进行提示
			val prefix = prefixLinkConfigsToUse.first().prefix!!
			val keywordToUse = keyword.drop(prefix.length)
			put(PlsCompletionKeys.keywordKey, keywordToUse)
			val resultToUse = result.withPrefixMatcher(keywordToUse)
			for(linkConfig in prefixLinkConfigsToUse) {
				//基于前缀进行提示，即使前缀的input_scopes不匹配前一个scope的output_scope
				
				completeScriptExpression(contextElement, linkConfig.dataSource!!, linkConfig.config, resultToUse, outputScope)
			}
			put(PlsCompletionKeys.keywordKey, keyword)
		} else {
			//没有前缀，提示所有可能的前缀
			val resultToUse = result.withPrefixMatcher(keyword)
			val lookupElements = mutableSetOf<LookupElement>()
			for(linkConfig in prefixLinkConfigs) {
				//排除input_scopes不匹配前一个scope的output_scope的情况
				val isScopeMatched = matchScope(outputScope, linkConfig.inputScopes, configGroup)
				if(!isScopeMatched) continue
				
				val name = linkConfig.prefix ?: continue
				//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
				val element = linkConfig.pointer.element ?: continue
				val tailText = " from scope link ${linkConfig.name}"
				val typeFile = linkConfig.pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withExpectedIcon(PlsIcons.ScopeFieldPrefix)
					.withBoldness(true)
					.withTailText(tailText, true)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withPriority(PlsCompletonPriorities.scopeFieldPrefixPriority)
				lookupElements.add(lookupElement)
			}
			resultToUse.addAllElements(lookupElements)
			
			//基于所有没有前缀的dataSource进行提示
			val linkConfigsNoPrefix = linkConfigs.values
				.filter { it.prefix == null && it.dataSource != null }
			if(linkConfigsNoPrefix.isNotEmpty()) {
				for(linkConfig in linkConfigsNoPrefix) {
					//排除input_scopes不匹配前一个scope的output_scope的情况
					val isScopeMatched = matchScope(outputScope, linkConfig.inputScopes, configGroup)
					if(!isScopeMatched) continue
					
					completeScriptExpression(contextElement, linkConfig.dataSource!!, linkConfig.config, resultToUse, outputScope)
				}
			}
		}
	}
	
	fun ProcessingContext.completeValueOfValueField(result: CompletionResultSet) {
		//TODO 进一步匹配scope
		val keyword = keyword
		val lookupElements = mutableSetOf<LookupElement>()
		val linkConfigs = configGroup.linksAsValueNotData
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		
		for(linkConfig in linkConfigs.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val isScopeMatched = matchScope(outputScope, linkConfig.inputScopes, configGroup)
			if(!isScopeMatched) continue
			
			val name = linkConfig.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from values"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.ValueOfValueField)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletonPriorities.valueOfValueFieldPriority)
			lookupElements.add(lookupElement)
		}
		result.withPrefixMatcher(keyword).addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeValueFieldPrefixOrDataSource(result: CompletionResultSet) {
		//TODO 进一步匹配scope
		val keyword = keyword
		val linkConfigs = configGroup.linksAsValue
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		//合法的表达式需要匹配scopeName或者scopeGroupName，来自scope[xxx]或者scope_group[xxx]中的xxx，但进行提示时不匹配
		
		val prefixLinkConfigs = linkConfigs.values
			.filter { it.prefix != null && it.dataSource != null }
		val prefixLinkConfigsToUse = prefixLinkConfigs.filter { keyword.startsWith(it.prefix!!) }
		if(prefixLinkConfigsToUse.isNotEmpty()){
			//有前缀，基于匹配前缀的dataSource进行提示
			val prefix = prefixLinkConfigsToUse.first().prefix!!
			val keywordToUse = keyword.drop(prefix.length)
			put(PlsCompletionKeys.keywordKey, keywordToUse)
			val resultToUse = result.withPrefixMatcher(keywordToUse)
			for(linkConfig in prefixLinkConfigsToUse) {
				//基于前缀进行提示，即使前缀的input_scopes不匹配前一个scope的output_scope
				
				completeScriptExpression(contextElement, linkConfig.dataSource!!, linkConfig.config, resultToUse, outputScope)
			}
			put(PlsCompletionKeys.keywordKey, keyword)
		} else {
			//没有前缀，提示所有可能的前缀
			val resultToUse = result.withPrefixMatcher(keyword)
			val lookupElements = mutableSetOf<LookupElement>()
			for(linkConfig in prefixLinkConfigs) {
				//排除input_scopes不匹配前一个scope的output_scope的情况
				val isScopeMatched = matchScope(outputScope, linkConfig.inputScopes, configGroup)
				if(!isScopeMatched) continue
				
				val name = linkConfig.prefix ?: continue
				//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
				val element = linkConfig.pointer.element ?: continue
				val tailText = " from value link ${linkConfig.name}"
				val typeFile = linkConfig.pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withExpectedIcon(PlsIcons.ValueFieldPrefix)
					.withBoldness(true)
					.withTailText(tailText, true)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withPriority(PlsCompletonPriorities.valueFieldPrefixPriority)
				lookupElements.add(lookupElement)
			}
			//这里认为必须要有前缀
			resultToUse.addAllElements(lookupElements)
		}
	}
	
	fun ProcessingContext.completeLocalisationCommandScope(configGroup: CwtConfigGroup, result: CompletionResultSet) {
		//TODO 进一步匹配scope
		val lookupElements = mutableSetOf<LookupElement>()
		val systemScopeConfigs = InternalConfigHandler.getSystemScopeMap().values
		val localisationLinks = configGroup.localisationLinks
		val outputScope = prevScope?.let { prevScope -> localisationLinks[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		
		for(systemScopeConfig in systemScopeConfigs) {
			val name = systemScopeConfig.id
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = systemScopeConfig.pointer.element ?: continue
			val tailText = " from system scopes"
			val typeFile = systemScopeConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.SystemScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletonPriorities.systemScopePriority)
			lookupElements.add(lookupElement)
		}
		for(linkConfig in localisationLinks.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val isScopeMatched = matchScope(outputScope, linkConfig.inputScopes, configGroup)
			if(!isScopeMatched) continue
			
			val name = linkConfig.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from localisation scopes"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.LocalisationCommandScope)
				.withTailText(tailText)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletonPriorities.scopePriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeLocalisationCommandField(configGroup: CwtConfigGroup, result: CompletionResultSet) {
		//TODO 匹配scope
		val localisationCommands = configGroup.localisationCommands
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for(localisationCommand in localisationCommands) {
			val config = localisationCommand.value
			val name = config.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = config.pointer.element ?: continue
			val tailText = " from localisation commands"
			val typeFile = config.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.LocalisationCommandField)
				.withTailText(tailText)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletonPriorities.localisationCommandPriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeParameters(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, result: CompletionResultSet) {
		if(quoted) return //输入参数不允许用引号括起
		val definitionName = propertyElement.name
		val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(propertyElement)
		val definitionType = propertyConfig.parent?.castOrNull<CwtPropertyConfig>()
			?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.keyExpression
			?.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: return //不期望的结果
		val definition = findDefinitionByType(definitionName, definitionType, configGroup.project, selector = selector) ?: return
		val parameterMap = definition.parameterMap
		if(parameterMap.isEmpty()) return
		val existParameterNames = mutableSetOf<String>()
		propertyElement.block?.processProperty { existParameterNames.add(it.text) }
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for((parameterName, parameters) in parameterMap) {
			if(parameterName in existParameterNames || parameters.isEmpty()) continue //排除已输入的
			val tailText = " from parameters"
			val lookupElement = LookupElementBuilder.create(parameters.first(), parameterName)
				.withExpectedIcon(PlsIcons.Parameter)
				.withTailText(tailText)
				.withTypeText(definitionName, definition.icon, true)
				.withExpectedInsertHandler(isKey)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeScriptValueParameters(svName:String, parameterNames: Set<String>, result: CompletionResultSet) {
		val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(contextElement)
		val svList = findDefinitionsByType(svName, "script_value", configGroup.project, selector = selector)
		if(svList.isEmpty()) return
		val existParameterNames = mutableSetOf<String>()
		existParameterNames.addAll(parameterNames)
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for(sv in svList) {
			val parameterMap = sv.parameterMap
			if(parameterMap.isEmpty()) continue
			for((parameterName, parameters) in parameterMap) {
				if(parameterName in existParameterNames || parameters.isEmpty()) continue //排除已输入的
				val tailText = " from parameters"
				val lookupElement = LookupElementBuilder.create(parameters.first(), parameterName)
					.withExpectedIcon(PlsIcons.Parameter)
					.withTailText(tailText)
					.withTypeText(svName, sv.icon, true)
					.withExpectedInsertHandler(false)
				lookupElements.add(lookupElement)
			}
		}
		result.withPrefixMatcher(keyword).addAllElements(lookupElements)
	}
	
	private val boolLookupElements = booleanValues.map { value ->
		LookupElementBuilder.create(value).bold().withPriority(PlsCompletonPriorities.keywordPriority)
	}
	
	private fun LookupElementBuilder.withExpectedIcon(icon: Icon, config: CwtConfig<*>? = null): LookupElementBuilder {
		return withIcon(getExpectedIcon(icon, config))
	}
	
	private fun getExpectedIcon(icon: Icon, config: CwtConfig<*>?): Icon {
		if(config is CwtKvConfig<*>) {
			val iconOption = config.options?.find { it.key == "icon" }?.value
			if(iconOption != null) {
				when(iconOption) {
					"tag" -> return PlsIcons.Tag
					"property" -> return PlsIcons.Property
					"value" -> return PlsIcons.Value
					//TO IMPLEMENT
				}
			}
		}
		return icon
	}
	
	private val separatorChars = charArrayOf('=', '<', '>', '!')
	
	private fun LookupElementBuilder.withExpectedInsertHandler(isKey: Boolean): LookupElementBuilder {
		if(isKey) return withInsertHandler(getExpectedInsertHandler(isKey))
		return this
	}
	
	private fun getExpectedInsertHandler(isKey: Boolean): InsertHandler<LookupElement> {
		return InsertHandler { context, _ ->
			//如果后面没有分隔符，则要加上等号，并且根据代码格式设置来判断是否加上等号周围的空格
			val editor = context.editor
			val document = editor.document
			val chars = document.charsSequence
			val charsLength = chars.length
			val caretOffset = editor.caretModel.offset
			if(isKey){
				//得到光标之后的分隔符的位置
				var offset = caretOffset
				while(offset < charsLength && chars[offset].isWhitespace()) {
					offset++
				}
				if(offset == charsLength || chars[offset] !in separatorChars) {
					val customSettings = CodeStyle.getCustomSettings(context.file, ParadoxScriptCodeStyleSettings::class.java)
					val separator = if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "="
					EditorModificationUtil.insertStringAtCaret(editor, separator)
				}
			}
		}
	}
	//endregion
	
	//region Resolve Methods
	//TODO 基于cwt规则文件的解析方法需要进一步匹配scope
	fun resolveKey(keyElement: ParadoxScriptPropertyKey, file: PsiFile?, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): PsiElement? {
		return resolveScriptExpression(keyElement, file, isKey = true) { it is CwtKeyExpression && expressionPredicate(it) }
	}
	
	fun multiResolveKey(keyElement: ParadoxScriptPropertyKey, file: PsiFile?, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): Collection<PsiElement> {
		return multiResolveScriptExpression(keyElement, file, isKey = true) { it is CwtKeyExpression && expressionPredicate(it) }
	}
	
	fun resolveValue(valueElement: ParadoxScriptString, file: PsiFile?, expressionPredicate: (CwtValueExpression) -> Boolean = { true }): PsiElement? {
		return resolveScriptExpression(valueElement, file, isKey = false) { it is CwtValueExpression && expressionPredicate(it) }
	}
	
	fun multiResolveValue(valueElement: ParadoxScriptString, file: PsiFile?, expressionPredicate: (CwtValueExpression) -> Boolean = { true }): Collection<PsiElement> {
		return multiResolveScriptExpression(valueElement, file, isKey = false) { it is CwtValueExpression && expressionPredicate(it) }
	}
	
	fun resolveScriptExpression(element: ParadoxScriptExpressionElement, file: PsiFile?, isKey: Boolean? = null, expressionPredicate: (CwtKvExpression) -> Boolean = { true }): PsiElement? {
		//根据对应的expression进行解析
		val config = element.getConfig() ?: return null
		val expression = config.expression
		if(!expressionPredicate(expression)) return null
		return resolveScriptExpression(element, file, expression, config, isKey = isKey)
	}
	
	fun resolveScriptExpression(element: ParadoxScriptExpressionElement, file: PsiFile?, expression: CwtKvExpression, config: CwtConfig<*>, rangeInElement: TextRange? = null, isKey: Boolean? = null): PsiElement? {
		val file by lazy { file ?: element.containingFile }
		
		//排除带参数的情况
		if(element.isParameterAwareExpression()) return null
		val project = element.project
		
		val text = rangeInElement?.substring(element.value) ?: element.value
		
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = text
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = text
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findSyncedLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.InlineLocalisation -> {
				if(element.isQuoted()) return null
				val name = text
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = text
				val path = filePath.toPathOrNull() ?: return null
				return VfsUtil.findFile(path, true)?.toPsiFile(project)
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(expression.value, text.normalizePath())
				val selector = fileSelector().gameTypeFrom(file).preferRootFrom(file)
				return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(expression.value, text.normalizePath()) ?: return null
				val selector = fileSelector().gameTypeFrom(file).preferRootFrom(file)
				return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
			}
			CwtDataTypes.TypeExpression -> {
				val name = text
				val typeExpression = expression.value ?: return null
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = text.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return null
				val selector = definitionSelector().gameTypeFrom(element).preferRootFrom(element)
				return findDefinitionByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.Enum -> {
				//TODO 支持complex_enum
				val enumName = expression.value ?: return null
				val name = text
				//解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val definitionName = element.parent?.parentOfType<ParadoxScriptProperty>()?.name ?: return null
					val definitionType = config.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.keyExpression
						?.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: return null
					val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
					val definitions = findDefinitionsByType(definitionName, definitionType, project, selector = selector)
					return definitions.firstNotNullOfOrNull { it.parameterMap[name]?.firstOrNull()?.element }
				}
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(element.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
				return enumValueConfig.pointer.element.castOrNull<CwtNamedElement>()
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return null
				val valueName = text
				val gameType = file.fileInfo?.gameType ?: return null
				//尝试解析为来自脚本文件的value
				run {
					val selector = valueSetValueSelector().gameType(gameType)
					val resolved = findValueSetValue(valueName, valueSetName, project, selector = selector)
					if(resolved != null) return resolved
				}
				//尝试解析为预定义的value
				run {
					val configGroup = getCwtConfig(project).getValue(gameType)
					val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
					val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
					if(resolved != null) return resolved
				}
				return null
			}
			CwtDataTypes.ValueSet -> {
				return element //自身
			}
			CwtDataTypes.ScopeGroup -> {
				//TODO 匹配scope
				val name = text
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveScope(name, configGroup)
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				return null //不在这里处理，参见：ParadoxScriptScopeLinkExpression
			}
			CwtDataTypes.Modifier -> {
				val name = text
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveModifier(name, configGroup)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> return null
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return null
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(element, file, text, element.isQuoted(), aliasName, configGroup)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return null
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(element, file, text, element.isQuoted(), aliasName, configGroup)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> return null
			CwtDataTypes.Constant -> {
				if(config is CwtKvConfig<*>) return config.resolved.pointer.element.castOrNull<CwtNamedElement>()
				return null
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> {
				if(isKey == true && config is CwtPropertyConfig) return config.keyResolved.pointer.element //TODO
				return null //TODO 
			}
		}
	}
	
	fun multiResolveScriptExpression(element: ParadoxScriptExpressionElement, file: PsiFile?, rangeInElement: TextRange? = null, isKey: Boolean? = null, expressionPredicate: (CwtKvExpression) -> Boolean = { true }): Collection<PsiElement> {
		//根据对应的expression进行解析
		val config = element.getConfig() ?: return emptyList()
		val expression = config.expression
		if(!expressionPredicate(expression)) return emptyList()
		return doMultiResolveScriptExpression(element, file, expression, config, rangeInElement, isKey)
	}
	
	@PublishedApi
	internal fun doMultiResolveScriptExpression(element: ParadoxScriptExpressionElement, file: PsiFile?, expression: CwtKvExpression, config: CwtKvConfig<*>, rangeInElement: TextRange?, isKey: Boolean?): Collection<PsiElement> {
		val file by lazy { file ?: element.containingFile }
		
		if(element !is ParadoxScriptString || element.isParameterAwareExpression()) return emptyList() //排除带参数的情况
		val project = file.project
		
		val text = rangeInElement?.substring(element.value) ?: element.value
		
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = text
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = text
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file) //不指定偏好的语言区域
				return findSyncedLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.InlineLocalisation -> {
				if(element.isQuoted()) return emptyList()
				val name = text
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = text
				val path = filePath.toPathOrNull() ?: return emptyList()
				return VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(expression.value, text.normalizePath())
				val selector = fileSelector().gameTypeFrom(file).preferRootFrom(file)
				return findFilesByFilePath(filePath, project, selector = selector).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(expression.value, text.normalizePath()) ?: return emptyList()
				val selector = fileSelector().gameTypeFrom(file).preferRootFrom(file)
				return findFilesByFilePath(filePath, project, selector = selector).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.TypeExpression -> {
				val name = text
				val typeExpression = expression.value ?: return emptyList()
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionsByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = text.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return emptyList()
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionsByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.Enum -> {
				//TODO 支持complex_enum
				val enumName = expression.value ?: return emptyList()
				val name = text
				//解析为参数名
				if(enumName == paramsEnumName && config is CwtPropertyConfig) {
					val definitionName = element.parent?.parentOfType<ParadoxScriptProperty>()?.name ?: return emptyList()
					val definitionType = config.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.keyExpression
						?.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: return emptyList()
					val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
					val definitions = findDefinitionsByType(definitionName, definitionType, project, selector = selector)
					return definitions.flatMap { it.parameterMap[name].orEmpty() }.mapNotNull { it.element }
				}
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
				return enumValueConfig.pointer.element.castOrNull<CwtNamedElement>().toSingletonListOrEmpty()
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return emptyList()
				val valueName = text
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				//尝试解析为来自脚本文件的value
				run {
					val selector = valueSetValueSelector().gameType(gameType)
					val resolved = findValueSetValues(valueName, valueSetName, project, selector = selector)
					if(resolved.isNotEmpty()) return resolved
				}
				//尝试解析为预定义的value
				run {
					val configGroup = getCwtConfig(project).getValue(gameType)
					val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
					val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
					if(resolved != null) return resolved.toSingletonList()
				}
				return emptyList()
			}
			CwtDataTypes.ValueSet -> {
				return element.toSingletonList() //自身
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				return emptyList() //不在这里处理，参见：ParadoxScriptScopeLinkExpression
			}
			CwtDataTypes.Modifier -> {
				val name = text
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveModifier(name, configGroup).toSingletonListOrEmpty()
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> return emptyList()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(element, file, text, element.isQuoted(), aliasName, configGroup).toSingletonListOrEmpty()
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(element, file, text, element.isQuoted(), aliasName, configGroup).toSingletonListOrEmpty()
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> return emptyList()
			CwtDataTypes.Constant -> {
				return config.pointer.element.castOrNull<CwtNamedElement>().toSingletonListOrEmpty()
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> {
				if(isKey == true && config is CwtPropertyConfig) return config.keyResolved.pointer.element.toSingletonListOrEmpty() //TODO
				return emptyList() //TODO 
			}
		}
	}
	
	private fun resolveAliasName(contextElement: PsiElement, file: PsiFile, name: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup): PsiElement? {
		val project = configGroup.project
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
		val aliasSubName = getAliasSubName(name, quoted, aliasName, configGroup)
		if(aliasSubName != null) {
			val expression = CwtKeyExpression.resolve(aliasSubName)
			when(expression.type) {
				CwtDataTypes.Localisation -> {
					val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(file) //不指定偏好的语言区域
					return findLocalisation(name, project, selector = selector)
				}
				CwtDataTypes.SyncedLocalisation -> {
					val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(file) //不指定偏好的语言区域
					return findSyncedLocalisation(name, project, selector = selector)
				}
				CwtDataTypes.TypeExpression -> {
					val typeExpression = expression.value ?: return null
					val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(file)
					return findDefinitionByType(name, typeExpression, project, selector = selector)
				}
				CwtDataTypes.TypeExpressionString -> {
					val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
					val nameToUse = name.removeSurrounding(prefix, suffix)
					val typeExpression = expression.value ?: return null
					val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(file)
					return findDefinitionByType(nameToUse, typeExpression, project, selector = selector)
				}
				CwtDataTypes.Enum -> {
					//TODO 支持complex_enum
					val enumName = expression.value ?: return null
					val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
					return enumValueConfig.pointer.element.castOrNull<CwtNamedElement>()
				}
				CwtDataTypes.Value -> {
					if(contextElement !is ParadoxScriptExpressionElement) return null
					val valueSetName = expression.value ?: return null
					val valueName = contextElement.value
					val gameType = configGroup.gameType
					//尝试解析为来自脚本文件的value
					run {
						val selector = valueSetValueSelector().gameType(gameType)
						val resolved = findValueSetValue(valueName, valueSetName, project, selector = selector)
						if(resolved != null) return resolved
					}
					//尝试解析为预定义的value
					run {
						val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
						val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
						if(resolved != null) return resolved
					}
					return null
				}
				CwtDataTypes.ValueSet -> {
					return contextElement //自身
				}
				CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
					return null //不在这里处理，参见：ParadoxScriptScopeLinkExpression
				}
				CwtDataTypes.Constant -> {
					//同名的定义有多个，取第一个即可
					val aliasSubNameIgnoreCase = configGroup.aliasKeysGroupConst.get(aliasName)?.get(aliasSubName)
					val aliases = aliasGroup[aliasSubNameIgnoreCase] //需要忽略大小写
					if(aliases != null) {
						val alias = aliases.firstOrNull()
						val element = alias?.pointer?.element
						if(element != null) return element
					}
					return null
				}
				else -> return null //TODO
			}
		}
		return null
	}
	
	fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val systemScope = InternalConfigHandler.getSystemScope(name, configGroup.project)
		if(systemScope != null) return systemScope.pointer.element
		
		val links = configGroup.linksAsScopeNotData
		if(links.isEmpty()) return null
		val linkConfig = links[name] ?: return null
		return linkConfig.pointer.element
	}
	
	fun resolveValueOfValueField(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val links = configGroup.linksAsValueNotData
		if(links.isEmpty()) return null
		val linkConfig = links[name] ?: return null
		return linkConfig.pointer.element
	}
	
	fun resolveModifier(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val modifier = configGroup.modifiers[name] ?: return null
		return modifier.pointer.element
	}
	
	fun resolveLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val systemScope = InternalConfigHandler.getSystemScope(name, configGroup.project)
		if(systemScope != null) return systemScope.pointer.element
		
		val links = configGroup.localisationLinks
		if(links.isEmpty()) return null
		val linkConfig = links[name] ?: return null
		return linkConfig.pointer.element
	}
	
	fun resolveLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val localisationCommands = configGroup.localisationCommands
		if(localisationCommands.isEmpty()) return null
		val commandConfig = localisationCommands[name] ?: return null
		return commandConfig.pointer.element
	}
	//endregion
}