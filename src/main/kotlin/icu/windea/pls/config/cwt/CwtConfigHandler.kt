@file:Suppress("UnusedReceiverParameter", "UNUSED_PARAMETER")

package icu.windea.pls.config.cwt

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.psi.*
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.text.removeSurrounding

/**
 * CWT规则的处理器。
 *
 * 提供基于CWT规则实现的匹配、校验、代码提示、引用解析等功能。
 */
object CwtConfigHandler {
	//region Misc
	const val paramsEnumName = "scripted_effect_params"
	
	fun getModifierLocalisationNameKeys(name: String, configGroup: CwtConfigGroup): List<String>? {
		//TODO 检查到底是如何确定的
		//mod_$, mod_country_$
		val modifier = configGroup.modifiers[name] ?: return null
		val isCountryModifier = !name.startsWith("country_") && modifier.categories.any { it.equals("country", true) || it.equals("countries", true) }
		if(isCountryModifier) return listOf("mod_${name}", "mod_country_${name}")
		return listOf("mod_${name}")
	}
	
	fun getModifierLocalisationDescKeys(name: String, configGroup: CwtConfigGroup): List<String>? {
		//TODO 检查到底是如何确定的
		//mod_$_desc, mod_country_$_desc
		val modifier = configGroup.modifiers[name] ?: return null
		val isCountryModifier = !name.startsWith("country_") && modifier.categories.any { it.equals("country", true) || it.equals("countries", true) }
		if(isCountryModifier) return listOf("mod_${name}_desc", "mod_country_${name}_desc")
		return listOf("mod_${name}_desc")
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
	
	fun matchesScope(scope: String?, scopesToMatch: Collection<String>?, configGroup: CwtConfigGroup): Boolean {
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
	
	fun isAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.keyExpression.type == CwtDataTypes.AliasName
			&& propertyConfig.valueExpression.type == CwtDataTypes.AliasMatchLeft
	}
	
	fun isSingleAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.valueExpression.type == CwtDataTypes.SingleAliasRight
	}
	
	fun isComplexEnum(config: CwtDataConfig<*>): Boolean {
		return config.expression.type == CwtDataTypes.Enum
			&& config.expression.value?.let { config.info.configGroup.complexEnums[it] } != null
	}
	
