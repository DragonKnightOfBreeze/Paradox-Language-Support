package icu.windea.pls.config.cwt

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.ProcessEntry.end
import icu.windea.pls.annotation.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*
import icu.windea.pls.util.*
import javax.swing.*
import kotlin.text.removeSurrounding

/**
 * CWT规则的处理器。
 *
 * 提供基于CWT规则实现的匹配、校验、代码提示、引用解析等功能。
 */
object CwtConfigHandler {
	private const val paramsEnumName = "scripted_effect_params"
	
	//region Common Extensions
	fun resolveAliasSubNameExpression(key: String, quoted: Boolean, aliasGroup: Map<@CaseInsensitive String, List<CwtAliasConfig>>, configGroup: CwtConfigGroup): String? {
		if(aliasGroup.keys.contains(key) && CwtKeyExpression.resolve(key).type == CwtDataTypes.Constant) return key
		return aliasGroup.keys.find {
			val expression = CwtKeyExpression.resolve(it)
			if(expression.type == CwtDataTypes.Constant) return@find false
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
	
	fun isInputParameter(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.keyExpression.let { it.type == CwtDataTypes.Enum && it.value == paramsEnumName }
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
	
	//region Supports Extensions
	fun supportsParameters(definition: ParadoxDefinitionProperty): Boolean {
		if(definition !is ParadoxScriptProperty) return false
		val definitionInfo = definition.definitionInfo ?: return false
		val definitionType = definitionInfo.type
		return definitionType == "scripted_effect" || definitionType == "scripted_trigger"
	}
	
	//effect, effect_clause -> scripted_effect
	//trigger, trigger_clause -> scripted_trigger
	//modifier_rule -> script_value, etc.
	
	fun supportsScopes(aliasName: String?): Boolean {
		if(aliasName == null) return false
		return aliasName == "effect" || aliasName == "trigger" || aliasName == "modifier_rule"
	}
	
	fun supportsScopes(propertyConfig: CwtPropertyConfig): Boolean {
		if(doSupportsScopes(propertyConfig)) return true
		propertyConfig.processParentProperty {
			if(doSupportsScopes(it)) return true
			true
		}
		return false
	}
	
	private fun doSupportsScopes(propertyConfig: CwtPropertyConfig): Boolean {
		var isAlias = true
		val aliasName = propertyConfig.inlineableConfig?.also { isAlias = it is CwtAliasConfig }?.name
			?: propertyConfig.keyExpression.takeIf { it.type == CwtDataTypes.AliasName }?.value
			?: propertyConfig.valueExpression.takeIf { it.type == CwtDataTypes.SingleAliasRight }?.also { isAlias = false }?.value
		return if(isAlias) {
			aliasName == "effect" || aliasName == "trigger" || aliasName == "modifier_rule"
		} else {
			aliasName == "effect_clause" || aliasName == "trigger_clause"
		}
	}
	
	//modifier -> (prescripted) modifier
	
	fun supportsModifiers(aliasOrSingleAliasName: String?): Boolean {
		if(aliasOrSingleAliasName == null) return false
		return aliasOrSingleAliasName == "modifier"
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
				//如果keyExpression需要匹配参数名，即使对应的特定定义声明中不存在对应名字的参数，也总是匹配
				if(enumName == paramsEnumName) return true
				val enumValues = configGroup.enums[enumName]?.values ?: return false
				return value in enumValues
			}
			CwtDataTypes.ComplexEnum -> {
				return false //TODO
			}
			CwtDataTypes.Scope -> {
				//TODO 匹配scope
				if(quoted) return false //scope不允许用引号括起
				val name = expression.value ?: return false
				return matchesLinkExpression(name, configGroup) //忽略大小写
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup, isKey = true)
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return false
				return matchesAliasName(value, quoted, aliasName, configGroup, isKey = true)
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
				//TODO 匹配scope
				if(quoted) return false //scope不允许用引号括起
				val scopeGroupName = expression.value ?: return false
				return matchesLinkExpression(value, configGroup) //忽略大小写
			}
			CwtDataTypes.Scope -> {
				//TODO 匹配scope
				if(quoted) return false //scope不允许用引号括起
				val scopeName = expression.value ?: return false
				return matchesLinkExpression(value, configGroup) //忽略大小写
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
				return matchesAliasName(value, quoted, aliasName, configGroup, isKey = false)
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
	
	fun matchesAliasName(name: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, isKey: Boolean): Boolean {
		//如果aliasName是effect或trigger，则name也可以是links中的link，或者其嵌套格式（root.owner）
		if(isKey && !quoted && supportsScopes(aliasName)) {
			if(matchesLinkExpression(name, configGroup)) return true
		}
		
		//如果aliasName是modifier，则name也可以是modifiers中的modifier
		if(supportsModifiers(aliasName)) {
			if(matchesModifier(name, configGroup)) return true
		}
		
		//TODO 匹配scope
		val aliasGroup = configGroup.aliases[aliasName] ?: return false
		val aliasSubName = resolveAliasSubNameExpression(name, quoted, aliasGroup, configGroup) ?: return false
		val expression = CwtKeyExpression.resolve(aliasSubName)
		return matchesKey(expression, name, ParadoxValueType.infer(name), quoted, configGroup)
	}
	
	fun matchesModifier(name: String, configGroup: CwtConfigGroup): Boolean {
		val modifiers = configGroup.modifiers
		return modifiers.containsKey(name)
	}
	
	fun matchesLinkExpression(nameExpression: String, configGroup: CwtConfigGroup, systemScopeOnly: Boolean = false): Boolean {
		if(nameExpression.contains('.')) {
			return nameExpression.split('.').all { name -> matchesLink(name, configGroup, systemScopeOnly) }
		} else {
			return matchesLink(nameExpression, configGroup, systemScopeOnly)
		}
	}
	
	fun matchesLink(name: String, configGroup: CwtConfigGroup, systemScopeOnly: Boolean = false): Boolean {
		val systemScopes = InternalConfigHandler.getSystemScopeMap(configGroup.project)
		if(systemScopes.containsKey(name)) return true
		if(systemScopeOnly) return false
		
		val links = configGroup.links
		return links.containsKey(name)
	}
	//endregion
	
	//region Complete Extensions
	fun addKeyCompletions(keyElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet): Boolean {
		val keyword = keyElement.keyword
		val quoted = keyElement.isQuoted()
		val project = propertyElement.project
		val definitionElementInfo = propertyElement.definitionElementInfo ?: return true
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val childPropertyConfigs = definitionElementInfo.childPropertyConfigs
		if(childPropertyConfigs.isEmpty()) return true
		//如果正在输入linkExpression，且可能的结果可以是linkExpression，则仅提示scope和systemScope
		if(keyword.contains('.') && childPropertyConfigs.any { supportsScopes(it) }) {
			completeLink(configGroup, result.withPrefixMatcher(keyword.substringAfterLast('.')))
			return false
		}
		//否则加入所有可能的结果，让IDEA自动进行前缀匹配
		for(propConfig in childPropertyConfigs) {
			if(shouldComplete(propConfig, definitionElementInfo)) {
				//如果可能正在输入参数名，则基于对应的特定定义声明中存在的参数名进行提示（排除已经输入完毕的，仅当补全key时特殊处理即可）
				if(propConfig.keyExpression.let { it.type == CwtDataTypes.Enum && it.value == paramsEnumName }) {
					completeParameters(propertyElement, quoted, configGroup, result)
					continue
				}
				
				completeKey(keyElement, propConfig.keyExpression, keyword, quoted, propConfig, configGroup, result, scope)
			}
		}
		return true
	}
	
	fun addValueCompletions(valueElement: PsiElement, propertyElement: ParadoxDefinitionProperty, result: CompletionResultSet): Boolean {
		val keyword = valueElement.keyword
		val quoted = valueElement.isQuoted()
		val project = propertyElement.project
		val definitionElementInfo = propertyElement.definitionElementInfo ?: return true
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val configs = definitionElementInfo.configs
		if(configs.isEmpty()) return true
		
		for(config in configs) {
			if(config is CwtPropertyConfig) {
				completeValue(valueElement, config.valueExpression, keyword, quoted, config, configGroup, result, scope)
			}
		}
		return true
	}
	
	fun addValueCompletionsInBlock(valueElement: PsiElement, blockElement: ParadoxScriptBlock, result: CompletionResultSet): Boolean {
		val keyword = valueElement.keyword
		val quoted = valueElement.isQuoted()
		val project = blockElement.project
		val definitionElementInfo = blockElement.definitionElementInfo ?: return true
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val childValueConfigs = definitionElementInfo.childValueConfigs
		if(childValueConfigs.isEmpty()) return true
		
		for(valueConfig in childValueConfigs) {
			if(shouldComplete(valueConfig, definitionElementInfo)) {
				completeValue(valueElement, valueConfig.valueExpression, keyword, quoted, valueConfig, configGroup, result, scope)
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
	
	fun completeKey(context: PsiElement, expression: CwtKeyExpression, keyword: String, quoted: Boolean,
		config: CwtPropertyConfig, configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null) {
		if(expression.isEmpty()) return
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withExpectedIcon(PlsIcons.localisationIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey = true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.SyncedLocalisation -> {
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				processSyncedLocalisationVariants(keyword, configGroup.project) { syncedLocalisation ->
					val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = syncedLocalisation.containingFile
					val lookupElement = LookupElementBuilder.create(syncedLocalisation, name)
						.withExpectedIcon(PlsIcons.localisationIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey = true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withExpectedIcon(PlsIcons.localisationIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey = true)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.TypeExpression -> {
				val typeExpression = expression.value ?: return
				val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
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
						.withExpectedInsertHandler(isKey = true)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpressionString -> {
				val typeExpression = expression.value ?: return
				val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
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
						.withExpectedInsertHandler(isKey = true)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return
				val valueConfig = configGroup.values[valueName] ?: return
				val valueValueConfigs = valueConfig.valueConfigMap.values
				if(valueValueConfigs.isEmpty()) return
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				for(valueValueConfig in valueValueConfigs) {
					if(quoted && valueValueConfig.stringValue == null) continue
					val n = valueValueConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = valueValueConfig.pointer.element ?: continue
					val typeFile = valueConfig.pointer.containingFile
					val lookupElement = LookupElementBuilder.create(element, name)
						.withExpectedIcon(PlsIcons.valueValueIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile?.name, typeFile?.icon, true)
						.withExpectedInsertHandler(isKey = true)
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
				val tailText = " by $expression in ${config.keyResolved.pointer.containingFile?.name ?: anonymousString}"
				for(enumValueConfig in enumValueConfigs) {
					if(quoted && enumValueConfig.stringValue == null) continue
					val n = enumValueConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = enumValueConfig.pointer.element ?: continue
					val typeFile = enumConfig.pointer.containingFile
					val lookupElement = LookupElementBuilder.create(element, name)
						.withExpectedIcon(PlsIcons.enumValueIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile?.name, typeFile?.icon, true)
						.withExpectedInsertHandler(isKey = true)
						.withCaseSensitivity(false) //忽略大小写
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.ComplexEnum -> {
				//TODO
			}
			CwtDataTypes.Scope -> {
				//TODO 匹配scope
				completeLink(configGroup, result)
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return
				completeAliasName(context, keyword, quoted, aliasName, config, configGroup, result, scope, isKey = true)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return
				completeAliasName(context, keyword, quoted, aliasName, config, configGroup, result, scope, isKey = true)
			}
			CwtDataTypes.Constant -> {
				val n = expression.value ?: return
				//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = config.keyResolved.pointer.element ?: return
				val typeFile = config.keyResolved.pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withExpectedIcon(PlsIcons.propertyIcon, config)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withExpectedInsertHandler(isKey = true)
					.withCaseSensitivity(false) //忽略大小写
					.withPriority(PlsPriorities.propertyPriority)
				result.addElement(lookupElement)
			}
			else -> pass()
		}
	}
	
	fun completeValue(context:PsiElement, expression: CwtValueExpression, keyword: String, quoted: Boolean, config: CwtKvConfig<*>,
		configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null) {
		if(expression.isEmpty()) return
		when(expression.type) {
			CwtDataTypes.Bool -> {
				result.addAllElements(boolLookupElements)
			}
			CwtDataTypes.Localisation -> {
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withExpectedIcon(PlsIcons.localisationIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey = false)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.SyncedLocalisation -> {
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				processSyncedLocalisationVariants(keyword, configGroup.project) { syncedLocalisation ->
					val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = syncedLocalisation.containingFile
					val lookupElement = LookupElementBuilder.create(syncedLocalisation, name)
						.withExpectedIcon(PlsIcons.localisationIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey = false)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				processLocalisationVariants(keyword, configGroup.project) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withExpectedIcon(PlsIcons.localisationIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey = false)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.AbsoluteFilePath -> pass() //不提示绝对路径
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val expressionValue = expression.value
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(configGroup.project, distinct = true, selector = ParadoxFileSelectors.preferSameRoot(context))
				} else {
					findFilesByFilePath(expressionValue, configGroup.project, expressionType = expressionType, distinct = true, selector = ParadoxFileSelectors.preferSameRoot(context))
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
						.withExpectedInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val expressionValue = expression.value
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(configGroup.project, distinct = true, selector = ParadoxFileSelectors.preferSameRoot(context))
				} else {
					findFilesByFilePath(expressionValue, configGroup.project, expressionType = expressionType, distinct = true, selector = ParadoxFileSelectors.preferSameRoot(context))
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
						.withExpectedInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpression -> {
				val typeExpression = expression.value ?: return
				val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
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
						.withExpectedInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpressionString -> {
				val typeExpression = expression.value ?: return
				val definitions = findDefinitionsByType(typeExpression, configGroup.project, distinct = true) //不预先过滤结果
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
						.withExpectedInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return
				val valueConfig = configGroup.values[valueName] ?: return
				val valueValueConfigs = valueConfig.valueConfigMap.values
				if(valueValueConfigs.isEmpty()) return
				val tailText = " by $expression in ${config.resolved.pointer.containingFile?.name ?: anonymousString}"
				val typeFile = valueConfig.pointer.containingFile
				for(valueValueConfig in valueValueConfigs) {
					if(quoted && valueValueConfig.stringValue == null) continue
					val n = valueValueConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = valueValueConfig.pointer.element ?: continue
					val lookupElement = LookupElementBuilder.create(element, name)
						.withExpectedIcon(PlsIcons.valueValueIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile?.name, typeFile?.icon, true)
						.withCaseSensitivity(false) //忽略大小写
						.withExpectedInsertHandler(isKey = false)
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
				val tailText = " by $expression in ${config.pointer.containingFile?.name ?: anonymousString}"
				val typeFile = enumConfig.pointer.containingFile
				for(enumValueConfig in enumValueConfigs) {
					if(quoted && enumValueConfig.stringValue == null) continue
					val n = enumValueConfig.value
					//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = n.quoteIf(quoted)
					val element = enumValueConfig.pointer.element ?: continue
					val lookupElement = LookupElementBuilder.create(element, name)
						.withExpectedIcon(PlsIcons.enumValueIcon)
						.withTailText(tailText, true)
						.withTypeText(typeFile?.name, typeFile?.icon, true)
						.withCaseSensitivity(false) //忽略大小写
						.withExpectedInsertHandler(isKey = false)
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.ComplexEnum -> {
				//TODO
			}
			CwtDataTypes.ScopeGroup -> {
				//TODO 匹配scope
				completeLink(configGroup, result)
			}
			CwtDataTypes.Scope -> {
				//TODO 匹配scope
				completeLink(configGroup, result)
			}
			CwtDataTypes.VariableField -> pass() //TODO
			CwtDataTypes.IntVariableField -> pass() //TODO
			CwtDataTypes.ValueField -> pass() //TODO
			CwtDataTypes.IntValueField -> pass() //TODO
			//规则会被内联，不应该被匹配到
			CwtDataTypes.SingleAliasRight -> throw InternalError()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return
				completeAliasName(context, keyword, quoted, aliasName, config, configGroup, result, scope, isKey = false)
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.AliasMatchLeft -> throw InternalError()
			CwtDataTypes.Constant -> {
				val n = expression.value ?: return
				//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = config.resolved.pointer.element ?: return
				val typeFile = config.resolved.pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withExpectedIcon(PlsIcons.valueIcon, config)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withExpectedInsertHandler(isKey = false)
					.withCaseSensitivity(false) //忽略大小写
					.withPriority(PlsPriorities.propertyPriority)
				result.addElement(lookupElement)
			}
			else -> pass()
		}
	}
	
	fun completeAliasName(context: PsiElement, keyword: String, quoted: Boolean, aliasName: String, config: CwtKvConfig<*>,
		configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String?, isKey: Boolean) {
		//如果aliasName是effect或trigger，则name也可以是links中的link，或者其嵌套格式（root.owner）
		if(isKey && !quoted && supportsScopes(aliasName)) {
			completeLink(configGroup, result)
		}
		//如果aliasName是modifier，则name也可以是modifiers中的modifier
		if(supportsModifiers(aliasName)) {
			//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
			val finalScope = config.parent?.scope ?: scope
			completeModifier(quoted, configGroup, result, finalScope, isKey)
		}
		
		val aliasGroup = configGroup.aliases[aliasName] ?: return
		for(aliasConfigs in aliasGroup.values) {
			//aliasConfigs的名字是相同的 
			val aliasConfig = aliasConfigs.firstOrNull() ?: continue
			//TODO alias的scope需要匹配（推断得到的scope为null时，总是提示）
			if(scope != null && !aliasConfig.supportedScopes.any { matchScope(scope, it, configGroup) }) continue
			//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
			val finalScope = config.parent?.scope ?: scope
			//aliasSubName是一个表达式
			if(isKey) {
				completeKey(context, aliasConfig.keyExpression, keyword, quoted, aliasConfig.config, configGroup, result, finalScope)
			} else {
				completeValue(context, aliasConfig.valueExpression, keyword, quoted, aliasConfig.config, configGroup, result, finalScope)
			}
		}
	}
	
	fun completeModifier(quoted: Boolean, configGroup: CwtConfigGroup, result: CompletionResultSet, scope: String? = null, isKey: Boolean) {
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
				.withExpectedIcon(PlsIcons.modifierIcon)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withPriority(PlsPriorities.modifierPriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeLink(configGroup: CwtConfigGroup, result: CompletionResultSet) {
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		
		val systemScopeConfigs = InternalConfigHandler.getSystemScopes()
		for(systemScopeConfig in systemScopeConfigs) {
			val name = systemScopeConfig.id
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = systemScopeConfig.pointer.element ?: continue
			val tailText = " from system scopes"
			val typeFile = systemScopeConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withExpectedIcon(PlsIcons.systemScopeIcon)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey = true)
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
				.withExpectedIcon(PlsIcons.scopeIcon)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey = true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsPriorities.scopePriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
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
				.withExpectedIcon(PlsIcons.localisationCommandFieldIcon)
				.withTailText(tailText)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	private fun completeParameters(propertyElement: ParadoxDefinitionProperty, quoted: Boolean, configGroup: CwtConfigGroup, result: CompletionResultSet) {
		if(quoted || propertyElement !is ParadoxScriptProperty) return //输入参数不允许用引号括起
		val definitionName = propertyElement.name
		val definition = findDefinitionByType(definitionName, "scripted_effect|scripted_trigger", configGroup.project) ?: return
		//得到所有存在的参数名并排除已经输入完毕的
		val parameterNames = definition.parameterNames ?: return
		if(parameterNames.isEmpty()) return
		val parameterNamesToUse = SmartList(parameterNames)
		propertyElement.block?.processProperty { parameterNamesToUse.remove(it.name).end() } //这里不需要关心正在输入的参数名
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for(parameterName in parameterNamesToUse) {
			val tailText = " from parameters"
			val lookupElement = LookupElementBuilder.create(parameterName) //目前并不解析参数
				.withExpectedIcon(PlsIcons.parameterIcon)
				.withTailText(tailText)
				.withTypeText(definitionName, definition.icon, true)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	val boolLookupElements = booleanValues.map { value ->
		LookupElementBuilder.create(value).bold().withPriority(PlsPriorities.keywordPriority)
	}
	
	private fun LookupElementBuilder.withExpectedIcon(icon: Icon, config: CwtConfig<*>? = null): LookupElementBuilder {
		return withIcon(getExpectedIcon(config, icon))
	}
	
	private fun getExpectedIcon(config: CwtConfig<*>?, icon: Icon): Icon {
		if(config is CwtKvConfig<*>) {
			val iconOption = config.options?.find { it.key == "icon" }?.value
			if(iconOption != null) {
				when(iconOption) {
					"tag" -> return PlsIcons.tagIcon
					"property" -> return PlsIcons.propertyIcon
					"value" -> return PlsIcons.valueIcon
					//TO IMPLEMENT
				}
			}
		}
		return icon
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
	
	private fun LookupElementBuilder.withExpectedInsertHandler(isKey: Boolean): LookupElementBuilder {
		if(isKey) return withInsertHandler(separatorInsertHandler)
		return this
	}
	//endregion
	
	//region Resolve Extensions
	//TODO 基于cwt规则文件的解析方法需要进一步匹配scope
	inline fun resolveKey(keyElement: ParadoxScriptPropertyKey, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): PsiElement? {
		//根据对应的expression进行解析
		val propertyConfig = keyElement.getPropertyConfig() ?: return null
		val expression = propertyConfig.keyExpression
		if(!expressionPredicate(expression)) return null
		return doResolveKey(keyElement, expression, propertyConfig)
	}
	
	@PublishedApi
	internal fun doResolveKey(keyElement: ParadoxScriptPropertyKey, expression: CwtKeyExpression, propertyConfig: CwtPropertyConfig): PsiElement? {
		//由于这里规则可能被内联，如果必要，需要判断是否可以基于inlineableConfig解析
		propertyConfig.inlineableConfig?.let { inlineableConfig ->
			if(inlineableConfig is CwtAliasConfig) {
				val aliasName = inlineableConfig.name
				val gameType = keyElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true, injectedOnly = true)
					?.let { return it }
			}
		}
		
		val project = keyElement.project
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = keyElement.value
				return findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = keyElement.value
				return findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.TypeExpression -> {
				val name = keyElement.value
				val typeExpression = expression.value ?: return null
				return findDefinitionByType(name, typeExpression, project)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = keyElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return null
				return findDefinitionByType(name, typeExpression, project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return null
				val name = keyElement.value
				val gameType = keyElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
				return valueValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ValueSet -> {
				return propertyConfig.keyResolved.pointer.element //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return null
				//如果keyExpression需要匹配参数名，目前不进行解析
				if(enumName == paramsEnumName) return null
				val name = keyElement.value
				val gameType = keyElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
				return enumValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ComplexEnum -> {
				return propertyConfig.keyResolved.pointer.element //TODO
			}
			CwtDataTypes.Scope -> {
				//TODO 匹配scope
				val name = keyElement.value
				val gameType = keyElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				return resolveLink(name, configGroup)
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return null
				val gameType = keyElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				return resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return null
				val gameType = keyElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				return resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true)
			}
			CwtDataTypes.Constant -> {
				return propertyConfig.keyResolved.pointer.element
			}
			else -> {
				return propertyConfig.keyResolved.pointer.element //TODO
			}
		}
	}
	
	inline fun multiResolveKey(keyElement: ParadoxScriptPropertyKey, expressionPredicate: (CwtKeyExpression) -> Boolean = { true }): List<PsiElement> {
		//根据对应的expression进行解析
		val propertyConfig = keyElement.getPropertyConfig() ?: return emptyList()
		val expression = propertyConfig.keyExpression
		if(!expressionPredicate(expression)) return emptyList()
		return doMultiResolveKey(keyElement, expression, propertyConfig)
	}
	
	@PublishedApi
	internal fun doMultiResolveKey(keyElement: ParadoxScriptPropertyKey, expression: CwtKeyExpression, propertyConfig: CwtPropertyConfig): List<PsiElement> {
		//由于这里规则可能被内联，如果必要，需要判断是否可以基于inlineableConfig解析
		propertyConfig.inlineableConfig?.let { inlineableConfig ->
			if(inlineableConfig is CwtAliasConfig) {
				val aliasName = inlineableConfig.name
				val gameType = keyElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true, injectedOnly = true)
					?.let { return it.toSingletonList() }
			}
		}
		
		val project = keyElement.project
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = keyElement.value
				return findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = keyElement.value
				return findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.TypeExpression -> {
				val name = keyElement.value
				val typeExpression = expression.value ?: return emptyList()
				return findDefinitionsByType(name, typeExpression, project)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = keyElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return emptyList()
				return findDefinitionsByType(name, typeExpression, project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return emptyList()
				val name = keyElement.value
				val gameType = keyElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return emptyList()
				return valueValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			CwtDataTypes.ValueSet -> {
				return propertyConfig.keyResolved.pointer.element.toSingletonListOrEmpty() //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return emptyList()
				//如果keyExpression需要匹配参数名，目前不进行解析
				if(enumName == paramsEnumName) return emptyList()
				val name = keyElement.value
				val gameType = keyElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
				return enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			CwtDataTypes.ComplexEnum -> {
				return propertyConfig.keyResolved.pointer.element.toSingletonListOrEmpty() //TODO
			}
			CwtDataTypes.Scope -> {
				//TODO 匹配scope
				val name = keyElement.value
				val gameType = keyElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				return resolveLink(name, configGroup).toSingletonListOrEmpty()
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = keyElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				return resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true).toSingletonListOrEmpty()
			}
			CwtDataTypes.AliasName -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = keyElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(keyElement.project).getValue(gameType)
				return resolveAliasName(keyElement.value, keyElement.isQuoted(), aliasName, configGroup, isKey = true).toSingletonListOrEmpty()
			}
			CwtDataTypes.Constant -> {
				return propertyConfig.keyResolved.pointer.element.toSingletonListOrEmpty()
			}
			else -> {
				return propertyConfig.keyResolved.pointer.element.toSingletonListOrEmpty() //TODO
			}
		}
	}
	
	inline fun resolveValue(valueElement: ParadoxScriptValue, expressionPredicate: (CwtValueExpression) -> Boolean = { true }): PsiElement? {
		//根据对应的expression进行解析
		val valueConfig = valueElement.getValueConfig() ?: return null
		val expression = valueConfig.valueExpression
		if(!expressionPredicate(expression)) return null
		return doResolveValue(valueElement, expression, valueConfig)
	}
	
	@PublishedApi
	internal fun doResolveValue(valueElement: ParadoxScriptValue, expression: CwtValueExpression, valueConfig: CwtValueConfig): PsiElement? {
		val project = valueElement.project
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = valueElement.value
				return findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = valueElement.value
				return findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.InlineLocalisation -> {
				if(valueElement.isQuoted()) return null
				val name = valueElement.value
				return findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = valueElement.value
				val path = filePath.toPathOrNull() ?: return null
				return VfsUtil.findFile(path, true)?.toPsiFile(project)
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(expression.value, valueElement.value)
				return findFileByFilePath(filePath, project, selector = ParadoxFileSelectors.preferSameRoot(valueElement))?.toPsiFile(project)
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(expression.value, valueElement.value) ?: return null
				return findFileByFilePath(filePath, project, selector = ParadoxFileSelectors.preferSameRoot(valueElement))?.toPsiFile(project)
			}
			CwtDataTypes.TypeExpression -> {
				val name = valueElement.value
				val typeExpression = expression.value ?: return null
				return findDefinitionByType(name, typeExpression, project)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = valueElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return null
				return findDefinitionByType(name, typeExpression, project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return null
				val name = valueElement.value
				val gameType = valueElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
				return valueValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ValueSet -> {
				return valueConfig.resolved.pointer.element.castOrNull<CwtString>() //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return null
				val name = valueElement.value
				val gameType = valueElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
				return enumValueConfig.pointer.element.castOrNull<CwtString>()
			}
			CwtDataTypes.ComplexEnum -> {
				return valueConfig.resolved.pointer.element.castOrNull<CwtString>() //TODO
			}
			CwtDataTypes.ScopeGroup -> {
				//TODO 匹配scope
				val name = valueElement.value
				val gameType = valueElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				return resolveLink(name, configGroup)
			}
			CwtDataTypes.Scope -> {
				//TODO 匹配scope
				val name = valueElement.value
				val gameType = valueElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				return resolveLink(name, configGroup)
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.SingleAliasRight -> throw InternalError()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return null
				val gameType = valueElement.fileInfo?.gameType ?: return null
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				return resolveAliasName(valueElement.value, valueElement.isQuoted(), aliasName, configGroup, isKey = false)
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.AliasMatchLeft -> throw InternalError()
			CwtDataTypes.Constant -> {
				return valueConfig.resolved.pointer.element.castOrNull<CwtString>()
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> return null //TODO
		}
	}
	
	inline fun multiResolveValue(valueElement: ParadoxScriptValue, expressionPredicate: (CwtValueExpression) -> Boolean = { true }): List<PsiElement> {
		//根据对应的expression进行解析
		val valueConfig = valueElement.getValueConfig() ?: return emptyList()
		val expression = valueConfig.valueExpression
		if(!expressionPredicate(expression)) return emptyList()
		return doMultiResolveValue(valueElement, expression, valueConfig)
	}
	
	@PublishedApi
	internal fun doMultiResolveValue(valueElement: ParadoxScriptValue, expression: CwtValueExpression, valueConfig: CwtValueConfig): List<PsiElement> {
		val project = valueElement.project
		when(expression.type) {
			CwtDataTypes.Localisation -> {
				val name = valueElement.value
				return findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = valueElement.value
				return findSyncedLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.InlineLocalisation -> {
				if(valueElement.isQuoted()) return emptyList()
				val name = valueElement.value
				return findLocalisations(name, inferParadoxLocale(), project, hasDefault = true) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = valueElement.value
				val path = filePath.toPathOrNull() ?: return emptyList()
				return VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(expression.value, valueElement.value)
				return findFilesByFilePath(filePath, project, selector = ParadoxFileSelectors.preferSameRoot(valueElement)).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(expression.value, valueElement.value) ?: return emptyList()
				return findFilesByFilePath(filePath, project, selector = ParadoxFileSelectors.preferSameRoot(valueElement)).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.TypeExpression -> {
				val name = valueElement.value
				val typeExpression = expression.value ?: return emptyList()
				return findDefinitionsByType(name, typeExpression, project)
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = valueElement.value.removeSurrounding(prefix, suffix)
				val typeExpression = expression.value ?: return emptyList()
				return findDefinitionsByType(name, typeExpression, project)
			}
			CwtDataTypes.Value -> {
				val valueName = expression.value ?: return emptyList()
				val name = valueElement.value
				val gameType = valueElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return emptyList()
				return valueValueConfig.pointer.element.castOrNull<CwtString>()?.toSingletonList() ?: return emptyList()
			}
			CwtDataTypes.ValueSet -> {
				return emptyList() //TODO
			}
			CwtDataTypes.Enum -> {
				val enumName = expression.value ?: return emptyList()
				val name = valueElement.value
				val gameType = valueElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return emptyList()
				return enumValueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			CwtDataTypes.ComplexEnum -> {
				return emptyList() //TODO
			}
			CwtDataTypes.ScopeGroup -> {
				//TODO 匹配scope
				val name = valueElement.value
				val gameType = valueElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				return resolveLink(name, configGroup).toSingletonListOrEmpty()
			}
			CwtDataTypes.Scope -> {
				//TODO 匹配scope
				val name = valueElement.value
				val gameType = valueElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				return resolveLink(name, configGroup).toSingletonListOrEmpty()
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.SingleAliasRight -> throw InternalError()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = expression.value ?: return emptyList()
				val gameType = valueElement.fileInfo?.gameType ?: return emptyList()
				val configGroup = getCwtConfig(valueElement.project).getValue(gameType)
				return resolveAliasName(valueElement.value, valueElement.isQuoted(), aliasName, configGroup, isKey = false).toSingletonListOrEmpty()
			}
			//规则会被内联，不应该被匹配到
			CwtDataTypes.AliasMatchLeft -> throw InternalError()
			CwtDataTypes.Constant -> {
				return valueConfig.pointer.element.castOrNull<CwtString>().toSingletonListOrEmpty()
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> return emptyList() //TODO
		}
	}
	
	fun resolveAliasName(name: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, isKey: Boolean, injectedOnly: Boolean = false): PsiElement? {
		val project = configGroup.project
		
		//如果aliasName是effect或trigger，则name也可以是links中的link，或者其嵌套格式（root.owner）
		if(isKey && !quoted && supportsScopes(aliasName)) {
			val resolvedLink = resolveLink(name, configGroup)
			if(resolvedLink != null) return resolvedLink
		}
		
		//如果aliasName是modifier，则name也可以是modifiers中的modifier
		if(supportsModifiers(aliasName)) {
			val resolvedModifier = resolveModifier(name, configGroup)
			if(resolvedModifier != null) return resolvedModifier
		}
		
		if(injectedOnly) return null
		
		val aliasGroup = configGroup.aliases[aliasName] ?: return null
		val aliasSubName = resolveAliasSubNameExpression(name, quoted, aliasGroup, configGroup)
		if(aliasSubName != null) {
			val expression = CwtKeyExpression.resolve(aliasSubName)
			when(expression.type) {
				CwtDataTypes.Localisation -> {
					return findLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
				}
				CwtDataTypes.SyncedLocalisation -> {
					return findSyncedLocalisation(name, inferParadoxLocale(), project, hasDefault = true)
				}
				CwtDataTypes.TypeExpression -> {
					val typeExpression = expression.value ?: return null
					return findDefinitionByType(name, typeExpression, project)
				}
				CwtDataTypes.TypeExpressionString -> {
					val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
					val nameToUse = name.removeSurrounding(prefix, suffix)
					val typeExpression = expression.value ?: return null
					return findDefinitionByType(nameToUse, typeExpression, project)
				}
				CwtDataTypes.Value -> {
					val valueName = expression.value ?: return null
					val valueValueConfig = configGroup.values.get(valueName)?.valueConfigMap?.get(name) ?: return null
					return valueValueConfig.pointer.element.castOrNull<CwtString>()
				}
				CwtDataTypes.ValueSet -> {
					return null //TODO
				}
				CwtDataTypes.Enum -> {
					val enumName = expression.value ?: return null
					val enumValueConfig = configGroup.enums.get(enumName)?.valueConfigMap?.get(name) ?: return null
					return enumValueConfig.pointer.element.castOrNull<CwtString>()
				}
				CwtDataTypes.ComplexEnum -> {
					return null //TODO
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
	
	fun resolveLink(name: String, configGroup: CwtConfigGroup): PsiElement? {
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