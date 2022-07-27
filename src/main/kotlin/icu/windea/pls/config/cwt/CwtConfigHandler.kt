@file:Suppress("NAME_SHADOWING")

package icu.windea.pls.config.cwt

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.model.*
import icu.windea.pls.script.codeInsight.completion.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.util.selector.*
import javax.swing.*
import kotlin.text.removeSurrounding

/**
 * CWT规则的处理器。
 *
 * 提供基于CWT规则实现的匹配、校验、代码提示、引用解析等功能。
 */
object CwtConfigHandler {
	const val paramsEnumName = "scripted_effect_params"
	const val modifierAliasName = "modifier"
	
	//region Internal Extensions
	val ProcessingContext.quoted get() = get(ParadoxDefinitionCompletionKeys.quotedKey)
	val ProcessingContext.caretOffset get() = get(ParadoxDefinitionCompletionKeys.caretOffsetKey)
	val ProcessingContext.keyword get() = get(ParadoxDefinitionCompletionKeys.keywordKey)
	val ProcessingContext.isKey get() = get(ParadoxDefinitionCompletionKeys.isKeyKey)
	val ProcessingContext.configGroup get() = get(ParadoxDefinitionCompletionKeys.configGroupKey)
	//endregion
	
	//region Misc Methods
	fun getAliasSubName(key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup): String? {
		val isConstKey = configGroup.aliasGroups[aliasName]?.containsKey(key)
		if(isConstKey == true) return key
		val keys = configGroup.aliasGroupKeysNoConst[aliasName] ?: return null
		return keys.find {
			val expression = CwtKeyExpression.resolve(it)
			matchesKey(expression, key, ParadoxValueType.infer(key), quoted, configGroup)
		}
	}
	
	fun getScopeName(scopeAlias: String, configGroup: CwtConfigGroup): String {
		val scopes = configGroup.scopes.values
		//handle "any" scope 
		if(scopeAlias.equals("any", true)) return "Any"
		//a scope may not have aliases, or not defined in scopes.cwt
		return scopes.find { it.name == scopeAlias || it.aliases.contains(scopeAlias) }?.name ?: scopeAlias.toCapitalizedWords()
	}
	