	fun isValueSetValue(config: CwtDataConfig<*>): Boolean {
		return config.expression.type == CwtDataTypes.Value
			|| config.expression.type == CwtDataTypes.ValueSet
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
	fun matchesScriptExpression(expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
		//匹配block
		if(configExpression == CwtValueExpression.EmptyExpression) {
			return expression.type == ParadoxDataType.BlockType
		}
		//匹配空字符串
		if(configExpression.isEmpty()) {
			return expression.isEmpty()
		}
		
		val project = configGroup.project
		val gameType = configGroup.gameType
		val isStatic = BitUtil.isSet(matchType, CwtConfigMatchType.STATIC)
		val isExact = BitUtil.isSet(matchType, CwtConfigMatchType.EXACT)
		val isParameterAware = expression.type == ParadoxDataType.StringType && expression.text.isParameterAwareExpression()
		when(configExpression.type) {
			CwtDataTypes.Any -> {
				return true
			}
			CwtDataTypes.Bool -> {
				return expression.type.isBooleanType()
			}
			CwtDataTypes.Int -> {
				//注意：用括号括起的整数（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				//匹配范围
				if(isExact && configExpression.extraValue<IntRange>()?.contains(expression.text.toIntOrNull()) != false) return true
				return false
			}
			CwtDataTypes.Float -> {
				//注意：用括号括起的浮点数（作为scalar）也匹配这个规则
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
				//匹配范围
				if(isExact && configExpression.extraValue<FloatRange>()?.contains(expression.text.toFloatOrNull()) != false) return true
				return false
			}
			CwtDataTypes.Scalar -> {
				//unquoted_string, quoted, any key
				return expression.type.isStringType() || (expression.isKey == true)
			}
			CwtDataTypes.ColorField -> {
				return expression.type.isColorType() && configExpression.value?.let { expression.text.startsWith(it) } != false
			}
			CwtDataTypes.PercentageField -> {
				if(!expression.type.isStringType()) return false
				return ParadoxDataType.isPercentageField(expression.text)
			}
			CwtDataTypes.DateField -> {
				if(!expression.type.isStringType()) return false
				return ParadoxDataType.isDateField(expression.text)
			}
			CwtDataTypes.Localisation -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return findLocalisation(expression.text, project, preferFirst = true, selector = selector) != null
				}
				return true
			}
			CwtDataTypes.SyncedLocalisation -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return findSyncedLocalisation(expression.text, project, preferFirst = true, selector = selector) != null
				}
				return true
			}
			CwtDataTypes.InlineLocalisation -> {
				if(!expression.type.isStringType()) return false
				if(expression.quoted) return true //"quoted_string" -> any string
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return findLocalisation(expression.text, project, preferFirst = true, selector = selector) != null
				}
				return true
			}
			CwtDataTypes.AbsoluteFilePath -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				val path = expression.text.toPathOrNull() ?: return false
				return VfsUtil.findFile(path, true) != null
			}
			CwtDataTypes.FilePath -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.FILE_PATH)) {
					val resolvedPath = CwtFilePathExpressionTypes.FilePath.resolve(configExpression.value, expression.text.normalizePath())
					val selector = fileSelector().gameType(gameType)
					return findFileByFilePath(resolvedPath, project, selector = selector) != null
				}
				return true
			}
			CwtDataTypes.Icon -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.FILE_PATH)) {
					val resolvedPath = CwtFilePathExpressionTypes.Icon.resolve(configExpression.value, expression.text.normalizePath()) ?: return false
					val selector = fileSelector().gameType(gameType)
					return findFileByFilePath(resolvedPath, project, selector = selector) != null
				}
				return true
			}
			CwtDataTypes.TypeExpression -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				val typeExpression = configExpression.value ?: return false //invalid cwt config
				if(BitUtil.isSet(matchType, CwtConfigMatchType.DEFINITION)) {
					val selector = definitionSelector().gameType(gameType)
					return ParadoxDefinitionSearch.search(expression.text, typeExpression, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataTypes.TypeExpressionString -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				val typeExpression = configExpression.value ?: return false //invalid cwt config
				if(BitUtil.isSet(matchType, CwtConfigMatchType.DEFINITION)) {
					val selector = definitionSelector().gameType(gameType)
					return ParadoxDefinitionSearch.search(expression.text, typeExpression, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataTypes.Enum -> {
				//if(!expression.type.isStringType()) return false
				if(!isStatic && isParameterAware) return true
				val name = expression.text
				val enumName = configExpression.value ?: return false //invalid cwt config
				//匹配参数名（即使对应的定义声明中不存在对应名字的参数，也总是匹配）
				if(!isStatic && expression.isKey == true && enumName == paramsEnumName) return true
				//匹配简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					return name in enumConfig.values
				}
				if(isStatic) return false
				//匹配复杂枚举
				val complexEnumConfig = configGroup.complexEnums[enumName]
				if(complexEnumConfig != null) {
					if(BitUtil.isSet(matchType, CwtConfigMatchType.COMPLEX_ENUM_VALUE)) {
						val selector = complexEnumValueSelector().gameType(gameType)
						val search = ParadoxComplexEnumValueSearch.search(name, enumName, project, selector = selector)
						return search.findFirst() != null
					}
				}
				return false
			}
			CwtDataTypes.Value -> {
				if(!expression.type.isStringType()) return false
				if(expression.quoted) return false //不允许用引号括起
				if(isStatic) return false
				if(isParameterAware) return true
				return true //任意字符串即可，不需要进一步匹配
			}
			CwtDataTypes.ValueSet -> {
				if(!expression.type.isStringType()) return false
				if(expression.quoted) return false //不允许用引号括起
				if(isStatic) return false
				if(isParameterAware) return true
				return true //任意字符串即可，不需要进一步匹配
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope -> {
				if(expression.quoted) return false //不允许用引号括起
				if(!isStatic && isParameterAware) return true
				val textRange = TextRange.create(0, expression.text.length)
				return ParadoxScopeFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey) != null
			}
			CwtDataTypes.ScopeGroup -> {
				if(expression.quoted) return false //不允许用引号括起
				if(!isStatic && isParameterAware) return true
				val textRange = TextRange.create(0, expression.text.length)
				return ParadoxScopeFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey) != null
			}
			CwtDataTypes.ValueField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
				if(!isStatic && isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				val textRange = TextRange.create(0, expression.text.length)
				return ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey) != null
			}
			CwtDataTypes.IntValueField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				if(!isStatic && isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				val textRange = TextRange.create(0, expression.text.length)
				return ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey) != null
			}
			CwtDataTypes.VariableField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				if(!isStatic && isParameterAware) return true
				return false //TODO
			}
			CwtDataTypes.IntVariableField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				if(!isStatic && isParameterAware) return true
				return false //TODO
			}
			CwtDataTypes.Modifier -> {
				if(!isStatic && isParameterAware) return true
				//匹配预定义的modifier
				return matchesModifier(expression.text, configGroup)
			}
			CwtDataTypes.SingleAliasRight -> {
				return false //不在这里处理
			}
			CwtDataTypes.SingleAliasRight -> {
				return false //不在这里处理
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				if(!isStatic && isParameterAware) return true
				val aliasName = configExpression.value ?: return false
				return matchesAliasName(expression, aliasName, configGroup, matchType)
			}
			CwtDataTypes.AliasName -> {
				if(!isStatic && isParameterAware) return true
				val aliasName = configExpression.value ?: return false
				return matchesAliasName(expression, aliasName, configGroup, matchType)
			}
			CwtDataTypes.AliasMatchLeft -> {
				return false //不在这里处理
			}
			CwtDataTypes.ConstantKey -> {
				val value = configExpression.value
				return expression.text.equals(value, true) //忽略大小写
			}
			CwtDataTypes.Constant -> {
				val text = expression.text
				val value = configExpression.value
				//常量的值也可能是yes/no
				if((value == "yes" || value == "no") && text.isQuoted()) return false
				return expression.text.equals(value, true) //忽略大小写
			}
			CwtDataTypes.Other -> {
				if(isStatic) return false
				return true
			}
		}
	}
	
	fun matchesAliasName(expression: ParadoxDataExpression, aliasName: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
		//TODO 匹配scope
		val aliasSubName = getAliasSubName(expression.text, expression.quoted, aliasName, configGroup, matchType) ?: return false
		val configExpression = CwtKeyExpression.resolve(aliasSubName)
		return matchesScriptExpression(expression, configExpression, configGroup, matchType)
	}
	
	fun matchesModifier(name: String, configGroup: CwtConfigGroup): Boolean {
		val modifiers = configGroup.modifiers
		return modifiers.containsKey(name)
	}
	
	fun getAliasSubName(key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): String? {
		val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
		if(constKey != null) return constKey
		val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
		val expression = ParadoxDataExpression.resolve(key, quoted, true)
		return keys.find {
			matchesScriptExpression(expression, CwtKeyExpression.resolve(it), configGroup, matchType)
		}
	}
	
	fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Int {
		return when(configExpression.type) {
			CwtDataTypes.Any -> 100
			CwtDataTypes.Bool -> 100
			CwtDataTypes.Int -> 90
			CwtDataTypes.Float -> 90
			CwtDataTypes.Scalar -> 90
			CwtDataTypes.ColorField -> 90
			CwtDataTypes.PercentageField -> 90
			CwtDataTypes.DateField -> 90
			CwtDataTypes.Localisation -> 50
			CwtDataTypes.SyncedLocalisation -> 50
			CwtDataTypes.InlineLocalisation -> 50
			CwtDataTypes.AbsoluteFilePath -> 70
			CwtDataTypes.FilePath -> 70
			CwtDataTypes.Icon -> 70
			CwtDataTypes.TypeExpression -> 60
			CwtDataTypes.TypeExpressionString -> 60
			CwtDataTypes.Enum -> {
				val enumName = configExpression.value ?: return 0 //不期望匹配到
				if(enumName == paramsEnumName) return 10
				if(configGroup.enums.containsKey(enumName)) return 80
				if(configGroup.complexEnums.containsKey(enumName)) return 45
				return 0 //不期望匹配到，规则有误！
			}
			CwtDataTypes.Value -> 40
			CwtDataTypes.ValueSet -> 40
			CwtDataTypes.ScopeField -> 30
			CwtDataTypes.Scope -> 30
			CwtDataTypes.ScopeGroup -> 30
			CwtDataTypes.ValueField -> 30
			CwtDataTypes.IntValueField -> 30
			CwtDataTypes.VariableField -> 20
			CwtDataTypes.IntVariableField -> 20
			CwtDataTypes.Modifier -> 80
			CwtDataTypes.SingleAliasRight -> 0 //不期望匹配到
			CwtDataTypes.AliasName -> 0 //不期望匹配到
			CwtDataTypes.AliasKeysField -> 0 //不期望匹配到
			CwtDataTypes.AliasMatchLeft -> 0 //不期望匹配到
			CwtDataTypes.ConstantKey -> 100
			CwtDataTypes.Constant -> 100
			CwtDataTypes.Other -> 0 //不期望匹配到
		}
	}
	//endregion
	
	//region Complete Methods
	fun addKeyCompletions(keyElement: PsiElement, propertyElement: ParadoxDefinitionProperty, context: ProcessingContext, result: CompletionResultSet) {
		val project = propertyElement.project
		val definitionElementInfo = propertyElement.definitionElementInfo ?: return
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val configs = definitionElementInfo.getChildPropertyConfigs()
		if(configs.isEmpty()) return
		
		context.put(PlsCompletionKeys.completionIdsKey, mutableSetOf())
		context.put(PlsCompletionKeys.isKeyKey, true)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		
		configs.groupBy { it.key }.forEach { (_, configsWithSameKey) ->
			for(config in configsWithSameKey) {
				if(shouldComplete(config, definitionElementInfo)) {
					context.put(PlsCompletionKeys.configKey, config)
					context.put(PlsCompletionKeys.configsKey, configsWithSameKey)
					completeScriptExpression(context, result, scope)
				}
			}
		}
		context.put(PlsCompletionKeys.completionIdsKey, null)
		context.put(PlsCompletionKeys.configKey, null)
		context.put(PlsCompletionKeys.configsKey, null)
		return
	}
	
	fun addValueCompletions(valueElement: PsiElement, propertyElement: ParadoxDefinitionProperty, context: ProcessingContext, result: CompletionResultSet) {
		val project = propertyElement.project
		val definitionElementInfo = propertyElement.definitionElementInfo ?: return
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val configs = definitionElementInfo.getConfigs()
		if(configs.isEmpty()) return
		
		context.put(PlsCompletionKeys.completionIdsKey, mutableSetOf())
		context.put(PlsCompletionKeys.isKeyKey, false)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		
		for(config in configs) {
			if(config is CwtPropertyConfig) {
				context.put(PlsCompletionKeys.configKey, config.valueConfig)
				completeScriptExpression(context, result, scope)
			}
		}
		context.put(PlsCompletionKeys.completionIdsKey, null)
		context.put(PlsCompletionKeys.configKey, null)
		return
	}
	
	fun addValueCompletionsInBlock(valueElement: PsiElement, blockElement: ParadoxScriptBlock, context: ProcessingContext, result: CompletionResultSet): Boolean {
		val project = blockElement.project
		val definitionElementInfo = blockElement.definitionElementInfo ?: return true
		val scope = definitionElementInfo.scope
		val gameType = definitionElementInfo.gameType
		val configGroup = getCwtConfig(project).getValue(gameType)
		val configs = definitionElementInfo.getChildValueConfigs()
		if(configs.isEmpty()) return true
		
		context.put(PlsCompletionKeys.completionIdsKey, mutableSetOf())
		context.put(PlsCompletionKeys.isKeyKey, false)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		
		for(config in configs) {
			if(shouldComplete(config, definitionElementInfo)) {
				context.put(PlsCompletionKeys.configKey, config)
				completeScriptExpression(context, result, scope)
			}
		}
		context.put(PlsCompletionKeys.completionIdsKey, null)
		context.put(PlsCompletionKeys.configKey, null)
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
			cardinality == null -> if(expression.type == CwtDataTypes.ConstantKey) 1 else null
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
	
	fun completeScriptExpression(context: ProcessingContext, result: CompletionResultSet, scope: String?): Unit = with(context) {
		val configExpression = config.expression ?: return@with
		val config = config
		val configGroup = configGroup
		val project = configGroup.project
		val gameType = configGroup.gameType
		
		if(configExpression.isEmpty()) return
		if(keyword.isParameterAwareExpression()) return //排除带参数或者的情况
		
		when(configExpression.type) {
			CwtDataTypes.Bool -> {
				result.addElement(PlsConstants.yesLookupElement)
				result.addElement(PlsConstants.noLookupElement)
			}
			CwtDataTypes.Localisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = getScriptExpressionTailText(config)
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processLocalisationVariants(keyword, project, selector = selector) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val lookupElement = PlsLookupElementBuilder
						.buildScriptExpressionLookupElement(localisation, name.quoteIf(quoted), context,
							icon = PlsIcons.Localisation,
							tailText = tailText,
							typeText = typeFile.name,
							typeIcon = typeFile.icon
						)
						?: return@processLocalisationVariants true
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.SyncedLocalisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = getScriptExpressionTailText(config)
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processSyncedLocalisationVariants(keyword, project, selector = selector) { syncedLocalisation ->
					val name = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = syncedLocalisation.containingFile
					val lookupElement = PlsLookupElementBuilder
						.buildScriptExpressionLookupElement(syncedLocalisation, name.quoteIf(quoted), context,
							icon = PlsIcons.Localisation,
							tailText = tailText,
							typeText = typeFile.name,
							typeIcon = typeFile.icon
						)
						?: return@processSyncedLocalisationVariants true
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.InlineLocalisation -> {
				if(quoted) return
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = getScriptExpressionTailText(config)
				processLocalisationVariants(keyword, project) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val lookupElement = PlsLookupElementBuilder
						.buildScriptExpressionLookupElement(localisation, name, context,
							icon = PlsIcons.Localisation,
							tailText = tailText,
							typeText = typeFile.name,
							typeIcon = typeFile.icon
						)
						?: return@processLocalisationVariants true
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.AbsoluteFilePath -> pass() //不提示绝对路径
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val expressionValue = configExpression.value
				val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement)
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(project, distinct = true, selector = selector)
				} else {
					findFilesByFilePath(expressionValue, project, expressionType = expressionType, distinct = true, selector = selector)
				}
				if(virtualFiles.isEmpty()) return
				val tailText = getScriptExpressionTailText(config)
				for(virtualFile in virtualFiles) {
					val file = virtualFile.toPsiFile<PsiFile>(project) ?: continue
					val filePath = virtualFile.fileInfo?.path?.path ?: continue
					val name = expressionType.extract(expressionValue, filePath) ?: continue
					//没有图标
					val lookupElement = PlsLookupElementBuilder
						.buildScriptExpressionLookupElement(file, name, context,
							tailText = tailText,
							typeText = file.name,
							typeIcon = file.icon
						)
						?: continue
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val expressionValue = configExpression.value
				val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement)
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(project, distinct = true, selector = selector)
				} else {
					findFilesByFilePath(expressionValue, project, expressionType = expressionType, distinct = true, selector = selector)
				}
				if(virtualFiles.isEmpty()) return
				val tailText = getScriptExpressionTailText(config)
				for(virtualFile in virtualFiles) {
					val file = virtualFile.toPsiFile<PsiFile>(project) ?: continue
					val filePath = virtualFile.fileInfo?.path?.path ?: continue
					val name = expressionType.extract(expressionValue, filePath) ?: continue
					//没有图标
					val lookupElement = PlsLookupElementBuilder
						.buildScriptExpressionLookupElement(file, name, context,
							tailText = tailText,
							typeText = file.name,
							typeIcon = file.icon
						)
						?: continue
					result.addElement(lookupElement)
				}
			}
			CwtDataTypes.TypeExpression -> {
				val typeExpression = configExpression.value ?: return
				val tailText = getScriptExpressionTailText(config)
				val selector = definitionSelector().gameType(gameType).preferRootFrom(contextElement).distinctByName()
				val definitionQuery = ParadoxDefinitionSearch.search(typeExpression, project, selector = selector)
				definitionQuery.processQuery { definition ->
					val name = definition.definitionInfo?.name ?: return@processQuery true
					val typeFile = definition.containingFile
					val lookupElement = PlsLookupElementBuilder
						.buildScriptExpressionLookupElement(definition, name.quoteIf(quoted), context,
							icon = PlsIcons.Definition,
							tailText = tailText,
							typeText = typeFile.name,
							typeIcon = typeFile.icon
						)
						?: return@processQuery true
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.TypeExpressionString -> {
				val typeExpression = configExpression.value ?: return
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return
				val tailText = getScriptExpressionTailText(config)
				val selector = definitionSelector().gameType(gameType).preferRootFrom(contextElement).distinctByName()
				val definitionQuery = ParadoxDefinitionSearch.search(typeExpression, project, selector = selector)
				definitionQuery.processQuery { definition ->
					val definitionName = definition.definitionInfo?.name ?: return@processQuery true
					val name = "$prefix$definitionName$suffix"
					val typeFile = definition.containingFile
					val lookupElement = PlsLookupElementBuilder
						.buildScriptExpressionLookupElement(definition, name.quoteIf(quoted), context,
							icon = PlsIcons.Definition,
							tailText = tailText,
							typeText = typeFile.name,
							typeIcon = typeFile.icon
						)
						?: return@processQuery true
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.Enum -> {
				val enumName = configExpression.value ?: return
				//提示参数名（仅限key）
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					ProgressManager.checkCanceled()
					val propertyElement = contextElement.findParentDefinitionProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return
					completeParametersForInvocationExpression(propertyElement, config, context, result)
					return
				}
				
				val tailText = getScriptExpressionTailText(config)
				//提示简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					ProgressManager.checkCanceled()
					val enumValueConfigs = enumConfig.valueConfigMap.values
					if(enumValueConfigs.isEmpty()) return
					val typeFile = enumConfig.pointer.containingFile
					for(enumValueConfig in enumValueConfigs) {
						val name = enumValueConfig.value
						//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
						val element = enumValueConfig.pointer.element ?: continue
						val lookupElement = PlsLookupElementBuilder
							.buildScriptExpressionLookupElement(element, name.quoteIf(quoted), context,
								icon = PlsIcons.EnumValue,
								tailText = tailText,
								typeText = typeFile?.name,
								typeIcon = typeFile?.icon
							)
							?.withCaseSensitivity(false) //忽略大小写
							?.withPriority(PlsCompletionPriorities.enumPriority)
							?: continue
						result.addElement(lookupElement)
					}
				}
				//提示复杂枚举
				val complexEnumConfig = configGroup.complexEnums[enumName]
				if(complexEnumConfig != null) {
					ProgressManager.checkCanceled()
					val typeFile = complexEnumConfig.pointer.containingFile
					val searchScope = complexEnumConfig.searchScope
					val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, contextElement).preferRootFrom(contextElement).distinctByName()
					val query = ParadoxComplexEnumValueSearch.search(enumName, project, selector = selector)
					query.processQuery { complexEnum ->
						val name = complexEnum.value
						//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
						val lookupElement = PlsLookupElementBuilder
							.buildScriptExpressionLookupElement(complexEnum, name.quoteIf(quoted), context,
								icon = PlsIcons.ComplexEnumValue,
								tailText = tailText,
								typeText = typeFile?.name,
								typeIcon = typeFile?.icon
							)
							?.withCaseSensitivity(false) //忽略大小写
							?: return@processQuery true
						result.addElement(lookupElement)
						true
					}
				}
			}
			CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
				if(config !is CwtDataConfig<*>) {
					completeValueSetValue(context, result)
					return
				}
				completeValueSetValueExpression(context, result)
			}
			CwtDataTypes.ScopeField -> {
				completeScopeFieldExpression(context, result)
			}
			CwtDataTypes.Scope -> {
				put(PlsCompletionKeys.scopeNameKey, configExpression.value)
				completeScopeFieldExpression(context, result)
				put(PlsCompletionKeys.scopeNameKey, null)
			}
			CwtDataTypes.ScopeGroup -> {
				put(PlsCompletionKeys.scopeGroupNameKey, configExpression.value)
				completeScopeFieldExpression(context, result)
				put(PlsCompletionKeys.scopeGroupNameKey, null)
			}
			CwtDataTypes.ValueField -> {
				completeValueFieldExpression(context, result)
			}
			CwtDataTypes.IntValueField -> {
				completeValueFieldExpression(context, result, isInt = true)
			}
			CwtDataTypes.VariableField -> pass() //TODO
			CwtDataTypes.IntVariableField -> pass() //TODO
			CwtDataTypes.Modifier -> {
				//提示预定义的modifier
				//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
				val nextScope = getNextScope(config, scope)
				completeModifier(context, result, nextScope)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> pass()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = configExpression.value ?: return
				completeAliasName(aliasName, context, result, scope)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = configExpression.value ?: return
				completeAliasName(aliasName, context, result, scope)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> pass()
			CwtDataTypes.ConstantKey -> {
				val name = configExpression.value ?: return
				//if(!name.matchesKeyword(keyword)) return //不预先过滤结果
				val element = config.resolved().pointer.element ?: return
				val typeFile = config.resolved().pointer.containingFile
				val lookupElement = PlsLookupElementBuilder
					.buildScriptExpressionLookupElement(element, name.quoteIf(quoted), context,
						icon = PlsIcons.Property,
						typeText = typeFile?.name,
						typeIcon = typeFile?.icon
					)
					?.withCaseSensitivity(false) //忽略大小写
					?.withPriority(PlsCompletionPriorities.constantKeyPriority)
					?: return
				result.addElement(lookupElement)
			}
			CwtDataTypes.Constant -> {
				val name = configExpression.value ?: return
				//常量的值也可能是yes/no
				if(name == "yes") {
					if(quoted) return
					result.addElement(PlsConstants.yesLookupElement)
					return
				}
				if(name == "no") {
					if(quoted) return
					result.addElement(PlsConstants.noLookupElement)
					return
				}
				//if(!name.matchesKeyword(keyword)) return //不预先过滤结果
				val element = config.resolved().pointer.element ?: return
				val typeFile = config.resolved().pointer.containingFile
				val lookupElement = PlsLookupElementBuilder
					.buildScriptExpressionLookupElement(element, name.quoteIf(quoted), context,
						icon = PlsIcons.Value,
						typeText = typeFile?.name,
						typeIcon = typeFile?.icon
					)
					?.withCaseSensitivity(false) //忽略大小写
					?.withPriority(PlsCompletionPriorities.constantPriority)
					?: return
				result.addElement(lookupElement)
			}
			else -> pass()
		}
		pass()
	}
	
	private fun getNextScope(config: CwtConfig<*>?, scope: String?): String? {
		if(config == null || config !is CwtDataConfig<*>) return scope
		return config.parent?.scope ?: scope
	}
	
	private fun getScriptExpressionTailText(config: CwtConfig<*>?): String? {
		if(config?.expression == null) return null
		return " by ${config.expression} in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
	}
	
	fun completeAliasName(aliasName: String, context: ProcessingContext, result: CompletionResultSet, scope: String?): Unit = with(context) {
		val config = config
		val configs = configs
		
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
		for(aliasConfigs in aliasGroup.values) {
			//aliasConfigs的名字是相同的 
			val aliasConfig = aliasConfigs.firstOrNull() ?: continue
			//假定所有同名的aliasConfig的supportedScopes都是相同的
			//TODO alias的scope需要匹配（推断得到的scope为null时，总是提示）
			val isScopeMatched = matchesScope(scope, aliasConfig.supportedScopes, configGroup)
			if(!isScopeMatched) continue
			
			//TODO 需要推断scope并向下传递，注意首先需要取config.parent.scope
			val nextScope = getNextScope(config, scope)
			//aliasSubName是一个表达式
			if(isKey == true) {
				context.put(PlsCompletionKeys.configKey, aliasConfig)
				context.put(PlsCompletionKeys.configsKey, aliasConfigs)
				completeScriptExpression(context, result, nextScope)
			} else {
				context.put(PlsCompletionKeys.configKey, aliasConfig)
				completeScriptExpression(context, result, nextScope)
			}
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.configsKey, configs)
		}
	}
	
	fun completeModifier(context: ProcessingContext, result: CompletionResultSet, scope: String?): Unit = with(context) {
		val modifiers = configGroup.modifiers
		if(modifiers.isEmpty()) return
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for(modifierConfig in modifiers.values) {
			//排除不匹配modifier的supported_scopes的情况
			val isScopeMatched = scope == null || modifierConfig.categoryConfigMap.values.any { c -> matchesScope(scope, c.supportedScopes, configGroup) }
			if(!isScopeMatched) continue
			
			val name = modifierConfig.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = modifierConfig.pointer.element ?: continue
			val tailText = " from modifiers"
			val typeFile = modifierConfig.pointer.containingFile
			val lookupElement = PlsLookupElementBuilder
				.buildScriptExpressionLookupElement(element, name.quoteIf(quoted), context,
					icon = PlsIcons.Modifier,
					tailText = tailText,
					typeText = typeFile?.name,
					typeIcon = typeFile?.icon
				)
				//?.apply { if(!scopeMatched) withItemTextForeground(Color.GRAY) }
				?.withPriority(PlsCompletionPriorities.modifierPriority)
				?: continue
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeScopeFieldExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		////基于当前位置的代码补全
		if(quoted) return
		val textRange = TextRange.create(0, keyword.length)
		val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(keyword, textRange, configGroup, isKey, true) ?: return
		scopeFieldExpression.complete(context, result)
	}
	
	fun completeValueFieldExpression(context: ProcessingContext, result: CompletionResultSet, isInt: Boolean = false): Unit = with(context) {
		//基于当前位置的代码补全
		if(quoted) return
		val textRange = TextRange.create(0, keyword.length)
		val scopeFieldExpression = ParadoxValueFieldExpression.resolve(keyword, textRange, configGroup, isKey, true) ?: return
		scopeFieldExpression.complete(context, result)
	}
	
	fun completeValueSetValueExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//基于当前位置的代码补全
		if(quoted) return
		val textRange = TextRange.create(0, keyword.length)
		val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(keyword, textRange, config, configGroup, isKey, true) ?: return
		valueSetValueExpression.complete(context, result)
	}
	
	fun completeSystemScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val lookupElements = mutableSetOf<LookupElement>()
		val systemScopeConfigs = configGroup.systemScopes
		for(systemScopeConfig in systemScopeConfigs.values) {
			val name = systemScopeConfig.id
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = systemScopeConfig.pointer.element ?: continue
			val tailText = " from system scopes"
			val typeFile = systemScopeConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.SystemScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.systemScopePriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//TODO 进一步匹配scope
		val lookupElements = mutableSetOf<LookupElement>()
		val linkConfigs = configGroup.linksAsScopeNotData
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		for(linkConfig in linkConfigs.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
			if(!isScopeMatched) continue
			
			val name = linkConfig.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from scopes"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.Scope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.scopePriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val linkConfigs = configGroup.linksAsScopeWithPrefix
		
		//TODO 进一步匹配scope
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		//合法的表达式需要匹配scopeName或者scopeGroupName，来自scope[xxx]或者scope_group[xxx]中的xxx
		
		val lookupElements = mutableSetOf<LookupElement>()
		for(linkConfig in linkConfigs.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
			if(!isScopeMatched) continue
			
			val name = linkConfig.prefix ?: continue
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from scope link ${linkConfig.name}"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.ScopeLinkPrefix)
				.withBoldness(true)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withPriority(PlsCompletionPriorities.scopeLinkPrefixPriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeScopeLinkDataSource(
		context: ProcessingContext,
		result: CompletionResultSet,
		prefix: String?,
		dataSourceNodeToCheck: ParadoxExpressionNode?
	): Unit = with(context) {
		val config = config
		val configs = configs
		
		val allLinkConfigs = if(prefix == null) configGroup.linksAsScopeWithoutPrefix else configGroup.linksAsScopeWithPrefix
		val linkConfigs = if(prefix == null) allLinkConfigs.values else allLinkConfigs.values.filter { prefix == it.prefix }
		
		//TODO 进一步匹配scope
		val outputScope = prevScope?.let { prevScope -> allLinkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		//合法的表达式需要匹配scopeName或者scopeGroupName，来自scope[xxx]或者scope_group[xxx]中的xxx
		
		if(dataSourceNodeToCheck is ParadoxValueSetValueExpression) {
			context.put(PlsCompletionKeys.configKey, dataSourceNodeToCheck.configs.first())
			context.put(PlsCompletionKeys.configsKey, dataSourceNodeToCheck.configs)
			dataSourceNodeToCheck.complete(context, result)
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.configsKey, configs)
			return@with
		}
		
		context.put(PlsCompletionKeys.configsKey, linkConfigs)
		for(linkConfig in linkConfigs) {
			//基于前缀进行提示，即使前缀的input_scopes不匹配前一个scope的output_scope
			//如果没有前缀，排除input_scopes不匹配前一个scope的output_scope的情况
			if(prefix == null) {
				val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
				if(!isScopeMatched) continue
			}
			context.put(PlsCompletionKeys.configKey, linkConfig)
			completeScriptExpression(context, result, outputScope)
		}
		context.put(PlsCompletionKeys.configKey, config)
		context.put(PlsCompletionKeys.configsKey, configs)
	}
	
	fun completeValueLinkValue(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//TODO 进一步匹配scope
		val linkConfigs = configGroup.linksAsValueNotData
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		
		val lookupElements = mutableSetOf<LookupElement>()
		for(linkConfig in linkConfigs.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
			if(!isScopeMatched) continue
			
			val name = linkConfig.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from values"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.ValueLinkValue)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.valueLinkValuePriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeValueLinkPrefix(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val linkConfigs = configGroup.linksAsValueWithPrefix
		
		//TODO 进一步匹配scope
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		//合法的表达式需要匹配scopeName或者scopeGroupName，来自scope[xxx]或者scope_group[xxx]中的xxx
		
		val lookupElements = mutableSetOf<LookupElement>()
		for(linkConfig in linkConfigs.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
			if(!isScopeMatched) continue
			
			val name = linkConfig.prefix ?: continue
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from value link ${linkConfig.name}"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.ValueLinkPrefix)
				.withBoldness(true)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withPriority(PlsCompletionPriorities.scopeLinkPrefixPriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeValueLinkDataSource(
		context: ProcessingContext,
		result: CompletionResultSet,
		prefix: String?,
		dataSourceNodeToCheck: ParadoxExpressionNode?
	): Unit = with(context) {
		val config = config
		val configs = configs
		
		val allLinkConfigs = if(prefix == null) configGroup.linksAsValueWithoutPrefix else configGroup.linksAsValueWithPrefix
		val linkConfigs = if(prefix == null) allLinkConfigs.values else allLinkConfigs.values.filter { prefix == it.prefix }
		
		//TODO 进一步匹配scope
		val outputScope = prevScope?.let { prevScope -> allLinkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		//合法的表达式需要匹配scopeName或者scopeGroupName，来自scope[xxx]或者scope_group[xxx]中的xxx
		
		if(dataSourceNodeToCheck is ParadoxValueSetValueExpression) {
			context.put(PlsCompletionKeys.configKey, dataSourceNodeToCheck.configs.first())
			context.put(PlsCompletionKeys.configsKey, dataSourceNodeToCheck.configs)
			dataSourceNodeToCheck.complete(context, result)
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.configsKey, configs)
			return@with
		}
		if(dataSourceNodeToCheck is ParadoxScriptValueExpression) {
			context.put(PlsCompletionKeys.configKey, dataSourceNodeToCheck.config)
			context.put(PlsCompletionKeys.configsKey, null)
			dataSourceNodeToCheck.complete(context, result)
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.configsKey, configs)
			return@with
		}
		
		context.put(PlsCompletionKeys.configsKey, linkConfigs)
		for(linkConfig in linkConfigs) {
			//基于前缀进行提示，即使前缀的input_scopes不匹配前一个scope的output_scope
			//如果没有前缀，排除input_scopes不匹配前一个scope的output_scope的情况
			if(prefix == null) {
				val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
				if(!isScopeMatched) continue
			}
			context.put(PlsCompletionKeys.configKey, linkConfig)
			completeScriptExpression(context, result, outputScope)
		}
		context.put(PlsCompletionKeys.configKey, config)
		context.put(PlsCompletionKeys.configsKey, configs)
	}
	
	fun completeValueSetValue(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		if(quoted) return@with
		val configs = configs
		if(configs != null && configs.isNotEmpty()) {
			for(config in configs) {
				doCompleteValueSetValue(context, result, config)
			}
		} else {
			val config = config
			if(config != null) {
				doCompleteValueSetValue(context, result, config)
			}
		}
	}
	
	private fun doCompleteValueSetValue(context: ProcessingContext, result: CompletionResultSet, config: CwtConfig<*>): Unit = with(context) {
		val gameType = this.configGroup.gameType
		val project = this.configGroup.project
		
		val configExpression = config.expression ?: return@with
		val valueSetName = configExpression.value ?: return@with
		val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
		//提示预定义的value
		run {
			ProgressManager.checkCanceled()
			if(configExpression.type == CwtDataTypes.Value) {
				val valueConfig = this.configGroup.values[valueSetName] ?: return@run
				val valueSetValueConfigs = valueConfig.valueConfigMap.values
				if(valueSetValueConfigs.isEmpty()) return@run
				for(valueSetValueConfig in valueSetValueConfigs) {
					//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
					val name = valueSetValueConfig.value
					val element = valueSetValueConfig.pointer.element ?: continue
					val typeFile = valueConfig.pointer.containingFile
					val lookupElement = PlsLookupElementBuilder
						.buildScriptExpressionLookupElement(element, name, context,
							icon = PlsIcons.PredefinedValueSetValue,
							tailText = tailText,
							typeText = typeFile?.name,
							typeIcon = typeFile?.icon
						)
						?.withCaseSensitivity(false) //忽略大小写
						?.withPriority(PlsCompletionPriorities.predefinedValueSetValuePriority)
						?: continue
					result.addElement(lookupElement)
				}
			}
		}
		//提示来自脚本文件的value
		run {
			ProgressManager.checkCanceled()
			val selector = valueSetValueSelector().gameType(gameType).distinctByValue()
			val valueSetValueQuery = ParadoxValueSetValueSearch.search(valueSetName, project, selector = selector)
			valueSetValueQuery.processQuery { valueSetValue ->
				//去除后面的作用域信息
				val value = ParadoxValueSetValueInfoHandler.getName(valueSetValue) ?: return@processQuery true
				//没有其他地方使用到时，排除当前正在输入的那个
				val keywordValue = ParadoxValueSetValueInfoHandler.getName(this.keyword)
				if(value == keywordValue && valueSetValue isSamePosition contextElement) return@processQuery true
				val icon = when(valueSetName) {
					"variable" -> PlsIcons.Variable
					else -> PlsIcons.ValueSetValue
				}
				//不显示typeText
				val lookupElement = PlsLookupElementBuilder
					.buildScriptExpressionLookupElement(valueSetValue, value, context,
						icon = icon,
						tailText = tailText
					)
					?.withCaseSensitivity(false) //忽略大小写
					?: return@processQuery true
				result.addElement(lookupElement)
				true
			}
		}
	}
	
	fun completeLocalisationCommandScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//TODO 进一步匹配scope
		val lookupElements = mutableSetOf<LookupElement>()
		val localisationLinks = configGroup.localisationLinks
		val outputScope = prevScope?.let { prevScope -> localisationLinks[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		for(linkConfig in localisationLinks.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
			if(!isScopeMatched) continue
			
			val name = linkConfig.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from localisation scopes"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.LocalisationCommandScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.scopePriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeLocalisationCommandField(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
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
				.withIcon(PlsIcons.LocalisationCommandField)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.localisationCommandPriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeParameters(element: PsiElement, read: Boolean, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//向上找到definition
		val definition = element.findParentDefinition() ?: return
		val definitionInfo = definition.definitionInfo ?: return
		val parameterMap = definition.parameterMap
		if(parameterMap.isEmpty()) return
		val lookupElements = mutableListOf<LookupElement>()
		for((parameterName, parameters) in parameterMap) {
			val parameter = parameters.firstNotNullOfOrNull { it.element } ?: continue
			////没有其他地方使用到时，排除当前正在输入的那个
			if(parameters.size == 1 && element isSamePosition parameter) continue
			//如果要提示的是$PARAM$中的PARAM，需要忽略与之相同参数名
			if(read && parameterName == keyword) continue
			val lookupElement = LookupElementBuilder.create(parameter, parameterName)
				.withIcon(PlsIcons.Parameter)
				.withTypeText(definitionInfo.name, definition.icon, true)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeParametersForInvocationExpression(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		if(quoted) return //输入参数不允许用引号括起
		val definitionName = propertyElement.name
		val definitionType = propertyConfig.parent?.castOrNull<CwtPropertyConfig>()
			?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.subNameExpression
			?.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: return //不期望的结果
		val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(propertyElement)
		val definition = ParadoxDefinitionSearch.search(definitionName, definitionType, configGroup.project, selector = selector).find() ?: return
		val parameterMap = definition.parameterMap
		if(parameterMap.isEmpty()) return
		val existParameterNames = mutableSetOf<String>()
		propertyElement.block?.processProperty { existParameterNames.add(it.text) }
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for((parameterName, parameters) in parameterMap) {
			if(parameterName in existParameterNames) continue //排除已输入的
			val parameter = parameters.firstOrNull() ?: continue
			val lookupElement = LookupElementBuilder.create(parameter, parameterName)
				.withIcon(PlsIcons.Parameter)
				.withTypeText(definitionName, definition.icon, true)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun completeParametersForScriptValueExpression(svName: String, parameterNames: Set<String>, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//整合所有匹配名字的SV的参数
		val existParameterNames = mutableSetOf<String>()
		existParameterNames.addAll(parameterNames)
		val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(contextElement)
		val svQuery = ParadoxDefinitionSearch.search(svName, "script_value", configGroup.project, selector = selector)
		svQuery.processQuery { sv ->
			val parameterMap = sv.parameterMap
			if(parameterMap.isEmpty()) return@processQuery true
			for((parameterName, parameters) in parameterMap) {
				if(parameterName in existParameterNames) continue //排除已输入的
				val parameter = parameters.firstNotNullOfOrNull { it.element } ?: continue
				val lookupElement = LookupElementBuilder.create(parameter, parameterName)
					.withIcon(PlsIcons.Parameter)
					.withTypeText(svName, sv.icon, true)
				result.addElement(lookupElement)
			}
			true
		}
	}
	//endregion
	
	//region Resolve Methods
	//TODO 基于cwt规则文件的解析方法需要进一步匹配scope
	//NOTE 不要传入psiFile - 推断gameType时可能需要直接从上下文psiElement推断
	/**
	 * @param element 需要解析的PSI元素。
	 * @param rangeInElement 需要解析的文本在需要解析的PSI元素对应的整个文本中的位置。
	 */
	fun resolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
		if(element.isParameterAwareExpression()) return null //排除带参数的情况
		
		val project = element.project
		val gameType = configGroup.gameType
		val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.value
		when(configExpression.type) {
			CwtDataTypes.Localisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return findSyncedLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.InlineLocalisation -> {
				if(element.isQuoted()) return null
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = expression
				val path = filePath.toPathOrNull() ?: return null
				return VfsUtil.findFile(path, true)?.toPsiFile(project)
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath())
				val selector = fileSelector().gameType(gameType).preferRootFrom(element, exact)
				return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath()) ?: return null
				val selector = fileSelector().gameType(gameType).preferRootFrom(element, exact)
				return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
			}
			CwtDataTypes.TypeExpression -> {
				val name = expression
				val typeExpression = configExpression.value ?: return null
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = expression.removeSurrounding(prefix, suffix)
				val typeExpression = configExpression.value ?: return null
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
			}
			CwtDataTypes.Enum -> {
				val enumName = configExpression.value ?: return null
				val name = expression
				//尝试解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val definitionName = element.parent?.castOrNull<ParadoxScriptProperty>()?.name ?: return null
					val definitionType = config.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.subNameExpression
						?.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: return null
					return ParadoxParameterElement(element, name, definitionName, definitionType, project, gameType, false)
				}
				//尝试解析为简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return null
					return enumValueConfig.pointer.element.castOrNull<CwtNamedElement>()
				}
				//尝试解析为复杂枚举
				val complexEnumConfig = configGroup.complexEnums[enumName]
				if(complexEnumConfig != null) {
					val searchScope = complexEnumConfig.searchScope
					val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, element).preferRootFrom(element, exact)
					return ParadoxComplexEnumValueSearch.search(name, enumName, project, selector = selector).find()
				}
				return null
			}
			CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
				//参见：ParadoxValueSetValueExpression
				val valueName = expression
				val valueSetName = configExpression.value ?: return null
				val read = configExpression.type == CwtDataTypes.Value
				if(read) {
					//首先尝试解析为预定义的value
					run {
						val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
						val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
						if(resolved != null) return resolved
					}
				}
				return ParadoxValueSetValueElement(element, valueName, valueSetName, configGroup.project, configGroup.gameType, read)
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				//不在这里处理，参见：ParadoxScopeFieldExpression
				return null
			}
			CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
				//不在这里处理，参见：ParadoxValueFieldExpression
				return null
			}
			CwtDataTypes.VariableField, CwtDataTypes.IntVariableField -> {
				return null //TODO
			}
			CwtDataTypes.Modifier -> {
				val name = expression
				return resolveModifier(name, configGroup)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> return null
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = configExpression.value ?: return null
				return resolveAliasName(element, expression, element.isQuoted(), aliasName, configGroup, exact = exact)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = configExpression.value ?: return null
				return resolveAliasName(element, expression, element.isQuoted(), aliasName, configGroup, exact = exact)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> return null
			CwtDataTypes.ConstantKey, CwtDataTypes.Constant -> {
				when {
					config == null -> return null
					config is CwtDataConfig<*> -> return config.resolved().pointer.element
					else -> return config.pointer.element
				}
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> {
				if(isKey == true && config is CwtPropertyConfig) return config.resolved().pointer.element
				return null
			}
		}
	}
	
	fun multiResolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, configExpression: CwtDataExpression, config: CwtConfig<*>?, configGroup: CwtConfigGroup, isKey: Boolean? = null): Collection<PsiElement> {
		if(element.isParameterAwareExpression()) return emptyList() //排除带参数的情况  
		
		val project = element.project
		val gameType = configGroup.gameType
		val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.value
		when(configExpression.type) {
			CwtDataTypes.Localisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findSyncedLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.InlineLocalisation -> {
				if(element.isQuoted()) return emptyList()
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = expression
				val path = filePath.toPathOrNull() ?: return emptyList()
				return VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath())
				val selector = fileSelector().gameType(gameType).preferRootFrom(element)
				return findFilesByFilePath(filePath, project, selector = selector).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath()) ?: return emptyList()
				val selector = fileSelector().gameType(gameType).preferRootFrom(element)
				return findFilesByFilePath(filePath, project, selector = selector).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.TypeExpression -> {
				val name = expression
				val typeExpression = configExpression.value ?: return emptyList()
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findAll()
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = expression.removeSurrounding(prefix, suffix)
				val typeExpression = configExpression.value ?: return emptyList()
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findAll()
			}
			CwtDataTypes.Enum -> {
				val enumName = configExpression.value ?: return emptyList()
				val name = expression
				//尝试解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val definitionName = element.parent?.castOrNull<ParadoxScriptProperty>()?.name ?: return emptyList()
					val definitionType = config.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.subNameExpression
						?.takeIf { it.type == CwtDataTypes.TypeExpression }?.value ?: return emptyList()
					return ParadoxParameterElement(element, name, definitionName, definitionType, project, gameType, false).toSingletonList()
				}
				//尝试解析为简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return emptyList()
					return enumValueConfig.pointer.element.castOrNull<CwtNamedElement>().toSingletonListOrEmpty()
				}
				//尝试解析为复杂枚举
				val complexEnumConfig = configGroup.complexEnums[enumName]
				if(complexEnumConfig != null) {
					val searchScope = complexEnumConfig.searchScope
					val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, element).preferRootFrom(element)
					return ParadoxComplexEnumValueSearch.search(name, enumName, project, selector = selector).findAll()
				}
				return emptyList()
			}
			CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
				//参见：ParadoxValueSetValueExpression
				val valueName = expression
				val valueSetName = configExpression.value ?: return emptyList()
				val read = configExpression.type == CwtDataTypes.Value
				if(read) {
					//首先尝试解析为预定义的value
					run {
						val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
						val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
						if(resolved != null) return resolved.toSingletonList()
					}
				}
				return ParadoxValueSetValueElement(element, valueName, valueSetName, configGroup.project, configGroup.gameType, read).toSingletonList()
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				//不在这里处理，参见：ParadoxScopeFieldExpression
				return emptyList()
			}
			CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
				//不在这里处理，参见：ParadoxValueFieldExpression
				return emptyList()
			}
			CwtDataTypes.Modifier -> {
				val name = expression
				return resolveModifier(name, configGroup).toSingletonListOrEmpty()
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> return emptyList()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = configExpression.value ?: return emptyList()
				return multiResolveAliasName(element, expression, element.isQuoted(), aliasName, configGroup)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = configExpression.value ?: return emptyList()
				return multiResolveAliasName(element, expression, element.isQuoted(), aliasName, configGroup)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> return emptyList()
			CwtDataTypes.ConstantKey, CwtDataTypes.Constant -> {
				when {
					config == null -> return emptyList()
					config is CwtDataConfig<*> -> return config.resolved().pointer.element.toSingletonListOrEmpty()
					else -> return config.pointer.element.toSingletonListOrEmpty()
				}
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> {
				if(isKey == true && config is CwtPropertyConfig) return config.resolved().pointer.element.toSingletonListOrEmpty()
				return emptyList()
			}
		}
	}
	
	fun resolveAliasName(element: PsiElement, expression: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, exact: Boolean = true): PsiElement? {
		val project = configGroup.project
		val gameType = configGroup.gameType
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
		val aliasSubName = getAliasSubName(expression, quoted, aliasName, configGroup)
		if(aliasSubName != null) {
			val configExpression = CwtKeyExpression.resolve(aliasSubName)
			when(configExpression.type) {
				CwtDataTypes.Localisation -> {
					val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
					return findLocalisation(expression, project, selector = selector)
				}
				CwtDataTypes.SyncedLocalisation -> {
					val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
					return findSyncedLocalisation(expression, project, selector = selector)
				}
				CwtDataTypes.TypeExpression -> {
					val typeExpression = configExpression.value ?: return null
					val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
					return ParadoxDefinitionSearch.search(expression, typeExpression, project, selector = selector).findFirst()
				}
				CwtDataTypes.TypeExpressionString -> {
					val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return null
					val nameToUse = expression.removeSurrounding(prefix, suffix)
					val typeExpression = configExpression.value ?: return null
					val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
					return ParadoxDefinitionSearch.search(nameToUse, typeExpression, project, selector = selector).findFirst()
				}
				CwtDataTypes.Enum -> {
					val enumName = configExpression.value ?: return null
					//这里不需要尝试解析为参数名
					//尝试解析为简单枚举
					val enumConfig = configGroup.enums[enumName]
					if(enumConfig != null) {
						val enumValueConfig = enumConfig.valueConfigMap.get(expression) ?: return null
						return enumValueConfig.pointer.element.castOrNull<CwtNamedElement>()
					}
					//尝试解析为复杂枚举
					val complexEnumConfig = configGroup.complexEnums[enumName]
					if(complexEnumConfig != null) {
						val searchScope = complexEnumConfig.searchScope
						val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, element).preferRootFrom(element, exact)
						return ParadoxComplexEnumValueSearch.search(expression, enumName, project, selector = selector).find()
					}
					return null
				}
				CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
					//参见：ParadoxValueSetValueExpression
					val valueName = expression
					val valueSetName = configExpression.value ?: return null
					val read = configExpression.type == CwtDataTypes.Value
					if(read) {
						//首先尝试解析为预定义的value
						run {
							val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
							val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
							if(resolved != null) return resolved
						}
					}
					return ParadoxValueSetValueElement(element, valueName, valueSetName, configGroup.project, configGroup.gameType, read)
				}
				CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
					//不在这里处理，参见：ParadoxScopeFieldExpression
					return null
				}
				CwtDataTypes.ConstantKey -> {
					//这里需要解析的应当是value，因此取第一个即可
					val aliasSubNameIgnoreCase = configGroup.aliasKeysGroupConst.get(aliasName)?.get(aliasSubName)
					val aliases = aliasGroup[aliasSubNameIgnoreCase] //需要忽略大小写
					if(aliases != null) {
						val alias = aliases.firstOrNull() ?: return null
						return alias.pointer.element
					}
					return null
				}
				else -> return null
			}
		}
		return null
	}
	
	fun multiResolveAliasName(element: PsiElement, expression: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, exact: Boolean = true): Collection<PsiElement> {
		val project = configGroup.project
		val gameType = configGroup.gameType
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
		val aliasSubName = getAliasSubName(expression, quoted, aliasName, configGroup)
		if(aliasSubName != null) {
			val configExpression = CwtKeyExpression.resolve(aliasSubName)
			when(configExpression.type) {
				CwtDataTypes.Localisation -> {
					val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
					return findLocalisations(expression, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
				}
				CwtDataTypes.SyncedLocalisation -> {
					val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
					return findSyncedLocalisations(expression, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
				}
				CwtDataTypes.TypeExpression -> {
					val typeExpression = configExpression.value ?: return emptyList()
					val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
					return ParadoxDefinitionSearch.search(expression, typeExpression, project, selector = selector).findAll()
				}
				CwtDataTypes.TypeExpressionString -> {
					val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
					val nameToUse = expression.removeSurrounding(prefix, suffix)
					val typeExpression = configExpression.value ?: return emptyList()
					val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
					return ParadoxDefinitionSearch.search(nameToUse, typeExpression, project, selector = selector).findAll()
				}
				CwtDataTypes.Enum -> {
					val enumName = configExpression.value ?: return emptyList()
					//这里不需要尝试解析为参数名
					//尝试解析为简单枚举
					val enumConfig = configGroup.enums[enumName]
					if(enumConfig != null) {
						val enumValueConfig = enumConfig.valueConfigMap.get(expression) ?: return emptyList()
						return enumValueConfig.pointer.element.castOrNull<CwtNamedElement>().toSingletonListOrEmpty()
					}
					//尝试解析为复杂枚举
					val complexEnumConfig = configGroup.complexEnums[enumName]
					if(complexEnumConfig != null) {
						val searchScope = complexEnumConfig.searchScope
						val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, element).preferRootFrom(element, exact)
						return ParadoxComplexEnumValueSearch.search(expression, enumName, project, selector = selector).findAll()
					}
					return emptyList()
				}
				CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
					//参见：ParadoxValueSetValueExpression
					val valueName = aliasSubName
					val valueSetName = configExpression.value ?: return emptyList()
					val read = configExpression.type == CwtDataTypes.Value
					if(read) {
						//首先尝试解析为预定义的value
						run {
							val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(valueName) ?: return@run
							val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
							if(resolved != null) return resolved.toSingletonList()
						}
					}
					return ParadoxValueSetValueElement(element, valueName, valueSetName, configGroup.project, configGroup.gameType, read).toSingletonList()
				}
				CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
					//不在这里处理，参见：ParadoxScopeFieldExpression
					return emptyList()
				}
				CwtDataTypes.ConstantKey -> {
					//这里需要解析的应当是value，因此取第一个即可
					val aliasSubNameIgnoreCase = configGroup.aliasKeysGroupConst.get(aliasName)?.get(aliasSubName)
					val aliases = aliasGroup[aliasSubNameIgnoreCase] //需要忽略大小写
					if(aliases != null) {
						val alias = aliases.firstOrNull() ?: return emptyList()
						return alias.pointer.element.toSingletonListOrEmpty()
					}
					return emptyList()
				}
				else -> return emptyList()
			}
		}
		return emptyList()
	}
	
	fun resolveSystemScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val systemScope = configGroup.systemScopes[name]
			?: return null
		return systemScope.pointer.element
	}
	
	fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val linkConfig = configGroup.linksAsScopeNotData[name]
			?: return null
		return linkConfig.pointer.element
	}
	
	fun resolveValueLinkValue(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val linkConfig = configGroup.linksAsValueNotData[name]
			?: return null
		return linkConfig.pointer.element
	}
	
	fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, configs: List<CwtConfig<*>>, configGroup: CwtConfigGroup): PsiElement? {
		for(config in configs) {
			val configExpression = config.expression ?: return null
			val valueSetName = configExpression.value ?: return null
			val read = configExpression.type == CwtDataTypes.Value
			if(read) {
				//首先尝试解析为预定义的value
				run {
					val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(name) ?: return@run
					val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
					if(resolved != null) return resolved
				}
			}
		}
		val read = configs.first().expression?.type == CwtDataTypes.Value //first is ok
		val valueSetNames = configs.mapNotNull { it.expression?.value }
		return ParadoxValueSetValueElement(element, name, valueSetNames, configGroup.project, configGroup.gameType, read)
	}
	
	fun resolveModifier(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val modifier = configGroup.modifiers[name] ?: return null
		return modifier.pointer.element
	}
	
	fun resolveLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
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
