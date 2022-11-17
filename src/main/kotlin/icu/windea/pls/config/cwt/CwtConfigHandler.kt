@file:Suppress("UnusedReceiverParameter", "UNUSED_PARAMETER")

package icu.windea.pls.config.cwt

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.internal.*
import icu.windea.pls.core.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.cwt.psi.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.exp.*
import icu.windea.pls.script.expression.*
import icu.windea.pls.script.expression.ParadoxScriptExpression
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
		val isParameterAware = expression.type == ParadoxDataType.StringType && expression.value.isParameterAwareExpression()
		when(configExpression.type) {
			CwtDataTypes.Any -> {
				return true
			}
			CwtDataTypes.Bool -> {
				return expression.type.isBooleanType()
			}
			CwtDataTypes.Int -> {
				//注意：用括号括起的整数（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.value).isIntType()) return true
				//匹配范围
				if(isExact && configExpression.extraValue<IntRange>()?.contains(expression.value.toIntOrNull()) != false) return true
				return false
			}
			CwtDataTypes.Float -> {
				//注意：用括号括起的浮点数（作为scalar）也匹配这个规则
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.value).isFloatType()) return true
				//匹配范围
				if(isExact && configExpression.extraValue<FloatRange>()?.contains(expression.value.toFloatOrNull()) != false) return true
				return false
			}
			CwtDataTypes.Scalar -> {
				//unquoted_string, quoted, any key
				return expression.type.isStringType() || (expression.isKey == true)
			}
			CwtDataTypes.ColorField -> {
				return expression.type.isColorType() && configExpression.value?.let { expression.value.startsWith(it) } != false
			}
			CwtDataTypes.PercentageField -> {
				if(!expression.type.isStringType()) return false
				return ParadoxDataType.isPercentageField(expression.value)
			}
			CwtDataTypes.DateField -> {
				if(!expression.type.isStringType()) return false
				return ParadoxDataType.isDateField(expression.value)
			}
			CwtDataTypes.Localisation -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return findLocalisation(expression.value, project, preferFirst = true, selector = selector) != null
				}
				return true
			}
			CwtDataTypes.SyncedLocalisation -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return findSyncedLocalisation(expression.value, project, preferFirst = true, selector = selector) != null
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
					return findLocalisation(expression.value, project, preferFirst = true, selector = selector) != null
				}
				return true
			}
			CwtDataTypes.AbsoluteFilePath -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				val path = expression.value.toPathOrNull() ?: return false
				return VfsUtil.findFile(path, true) != null
			}
			CwtDataTypes.FilePath -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.FILE_PATH)) {
					val resolvedPath = CwtFilePathExpressionTypes.FilePath.resolve(configExpression.value, expression.value.normalizePath())
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
					val resolvedPath = CwtFilePathExpressionTypes.Icon.resolve(configExpression.value, expression.value.normalizePath()) ?: return false
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
					return ParadoxDefinitionSearch.search(expression.value, typeExpression, project, selector = selector).findFirst() != null
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
					return ParadoxDefinitionSearch.search(expression.value, typeExpression, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataTypes.Enum -> {
				if(!expression.type.isStringType()) return false
				if(!isStatic && isParameterAware) return true
				val enumName = configExpression.value ?: return false //invalid cwt config
				//匹配参数名（即使对应的定义声明中不存在对应名字的参数，也总是匹配）
				if(!isStatic && expression.isKey == true && enumName == paramsEnumName) return true
				//匹配简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					return expression.value in enumConfig.values
				}
				if(isStatic) return false
				//匹配复杂枚举
				val complexEnumConfig = configGroup.complexEnums[enumName]
				if(complexEnumConfig != null) {
					if(BitUtil.isSet(matchType, CwtConfigMatchType.COMPLEX_ENUM_VALUE)) {
						val selector = complexEnumValueSelector().gameType(gameType)
						val search = ParadoxComplexEnumValueSearch.search(enumName, project, selector = selector)
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
				return ParadoxScriptExpression.resolveScopeField(expression.value, configGroup).isMatched()
			}
			CwtDataTypes.ScopeGroup -> {
				if(expression.quoted) return false //不允许用引号括起
				if(!isStatic && isParameterAware) return true
				return ParadoxScriptExpression.resolveScopeField(expression.value, configGroup).isMatched()
			}
			CwtDataTypes.ValueField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.value).isFloatType()) return true
				if(!isStatic && isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				return ParadoxScriptExpression.resolveValueField(expression.value, configGroup).isMatched()
			}
			CwtDataTypes.IntValueField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.value).isIntType()) return true
				if(!isStatic && isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				return ParadoxScriptExpression.resolveValueField(expression.value, configGroup).isMatched()
			}
			CwtDataTypes.VariableField -> {
				if(!isStatic && isParameterAware) return true
				return false //TODO
			}
			CwtDataTypes.IntVariableField -> {
				if(!isStatic && isParameterAware) return true
				return false //TODO
			}
			CwtDataTypes.Modifier -> {
				if(!isStatic && isParameterAware) return true
				//匹配预定义的modifier
				return matchesModifier(expression.value, configGroup)
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
			CwtDataTypes.Constant -> {
				return expression.value.equals(configExpression.value, true) //忽略大小写
			}
			CwtDataTypes.Other -> {
				if(isStatic) return false
				return true
			}
		}
	}
	
	fun matchesAliasName(expression: ParadoxDataExpression, aliasName: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
		//TODO 匹配scope
		val aliasSubName = getAliasSubName(expression.value, expression.quoted, aliasName, configGroup, matchType) ?: return false
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
			CwtDataTypes.Any -> 90
			CwtDataTypes.Bool -> 90
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
			CwtDataTypes.Constant -> 100
			CwtDataTypes.Other -> 0 //不期望匹配到
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
		val childPropertyConfigs = definitionElementInfo.getChildPropertyConfigs()
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
		val configs = definitionElementInfo.getConfigs()
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
		val childValueConfigs = definitionElementInfo.getChildValueConfigs()
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
	
	fun ProcessingContext.completeKey(contextElement: PsiElement, configExpression: CwtKeyExpression, config: CwtPropertyConfig, result: CompletionResultSet, scope: String?) {
		completeScriptExpression(contextElement, configExpression, config, result, scope)
	}
	
	fun ProcessingContext.completeValue(contextElement: PsiElement, configExpression: CwtValueExpression, config: CwtDataConfig<*>, result: CompletionResultSet, scope: String?) {
		completeScriptExpression(contextElement, configExpression, config, result, scope)
	}
	
	fun ProcessingContext.completeScriptExpression(contextElement: PsiElement, configExpression: CwtDataExpression, config: CwtDataConfig<*>, result: CompletionResultSet, scope: String?) {
		if(configExpression.isEmpty()) return
		if(keyword.isParameterAwareExpression()) return //排除带参数或者的情况
		
		val project = configGroup.project
		val gameType = configGroup.gameType
		when(configExpression.type) {
			CwtDataTypes.Bool -> {
				result.addAllElements(boolLookupElements)
			}
			CwtDataTypes.Localisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processLocalisationVariants(keyword, project, selector = selector) { localisation ->
					val n = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withIcon(PlsIcons.Localisation)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.SyncedLocalisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processSyncedLocalisationVariants(keyword, project, selector = selector) { syncedLocalisation ->
					val n = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val name = n.quoteIf(quoted)
					val typeFile = syncedLocalisation.containingFile
					val lookupElement = LookupElementBuilder.create(syncedLocalisation, name)
						.withIcon(PlsIcons.Localisation)
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
				val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				processLocalisationVariants(keyword, project) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val lookupElement = LookupElementBuilder.create(localisation, name)
						.withIcon(PlsIcons.Localisation)
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
				val expressionValue = configExpression.value
				val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement)
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(project, distinct = true, selector = selector)
				} else {
					findFilesByFilePath(expressionValue, project, expressionType = expressionType, distinct = true, selector = selector)
				}
				if(virtualFiles.isEmpty()) return
				val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				for(virtualFile in virtualFiles) {
					val file = virtualFile.toPsiFile<PsiFile>(project) ?: continue
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
				val expressionValue = configExpression.value
				val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement)
				val virtualFiles = if(expressionValue == null) {
					findAllFilesByFilePath(project, distinct = true, selector = selector)
				} else {
					findFilesByFilePath(expressionValue, project, expressionType = expressionType, distinct = true, selector = selector)
				}
				if(virtualFiles.isEmpty()) return
				val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				for(virtualFile in virtualFiles) {
					val file = virtualFile.toPsiFile<PsiFile>(project) ?: continue
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
				val typeExpression = configExpression.value ?: return
				val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				val selector = definitionSelector().gameType(gameType).preferRootFrom(contextElement).distinctByName()
				val definitionQuery = ParadoxDefinitionSearch.search(typeExpression, project, selector = selector)
				definitionQuery.processResult { definition ->
					val n = definition.definitionInfo?.name ?: return@processResult true
					val name = n.quoteIf(quoted)
					val typeFile = definition.containingFile
					val lookupElement = LookupElementBuilder.create(definition, name)
						.withIcon(PlsIcons.Definition)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.TypeExpressionString -> {
				val typeExpression = configExpression.value ?: return
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return
				val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				val selector = definitionSelector().gameType(gameType).preferRootFrom(contextElement).distinctByName()
				val definitionQuery = ParadoxDefinitionSearch.search(typeExpression, project, selector = selector)
				definitionQuery.processResult { definition ->
					val definitionName = definition.definitionInfo?.name ?: return@processResult true
					val n = "$prefix$definitionName$suffix"
					val name = n.quoteIf(quoted)
					val typeFile = definition.containingFile
					val lookupElement = LookupElementBuilder.create(definition, name)
						.withIcon(PlsIcons.Definition)
						.withTailText(tailText, true)
						.withTypeText(typeFile.name, typeFile.icon, true)
						.withExpectedInsertHandler(isKey)
					result.addElement(lookupElement)
					true
				}
			}
			CwtDataTypes.Enum -> {
				val enumName = configExpression.value ?: return
				//提示参数名（仅限key）
				if(isKey && enumName == paramsEnumName && config is CwtPropertyConfig) {
					ProgressManager.checkCanceled()
					val propertyElement = contextElement.findParentDefinitionProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return
					completeParametersForInvocationExpression(propertyElement, config, result)
					return
				}
				
				val tailText = " by $configExpression in ${config.pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				//提示简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					ProgressManager.checkCanceled()
					val enumValueConfigs = enumConfig.valueConfigMap.values
					if(enumValueConfigs.isEmpty()) return
					val typeFile = enumConfig.pointer.containingFile
					for(enumValueConfig in enumValueConfigs) {
						if(quoted && enumValueConfig.stringValue == null) continue
						val n = enumValueConfig.value
						//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
						val name = n.quoteIf(quoted)
						val element = enumValueConfig.pointer.element ?: continue
						val lookupElement = LookupElementBuilder.create(element, name)
							.withIcon(PlsIcons.EnumValue)
							.withTailText(tailText, true)
							.withTypeText(typeFile?.name, typeFile?.icon, true)
							.withExpectedInsertHandler(isKey)
							.withCaseSensitivity(false) //忽略大小写
							.withPriority(PlsCompletionPriorities.enumPriority)
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
					query.processResult { complexEnum ->
						val n = complexEnum.value
						//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
						val name = n.quoteIf(quoted)
						val lookupElement = LookupElementBuilder.create(complexEnum, name)
							.withIcon(PlsIcons.ComplexEnumValue)
							.withTailText(tailText, true)
							.withTypeText(typeFile?.name, typeFile?.icon, true)
							.withExpectedInsertHandler(isKey)
							.withCaseSensitivity(false) //忽略大小写
						result.addElement(lookupElement)
						true
					}
				}
			}
			CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
				//光标在'@'后面时，不进行提示
				val atIndex = keyword.indexOf('@')
				if(atIndex != -1 && offsetInParent > atIndex) return
				
				if(quoted) return
				val valueSetName = configExpression.value ?: return
				val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name ?: PlsConstants.anonymousString}"
				//提示预定义的value
				run {
					ProgressManager.checkCanceled()
					if(configExpression.type == CwtDataTypes.Value) {
						val valueConfig = configGroup.values[valueSetName] ?: return@run
						val valueSetValueConfigs = valueConfig.valueConfigMap.values
						if(valueSetValueConfigs.isEmpty()) return@run
						for(valueSetValueConfig in valueSetValueConfigs) {
							if(quoted && valueSetValueConfig.stringValue == null) continue
							//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
							val name = valueSetValueConfig.value
							val element = valueSetValueConfig.pointer.element ?: continue
							val typeFile = valueConfig.pointer.containingFile
							val lookupElement = LookupElementBuilder.create(element, name)
								.withIcon(PlsIcons.PredefinedValueSetValue)
								.withTailText(tailText, true)
								.withTypeText(typeFile?.name, typeFile?.icon, true)
								.withExpectedInsertHandler(isKey)
								.withCaseSensitivity(false) //忽略大小写
								.withPriority(PlsCompletionPriorities.predefinedValueSetValuePriority)
							result.addElement(lookupElement)
						}
					}
				}
				//提示来自脚本文件的value
				run {
					ProgressManager.checkCanceled()
					val selector = valueSetValueSelector().gameType(gameType).distinctByValue()
					val valueSetValueQuery = ParadoxValueSetValueSearch.search(valueSetName, project, selector = selector)
					valueSetValueQuery.processResult { valueSetValue ->
						//去除后面的作用域信息
						val value = valueSetValue.value.substringBefore('@')
						//排除当前正在输入的那个
						if(value == keyword.substringBefore('@') && valueSetValue isSamePosition contextElement) return@processResult true
						val icon = when(valueSetName) {
							"variable" -> PlsIcons.Variable
							else -> PlsIcons.ValueSetValue
						}
						//不显示typeText
						val lookupElement = LookupElementBuilder.create(valueSetValue, value)
							.withIcon(icon)
							.withTailText(tailText, true)
							.withExpectedInsertHandler(isKey)
							.withCaseSensitivity(false) //忽略大小写
						result.addElement(lookupElement)
						true
					}
				}
			}
			CwtDataTypes.ScopeField -> {
				completeScopeFieldExpression(result)
			}
			CwtDataTypes.Scope -> {
				put(PlsCompletionKeys.scopeNameKey, configExpression.value)
				completeScopeFieldExpression(result)
				put(PlsCompletionKeys.scopeNameKey, null)
			}
			CwtDataTypes.ScopeGroup -> {
				put(PlsCompletionKeys.scopeGroupNameKey, configExpression.value)
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
				val aliasName = configExpression.value ?: return
				completeAliasName(contextElement, aliasName, config, result, scope)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = configExpression.value ?: return
				completeAliasName(contextElement, aliasName, config, result, scope)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> pass()
			CwtDataTypes.Constant -> {
				val n = configExpression.value ?: return
				//if(!n.matchesKeyword(keyword)) return //不预先过滤结果
				val name = n.quoteIf(quoted)
				val element = config.resolved().pointer.element ?: return
				val typeFile = config.resolved().pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withIcon(if(isKey) PlsIcons.Property else PlsIcons.Value)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withExpectedInsertHandler(isKey)
					.withCaseSensitivity(false) //忽略大小写
					.withPriority(PlsCompletionPriorities.constantPriority)
				result.addElement(lookupElement)
			}
			else -> pass()
		}
	}
	
	fun ProcessingContext.completeAliasName(contextElement: PsiElement, aliasName: String, config: CwtDataConfig<*>, result: CompletionResultSet, scope: String?) {
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
		for(aliasConfigs in aliasGroup.values) {
			//aliasConfigs的名字是相同的 
			val aliasConfig = aliasConfigs.firstOrNull() ?: continue
			//TODO alias的scope需要匹配（推断得到的scope为null时，总是提示）
			val isScopeMatched = matchesScope(scope, aliasConfig.supportedScopes, configGroup)
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
			val isScopeMatched = scope == null || modifierConfig.categoryConfigMap.values.any { c -> matchesScope(scope, c.supportedScopes, configGroup) }
			if(!isScopeMatched) continue
			
			val n = modifierConfig.name
			//if(!n.matchesKeyword(keyword)) continue //不预先过滤结果
			val name = n.quoteIf(quoted)
			val element = modifierConfig.pointer.element ?: continue
			val tailText = " from modifiers"
			val typeFile = modifierConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				//.apply { if(!scopeMatched) withItemTextForeground(Color.GRAY) }
				.withIcon(PlsIcons.Modifier)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withPriority(PlsCompletionPriorities.modifierPriority)
			//.withPriority(PlsCompletionPriorities.modifierPriority, scopeMatched)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeScopeFieldExpression(result: CompletionResultSet) {
		//基于当前位置的代码补全
		val expression = ParadoxScriptExpression.resolveScopeField(keyword, configGroup)
		expression.complete(result, this)
	}
	
	fun ProcessingContext.completeValueFieldExpression(result: CompletionResultSet, isInt: Boolean = false) {
		//基于当前位置的代码补全
		val expression = ParadoxScriptExpression.resolveValueField(keyword, configGroup)
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
				.withIcon(PlsIcons.SystemScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.systemScopePriority)
			lookupElements.add(lookupElement)
		}
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
				.withExpectedInsertHandler(isKey)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.scopePriority)
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
		if(prefixLinkConfigsToUse.isNotEmpty()) {
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
				val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
				if(!isScopeMatched) continue
				
				val name = linkConfig.prefix ?: continue
				//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
				val element = linkConfig.pointer.element ?: continue
				val tailText = " from scope link ${linkConfig.name}"
				val typeFile = linkConfig.pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withIcon(PlsIcons.ScopeFieldPrefix)
					.withBoldness(true)
					.withTailText(tailText, true)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withPriority(PlsCompletionPriorities.scopeFieldPrefixPriority)
				lookupElements.add(lookupElement)
			}
			resultToUse.addAllElements(lookupElements)
			
			//基于所有没有前缀的dataSource进行提示
			val linkConfigsNoPrefix = linkConfigs.values
				.filter { it.prefix == null && it.dataSource != null }
			if(linkConfigsNoPrefix.isNotEmpty()) {
				for(linkConfig in linkConfigsNoPrefix) {
					//排除input_scopes不匹配前一个scope的output_scope的情况
					val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
					if(!isScopeMatched) continue
					
					completeScriptExpression(contextElement, linkConfig.dataSource!!, linkConfig.config, resultToUse, outputScope)
				}
			}
		}
	}
	
	fun ProcessingContext.completeValueFieldValue(result: CompletionResultSet) {
		//TODO 进一步匹配scope
		val keyword = keyword
		val lookupElements = mutableSetOf<LookupElement>()
		val linkConfigs = configGroup.linksAsValueNotData
		val outputScope = prevScope?.let { prevScope -> linkConfigs[prevScope]?.takeUnless { it.outputAnyScope }?.outputScope }
		
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
				.withIcon(PlsIcons.ValueFieldValue)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withExpectedInsertHandler(isKey)
				.withCaseSensitivity(false) //忽略大小写
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
		if(prefixLinkConfigsToUse.isNotEmpty()) {
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
				val isScopeMatched = matchesScope(outputScope, linkConfig.inputScopes, configGroup)
				if(!isScopeMatched) continue
				
				val name = linkConfig.prefix ?: continue
				//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
				val element = linkConfig.pointer.element ?: continue
				val tailText = " from value link ${linkConfig.name}"
				val typeFile = linkConfig.pointer.containingFile
				val lookupElement = LookupElementBuilder.create(element, name)
					.withIcon(PlsIcons.ValueFieldPrefix)
					.withBoldness(true)
					.withTailText(tailText, true)
					.withTypeText(typeFile?.name, typeFile?.icon, true)
					.withPriority(PlsCompletionPriorities.valueFieldPrefixPriority)
				lookupElements.add(lookupElement)
			}
			//这里认为必须要有前缀
			resultToUse.addAllElements(lookupElements)
		}
	}
	
	fun ProcessingContext.completeValueSetValue(result: CompletionResultSet) {
		//TODO
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
				.withIcon(PlsIcons.SystemScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.systemScopePriority)
			lookupElements.add(lookupElement)
		}
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
				.withIcon(PlsIcons.LocalisationCommandField)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.localisationCommandPriority)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeParameters(element: PsiElement, read: Boolean, result: CompletionResultSet) {
		//向上找到definition
		val definition = element.findParentDefinition() ?: return
		val definitionInfo = definition.definitionInfo ?: return
		val parameterMap = definition.parameterMap
		if(parameterMap.isEmpty()) return
		val lookupElements = mutableListOf<LookupElement>()
		for((parameterName, parameters) in parameterMap) {
			val parameter = parameters.firstNotNullOfOrNull { it.element } ?: continue
			//排除当前正在输入的那个
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
	
	fun ProcessingContext.completeParametersForInvocationExpression(propertyElement: ParadoxScriptProperty, propertyConfig: CwtPropertyConfig, result: CompletionResultSet) {
		if(quoted) return //输入参数不允许用引号括起
		val definitionName = propertyElement.name
		val definitionType = propertyConfig.parent?.castOrNull<CwtPropertyConfig>()
			?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.keyExpression
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
				.withExpectedInsertHandler(isKey)
			lookupElements.add(lookupElement)
		}
		result.addAllElements(lookupElements)
	}
	
	fun ProcessingContext.completeParametersForScriptValueExpression(svName: String, parameterNames: Set<String>, result: CompletionResultSet) {
		//整合所有匹配名字的SV的参数
		val resultToUse = result.withPrefixMatcher(keyword)
		val existParameterNames = mutableSetOf<String>()
		existParameterNames.addAll(parameterNames)
		val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(contextElement)
		val svQuery = ParadoxDefinitionSearch.search(svName, "script_value", configGroup.project, selector = selector)
		svQuery.processResult { sv ->
			val parameterMap = sv.parameterMap
			if(parameterMap.isEmpty()) return@processResult true
			for((parameterName, parameters) in parameterMap) {
				if(parameterName in existParameterNames) continue //排除已输入的
				val parameter = parameters.firstNotNullOfOrNull { it.element } ?: continue
				val lookupElement = LookupElementBuilder.create(parameter, parameterName)
					.withIcon(PlsIcons.Parameter)
					.withTypeText(svName, sv.icon, true)
					.withExpectedInsertHandler(false)
				resultToUse.addElement(lookupElement)
			}
			true
		}
	}
	
	private val boolLookupElements = PlsConstants.booleanValues.map { value ->
		LookupElementBuilder.create(value).bold().withPriority(PlsCompletionPriorities.keywordPriority)
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
			if(isKey) {
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
	//NOTE 不要传入psiFile - 推断gameType时可能需要直接从上下文psiElement推断
	/**
	 * @param element 需要解析的PSI元素。
	 * @param rangeInElement 需要解析的文本在需要解析的PSI元素对应的整个文本中的位置。
	 */
	fun resolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, configExpression: CwtDataExpression, config: CwtConfig<*>, isKey: Boolean? = null): PsiElement? {
		if(element.isParameterAwareExpression()) return null //排除带参数的情况
		
		val project = element.project
		val configGroup = config.info.configGroup
		val gameType = configGroup.gameType
		val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.value
		when(configExpression.type) {
			CwtDataTypes.Localisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element).preferLocale(preferredParadoxLocale())
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element).preferLocale(preferredParadoxLocale())
				return findSyncedLocalisation(name, project, selector = selector)
			}
			CwtDataTypes.InlineLocalisation -> {
				if(element.isQuoted()) return null
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element).preferLocale(preferredParadoxLocale())
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
				val selector = fileSelector().gameType(gameType).preferRootFrom(element)
				return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath()) ?: return null
				val selector = fileSelector().gameType(gameType).preferRootFrom(element)
				return findFileByFilePath(filePath, project, selector = selector)?.toPsiFile(project)
			}
			CwtDataTypes.TypeExpression -> {
				val name = expression
				val typeExpression = configExpression.value ?: return null
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = expression.removeSurrounding(prefix, suffix)
				val typeExpression = configExpression.value ?: return null
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
			}
			CwtDataTypes.Enum -> {
				val enumName = configExpression.value ?: return null
				val name = expression
				//尝试解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val definitionName = element.parent?.parentOfType<ParadoxScriptProperty>()?.name ?: return null
					val definitionType = config.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.keyExpression
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
					val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, element).preferRootFrom(element)
					return ParadoxComplexEnumValueSearch.search(name, enumName, project, selector = selector).find()
				}
				return null
			}
			CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
				return null //不在这里处理，参见：ParadoxScriptValueSetValueExpression
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				return null //不在这里处理，参见：ParadoxScriptScopeFieldExpression
			}
			CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
				return null //不在这里处理，参见：ParadoxScriptValueFieldExpression
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
				return resolveAliasName(element, expression, element.isQuoted(), aliasName, configGroup)
			}
			CwtDataTypes.AliasName -> {
				val aliasName = configExpression.value ?: return null
				return resolveAliasName(element, expression, element.isQuoted(), aliasName, configGroup)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> return null
			CwtDataTypes.Constant -> {
				if(config is CwtDataConfig<*>) return config.resolved().pointer.element.castOrNull<CwtNamedElement>()
				return null
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> {
				if(isKey == true && config is CwtPropertyConfig) return config.resolved().pointer.element
				return null
			}
		}
	}
	
	fun multiResolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, configExpression: CwtDataExpression, config: CwtDataConfig<*>, isKey: Boolean?): Collection<PsiElement> {
		if(element.isParameterAwareExpression()) return emptyList() //排除带参数的情况  
		
		val project = element.project
		val configGroup = config.info.configGroup
		val gameType = configGroup.gameType
		val text = rangeInElement?.substring(element.text)?.unquote() ?: element.value
		when(configExpression.type) {
			CwtDataTypes.Localisation -> {
				val name = text
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.SyncedLocalisation -> {
				val name = text
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findSyncedLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.InlineLocalisation -> {
				if(element.isQuoted()) return emptyList()
				val name = text
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataTypes.AbsoluteFilePath -> {
				val filePath = text
				val path = filePath.toPathOrNull() ?: return emptyList()
				return VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
			}
			CwtDataTypes.FilePath -> {
				val expressionType = CwtFilePathExpressionTypes.FilePath
				val filePath = expressionType.resolve(configExpression.value, text.normalizePath())
				val selector = fileSelector().gameType(gameType).preferRootFrom(element)
				return findFilesByFilePath(filePath, project, selector = selector).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.Icon -> {
				val expressionType = CwtFilePathExpressionTypes.Icon
				val filePath = expressionType.resolve(configExpression.value, text.normalizePath()) ?: return emptyList()
				val selector = fileSelector().gameType(gameType).preferRootFrom(element)
				return findFilesByFilePath(filePath, project, selector = selector).mapNotNull { it.toPsiFile(project) }
			}
			CwtDataTypes.TypeExpression -> {
				val name = text
				val typeExpression = configExpression.value ?: return emptyList()
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findAll()
			}
			CwtDataTypes.TypeExpressionString -> {
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = text.removeSurrounding(prefix, suffix)
				val typeExpression = configExpression.value ?: return emptyList()
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findAll()
			}
			CwtDataTypes.Enum -> {
				val enumName = configExpression.value ?: return emptyList()
				val name = text
				//尝试解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val definitionName = element.parent?.parentOfType<ParadoxScriptProperty>()?.name ?: return emptyList()
					val definitionType = config.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.keyExpression
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
				return emptyList() //不在这里处理，参见：ParadoxScriptValueSetValueExpression
			}
			CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
				return emptyList() //不在这里处理，参见：ParadoxScriptScopeFieldExpression
			}
			CwtDataTypes.ValueField, CwtDataTypes.IntValueField -> {
				return emptyList() //不在这里处理，参见：ParadoxScriptValueFieldExpression
			}
			CwtDataTypes.Modifier -> {
				val name = text
				return resolveModifier(name, configGroup).toSingletonListOrEmpty()
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.SingleAliasRight -> return emptyList()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataTypes.AliasKeysField -> {
				val aliasName = configExpression.value ?: return emptyList()
				return resolveAliasName(element, text, element.isQuoted(), aliasName, configGroup).toSingletonListOrEmpty()
			}
			CwtDataTypes.AliasName -> {
				val aliasName = configExpression.value ?: return emptyList()
				return resolveAliasName(element, text, element.isQuoted(), aliasName, configGroup).toSingletonListOrEmpty()
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataTypes.AliasMatchLeft -> return emptyList()
			CwtDataTypes.Constant -> {
				return config.pointer.element.castOrNull<CwtNamedElement>().toSingletonListOrEmpty()
			}
			//对于值，如果类型是scalar、int等，不进行解析
			else -> {
				if(isKey == true && config is CwtPropertyConfig) return config.resolved().pointer.element.toSingletonListOrEmpty() //TODO
				return emptyList() //TODO 
			}
		}
	}
	
	fun resolveAliasName(element: PsiElement, name: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup): PsiElement? {
		val project = configGroup.project
		val gameType = configGroup.gameType
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
		val aliasSubName = getAliasSubName(name, quoted, aliasName, configGroup)
		if(aliasSubName != null) {
			val expression = CwtKeyExpression.resolve(aliasSubName)
			when(expression.type) {
				CwtDataTypes.Localisation -> {
					val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
					return findLocalisation(name, project, selector = selector)
				}
				CwtDataTypes.SyncedLocalisation -> {
					val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
					return findSyncedLocalisation(name, project, selector = selector)
				}
				CwtDataTypes.TypeExpression -> {
					val typeExpression = expression.value ?: return null
					val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
					return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findFirst()
				}
				CwtDataTypes.TypeExpressionString -> {
					val (prefix, suffix) = expression.extraValue?.cast<TypedTuple2<String>>() ?: return null
					val nameToUse = name.removeSurrounding(prefix, suffix)
					val typeExpression = expression.value ?: return null
					val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
					return ParadoxDefinitionSearch.search(nameToUse, typeExpression, project, selector = selector).findFirst()
				}
				CwtDataTypes.Enum -> {
					val enumName = expression.value ?: return null
					//这里不需要尝试解析为参数名
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
						val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, element).preferRootFrom(element)
						return ParadoxComplexEnumValueSearch.search(name, enumName, project, selector = selector).find()
					}
					return null
				}
				CwtDataTypes.Value, CwtDataTypes.ValueSet -> {
					return null //不在这里处理，参见：ParadoxScriptValueSetValueExpression
				}
				CwtDataTypes.ScopeField, CwtDataTypes.Scope, CwtDataTypes.ScopeGroup -> {
					return null //不在这里处理，参见：ParadoxScriptScopeFieldExpression
				}
				CwtDataTypes.Constant -> {
					//同名的定义有多个，取第一个即可
					val aliasSubNameIgnoreCase = configGroup.aliasKeysGroupConst.get(aliasName)?.get(aliasSubName)
					val aliases = aliasGroup[aliasSubNameIgnoreCase] //需要忽略大小写
					if(aliases != null) {
						val alias = aliases.firstOrNull() ?: return null
						return alias.pointer.element
					}
					return null
				}
				else -> return null //TODO
			}
		}
		return null
	}
	
	fun resolveSystemScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val systemScope = InternalConfigHandler.getSystemScope(name, configGroup.project) ?: return null
		return systemScope.pointer.element
	}
	
	fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val links = configGroup.linksAsScopeNotData
		if(links.isEmpty()) return null
		val linkConfig = links[name] ?: return null
		return linkConfig.pointer.element
	}
	
	fun resolveValueFieldValue(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val links = configGroup.linksAsValueNotData
		if(links.isEmpty()) return null
		val linkConfig = links[name] ?: return null
		return linkConfig.pointer.element
	}
	
	fun resolveValueSetValue(element: ParadoxScriptExpressionElement, name: String, config: CwtDataConfig<*>): PsiElement? {
		val valueSetName = config.expression.value ?: return null
		val configGroup = config.info.configGroup
		val read = config.expression.type == CwtDataTypes.Value
		if(read) {
			//首先尝试解析为预定义的value
			run {
				val valueSetValueConfig = configGroup.values.get(valueSetName)?.valueConfigMap?.get(name) ?: return@run
				val resolved = valueSetValueConfig.pointer.element.castOrNull<CwtNamedElement>()
				if(resolved != null) return resolved
			}
		}
		return ParadoxValueSetValueElement(element, name, valueSetName, configGroup.project, configGroup.gameType, read)
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