	private fun isAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.keyExpression.type == CwtDataTypes.AliasName &&
			propertyConfig.valueExpression.type == CwtDataTypes.AliasMatchLeft
	}
	
	private fun isSingleAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.valueExpression.type == CwtDataTypes.SingleAliasRight
	}
	
	private fun matchScope(alias: String, otherAlias: String, configGroup: CwtConfigGroup): Boolean {
		return alias == otherAlias || configGroup.scopeAliasMap[alias]?.aliases?.contains(otherAlias) ?: false
	}
	
	fun mergeScope(scopeMap: MutableMap<String, String>, thisScope: String?): MutableMap<String, String> {
		if(thisScope == null) return scopeMap
		val mergedScopeMap = scopeMap.toMutableMap()
		mergedScopeMap.put("this", thisScope)
		return scopeMap
	}
	//endregion
	
	//region Matches Methods
	//NOTE 在这里的匹配方法中不要访问索引，避免SOF
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
		if(expression.isEmpty() && value.isEmpty()) return true //匹配空字符串
		when(expression.type) {
			CwtDataTypes.Any -> {
				return true
			}
			CwtDataTypes.Int -> {
				return valueType.matchesIntType() && expression.extraValue?.cast<IntRange>()?.contains(value.toIntOrNull()) ?: true
			}
			CwtDataTypes.Float -> {
				return valueType.matchesFloatType() && expression.extraValue?.cast<FloatRange>()?.contains(value.toFloatOrNull()) ?: true
			}
			CwtDataTypes.Scalar -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				return true
			}
			CwtDataTypes.Localisation -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val selector = localisationSelector().gameType(configGroup.gameType)
				//return findLocalisation(value, configGroup.project, preferFirst = true, selector = selector) != null
				return true
			}
			CwtDataTypes.SyncedLocalisation -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val selector = localisationSelector().gameType(configGroup.gameType)
				//return findSyncedLocalisation(value, configGroup.project, preferFirst = true, selector = selector) != null
				return true
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return true
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val selector = localisationSelector().gameType(configGroup.gameType)
				//return findLocalisation(value, configGroup.project, preferFirst = true, selector = selector) != null
				return true
			}
			CwtDataTypes.TypeExpression -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val typeExpression = expression.value ?: return false
				//val selector = definitionSelector().gameType(configGroup.gameType)
				//return findDefinitionByType(value, typeExpression, configGroup.project, preferFirst = true, selector = selector) != null
				return true
			}
			CwtDataTypes.TypeExpressionString -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val typeExpression = expression.value ?: return false
				//val selector = definitionSelector().gameType(configGroup.gameType)
				//return findDefinitionByType(value, typeExpression, configGroup.project, preferFirst = true, selector = selector) != null
				return true
			}
			CwtDataTypes.Enum -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				val enumName = expression.value ?: return false
				//匹配参数名（即使对应的定义声明中不存在对应名字的参数，也总是匹配）
				if(enumName == paramsEnumName) return true
				val enumValues = configGroup.enums[enumName]?.values ?: return false
				return value in enumValues
			}
			CwtDataTypes.ComplexEnum -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				return false //TODO
			}
			CwtDataTypes.Value -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//val valueSetName = expression.value ?: return false
				//val valueValues = configGroup.values[valueSetName]?.values ?: return false
				//return value in valueValues
				return true //任意不带参数，不为复杂表达式的字符串
			}
			CwtDataTypes.ValueSet -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				return true //任意不带参数，不为复杂表达式的字符串
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope -> {
				if(quoted) return false //不允许用引号括起
				if(value.isParameterAwareExpression()) return true
				//TODO 匹配scope
				val scopeName = expression.value?.takeIf { it != "any" }
				return matchesScopeExpression(value, configGroup) //忽略大小写
			}
			CwtDataTypes.ScopeGroup -> {
				if(quoted) return false //不允许用引号括起
				if(value.isParameterAwareExpression()) return true
				//TODO 匹配scope
				val scopeGroupName = expression.value ?: return false
				return matchesScopeExpression(value, configGroup) //忽略大小写
			}
			CwtDataTypes.AliasName -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup, isKey = true)
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup, isKey = true)
			}
			CwtDataTypes.Constant -> {
				return value.equals(expression.value, true) //忽略大小写
			}
			CwtDataTypes.Other -> return true
		}
	}
	
	fun matchesValue(expression: CwtValueExpression, valueElement: ParadoxScriptValue, configGroup: CwtConfigGroup): Boolean {
		//当valueElement是scriptBlock时，expression必须是emptyExpression
		if(valueElement is IParadoxScriptBlock) return expression == CwtValueExpression.EmptyExpression
		
		return matchesValue(expression, valueElement.value, valueElement.valueType, valueElement.isQuoted(), configGroup)
	}
	
	fun matchesValue(expression: CwtValueExpression, value: String, valueType: ParadoxValueType, quoted: Boolean, configGroup: CwtConfigGroup): Boolean {
		if(expression.isEmpty() && value.isEmpty()) return true //匹配空字符串
		when(expression.type) {
			CwtDataTypes.Any -> {
				return true
			}
			CwtDataTypes.Bool -> {
				return valueType.matchesBooleanType()
			}
			CwtDataTypes.Int -> {
				//注意：用括号括起的整数也匹配这个规则
				return valueType.matchesIntType() || ParadoxValueType.infer(value).matchesIntType() && expression.extraValue?.cast<IntRange>()?.contains(value.toIntOrNull()) ?: true
			}
			CwtDataTypes.Float -> {
				//注意：用括号括起的浮点数也匹配这个规则
				return valueType.matchesFloatType() || ParadoxValueType.infer(value).matchesFloatType() && expression.extraValue?.cast<FloatRange>()?.contains(value.toFloatOrNull()) ?: true
			}
			CwtDataTypes.Scalar -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
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
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val selector = localisationSelector().gameType(configGroup.gameType)
				//return findLocalisation(value, configGroup.project, selector = selector) != null
				return true
			}
			CwtDataTypes.SyncedLocalisation -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val selector = localisationSelector().gameType(configGroup.gameType)
				//return findSyncedLocalisation(value, configGroup.project, preferFirst = true, selector = selector) != null
				return true
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return true
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val selector = localisationSelector().gameType(configGroup.gameType)
				//return findLocalisation(value, configGroup.project, selector = selector) != null
				return true
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
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val typeExpression = expression.value ?: return false
				//val selector = definitionSelector().gameType(configGroup.gameType)
				//return findDefinitionByType(value, typeExpression, configGroup.project, preferFirst = true, selector = selector) != null
				return true
			}
			CwtDataTypes.TypeExpressionString -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//不要在这里访问索引
				//val typeExpression = expression.value ?: return false
				//val selector = definitionSelector().gameType(configGroup.gameType)
				//return findDefinitionByType(value, typeExpression, configGroup.project, preferFirst = true, selector = selector) != null
				return true
			}
			CwtDataTypes.Enum -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				val enumName = expression.value ?: return false
				val enumValues = configGroup.enums[enumName]?.values ?: return false
				return value in enumValues
			}
			CwtDataTypes.ComplexEnum -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				return false //TODO
			}
			CwtDataTypes.Value -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				//val valueSetName = expression.value ?: return false
				//val valueValues = configGroup.values[valueSetName]?.values ?: return false
				//return value in valueValues
				return true //任意不带参数，不为复杂表达式的字符串
			}
			CwtDataTypes.ValueSet -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				return true //任意不带参数，不为复杂表达式的字符串
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope -> {
				if(quoted) return false //不允许用引号括起
				if(value.isParameterAwareExpression()) return true
				//TODO 匹配scope
				val scopeName = expression.value ?: return false
				return matchesScopeExpression(value, configGroup) //忽略大小写
			}
			CwtDataTypes.ScopeGroup -> {
				if(quoted) return false //不允许用引号括起
				if(value.isParameterAwareExpression()) return true
				//TODO 匹配scope
				val scopeGroupName = expression.value ?: return false
				return matchesScopeExpression(value, configGroup) //忽略大小写
			}
			CwtDataTypes.VariableField -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				return false //TODO
			}
			CwtDataTypes.IntVariableField -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				return false //TODO
			}
			CwtDataTypes.ValueField -> {
				if(value.isParameterAwareExpression()) return true
				return false //TODO
			}
			CwtDataTypes.IntValueField -> {
				if(value.isParameterAwareExpression()) return true
				return false //TODO
			}
			CwtDataTypes.SingleAliasRight -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				return false //不在这里处理
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup, isKey = false)
			}
			CwtDataTypes.AliasMatchLeft -> {
				if(value.isParameterAwareExpression()) return true
				if(!value.isSimpleScriptExpression()) return false
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
		//如果aliasName是modifier，则name也可以是modifiers中的modifier
		if(aliasName == modifierAliasName) {
			if(matchesModifier(name, configGroup)) return true
		}
		
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
	
	fun matchesScope(name: String, configGroup: CwtConfigGroup, systemScopeOnly: Boolean = false): Boolean {
		val systemScopes = InternalConfigHandler.getSystemScopeMap(configGroup.project)
		if(systemScopes.containsKey(name)) return true
		if(systemScopeOnly) return false
		
		val links = configGroup.links
		return links.containsKey(name)
	}
	
	fun matchesScopeExpression(nameExpression: String, configGroup: CwtConfigGroup, systemScopeOnly: Boolean = false): Boolean {
		if(nameExpression.contains('.')) {
			//return nameExpression.split('.').all { name -> matchesScope(name, configGroup, systemScopeOnly) }
			return true //直接认为匹配
		} else {
			return matchesScope(nameExpression, configGroup, systemScopeOnly)
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
		
		context.put(ParadoxDefinitionCompletionKeys.isKeyKey, true)
		context.put(ParadoxDefinitionCompletionKeys.configGroupKey, configGroup)
		
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
		
		context.put(ParadoxDefinitionCompletionKeys.isKeyKey, false)
		context.put(ParadoxDefinitionCompletionKeys.configGroupKey, configGroup)
		
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
		
		context.put(ParadoxDefinitionCompletionKeys.isKeyKey, false)
		context.put(ParadoxDefinitionCompletionKeys.configGroupKey, configGroup)
		
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
		if(expression.isEmpty()) return
		if(!keyword.isSimpleScriptExpression()) return //排除带参数或者为复杂表达式的情况
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processLocalisationVariants(keyword, configGroup.project, selector = selector) { localisation ->
					val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withExpectedIcon(PlsIcons.Localisation)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.SyncedLocalisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processSyncedLocalisationVariants(keyword, configGroup.project, selector = selector) { syncedLocalisation ->
					val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = syncedLocalisation.containingFile
					val lookupElement = LookupElementBuilder.create(syncedLocalisation, name)
						.withExpectedIcon(PlsIcons.Localisation)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withExpectedIcon(PlsIcons.Localisation)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.TypeExpression -> {
				val typeExpression = expression.value ?: return
				val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(contextElement)
				val definitions = findAllDefinitionsByType(typeExpression, configGroup.project, distinct = true, selector = selector) //不预先过滤结果
				if(definitions.isEmpty()) return
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				for(definition in definitions) {
					val n = definition.definitionInfo?.name ?: continue
					val name = n.quoteIf(quoted)
					val typeFile = definition.containingFile
					val lookupElement = LookupElementBuilder.create(definition, name)
						.withExpectedIcon(PlsIcons.definitionIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(true)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpressionString -> {
				val typeExpression = expression.value ?: return
				val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(contextElement)
				val definitions = findAllDefinitionsByType(typeExpression, configGroup.project, distinct = true, selector = selector) //不预先过滤结果
				if(definitions.isEmpty()) return
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				for(definition in definitions) {
					val definitionName = definition.definitionInfo?.name ?: continue
					val n = "$prefix$definitionName$suffix"
					val name = n.quoteIf(quoted)
					val typeFile = definition.containingFile
					val lookupElement = LookupElementBuilder.create(definition, name)
						.withExpectedIcon(PlsIcons.definitionIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(true)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return
				//提示参数名（仅限key）
				if(enumName == paramsEnumName) {
					val propertyElement = contextElement.findParentDefinitionProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return
					completeParameters(propertyElement, config, quoted, configGroup, result)
					return
				}
				val enumConfig = configGroup.enums[enumName] ?: return
				val enumValueConfigs = enumConfig.valueConfigMap.values
				if(enumValueConfigs.isEmpty()) return
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				for(enumValueConfig in enumValueConfigs) {
					if(quoted && enumValueConfig.stringValue == null) continue
					val n = enumValueConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = enumValueConfig.pointer.element ?: continue
					val typeFile = enumConfig.pointer.containingFile
					val lookupElement = LookupElementBuilder.create(element, name)
						.withExpectedIcon(PlsIcons.EnumValue)
						.withTailText(tailText, true)
						.withTypeText(typeFile?.name, typeFile?.icon, true)
						.withExpectedInsertHandler(true)
						.withCaseSensitivity(false) //忽略大小写
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.ComplexEnum -> {
				//TODO
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				//提示来自脚本文件的value
				this@CwtConfigHandler.run {
					val selector = valueInValueSetSelector().gameType(configGroup.gameType)
					val valuesInValueSet = findAllValuesInValueSet(valueSetName, configGroup.project, distinct = true, selector = selector)
					for(valueInValueSet in valuesInValueSet) {
						val n = runCatching { valueInValueSet.stub?.castOrNull<ParadoxValueInValueSetStub>()?.name }.getOrNull() ?: valueInValueSet.value
						val name = n.quoteIf(quoted)
						val element = valueInValueSet
						//不显示typeText
						val lookupElement = LookupElementBuilder.create(element, name)
							.withExpectedIcon(PlsIcons.ValueInValueSet)
							.withTailText(tailText, true)
							.withExpectedInsertHandler(true)
							.withCaseSensitivity(false) //忽略大小写
						result.addElement(lookupElement)
					}
				}
				//提示预定义的value
				this@CwtConfigHandler.run {
					val valueConfig = configGroup.values[valueSetName] ?: return@run
					val valueInValueSetConfigs = valueConfig.valueConfigMap.values
					if(valueInValueSetConfigs.isEmpty()) return@run
					for(valueInValueSetConfig in valueInValueSetConfigs) {
						if(quoted && valueInValueSetConfig.stringValue == null) continue
						val n = valueInValueSetConfig.value
						//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
						val name = n.quoteIf(quoted)
						val element = valueInValueSetConfig.pointer.element ?: continue
						val typeFile = valueConfig.pointer.containingFile
						val lookupElement = LookupElementBuilder.create(element, name)
							.withExpectedIcon(PlsIcons.ValueInValueSet)
							.withTailText(tailText, true)
							.withTypeText(typeFile?.name, typeFile?.icon, true)
							.withExpectedInsertHandler(true)
							.withCaseSensitivity(false) //忽略大小写
						result.addElement(lookupElement)
					}
				}
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope -> {
				completeScope(result)
			}
			CwtDataTypes.ScopeGroup -> {
				completeScope(result)
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return
				completeAliasName(contextElement, aliasName, config, result, scope)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return
				completeAliasName(contextElement, aliasName, config, result, scope)
			}
			CwtDataTypes.Constant -> {
				val n = expression.value ?: return
				//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = config.keyResolved.pointer.element ?: return
				val typeFile = config.keyResolved.pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withExpectedIcon(PlsIcons.Property, config)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withExpectedInsertHandler(true)
					.withCaseSensitivity(false) //忽略大小写
					.withPriority(PlsPriorities.propertyPriority)
				result.addElement(lookupElement)
			}
			else -> pass()
		}
	}
	
	fun ProcessingContext.completeValue(contextElement: PsiElement, expression: CwtValueExpression, config: CwtKvConfig<*>, result: CompletionResultSet, scope: String?) {
		if(expression.isEmpty()) return
		if(!keyword.isSimpleScriptExpression()) return //排除带参数或者为复杂表达式的情况
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
						.withExpectedInsertHandler(false)
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
						.withExpectedInsertHandler(false)
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
						.withExpectedInsertHandler(false)
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
						.withExpectedInsertHandler(false)
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
						.withExpectedInsertHandler(false)
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
						.withExpectedIcon(PlsIcons.definitionIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(false)
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
						.withExpectedIcon(PlsIcons.definitionIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return
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
						.withExpectedInsertHandler(false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.ComplexEnum -> {
				//TODO
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				//提示来自脚本文件的value
				this@CwtConfigHandler.run {
					val selector = valueInValueSetSelector().gameType(configGroup.gameType)
					val valuesInValueSet = findAllValuesInValueSet(valueSetName, configGroup.project, distinct = true, selector = selector)
					for(valueInValueSet in valuesInValueSet) {
						val n = runCatching { valueInValueSet.stub?.castOrNull<ParadoxValueInValueSetStub>()?.name }.getOrNull() ?: valueInValueSet.value
						val name = n.quoteIf(quoted)
						val element = valueInValueSet
						//不显示typeText
						val lookupElement = LookupElementBuilder.create(element, name)
							.withExpectedIcon(PlsIcons.ValueInValueSet)
							.withTailText(tailText, true)
							.withExpectedInsertHandler(false)
							.withCaseSensitivity(false) //忽略大小写
						result.addElement(lookupElement)
					}
				}
				//提示预定义的value
				this@CwtConfigHandler.run {
					val valueConfig = configGroup.values[valueSetName] ?: return@run
					val valueInValueSetConfigs = valueConfig.valueConfigMap.values
					if(valueInValueSetConfigs.isEmpty()) return@run
					for(valueInValueSetConfig in valueInValueSetConfigs) {
						if(quoted && valueInValueSetConfig.stringValue == null) continue
						val n = valueInValueSetConfig.value
						//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
						val name = n.quoteIf(quoted)
						val element = valueInValueSetConfig.pointer.element ?: continue
						val typeFile = valueConfig.pointer.containingFile
						val lookupElement = LookupElementBuilder.create(element, name)
							.withExpectedIcon(PlsIcons.ValueInValueSet)
							.withTailText(tailText, true)
							.withTypeText(typeFile?.name, typeFile?.icon, true)
							.withExpectedInsertHandler(true)
							.withCaseSensitivity(false) //忽略大小写
						result.addElement(lookupElement)
					}
				}
			}
			CwtDataTypes.ValueSet -> {
				return //不需要进行提示
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope -> {
				completeScope(result)
			}
			CwtDataTypes.ScopeGroup -> {
				completeScope(result)
			}
			CwtDataTypes.VariableField -> pass() //TODO
			CwtDataTypes.IntVariableField -> pass() //TODO
			CwtDataTypes.ValueField -> pass() //TODO
			CwtDataTypes.IntValueField -> pass() //TODO
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> pass()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
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
					.withExpectedIcon(PlsIcons.Value, config)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withExpectedInsertHandler(false)
					.withCaseSensitivity(false) //忽略大小写
					.withPriority(PlsPriorities.propertyPriority)
				result.addElement(lookupElement)
			}
			else -> pass()
		}
	}
	
	private fun ProcessingContext.completeAliasName(contextElement: PsiElement, aliasName: String, config: CwtKvConfig<*>, result: CompletionResultSet, scope: String?) {
		//如果aliasName是modifier，则name也可以是modifiers中的modifier
		if(aliasName == modifierAliasName) {
			//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
			val nextScope = config.parent?.scope ?: scope
			completeModifier(result, nextScope)
		}
		
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
		for(aliasConfigs in aliasGroup.values) {
			//aliasConfigs的名字是相同的 
			val aliasConfig = aliasConfigs.firstOrNull() ?: continue
			//TODO alias的scope需要匹配（推断得到的scope为null时，总是提示）
			if(scope != null && aliasConfig.supportedScopes?.any { matchScope(scope, it, configGroup) } == false) continue
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
			//匹配scope
			val categoryConfigMap = modifierConfig.categoryConfigMap
			if(categoryConfigMap.isEmpty()) continue
			if(scope != null && !categoryConfigMap.values.any { c -> c.supportedScopes.any { s -> matchScope(scope, s, configGroup) } }) continue
			val n = modifierConfig.name
			//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
			val name = n.quoteIf(quoted)
			val element = modifierConfig.pointer.element ?: continue
			val tailText = " from modifiers"
			val typeFile = modifierConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.Modifier)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withPriority(PlsPriorities.modifierPriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeScope(result: CompletionResultSet) {
		//批量提示
		//TODO 不匹配scope以灰色显示
		val keywordToUse = keyword.take(caretOffset).substringAfterLast('.')
		
		val lookupElements = mutableSetOf<LookupElement>()
		val systemScopeConfigs = InternalConfigHandler.getSystemScopes()
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
				.withPriority(PlsPriorities.systemScopePriority)
			lookupElements.add(lookupElement)
		}
		val links = configGroup.links
		for(linkConfig in links.values) {
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
				.withPriority(PlsPriorities.scopePriority)
			lookupElements.add(lookupElement)
		}
		result.withPrefixMatcher(keywordToUse).addAllElements(lookupElements)
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
			val tailText = " from localisation commands"
			val typeFile = config.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.LocalisationCommandField)
				.withTailText(tailText)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeParameters(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, quoted: Boolean, configGroup: CwtConfigGroup, result: CompletionResultSet) {
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
		propertyElement.block?.processProperty { existParameterNames.add(it.text) } //输入参数不允许用引号括起
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for((parameterName, parameters) in parameterMap) {
			if(parameterName in existParameterNames || parameters.isEmpty()) continue //排除已输入的
			val tailText = " from parameters"
			val lookupElement = LookupElementBuilder.create(parameters.first(), parameterName)
				.withExpectedIcon(PlsIcons.Parameter)
				.withTailText(tailText)
				.withTypeText(definitionName, definition.icon, true)
				.withExpectedInsertHandler(true)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	private val boolLookupElements = booleanValues.map { value ->
		LookupElementBuilder.create(value).bold().withPriority(PlsPriorities.keywordPriority)
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
	
	private fun LookupElementBuilder.withExpectedInsertHandler(isKey: Boolean, suffix: String = ""): LookupElementBuilder {
		if(isKey || suffix.isNotEmpty()) return withInsertHandler(getExpectedInsertHandler(isKey, suffix))
		return this
	}
	
	private fun getExpectedInsertHandler(isKey: Boolean, suffix: String = ""): InsertHandler<LookupElement> {
		return InsertHandler { context, _ ->
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
			if(offset < charsLength) {
				val toInsert = StringBuilder()
				if(suffix.isNotEmpty()) {
					toInsert.append(suffix)
				}
				if(isKey && chars[offset] !in separatorChars) {
					val customSettings = CodeStyle.getCustomSettings(context.file, ParadoxScriptCodeStyleSettings::class.java)
					val separator = if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "="
					toInsert.append(separator)
				}
				EditorModificationUtil.insertStringAtCaret(editor, toInsert.toString())
			}
		}
	}
	//endregion
	
	//region Resolve Methods
	//TODO 基于cwt规则文件的解析方法需要进一步匹配scope
	inline fun resolveKey(keyElement: ParadoxScriptPropertyKey, file: PsiFile?, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): PsiElement? {
		//根据对应的expression进行解析
		val propertyConfig = keyElement.getPropertyConfig() ?: return null
		val expression = propertyConfig.keyExpression
		if(!expressionPredicate(expression)) return null
		return doResolveKey(keyElement, file, expression, propertyConfig)
	}
	
	@PublishedApi
	internal fun doResolveKey(keyElement: ParadoxScriptPropertyKey, file: PsiFile?, expression: CwtKeyExpression, propertyConfig: CwtPropertyConfig): PsiElement? {
		val file by lazy { file ?: keyElement.containingFile }
		
		if(!keyElement.isSimpleScriptExpression()) return null //排除带参数或者为复杂表达式的情况
		val project = file.project
		
		//由于这里规则可能被内联，如果必要，需要判断是否可以基于inlineableConfig解析
		propertyConfig.inlineableConfig?.let { inlineableConfig ->
			if(inlineableConfig is CwtAliasConfig) {
				val aliasName = inlineableConfig.name
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(keyElement, file, keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true, injectedOnly = true)
					?.let { return it }
			}
		}
		
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = keyElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = keyElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findSyncedLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.TypeExpression -> {
				val name = keyElement.value
				val typeExpression = expression.value ?: return null
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = keyElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return null
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return null
				val name = keyElement.value
				//解析为参数名
				if(enumName == paramsEnumName) {
					val definitionName = keyElement.parent?.parentOfType<ParadoxScriptProperty>()?.name ?: return null
					val definitionType = propertyConfig.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.keyExpression
						?.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: return null
					val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
					val definitions = findDefinitionsByType(definitionName, definitionType, project, selector = selector)
					return definitions.firstNotNullOfOrNull { it.parameterMap[name]?.firstOrNull()?.element }
				}
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
				return enumValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ComplexEnum -> {
				return propertyConfig.keyResolved.pointer.element //TODO
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return null
				val valueName = keyElement.value
				val gameType = file.fileInfo?.gameType ?: return null
				//尝试解析为来自脚本文件的value
				run {
					val selector = valueInValueSetSelector().gameType(gameType)
					val resolved = findValueInValueSet(valueName, valueSetName, project, selector = selector)
					if(resolved != null) return resolved
				}
				//尝试解析为预定义的value
				run {
					val configGroup = getCwtConfig(project).getValue(gameType)
					val valueInValueSetConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
					val resolved = valueInValueSetConfig.pointer.element.castOrNull<CwtString>()
					if(resolved != null) return resolved
				}
				return null
			}
			CwtDataTypes.ValueSet -> {
				return keyElement //自身
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				return null //不在这里处理，参见：ParadoxScriptScopeLinkExpression
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return null
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(keyElement, file, keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return null
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(keyElement, file, keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true)
			}
			CwtDataTypes.Constant -> {
				return propertyConfig.keyResolved.pointer.element
			}
			else -> {
				return propertyConfig.keyResolved.pointer.element //TODO
			}
		}
	}
	
	inline fun multiResolveKey(keyElement: ParadoxScriptPropertyKey, file: PsiFile?, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): Collection<PsiElement> {
		//根据对应的expression进行解析
		val propertyConfig = keyElement.getPropertyConfig() ?: return emptyList()
		val expression = propertyConfig.keyExpression
		if(!expressionPredicate(expression)) return emptyList()
		return doMultiResolveKey(keyElement, file, expression, propertyConfig)
	}
	
	@PublishedApi
	internal fun doMultiResolveKey(keyElement: ParadoxScriptPropertyKey, file: PsiFile?, expression: CwtKeyExpression, propertyConfig: CwtPropertyConfig): Collection<PsiElement> {
		val file by lazy { file ?: keyElement.containingFile }
		
		if(!keyElement.isSimpleScriptExpression()) return emptyList() //排除带参数或者为复杂表达式的情况
		val project = keyElement.project
		
		//由于这里规则可能被内联，如果必要，需要判断是否可以基于inlineableConfig解析
		propertyConfig.inlineableConfig?.let { inlineableConfig ->
			if(inlineableConfig is CwtAliasConfig) {
				val aliasName = inlineableConfig.name
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				resolveAliasName(keyElement, file, keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true, injectedOnly = true)
					?.let { return it.toSingletonList() }
			}
		}
		
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = keyElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = keyElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findSyncedLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.TypeExpression -> {
				val name = keyElement.value
				val typeExpression = expression.value ?: return emptyList()
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionsByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = keyElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return emptyList()
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionsByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return emptyList()
				val name = keyElement.value
				//解析为参数名
				if(enumName == paramsEnumName) {
					val definitionName = keyElement.parent?.parentOfType<ParadoxScriptProperty>()?.name ?: return emptyList()
					val definitionType = propertyConfig.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.keyExpression
						?.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: return emptyList()
					val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
					val definitions = findDefinitionsByType(definitionName, definitionType, project, selector = selector)
					return definitions.flatMap { it.parameterMap[name].orEmpty() }.mapNotNull { it.element }
				}
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
				return enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			CwtDataTypes.ComplexEnum -> {
				return propertyConfig.keyResolved.pointer.element.toSingletonListOrEmpty() //TODO
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return emptyList()
				val valueName = keyElement.value
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				//尝试解析为来自脚本文件的value
				run {
					val selector = valueInValueSetSelector().gameType(gameType)
					val resolved = findValuesInValueSet(valueName, valueSetName, project, selector = selector)
					if(resolved.isNotEmpty()) return resolved
				}
				//尝试解析为预定义的value
				run {
					val configGroup = getCwtConfig(project).getValue(gameType)
					val valueInValueSetConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
					val resolved = valueInValueSetConfig.pointer.element.castOrNull<CwtString>()
					if(resolved != null) return resolved.toSingletonList()
				}
				return emptyList()
			}
			CwtDataTypes.ValueSet -> {
				return keyElement.toSingletonList() //自身
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				return emptyList() //不在这里处理，参见：ParadoxScriptScopeLinkExpression
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(keyElement, file, keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true).toSingletonListOrEmpty()
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(keyElement, file, keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true).toSingletonListOrEmpty()
			}
			CwtDataTypes.Constant -> {
				return propertyConfig.keyResolved.pointer.element.toSingletonListOrEmpty()
			}
			else -> {
				return propertyConfig.keyResolved.pointer.element.toSingletonListOrEmpty() //TODO
			}
		}
	}
	
	inline fun resolveValue(valueElement: ParadoxScriptValue, file: PsiFile?, expressionPredicate: (CwtValueExpression) -> Boolean = { true }): PsiElement? {
		//根据对应的expression进行解析
		val valueConfig = valueElement.getValueConfig() ?: return null
		val expression = valueConfig.valueExpression
		if(!expressionPredicate(expression)) return null
		return doResolveValue(valueElement, file, expression, valueConfig)
	}
	
	@PublishedApi
	internal fun doResolveValue(valueElement: ParadoxScriptValue, file: PsiFile?, expression: CwtValueExpression, valueConfig: CwtValueConfig): PsiElement? {
		val file by lazy { file ?: valueElement.containingFile }
		
		if(valueElement !is ParadoxScriptString || !valueElement.isSimpleScriptExpression()) return null //排除带参数或者为复杂表达式的情况
		val project = valueElement.project
		
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = valueElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = valueElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findSyncedLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.InlineLocalisation -> {
				if(valueElement.isQuoted()) return null
				val name = valueElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = valueElement.value
				val path = filePath.toPathOrNull() ?: return null
				return VfsUtil.findFile(path, true)?.toPsiFile(project)
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(expression.value, valueElement.value.normalizePath())
				val selector = fileSelector().gameTypeFrom(file).preferRootFrom(file)
				return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(expression.value, valueElement.value.normalizePath()) ?: return null
				val selector = fileSelector().gameTypeFrom(file).preferRootFrom(file)
				return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
			}
			CwtDataTypes.TypeExpression -> {
				val name = valueElement.value
				val typeExpression = expression.value ?: return null
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = valueElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return null
				val selector = definitionSelector().gameTypeFrom(valueElement).preferRootFrom(valueElement)
				return findDefinitionByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return null
				val name = valueElement.value
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
				return enumValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ComplexEnum -> {
				return valueConfig.resolved.pointer.element.castOrNull<CwtString>() //TODO
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return null
				val valueName = valueElement.value
				val gameType = file.fileInfo?.gameType ?: return null
				//尝试解析为来自脚本文件的value
				run {
					val selector = valueInValueSetSelector().gameType(gameType)
					val resolved = findValueInValueSet(valueName, valueSetName, project, selector = selector)
					if(resolved != null) return resolved
				}
				//尝试解析为预定义的value
				run {
					val configGroup = getCwtConfig(project).getValue(gameType)
					val valueInValueSetConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
					val resolved = valueInValueSetConfig.pointer.element.castOrNull<CwtString>()
					if(resolved != null) return resolved
				}
				return null
			}
			CwtDataTypes.ValueSet -> {
				return valueElement //自身
			}
			CwtDataTypes.ScopeGroup -> {
				//TODO 匹配scope
				val name = valueElement.value
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveScope(name, configGroup)
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				return null //不在这里处理，参见：ParadoxScriptScopeLinkExpression
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> return null
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return null
				val gameType = file.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(valueElement, file, valueElement.value, valueElement.isQuoted(), aliasName, configGroup, isKey = false)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> return null
			CwtDataTypes.Constant -> {
				return valueConfig.resolved.pointer.element.castOrNull<CwtString>()
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> return null //TODO
		}
	}
	
	inline fun multiResolveValue(valueElement: ParadoxScriptValue, file: PsiFile?, expressionPredicate: (CwtValueExpression) -> Boolean = { true }): Collection<PsiElement> {
		//根据对应的expression进行解析
		val valueConfig = valueElement.getValueConfig() ?: return emptyList()
		val expression = valueConfig.valueExpression
		if(!expressionPredicate(expression)) return emptyList()
		return doMultiResolveValue(valueElement, file, expression, valueConfig)
	}
	
	@PublishedApi
	internal fun doMultiResolveValue(valueElement: ParadoxScriptValue, file: PsiFile?, expression: CwtValueExpression, valueConfig: CwtValueConfig): Collection<PsiElement> {
		val file by lazy { file ?: valueElement.containingFile }
		
		if(valueElement !is ParadoxScriptString || !valueElement.isSimpleScriptExpression()) return emptyList() //排除带参数或者为复杂表达式的情况
		val project = file.project
		
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = valueElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = valueElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findSyncedLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.InlineLocalisation -> {
				if(valueElement.isQuoted()) return emptyList()
				val name = valueElement.value
				val selector = localisationSelector().gameTypeFrom(file).preferRootFrom(file).preferLocale(preferredParadoxLocale())
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = valueElement.value
				val path = filePath.toPathOrNull() ?: return emptyList()
				return VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(expression.value, valueElement.value.normalizePath())
				val selector = fileSelector().gameTypeFrom(file).preferRootFrom(file)
				return findFilesByFilePath(filePath, project, selector = selector).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(expression.value, valueElement.value.normalizePath()) ?: return emptyList()
				val selector = fileSelector().gameTypeFrom(file).preferRootFrom(file)
				return findFilesByFilePath(filePath, project, selector = selector).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.TypeExpression -> {
				val name = valueElement.value
				val typeExpression = expression.value ?: return emptyList()
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionsByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = valueElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return emptyList()
				val selector = definitionSelector().gameTypeFrom(file).preferRootFrom(file)
				return findDefinitionsByType(name, typeExpression, project, selector = selector)
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return emptyList()
				val name = valueElement.value
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
				return enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			CwtDataTypes.ComplexEnum -> {
				return emptyList() //TODO
			}
			CwtDataTypes.Value -> {
				val valueSetName = expression.value ?: return emptyList()
				val valueName = valueElement.value
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				//尝试解析为来自脚本文件的value
				run {
					val selector = valueInValueSetSelector().gameType(gameType)
					val resolved = findValuesInValueSet(valueName, valueSetName, project, selector = selector)
					if(resolved.isNotEmpty()) return resolved
				}
				//尝试解析为预定义的value
				run {
					val configGroup = getCwtConfig(project).getValue(gameType)
					val valueInValueSetConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
					val resolved = valueInValueSetConfig.pointer.element.castOrNull<CwtString>()
					if(resolved != null) return resolved.toSingletonList()
				}
				return emptyList()
			}
			CwtDataTypes.ValueSet -> {
				return valueElement.toSingletonList() //自身
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				return emptyList() //不在这里处理，参见：ParadoxScriptScopeLinkExpression
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> return emptyList()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = file.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(project).getValue(gameType)
				return resolveAliasName(valueElement, file, valueElement.value, valueElement.isQuoted(), aliasName, configGroup, isKey = false).toSingletonListOrEmpty()
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> return emptyList()
			CwtDataTypes.Constant -> {
				return valueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> return emptyList() //TODO
		}
	}
	
	private fun resolveAliasName(contextElement: PsiElement, file: PsiFile, name: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, isKey: Boolean, injectedOnly: Boolean = false): PsiElement? {
		val project = configGroup.project
		
		//如果aliasName是modifier，则name也可以是modifiers中的modifier
		if(aliasName == modifierAliasName) {
			val resolvedModifier = resolveModifier(name, configGroup)
			if(resolvedModifier != null) return resolvedModifier
		}
		
		if(injectedOnly) return null
		
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
		val aliasSubName = getAliasSubName(name, quoted, aliasName, configGroup)
		if(aliasSubName != null) {
			val expression = CwtKeyExpression.resolve(aliasSubName)
			when(expression.type) {
				CwtDataTypes.Localisation -> {
					val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(file).preferLocale(preferredParadoxLocale())
					return findLocalisation(name, project, selector = selector)
				}
				CwtDataTypes.SyncedLocalisation -> {
					val selector = localisationSelector().gameType(configGroup.gameType).preferRootFrom(file).preferLocale(preferredParadoxLocale())
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
					val enumName = expression.value ?: return null
					val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
					return enumValueConfig.pointer.element.castOrNull<CwtString>()
				}
				CwtDataTypes.ComplexEnum -> {
					return null //TODO
				}
				CwtDataTypes.Value -> {
					if(contextElement !is ParadoxScriptExpressionElement) return null
					val valueSetName = expression.value ?: return null
					val valueName = contextElement.value
					val gameType = configGroup.gameType
					//尝试解析为来自脚本文件的value
					run {
						val selector = valueInValueSetSelector().gameType(gameType)
						val resolved = findValueInValueSet(valueName, valueSetName, project, selector = selector)
						if(resolved != null) return resolved
					}
					//尝试解析为预定义的value
					run {
						val valueInValueSetConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
						val resolved = valueInValueSetConfig.pointer.element.castOrNull<CwtString>()
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
					val aliases = aliasGroup[aliasSubName]
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
		
		val links = configGroup.links
		if(links.isEmpty()) return null
		val linkConfig = links[name] ?: return null
		return linkConfig.pointer.element
	}
	
	fun resolveLinkValuePrefix(name: String, configGroup: CwtConfigGroup): PsiElement? {
		return null //TODO
	}
	
	fun resolveLinkValue(name: String, configGroup: CwtConfigGroup): PsiElement? {
		return null //TODO
	}
	
	fun resolveModifier(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val modifier = configGroup.modifiers[name] ?: return null
		return modifier.pointer.element
	}
	
	fun resolveLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val localisationCommands = configGroup.localisationCommands
		if(localisationCommands.isEmpty()) return null
		val commandConfig = localisationCommands[name] ?: return null
		return commandConfig.pointer.element
	}
	//endregion
}