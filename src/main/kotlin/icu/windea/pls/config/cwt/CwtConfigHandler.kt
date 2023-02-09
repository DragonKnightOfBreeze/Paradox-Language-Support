@file:Suppress("UnusedReceiverParameter", "UNUSED_PARAMETER")

package icu.windea.pls.config.cwt

import com.intellij.application.options.*
import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.support.*
import icu.windea.pls.script.codeStyle.*
import icu.windea.pls.script.psi.*

/**
 * CWT规则的处理器。
 *
 * 提供基于CWT规则实现的匹配、校验、代码提示、引用解析等功能。
 */
object CwtConfigHandler {
	//region Common Methods
	const val paramsEnumName = "scripted_effect_params"
	
	fun isParameter(config: CwtDataConfig<*>?): Boolean {
		if(config !is CwtPropertyConfig) return false
		val keyExpression = config.keyExpression
		return keyExpression.type == CwtDataType.Enum && keyExpression.value == paramsEnumName
	}
	
	fun isAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.keyExpression.type == CwtDataType.AliasName
			&& propertyConfig.valueExpression.type == CwtDataType.AliasMatchLeft
	}
	
	fun isSingleAlias(propertyConfig: CwtPropertyConfig): Boolean {
		return propertyConfig.valueExpression.type == CwtDataType.SingleAliasRight
	}
	
	fun isComplexEnum(config: CwtDataConfig<*>): Boolean {
		return config.expression.type == CwtDataType.Enum
			&& config.expression.value?.let { config.info.configGroup.complexEnums[it] } != null
	}
	
	/**
	 * 从CWT规则元素推断得到对应的CWT规则组。
	 */
	@InferMethod
	fun getConfigGroupFromCwt(from: PsiElement, project: Project): CwtConfigGroup? {
		val file = from.containingFile ?: return null
		val virtualFile = file.virtualFile ?: return null
		val path = virtualFile.path
		//这里的key可能是"core"，而这不是gameType
		val key = path.substringAfter("config/cwt/", "").substringBefore("/", "")
		if(key.isEmpty()) return null
		return getCwtConfig(project).get(key)
	}
	
	/**
	 * 内联规则以便后续的代码提示、引用解析和结构验证。
	 */
	fun inlineConfig(element: PsiElement, key: String, isQuoted: Boolean, config: CwtPropertyConfig, configGroup: CwtConfigGroup, result: MutableList<CwtDataConfig<*>>, matchType: Int) {
		//内联类型为single_alias_right或alias_match_left的规则
		run {
			val valueExpression = config.valueExpression
			when(valueExpression.type) {
				CwtDataType.SingleAliasRight -> {
					val singleAliasName = valueExpression.value ?: return@run
					val singleAlias = configGroup.singleAliases[singleAliasName] ?: return@run
					result.add(config.inlineFromSingleAliasConfig(singleAlias))
					return
				}
				CwtDataType.AliasMatchLeft -> {
					val aliasName = valueExpression.value ?: return@run
					val aliasGroup = configGroup.aliasGroups[aliasName] ?: return@run
					val aliasSubName = getAliasSubName(element, key, isQuoted, aliasName, configGroup, matchType) ?: return@run
					val aliases = aliasGroup[aliasSubName] ?: return@run
					for(alias in aliases) {
						var inlinedConfig = config.inlineFromAliasConfig(alias)
						if(inlinedConfig.valueExpression.type == CwtDataType.SingleAliasRight) {
							val singleAliasName = inlinedConfig.valueExpression.value ?: return@run
							val singleAlias = configGroup.singleAliases[singleAliasName] ?: return@run
							inlinedConfig = inlinedConfig.inlineFromSingleAliasConfig(singleAlias)
						}
						result.add(inlinedConfig)
					}
					return
				}
				else -> pass()
			}
		}
		result.add(config)
	}
	
	fun inlineConfigAsChild(key: String, quoted: Boolean, parentConfig: CwtPropertyConfig, configGroup: CwtConfigGroup, result: SmartList<CwtDataConfig<*>>): Boolean {
		//内联特定的规则：inline_script
		val inlineConfigs = configGroup.inlineConfigGroup[key]
		if(inlineConfigs.isNullOrEmpty()) return false
		for(inlineConfig in inlineConfigs) {
			result.add(parentConfig.inlineConfigAsChild(inlineConfig))
		}
		return true
	}
	
	//endregion
	
	//region Matches Methods
	//DONE 基于cwt规则文件的匹配方法需要进一步匹配scope
	//DONE 兼容variableReference inlineMath parameter
	fun matchesScriptExpression(
		element: PsiElement,
		expression: ParadoxDataExpression,
		configExpression: CwtDataExpression,
		config: CwtConfig<*>?,
		configGroup: CwtConfigGroup,
		matchType: Int = CwtConfigMatchType.ALL
	): Boolean {
		val isStatic = BitUtil.isSet(matchType, CwtConfigMatchType.STATIC)
		val isNotExact = BitUtil.isSet(matchType, CwtConfigMatchType.NOT_EXACT)
		
		//匹配block
		if(configExpression == CwtValueExpression.BlockExpression) {
			if(expression.isKey != false) return false
			if(expression.type != ParadoxDataType.BlockType) return false
			if(element !is ParadoxScriptBlock) return true
			if(isNotExact) return true //非精确匹配 - 直接使用第一个
			val configsInBlock = config?.castOrNull<CwtDataConfig<*>>()?.configs ?: return true
			return matchesScriptExpressionInBlock(element, configsInBlock, configGroup)
		}
		
		//匹配空字符串
		if(configExpression.isEmpty()) {
			return expression.isEmpty()
		}
		
		val project = configGroup.project
		val gameType = configGroup.gameType
		val isParameterAware = expression.type == ParadoxDataType.StringType && expression.text.isParameterAwareExpression()
		when(configExpression.type) {
			CwtDataType.Bool -> {
				return expression.type.isBooleanType()
			}
			CwtDataType.Int -> {
				//quoted number (e.g. "1") -> ok according to vanilla game files
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) {
					if(isNotExact) return true
					val (min, max) = configExpression.extraValue<Tuple2<Int, Int?>>() ?: return true
					val value = expression.text.toIntOrNull() ?: return true
					return min <= value && (max == null || max >= value)
				}
				return false
			}
			CwtDataType.Float -> {
				//quoted number (e.g. "1") -> ok according to vanilla game files
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) {
					if(isNotExact) return true
					val (min, max) = configExpression.extraValue<Tuple2<Float, Float?>>() ?: return true
					val value = expression.text.toFloatOrNull() ?: return true
					return min <= value && (max == null || max >= value)
				}
				return false
			}
			CwtDataType.Scalar -> {
				//parameter value -> all no clause-like types are ok
				val propertyConfig = when(config) {
					is CwtPropertyConfig -> config
					is CwtValueConfig -> config.propertyConfig
					else -> null
				}
				if(isParameter(propertyConfig)) {
					return !expression.type.isBlockLikeType()
				}
				
				return when {
					expression.isKey == true -> true //key -> ok
					expression.type == ParadoxDataType.ParameterType -> true //parameter -> ok
					expression.type == ParadoxDataType.IntType -> true //number -> ok according to vanilla game files
					expression.type == ParadoxDataType.FloatType -> true //number -> ok according to vanilla game files
					expression.type.isStringType() -> true //unquoted/quoted string -> ok
					else -> false
				}
			}
			CwtDataType.ColorField -> {
				return expression.type.isColorType() && configExpression.value?.let { expression.text.startsWith(it) } != false
			}
			CwtDataType.PercentageField -> {
				if(!expression.type.isStringType()) return false
				return ParadoxDataType.isPercentageField(expression.text)
			}
			CwtDataType.DateField -> {
				if(!expression.type.isStringType()) return false
				return ParadoxDataType.isDateField(expression.text)
			}
			CwtDataType.Localisation -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return true
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return ParadoxLocalisationSearch.search(expression.text, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataType.SyncedLocalisation -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return true
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return ParadoxSyncedLocalisationSearch.search(expression.text, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataType.InlineLocalisation -> {
				if(!expression.type.isStringType()) return false
				if(expression.quoted) return true //"quoted_string" -> any string
				if(isStatic) return true
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return ParadoxLocalisationSearch.search(expression.text, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataType.StellarisNameFormat -> {
				if(!expression.type.isStringType()) return false
				return true //specific expression
			}
			CwtDataType.AbsoluteFilePath -> {
				if(!expression.type.isStringType()) return false
				return true //总是匹配
			}
			CwtDataType.Definition -> {
				//注意这里可能是一个整数，例如，对于<technology_tier>
				if(!expression.type.isStringType() && expression.type != ParadoxDataType.IntType) return false
				if(isStatic) return true
				if(isParameterAware) return true
				val typeExpression = configExpression.value ?: return false //invalid cwt config
				if(BitUtil.isSet(matchType, CwtConfigMatchType.DEFINITION)) {
					val selector = definitionSelector().gameType(gameType)
					return ParadoxDefinitionSearch.search(expression.text, typeExpression, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataType.Enum -> {
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
				if(isStatic) return true
				//匹配复杂枚举
				val complexEnumConfig = configGroup.complexEnums[enumName]
				if(complexEnumConfig != null) {
					if(BitUtil.isSet(matchType, CwtConfigMatchType.COMPLEX_ENUM_VALUE)) {
						val searchScope = complexEnumConfig.searchScope
						val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, element)
						val search = ParadoxComplexEnumValueSearch.search(name, enumName, project, selector = selector)
						return search.findFirst() != null
					}
				}
				return false
			}
			CwtDataType.Value -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return true
				if(isParameterAware) return true
				return true //任意字符串即可，不需要进一步匹配
			}
			CwtDataType.ValueSet -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return true
				if(isParameterAware) return true
				return true //任意字符串即可，不需要进一步匹配
			}
			CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> {
				if(expression.quoted) return false //不允许用引号括起
				if(isStatic) return true
				if(isParameterAware) return true
				val textRange = TextRange.create(0, expression.text.length)
				val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
				if(scopeFieldExpression == null) return false
				if(isNotExact) return true
				when(configExpression.type) {
					CwtDataType.ScopeField -> {
						return true
					}
					CwtDataType.Scope -> {
						val expectedScope = configExpression.value ?: return true
						val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return true
						val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return true
						val scopeContext = ParadoxScopeHandler.resolveScopeContext(scopeFieldExpression, parentScopeContext)
						if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return true
					}
					CwtDataType.ScopeGroup -> {
						val expectedScopeGroup = configExpression.value ?: return true
						val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return true
						val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return true
						val scopeContext = ParadoxScopeHandler.resolveScopeContext(scopeFieldExpression, parentScopeContext)
						if(ParadoxScopeHandler.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return true
					}
					else -> pass()
				}
				return false
			}
			CwtDataType.ValueField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
				if(isStatic) return true
				if(isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				val textRange = TextRange.create(0, expression.text.length)
				val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
				return valueFieldExpression != null
			}
			CwtDataType.IntValueField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				if(isStatic) return true
				if(isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				val textRange = TextRange.create(0, expression.text.length)
				val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
				return valueFieldExpression != null
			}
			CwtDataType.VariableField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
				if(isStatic) return true
				if(isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				val textRange = TextRange.create(0, expression.text.length)
				val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
				return variableFieldExpression != null
			}
			CwtDataType.IntVariableField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				if(isStatic) return true
				if(isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				val textRange = TextRange.create(0, expression.text.length)
				val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
				return variableFieldExpression != null
			}
			CwtDataType.Modifier -> {
				if(!isStatic && isParameterAware) return true
				//匹配预定义的modifier
				return matchesModifier(element, expression.text, configGroup)
			}
			CwtDataType.SingleAliasRight -> {
				return false //不在这里处理
			}
			CwtDataType.AliasKeysField -> {
				if(!isStatic && isParameterAware) return true
				val aliasName = configExpression.value ?: return false
				return matchesAliasName(element, expression, aliasName, configGroup, matchType)
			}
			CwtDataType.AliasName -> {
				if(!isStatic && isParameterAware) return true
				val aliasName = configExpression.value ?: return false
				return matchesAliasName(element, expression, aliasName, configGroup, matchType)
			}
			CwtDataType.AliasMatchLeft -> {
				return false //不在这里处理
			}
			CwtDataType.TemplateExpression -> {
				if(!expression.type.isStringType()) return false
				//允许用引号括起
				if(isStatic) return true
				if(isParameterAware) return true
				return matchesTemplateExpression(element, expression, configExpression, configGroup)
			}
			CwtDataType.Constant -> {
				val value = configExpression.value
				if(configExpression is CwtValueExpression) {
					//常量的值也可能是yes/no
					val text = expression.text
					if((value == "yes" || value == "no") && text.isLeftQuoted()) return false
				}
				return expression.text.equals(value, true) //忽略大小写
			}
			CwtDataType.Other -> {
				return true
			}
			else -> {
				val pathReferenceExpression = ParadoxPathReferenceExpression.get(configExpression)
				if(pathReferenceExpression != null) {
					if(!expression.type.isStringType()) return false
					if(isStatic) return true
					if(isParameterAware) return true
					if(BitUtil.isSet(matchType, CwtConfigMatchType.FILE_PATH)) {
						val pathReference = expression.text.normalizePath()
						val selector = fileSelector().gameType(gameType)
						return ParadoxFilePathSearch.search(pathReference, project, configExpression, selector = selector).findFirst() != null
					}
					return true
				}
				return false
			}
		}
	}
	
	private fun matchesScriptExpressionInBlock(block: ParadoxScriptBlock, configsInBlock: List<CwtConfig<*>>, configGroup: CwtConfigGroup): Boolean {
		//简单判断：如果block中包含configsInBlock声明的任意propertyKey（作为常量字符串，忽略大小写），则认为匹配
		val propertyKeys = caseInsensitiveStringSet()
		configsInBlock.forEach { 
			if(it is CwtPropertyConfig && it.keyExpression.type == CwtDataType.Constant) {
				propertyKeys.add(it.key)
			} 
		}
		var result = false
		block.processData(conditional = true, inline = true) {
			if(it is ParadoxScriptProperty && it.name in propertyKeys) {
				result = true
			}
			true
		}
		return result
	}
	
	fun matchesAliasName(
		element: PsiElement,
		expression: ParadoxDataExpression,
		aliasName: String,
		configGroup: CwtConfigGroup,
		matchType: Int = CwtConfigMatchType.ALL
	): Boolean {
		val aliasSubName = getAliasSubName(element, expression.text, expression.quoted, aliasName, configGroup, matchType) ?: return false
		val configExpression = CwtKeyExpression.resolve(aliasSubName)
		return matchesScriptExpression(element, expression, configExpression, null, configGroup, matchType)
	}
	
	fun matchesModifier(element: PsiElement, name: String, configGroup: CwtConfigGroup): Boolean {
		return ParadoxModifierHandler.matchesModifier(name, element, configGroup)
	}
	
	fun matchesTemplateExpression(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
		val templateConfigExpression = CwtTemplateExpression.resolve(configExpression.expressionString)
		return templateConfigExpression.matches(expression.text, element, configGroup, matchType)
	}
	
	fun getAliasSubName(element: PsiElement, key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): String? {
		val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
		if(constKey != null) return constKey
		val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
		val expression = ParadoxDataExpression.resolve(key, quoted, true)
		return keys.find {
			matchesScriptExpression(element, expression, CwtKeyExpression.resolve(it), null, configGroup, matchType)
		}
	}
	
	fun requireNotExactMatch(configExpression: CwtDataExpression): Boolean {
		return when {
			configExpression == CwtValueExpression.BlockExpression -> true
			configExpression.type == CwtDataType.Int && configExpression.extraValue != null -> true
			configExpression.type == CwtDataType.Float && configExpression.extraValue != null -> true
			configExpression.type == CwtDataType.ColorField && configExpression.value != null -> true
			configExpression.type == CwtDataType.Scope && configExpression.value != null -> true
			configExpression.type == CwtDataType.ScopeGroup && configExpression.value != null -> true
			else -> false
		}
	}
	
	fun getPriority(configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Int {
		return when(configExpression.type) {
			CwtDataType.Bool -> 100
			CwtDataType.Int -> 90
			CwtDataType.Float -> 90
			CwtDataType.Scalar -> 90
			CwtDataType.ColorField -> 90
			CwtDataType.PercentageField -> 90
			CwtDataType.DateField -> 90
			CwtDataType.Localisation -> 50
			CwtDataType.SyncedLocalisation -> 50
			CwtDataType.InlineLocalisation -> 50
			CwtDataType.StellarisNameFormat -> 50
			CwtDataType.AbsoluteFilePath -> 70
			CwtDataType.Icon -> 70
			CwtDataType.FilePath -> 70
			CwtDataType.FileName -> 70
			CwtDataType.Definition -> 60
			CwtDataType.Enum -> {
				val enumName = configExpression.value ?: return 0 //不期望匹配到
				if(enumName == paramsEnumName) return 10
				if(configGroup.enums.containsKey(enumName)) return 80
				if(configGroup.complexEnums.containsKey(enumName)) return 45
				return 0 //不期望匹配到，规则有误！
			}
			CwtDataType.Value -> 40
			CwtDataType.ValueSet -> 40
			CwtDataType.ScopeField -> 30
			CwtDataType.Scope -> 30
			CwtDataType.ScopeGroup -> 30
			CwtDataType.ValueField -> 30
			CwtDataType.IntValueField -> 30
			CwtDataType.VariableField -> 30
			CwtDataType.IntVariableField -> 30
			CwtDataType.Modifier -> 55 //lower than definition
			CwtDataType.SingleAliasRight -> 0 //不期望匹配到
			CwtDataType.AliasName -> 0 //不期望匹配到
			CwtDataType.AliasKeysField -> 0 //不期望匹配到
			CwtDataType.AliasMatchLeft -> 0 //不期望匹配到
			CwtDataType.TemplateExpression -> 65
			CwtDataType.Constant -> 100
			CwtDataType.Other -> 0 //不期望匹配到
		}
	}
	//endregion
	
	//region Complete Methods
	fun addRootKeyCompletions(definitionElement: ParadoxScriptDefinitionElement, context: ProcessingContext, result: CompletionResultSet) {
		val originalFile = context.originalFile
		val project = originalFile.project
		val gameType = selectGameType(originalFile) ?: return
		val configGroup = getCwtConfig(project).getValue(gameType)
		val elementPath = ParadoxElementPathHandler.getFromFile(definitionElement, PlsConstants.maxDefinitionDepth) ?: return
		
		context.put(PlsCompletionKeys.isKeyKey, true)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		
		completeRootKey(context, result, elementPath)
	}
	
	fun addKeyCompletions(definitionElement: ParadoxScriptDefinitionElement, context: ProcessingContext, result: CompletionResultSet) {
		val definitionMemberInfo = definitionElement.definitionMemberInfo
		if(definitionMemberInfo == null || definitionMemberInfo.elementPath.isEmpty()) {
			//仅提示不在定义声明中的rootKey
			addRootKeyCompletions(definitionElement, context, result)
		}
		if(definitionMemberInfo == null) return
		val configGroup = definitionMemberInfo.configGroup
		val configs = definitionMemberInfo.getChildPropertyConfigs()
		if(configs.isEmpty()) return
		
		context.put(PlsCompletionKeys.isKeyKey, true)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		context.put(PlsCompletionKeys.scopeContextKey, ParadoxScopeHandler.getScopeContext(definitionElement))
		
		configs.groupBy { it.key }.forEach { (_, configsWithSameKey) ->
			for(config in configsWithSameKey) {
				if(shouldComplete(config, definitionMemberInfo)) {
					context.put(PlsCompletionKeys.configKey, config)
					context.put(PlsCompletionKeys.configsKey, configsWithSameKey)
					completeScriptExpression(context, result)
				}
			}
		}
		
		context.put(PlsCompletionKeys.configKey, null)
		context.put(PlsCompletionKeys.configsKey, null)
		return
	}
	
	fun addValueCompletions(definitionElement: ParadoxScriptDefinitionElement, context: ProcessingContext, result: CompletionResultSet) {
		val definitionMemberInfo = definitionElement.definitionMemberInfo
		if(definitionMemberInfo == null) return
		val configGroup = definitionMemberInfo.configGroup
		val configs = definitionMemberInfo.getConfigs()
		if(configs.isEmpty()) return
		
		context.put(PlsCompletionKeys.isKeyKey, false)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		context.put(PlsCompletionKeys.scopeContextKey, ParadoxScopeHandler.getScopeContext(definitionElement))
		
		for(config in configs) {
			if(config is CwtPropertyConfig) {
				val valueConfig = config.valueConfig ?: continue
				context.put(PlsCompletionKeys.configKey, valueConfig)
				completeScriptExpression(context, result)
			}
		}
		
		context.put(PlsCompletionKeys.configKey, null)
		return
	}
	
	fun addValueCompletionsInBlock(blockElement: ParadoxScriptBlock, context: ProcessingContext, result: CompletionResultSet) {
		val definitionMemberInfo = blockElement.definitionMemberInfo
		if(definitionMemberInfo == null) return
		val configGroup = definitionMemberInfo.configGroup
		val configs = definitionMemberInfo.getChildValueConfigs()
		if(configs.isEmpty()) return
		
		context.put(PlsCompletionKeys.isKeyKey, false)
		context.put(PlsCompletionKeys.configGroupKey, configGroup)
		
		for(config in configs) {
			if(shouldComplete(config, definitionMemberInfo)) {
				context.put(PlsCompletionKeys.configKey, config)
				completeScriptExpression(context, result)
			}
		}
		
		context.put(PlsCompletionKeys.configKey, null)
		return
	}
	
	private fun shouldComplete(config: CwtPropertyConfig, definitionMemberInfo: ParadoxDefinitionMemberInfo): Boolean {
		val expression = config.keyExpression
		//如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
		if(expression.type == CwtDataType.AliasName) return true
		val actualCount = definitionMemberInfo.childPropertyOccurrenceMap[expression]?.actual ?: 0
		//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
		//如果上限是动态的值（如，基于define的值），也不作限制
		val cardinality = config.cardinality
		val maxCount = when {
			cardinality == null -> if(expression.type == CwtDataType.Constant) 1 else null
			config.cardinalityMaxDefine != null -> null
			else -> cardinality.max
		}
		return maxCount == null || actualCount < maxCount
	}
	
	private fun shouldComplete(config: CwtValueConfig, definitionMemberInfo: ParadoxDefinitionMemberInfo): Boolean {
		val expression = config.valueExpression
		val actualCount = definitionMemberInfo.childValueOccurrenceMap[expression]?.actual ?: 0
		//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
		//如果上限是动态的值（如，基于define的值），也不作限制
		val cardinality = config.cardinality
		val maxCount = when {
			cardinality == null -> if(expression.type == CwtDataType.Constant) 1 else null
			config.cardinalityMaxDefine != null -> null
			else -> cardinality.max
		}
		return maxCount == null || actualCount < maxCount
	}
	
	fun completeRootKey(context: ProcessingContext, result: CompletionResultSet, elementPath: ParadoxElementPath) {
		val fileInfo = context.originalFile.fileInfo ?: return
		val configGroup = context.configGroup
		val path = fileInfo.entryPath //这里使用entryPath
		val infoMap = mutableMapOf<String, MutableList<Tuple2<CwtTypeConfig, CwtSubtypeConfig?>>>()
		for(typeConfig in configGroup.types.values) {
			if(ParadoxDefinitionHandler.matchesTypeWithUnknownDeclaration(typeConfig, path, null, null)) {
				val skipRootKeyConfig = typeConfig.skipRootKey
				if(skipRootKeyConfig == null || skipRootKeyConfig.isEmpty()) {
					if(elementPath.isEmpty()) {
						typeConfig.typeKeyFilter?.takeIf { it.notReversed }?.forEach {
							infoMap.getOrPut(it) { SmartList() }.add(typeConfig to null)
						}
						typeConfig.subtypes.values.forEach { subtypeConfig ->
							subtypeConfig.typeKeyFilter?.takeIf { it.notReversed }?.forEach {
								infoMap.getOrPut(it) { SmartList() }.add(typeConfig to subtypeConfig)
							}
						}
					}
				} else {
					for(skipConfig in skipRootKeyConfig) {
						val relative = elementPath.relativeTo(skipConfig) ?: continue
						if(relative.isEmpty()) {
							typeConfig.typeKeyFilter?.takeIf { it.notReversed }?.forEach {
								infoMap.getOrPut(it) { SmartList() }.add(typeConfig to null)
							}
							typeConfig.subtypes.values.forEach { subtypeConfig ->
								subtypeConfig.typeKeyFilter?.takeIf { it.notReversed }?.forEach {
									infoMap.getOrPut(it) { SmartList() }.add(typeConfig to subtypeConfig)
								}
							}
						} else {
							infoMap.getOrPut(relative) { SmartList() }
						}
						break
					}
				}
			}
		}
		for((key, tuples) in infoMap) {
			if(key == "any") return //skip any wildcard
			val typeConfigToUse = tuples.map { it.first }.distinctBy { it.name }.singleOrNull()
			val typeToUse = typeConfigToUse?.name
			//需要考虑不指定子类型的情况
			val subtypesToUse = when {
				typeConfigToUse == null || tuples.isEmpty() -> null
				else -> tuples.mapNotNull { it.second }.ifEmpty { null }?.distinctBy { it.name }?.map { it.name }
			}
			val config = if(typeToUse == null) null else configGroup.declarations[typeToUse]?.getMergedConfig(subtypesToUse, null)
			val element = config?.pointer?.element
			val icon = if(config != null) PlsIcons.Definition else PlsIcons.Property
			val tailText = if(tuples.isEmpty()) null
			else tuples.joinToString(", ", " for ") { (typeConfig, subTypeConfig) ->
				if(subTypeConfig != null) "${typeConfig.name}.${subTypeConfig.name}" else typeConfig.name
			}
			val typeFile = config?.pointer?.containingFile
			context.put(PlsCompletionKeys.configKey, config)
			val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, key)
				.withIcon(icon)
				.withTailText(tailText)
				.withTypeText(typeFile?.name)
				.withTypeIcon(typeFile?.icon)
				.withForceInsertCurlyBraces(tuples.isEmpty())
				.bold()
				.caseInsensitive()
				.withPriority(PlsCompletionPriorities.rootKeyPriority)
			result.addScriptExpressionElement(context, builder)
			context.put(PlsCompletionKeys.configKey, null)
		}
	}
	
	fun completeScriptExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val configExpression = config.expression ?: return@with
		val config = config
		val configGroup = configGroup
		val project = configGroup.project
		val gameType = configGroup.gameType
		
		if(configExpression.isEmpty()) return
		if(quoted != true && keyword.isParameterAwareExpression()) return //排除带参数的情况
		
		//匹配作用域
		val scopeContext = scopeContext
		val scopeMatched = when {
			scopeContext == null -> true
			config is CwtPropertyConfig -> ParadoxScopeHandler.matchesScope(scopeContext, config.supportedScopes, configGroup)
			config is CwtAliasConfig -> ParadoxScopeHandler.matchesScope(scopeContext, config.supportedScopes, configGroup)
			config is CwtLinkConfig -> ParadoxScopeHandler.matchesScope(scopeContext, config.inputScopes, configGroup)
			else -> true
		}
		if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) return
		put(PlsCompletionKeys.scopeMatchedKey, scopeMatched)
		
		if(configExpression == CwtValueExpression.BlockExpression) {
			result.addBlockElement(context)
			return
		}
		
		when(configExpression.type) {
			CwtDataType.Bool -> {
				result.addExpressionElement(context, PlsLookupElements.yesLookupElement)
				result.addExpressionElement(context, PlsLookupElements.noLookupElement)
			}
			CwtDataType.Localisation -> {
				val tailText = getScriptExpressionTailText(config)
				//这里selector不需要指定去重
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				ParadoxLocalisationSearch.processVariants(project, selector = selector) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val builder = ParadoxScriptExpressionLookupElementBuilder.create(localisation, name)
						.withIcon(PlsIcons.Localisation)
						.withTailText(tailText)
						.withTypeText(typeFile.name)
						.withTypeIcon(typeFile.icon)
					result.addScriptExpressionElement(context, builder)
					true
				}
			}
			CwtDataType.SyncedLocalisation -> {
				val tailText = getScriptExpressionTailText(config)
				//这里selector不需要指定去重
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				ParadoxSyncedLocalisationSearch.processVariants(project, selector = selector) { syncedLocalisation ->
					val name = syncedLocalisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = syncedLocalisation.containingFile
					val builder = ParadoxScriptExpressionLookupElementBuilder.create(syncedLocalisation, name)
						.withIcon(PlsIcons.Localisation)
						.withTailText(tailText)
						.withTypeText(typeFile.name)
						.withTypeIcon(typeFile.icon)
					result.addScriptExpressionElement(context, builder)
					true
				}
			}
			CwtDataType.InlineLocalisation -> {
				if(quoted) return
				val tailText = getScriptExpressionTailText(config)
				//这里selector不需要指定去重
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				ParadoxLocalisationSearch.processVariants(project, selector = selector) { localisation ->
					val name = localisation.name //=localisation.paradoxLocalisationInfo?.name
					val typeFile = localisation.containingFile
					val builder = ParadoxScriptExpressionLookupElementBuilder.create(localisation, name)
						.withIcon(PlsIcons.Localisation)
						.withTailText(tailText)
						.withTypeText(typeFile.name)
						.withTypeIcon(typeFile.icon)
					result.addScriptExpressionElement(context, builder)
					true
				}
			}
			CwtDataType.AbsoluteFilePath -> pass() //不提示绝对路径
			CwtDataType.Definition -> {
				val typeExpression = configExpression.value ?: return
				val tailText = getScriptExpressionTailText(config)
				val selector = definitionSelector().gameType(gameType).preferRootFrom(contextElement).distinctByName()
				ParadoxDefinitionSearch.search(typeExpression, project, selector = selector)
					.processQuery { definition ->
						val name = definition.definitionInfo?.name ?: return@processQuery true
						val typeFile = definition.containingFile
						val builder = ParadoxScriptExpressionLookupElementBuilder.create(definition, name)
							.withIcon(PlsIcons.Definition)
							.withTailText(tailText)
							.withTypeText(typeFile.name)
							.withTypeIcon(typeFile.icon)
						result.addScriptExpressionElement(context, builder)
						true
				}
			}
			CwtDataType.Enum -> {
				val enumName = configExpression.value ?: return
				//提示参数名（仅限key）
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					ProgressManager.checkCanceled()
					val invocationExpressionElement = contextElement.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return
					val invocationExpressionConfig = config.parent as? CwtPropertyConfig ?: return
					completeParametersForInvocationExpression(invocationExpressionElement, invocationExpressionConfig, context, result)
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
						val element = enumValueConfig.pointer.element ?: continue
						val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
							.withIcon(PlsIcons.EnumValue)
							.withTailText(tailText)
							.withTypeText(typeFile?.name)
							.withTypeIcon(typeFile?.icon)
							.caseInsensitive()
							.withScopeMatched(scopeMatched)
							.withPriority(PlsCompletionPriorities.enumPriority)
						result.addScriptExpressionElement(context, builder)
					}
				}
				//提示复杂枚举
				val complexEnumConfig = configGroup.complexEnums[enumName]
				if(complexEnumConfig != null) {
					ProgressManager.checkCanceled()
					val typeFile = complexEnumConfig.pointer.containingFile
					val searchScope = complexEnumConfig.searchScope
					val selector = complexEnumValueSelector().gameType(gameType).withSearchScope(searchScope, contextElement).preferRootFrom(contextElement).distinctByName()
					
					ParadoxComplexEnumValueSearch.searchAll(enumName, project, selector = selector)
						.processQuery { complexEnum ->
							val name = complexEnum.value
							val builder = ParadoxScriptExpressionLookupElementBuilder.create(complexEnum, name)
								.withIcon(PlsIcons.ComplexEnumValue)
								.withTailText(tailText)
								.withTypeText(typeFile?.name)
								.withTypeIcon(typeFile?.icon)
							result.addScriptExpressionElement(context, builder)
							true
						}
				}
			}
			CwtDataType.Value, CwtDataType.ValueSet -> {
				//not key/value or quoted -> only value set value name, no scope info
				if(config !is CwtDataConfig<*> || quoted) {
					completeValueSetValue(context, result)
					return
				}
				completeValueSetValueExpression(context, result)
			}
			CwtDataType.ScopeField -> {
				completeScopeFieldExpression(context, result)
			}
			CwtDataType.Scope -> {
				put(PlsCompletionKeys.scopeNameKey, configExpression.value)
				completeScopeFieldExpression(context, result)
				put(PlsCompletionKeys.scopeNameKey, null)
			}
			CwtDataType.ScopeGroup -> {
				put(PlsCompletionKeys.scopeGroupNameKey, configExpression.value)
				completeScopeFieldExpression(context, result)
				put(PlsCompletionKeys.scopeGroupNameKey, null)
			}
			CwtDataType.ValueField -> {
				completeValueFieldExpression(context, result)
			}
			CwtDataType.IntValueField -> {
				put(PlsCompletionKeys.isIntKey, true)
				completeValueFieldExpression(context, result)
				put(PlsCompletionKeys.isIntKey, null)
			}
			CwtDataType.VariableField -> {
				completeVariableFieldExpression(context, result)
			}
			CwtDataType.IntVariableField -> {
				put(PlsCompletionKeys.isIntKey, true)
				completeVariableFieldExpression(context, result)
				put(PlsCompletionKeys.isIntKey, null)
			}
			CwtDataType.Modifier -> {
				//提示预定义的modifier
				completeModifier(context, result)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.SingleAliasRight -> pass()
			CwtDataType.AliasKeysField -> {
				val aliasName = configExpression.value ?: return
				completeAliasName(aliasName, context, result)
			}
			CwtDataType.AliasName -> {
				val aliasName = configExpression.value ?: return
				completeAliasName(aliasName, context, result)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.AliasMatchLeft -> pass()
			CwtDataType.TemplateExpression -> {
				completeTemplateExpression(context, result)
			}
			CwtDataType.Constant -> {
				val icon = when(configExpression) {
					is CwtKeyExpression -> PlsIcons.Property
					is CwtValueExpression -> PlsIcons.Value
				}
				val name = configExpression.value ?: return
				if(configExpression is CwtValueExpression) {
					//常量的值也可能是yes/no
					if(name == "yes") {
						if(quoted) return
						result.addExpressionElement(context, PlsLookupElements.yesLookupElement)
						return
					}
					if(name == "no") {
						if(quoted) return
						result.addExpressionElement(context, PlsLookupElements.noLookupElement)
						return
					}
				}
				val element = config.resolved().pointer.element ?: return
				val typeFile = config.resolved().pointer.containingFile
				val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
					.withIcon(icon)
					.withTypeText(typeFile?.name)
					.withTypeIcon(typeFile?.icon)
					.caseInsensitive()
					.withScopeMatched(scopeMatched)
					.withPriority(PlsCompletionPriorities.constantPriority)
				result.addScriptExpressionElement(context, builder)
			}
			else -> {
				val pathReferenceExpression = ParadoxPathReferenceExpression.get(configExpression)
				if(pathReferenceExpression != null) {
					val tailText = getScriptExpressionTailText(config)
					val fileExtensions = when(config) {
						is CwtDataConfig<*> -> ParadoxFilePathHandler.getFileExtensionOptionValues(config)
						else -> emptySet()
					}
					//仅提示匹配file_extensions选项指定的扩展名的，如果存在
					val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement)
						.withFileExtensions(fileExtensions)
						.distinctByFilePath()
					ParadoxFilePathSearch.search(project, configExpression, selector = selector)
						.processQuery p@{ virtualFile ->
							val file = virtualFile.toPsiFile<PsiFile>(project) ?: return@p true
							val filePath = virtualFile.fileInfo?.path?.path ?: return@p true
							val name = pathReferenceExpression.extract(configExpression, filePath) ?: return@p true
							val builder = ParadoxScriptExpressionLookupElementBuilder.create(file, name)
								.withIcon(PlsIcons.PathReference)
								.withTailText(tailText)
								.withTypeText(file.name)
								.withTypeIcon(file.icon)
							result.addScriptExpressionElement(context, builder)
							true
						}
				}
			}
		}
		
		put(PlsCompletionKeys.scopeContextKey, scopeContext)
		put(PlsCompletionKeys.scopeMatchedKey, null)
	}
	
	fun completeAliasName(aliasName: String, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val config = config
		val configs = configs
		
		val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
		for(aliasConfigs in aliasGroup.values) {
			//aliasConfigs的名字是相同的 
			val aliasConfig = aliasConfigs.firstOrNull() ?: continue
			
			//aliasSubName是一个表达式
			if(isKey == true) {
				context.put(PlsCompletionKeys.configKey, aliasConfig)
				context.put(PlsCompletionKeys.configsKey, aliasConfigs)
				completeScriptExpression(context, result)
			} else {
				context.put(PlsCompletionKeys.configKey, aliasConfig)
				completeScriptExpression(context, result)
			}
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.configsKey, configs)
		}
	}
	
	fun completeModifier(context: ProcessingContext, result: CompletionResultSet) {
		return ParadoxModifierHandler.completeModifier(context, result)
	}
	
	fun completeTemplateExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val element = contextElement
		if(element !is ParadoxScriptStringExpressionElement) return
		val configExpression = context.config.expression ?: return
		val template = CwtTemplateExpression.resolve(configExpression.expressionString)
		val scopeMatched = context.scopeMatched ?: true
		val tailText = getScriptExpressionTailText(context.config)
		template.processResolveResult(contextElement, configGroup) { expression ->
			val templateExpressionElement = resolveTemplateExpression(element, expression, configExpression, configGroup)
			val builder = ParadoxScriptExpressionLookupElementBuilder.create(templateExpressionElement, expression)
				.withIcon(PlsIcons.TemplateExpression)
				.withTailText(tailText)
				.caseInsensitive()
				.withScopeMatched(scopeMatched)
			result.addScriptExpressionElement(context, builder)
			true
		}
	}
	
	fun completeScopeFieldExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//基于当前位置的代码补全
		if(quoted) return
		val textRange = TextRange.create(0, keyword.length)
		val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(keyword, textRange, configGroup, isKey, true) ?: return
		//合法的表达式需要匹配scopeName或者scopeGroupName，来自scope[xxx]或者scope_group[xxx]中的xxx，目前不基于此进行过滤
		scopeFieldExpression.complete(context, result)
	}
	
	fun completeValueFieldExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//基于当前位置的代码补全
		if(quoted) return
		val textRange = TextRange.create(0, keyword.length)
		val valueFieldExpression = ParadoxValueFieldExpression.resolve(keyword, textRange, configGroup, isKey, true) ?: return
		valueFieldExpression.complete(context, result)
	}
	
	fun completeVariableFieldExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//基于当前位置的代码补全
		if(quoted) return
		val textRange = TextRange.create(0, keyword.length)
		val variableFieldExpression = ParadoxVariableFieldExpression.resolve(keyword, textRange, configGroup, isKey, true) ?: return
		variableFieldExpression.complete(context, result)
	}
	
	fun completeValueSetValueExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//基于当前位置的代码补全
		if(quoted) return
		val textRange = TextRange.create(0, keyword.length)
		val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(keyword, textRange, config, configGroup, isKey, true) ?: return
		valueSetValueExpression.complete(context, result)
	}
	
	fun completeSystemScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//总是提示，无论作用域是否匹配
		val systemLinkConfigs = configGroup.systemLinks
		for(systemLinkConfig in systemLinkConfigs.values) {
			val name = systemLinkConfig.id
			val element = systemLinkConfig.pointer.element ?: continue
			val tailText = " from system scopes"
			val typeFile = systemLinkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.SystemScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.systemLinkPriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = scopeContext
		
		val linkConfigs = configGroup.linksAsScopeNotData
		for(scope in linkConfigs.values) {
			val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, scope.inputScopes, configGroup)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val name = scope.name
			val element = scope.pointer.element ?: continue
			val tailText = " from scopes"
			val typeFile = scope.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.Scope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withScopeMatched(scopeMatched)
				.withPriority(PlsCompletionPriorities.scopePriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeScopeLinkPrefix(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = scopeContext
		
		val linkConfigs = configGroup.linksAsScopeWithPrefix
		for(linkConfig in linkConfigs.values) {
			val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val name = linkConfig.prefix ?: continue
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from scope link ${linkConfig.name}"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.ScopeLinkPrefix)
				.withBoldness(true)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withScopeMatched(scopeMatched)
				.withPriority(PlsCompletionPriorities.scopeLinkPrefixPriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeScopeLinkDataSource(context: ProcessingContext, result: CompletionResultSet, prefix: String?, dataSourceNodeToCheck: ParadoxExpressionNode?): Unit = with(context) {
		val config = config
		val configs = configs
		val scopeContext = scopeContext
		
		val linkConfigs = when {
			prefix == null -> configGroup.linksAsScopeWithoutPrefix.values
			else -> configGroup.linksAsScopeWithPrefix.values.filter { prefix == it.prefix }
		}
		
		if(dataSourceNodeToCheck is ParadoxScopeExpressionNode) {
			completeForScopeExpressionNode(dataSourceNodeToCheck, context, result)
			context.put(PlsCompletionKeys.scopeContextKey, scopeContext)
			return@with
		}
		if(dataSourceNodeToCheck is ParadoxValueSetValueExpression) {
			context.put(PlsCompletionKeys.configKey, dataSourceNodeToCheck.configs.first())
			context.put(PlsCompletionKeys.configsKey, dataSourceNodeToCheck.configs)
			context.put(PlsCompletionKeys.scopeContextKey, null) //don't check now
			dataSourceNodeToCheck.complete(context, result)
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.configsKey, configs)
			context.put(PlsCompletionKeys.scopeContextKey, scopeContext)
			return@with
		}
		
		context.put(PlsCompletionKeys.configsKey, linkConfigs)
		for(linkConfig in linkConfigs) {
			context.put(PlsCompletionKeys.configKey, linkConfig)
			completeScriptExpression(context, result)
		}
		context.put(PlsCompletionKeys.configKey, config)
		context.put(PlsCompletionKeys.configsKey, configs)
		context.put(PlsCompletionKeys.scopeMatchedKey, null)
	}
	
	fun completeValueLinkValue(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = scopeContext
		
		val linkConfigs = configGroup.linksAsValueNotData
		for(linkConfig in linkConfigs.values) {
			//排除input_scopes不匹配前一个scope的output_scope的情况
			val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val name = linkConfig.name
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from values"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.ValueLinkValue)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withScopeMatched(scopeMatched)
				.withPriority(PlsCompletionPriorities.valueLinkValuePriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeValueLinkPrefix(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = scopeContext
		
		val linkConfigs = configGroup.linksAsValueWithPrefix
		for(linkConfig in linkConfigs.values) {
			val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, linkConfig.inputScopes, configGroup)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val name = linkConfig.prefix ?: continue
			val element = linkConfig.pointer.element ?: continue
			val tailText = " from value link ${linkConfig.name}"
			val typeFile = linkConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.ValueLinkPrefix)
				.withBoldness(true)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withPriority(PlsCompletionPriorities.scopeLinkPrefixPriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeValueLinkDataSource(context: ProcessingContext, result: CompletionResultSet, prefix: String?, dataSourceNodeToCheck: ParadoxExpressionNode?, variableOnly: Boolean = false): Unit = with(context) {
		val config = config
		val configs = configs
		val scopeContext = scopeContext
		
		val linkConfigs = when {
			prefix == null -> configGroup.linksAsScopeWithoutPrefix.values
			else -> configGroup.linksAsScopeWithPrefix.values.filter { prefix == it.prefix }
		}
		
		if(dataSourceNodeToCheck is ParadoxValueSetValueExpression) {
			context.put(PlsCompletionKeys.configKey, dataSourceNodeToCheck.configs.first())
			context.put(PlsCompletionKeys.configsKey, dataSourceNodeToCheck.configs)
			context.put(PlsCompletionKeys.scopeContextKey, null) //don't check now
			dataSourceNodeToCheck.complete(context, result)
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.configsKey, configs)
			context.put(PlsCompletionKeys.scopeContextKey, scopeContext)
			return@with
		}
		if(dataSourceNodeToCheck is ParadoxScriptValueExpression) {
			context.put(PlsCompletionKeys.configKey, dataSourceNodeToCheck.config)
			context.put(PlsCompletionKeys.configsKey, null)
			context.put(PlsCompletionKeys.scopeContextKey, null) //don't check now
			dataSourceNodeToCheck.complete(context, result)
			context.put(PlsCompletionKeys.configKey, config)
			context.put(PlsCompletionKeys.configsKey, configs)
			context.put(PlsCompletionKeys.scopeContextKey, scopeContext)
			return@with
		}
		
		context.put(PlsCompletionKeys.configsKey, linkConfigs)
		for(linkConfig in linkConfigs) {
			context.put(PlsCompletionKeys.configKey, linkConfig)
			completeScriptExpression(context, result)
		}
		context.put(PlsCompletionKeys.configKey, config)
		context.put(PlsCompletionKeys.configsKey, configs)
		context.put(PlsCompletionKeys.scopeMatchedKey, null)
	}
	
	fun completeValueSetValue(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
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
		//提示预定义的value
		run {
			ProgressManager.checkCanceled()
			if(configExpression.type == CwtDataType.Value) {
				completePredefinedValueSetValue(valueSetName, result, context)
			}
		}
		//提示来自脚本文件的value
		run {
			ProgressManager.checkCanceled()
			val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name.orAnonymous()}"
			val contextElement = contextElement
			val selector = valueSetValueSelector().gameType(gameType)
				.notSamePosition(contextElement)
				.distinctByValue()
			val valueSetValueQuery = ParadoxValueSetValueSearch.search(valueSetName, project, selector = selector)
			valueSetValueQuery.processQuery { valueSetValue ->
				//去除后面的作用域信息
				val name = ParadoxValueSetValueHandler.getName(valueSetValue) ?: return@processQuery true
				val icon = PlsIcons.ValueSetValue(valueSetName)
				//不显示typeText
				val builder = ParadoxScriptExpressionLookupElementBuilder.create(valueSetValue, name)
					.withIcon(icon)
					.withTailText(tailText)
				result.addScriptExpressionElement(context, builder)
				true
			}
		}
	}
	
	fun completePredefinedValueSetValue(valueSetName: String, result: CompletionResultSet, context: ProcessingContext) = with(context) {
		val configExpression = config.expression ?: return@with
		val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name.orAnonymous()}"
		val valueConfig = configGroup.values[valueSetName] ?: return
		val valueSetValueConfigs = valueConfig.valueConfigMap.values
		if(valueSetValueConfigs.isEmpty()) return
		for(valueSetValueConfig in valueSetValueConfigs) {
			val name = valueSetValueConfig.value
			val element = valueSetValueConfig.pointer.element ?: continue
			val typeFile = valueConfig.pointer.containingFile
			val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.PredefinedValueSetValue)
				.withTailText(tailText)
				.withTypeText(typeFile?.name)
				.withTypeIcon(typeFile?.icon)
				.withPriority(PlsCompletionPriorities.predefinedValueSetValuePriority)
			result.addScriptExpressionElement(context, builder)
		}
	}
	
	fun completePredefinedLocalisationScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = context.scopeContext
		
		val localisationLinks = configGroup.localisationLinks
		for(localisationScope in localisationLinks.values) {
			val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, localisationScope.inputScopes, configGroup)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val name = localisationScope.name
			val element = localisationScope.pointer.element ?: continue
			val tailText = " from localisation scopes"
			val typeFile = localisationScope.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.LocalisationCommandScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withScopeMatched(scopeMatched)
				.withPriority(PlsCompletionPriorities.scopePriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completePredefinedLocalisationCommand(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = context.scopeContext
		
		val localisationCommands = configGroup.localisationCommands
		for(localisationCommand in localisationCommands.values) {
			val scopeMatched = ParadoxScopeHandler.matchesScope(scopeContext, localisationCommand.supportedScopes, configGroup)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val name = localisationCommand.name
			val element = localisationCommand.pointer.element ?: continue
			val tailText = " from localisation commands"
			val typeFile = localisationCommand.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.LocalisationCommandField)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withScopeMatched(scopeMatched)
				.withPriority(PlsCompletionPriorities.localisationCommandPriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeEventTarget(file: PsiFile, result: CompletionResultSet) {
		val project = file.project
		val eventTargetSelector = valueSetValueSelector().gameTypeFrom(file).preferRootFrom(file).distinctByValue()
		val eventTargetQuery = ParadoxValueSetValueSearch.search("event_target", project, selector = eventTargetSelector)
		eventTargetQuery.processQuery { eventTarget ->
			val value = ParadoxValueSetValueHandler.getName(eventTarget.value) ?: return@processQuery true
			val icon = PlsIcons.ValueSetValue
			val tailText = " from value[event_target]"
			val lookupElement = LookupElementBuilder.create(eventTarget, value)
				.withIcon(icon)
				.withTailText(tailText, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
		
		val globalEventTargetSelector = valueSetValueSelector().gameTypeFrom(file).preferRootFrom(file).distinctByValue()
		val globalEventTargetQuery = ParadoxValueSetValueSearch.search("global_event_target", project, selector = globalEventTargetSelector)
		globalEventTargetQuery.processQuery { globalEventTarget ->
			val value = ParadoxValueSetValueHandler.getName(globalEventTarget) ?: return@processQuery true
			val icon = PlsIcons.ValueSetValue
			val tailText = " from value[global_event_target]"
			val lookupElement = LookupElementBuilder.create(globalEventTarget, value)
				.withIcon(icon)
				.withTailText(tailText, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
	}
	
	fun completeScriptedLoc(file: PsiFile, result: CompletionResultSet) {
		val project = file.project
		val scriptedLocSelector = definitionSelector().gameTypeFrom(file).preferRootFrom(file).distinctByName()
		val scriptedLocQuery = ParadoxDefinitionSearch.search("scripted_loc", project, selector = scriptedLocSelector)
		scriptedLocQuery.processQuery { scriptedLoc ->
			val name = scriptedLoc.definitionInfo?.name ?: return@processQuery true //不应该为空
			val icon = PlsIcons.Definition
			val tailText = " from <scripted_loc>"
			val typeFile = scriptedLoc.containingFile
			val lookupElement = LookupElementBuilder.create(scriptedLoc, name).withIcon(icon)
				.withTailText(tailText, true)
				.withTypeText(typeFile.name, typeFile.icon, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
	}
	
	fun completeVariable(context: ProcessingContext, result: CompletionResultSet) {
		val file = context.originalFile.project
		val project = file
		val variableSelector = valueSetValueSelector().gameTypeFrom(file).preferRootFrom(file).distinctByValue()
		val variableQuery = ParadoxValueSetValueSearch.search("variable", project, selector = variableSelector)
		variableQuery.processQuery { variable ->
			val value = ParadoxValueSetValueHandler.getName(variable) ?: return@processQuery true
			val icon = PlsIcons.Variable
			val tailText = " from variables"
			val lookupElement = LookupElementBuilder.create(variable, value)
				.withIcon(icon)
				.withTailText(tailText, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
	}
	
	fun completeParameters(element: PsiElement, read: Boolean, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		//向上找到参数上下文
		val file = originalFile
		val parameterContext =  ParadoxParameterSupport.findContext(element, file) ?: return
		val parameterMap = parameterContext.parameters
		if(parameterMap.isEmpty()) return
		for((parameterName, parameterInfo) in parameterMap) {
			ProgressManager.checkCanceled()
			val parameter = parameterInfo.pointers.firstNotNullOfOrNull { it.element } ?: continue
			//排除当前正在输入的那个
			if(parameterInfo.pointers.size == 1 && element isSamePosition parameter) continue
            val parameterElement = ParadoxParameterSupport.resolveParameterWithContext(parameterName, element, parameterContext)
				?: continue
			val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
				.withIcon(PlsIcons.Parameter)
				.withTypeText(parameterElement.contextName, parameterContext.icon, true)
			result.addElement(lookupElement)
		}
	}
	
	fun completeParametersForInvocationExpression(invocationExpressionElement: ParadoxScriptProperty, invocationExpressionConfig: CwtPropertyConfig, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		if(quoted) return //输入参数不允许用引号括起
		val contextElement = context.contextElement
		val block = invocationExpressionElement.block ?: return
		val existParameterNames = mutableSetOf<String>()
		block.processProperty() {
			val propertyKey = it.propertyKey	
			val name = if(contextElement == propertyKey) propertyKey.getKeyword(context.offsetInParent) else propertyKey.name
			existParameterNames.add(name)
			true
		}
		val namesToDistinct = mutableSetOf<String>()
		
		//整合查找到的所有参数上下文
		val insertSeparator = contextElement !is ParadoxScriptPropertyKey
		ParadoxParameterSupport.processContextFromInvocationExpression(invocationExpressionElement, invocationExpressionConfig) p@{ parameterContext ->
			ProgressManager.checkCanceled()
			val parameterMap = parameterContext.parameters
			if(parameterMap.isEmpty()) return@p true
			for((parameterName, _) in parameterMap) {
				//排除已输入的
				if(parameterName in existParameterNames) continue
				if(!namesToDistinct.add(parameterName)) continue
				
				val parameterElement = ParadoxParameterSupport.resolveParameterWithContext(parameterName, contextElement, parameterContext)
					?: continue
				val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
					.withIcon(PlsIcons.Parameter)
					.withTypeText(parameterElement.contextName, parameterContext.icon, true)
					.letIf(insertSeparator) {
						it.withInsertHandler { c, _ ->
							val editor = c.editor
							val customSettings = CodeStyle.getCustomSettings(c.file, ParadoxScriptCodeStyleSettings::class.java)
							val text = if(customSettings.SPACE_AROUND_PROPERTY_SEPARATOR) " = " else "="
							EditorModificationUtil.insertStringAtCaret(editor, text, false, true)
						}
					}
				result.addElement(lookupElement)
			}
			true
		}
	}
	
	fun completeParametersForScriptValueExpression(svName: String, parameterNames: Set<String>, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val existParameterNames = mutableSetOf<String>()
		existParameterNames.addAll(parameterNames)
		val namesToDistinct = mutableSetOf<String>()
		
		//整合查找到的所有SV
		val selector = definitionSelector().gameType(configGroup.gameType).preferRootFrom(contextElement)
		ParadoxDefinitionSearch.search(svName, "script_value", configGroup.project, selector = selector).processQuery p@{ sv ->
			ProgressManager.checkCanceled()
			val parameterContext = sv
			val parameterMap = parameterContext.parameters
			if(parameterMap.isEmpty()) return@p true
			for((parameterName, _) in parameterMap) {
				//排除已输入的
				if(parameterName in existParameterNames) continue
				if(!namesToDistinct.add(parameterName)) continue
				
				val parameterElement = ParadoxParameterSupport.resolveParameterWithContext(parameterName, contextElement, parameterContext)
					?: continue
				val lookupElement = LookupElementBuilder.create(parameterElement, parameterName)
					.withIcon(PlsIcons.Parameter)
					.withTypeText(parameterElement.contextName, parameterContext.icon, true)
				result.addElement(lookupElement)
			}
			true
		}
	}
	
	fun getScriptExpressionTailText(config: CwtConfig<*>?, withExpression: Boolean = true): String? {
		if(config?.expression == null) return null
		if(withExpression) {
			return " by ${config.expression} in ${config.resolved().pointer.containingFile?.name.orAnonymous()}"
		} else {
			return " in ${config.resolved().pointer.containingFile?.name.orAnonymous()}"
		}
	}
	//endregion
	
	//region Resolve Methods
	/**
	 * @param element 需要解析的PSI元素。
	 * @param rangeInElement 需要解析的文本在需要解析的PSI元素对应的整个文本中的位置。
	 */
	fun resolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>?, configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
		ProgressManager.checkCanceled()
		if(configExpression == null) return null
		
		val project = element.project
		val gameType = configGroup.gameType ?: return null
		val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.value
		if(expression.isParameterAwareExpression()) return null //排除引用文本带参数的情况
		when(configExpression.type) {
			CwtDataType.Localisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return ParadoxLocalisationSearch.search(name, project, selector = selector).find()
			}
			CwtDataType.SyncedLocalisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return ParadoxSyncedLocalisationSearch.search(name, project, selector = selector).find()
			}
			CwtDataType.InlineLocalisation -> {
				if(element.text.isLeftQuoted()) return null //inline string
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return ParadoxLocalisationSearch.search(name, project, selector = selector).find()
			}
			CwtDataType.StellarisNameFormat -> {
				if(element.text.isLeftQuoted()) return null //specific expression
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return ParadoxLocalisationSearch.search(name, project, selector = selector).find() //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.AbsoluteFilePath -> {
				val filePath = expression
				val path = filePath.toPathOrNull() ?: return null
				return VfsUtil.findFile(path, true)?.toPsiFile(project)
			}
			CwtDataType.Definition -> {
				val name = expression
				val typeExpression = configExpression.value ?: return null
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
			}
			CwtDataType.Enum -> {
				val enumName = configExpression.value ?: return null
				val name = expression
				//尝试解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val invocationExpression = element.findParentProperty(fromParentBlock = true)
						?.castOrNull<ParadoxScriptProperty>()
						?: return null
					val invocationExpressionConfig = config.parent
						?.castOrNull<CwtPropertyConfig>()
						?: return null
					return ParadoxParameterSupport.resolveParameterFromInvocationExpression(name, invocationExpression, invocationExpressionConfig)
				}
				//尝试解析为简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					return resolvePredefinedEnumValue(element, name, enumName, configGroup)
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
			CwtDataType.Value, CwtDataType.ValueSet -> {
				//参见：ParadoxValueSetValueExpression
				val name = expression
				val predefinedResolved = resolvePredefinedValueSetValue(name, configExpression, configGroup)
				if(predefinedResolved != null) return predefinedResolved
				return ParadoxValueSetValueHandler.resolveValueSetValue(element, name, configExpression, configGroup)
			}
			CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> {
				//不在这里处理，参见：ParadoxScopeFieldExpression
				return null
			}
			CwtDataType.ValueField, CwtDataType.IntValueField -> {
				//不在这里处理，参见：ParadoxValueFieldExpression
				return null
			}
			CwtDataType.VariableField, CwtDataType.IntVariableField -> {
				//不在这里处理，参见：ParadoxVariableFieldExpression
				return null
			}
			CwtDataType.Modifier -> {
				return resolveModifier(element, expression, configGroup)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.SingleAliasRight -> return null
			CwtDataType.AliasKeysField -> {
				val aliasName = configExpression.value ?: return null
				val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
				val aliasSubName = getAliasSubName(element, expression, element.text.isLeftQuoted(), aliasName, configGroup)
				val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
				return resolveScriptExpression(element, rangeInElement, alias, alias.expression, configGroup, isKey, exact)
			}
			CwtDataType.AliasName -> {
				val aliasName = configExpression.value ?: return null
				val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
				val aliasSubName = getAliasSubName(element, expression, element.text.isLeftQuoted(), aliasName, configGroup)
				val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
				return resolveScriptExpression(element, rangeInElement, alias, alias.expression, configGroup, isKey, exact)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.AliasMatchLeft -> return null
			CwtDataType.TemplateExpression -> {
				return resolveTemplateExpression(element, expression, configExpression, configGroup)
			}
			CwtDataType.Constant -> {
				return when {
					config == null -> null
					config is CwtDataConfig<*> -> config.resolved().pointer.element
					else -> config.pointer.element
				}
			}
			else -> {
				if(ParadoxPathReferenceExpression.get(configExpression) != null) {
					val pathReference = expression.normalizePath()
					val selector = fileSelector().gameType(gameType).preferRootFrom(element)
					return ParadoxFilePathSearch.search(pathReference, project, configExpression, selector = selector).find()?.toPsiFile(project)
				}
				if(isKey == true && config is CwtPropertyConfig) return config.resolved().pointer.element
				return null
			}
		}
	}
	
	fun multiResolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>?, configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, isKey: Boolean? = null): Collection<PsiElement> {
		ProgressManager.checkCanceled()
		if(configExpression == null) return emptyList()
		
		val project = element.project
		val gameType = configGroup.gameType ?: return emptyList()
		val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.value
		if(expression.isParameterAwareExpression()) return emptyList() //排除引用文本带参数的情况
		when(configExpression.type) {
			CwtDataType.Localisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return ParadoxLocalisationSearch.search(name, project, selector = selector).findAll() //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.SyncedLocalisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return ParadoxSyncedLocalisationSearch.search(name, project, selector = selector).findAll() //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.InlineLocalisation -> {
				if(element.text.isLeftQuoted()) return emptyList() //inline string
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return ParadoxLocalisationSearch.search(name, project, selector = selector).findAll() //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.StellarisNameFormat -> {
				if(element.text.isLeftQuoted()) return emptyList() //specific expression
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return ParadoxLocalisationSearch.search(name, project, selector = selector).findAll() //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.AbsoluteFilePath -> {
				val filePath = expression
				val path = filePath.toPathOrNull() ?: return emptyList()
				return VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
			}
			CwtDataType.Definition -> {
				val name = expression
				val typeExpression = configExpression.value ?: return emptyList()
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findAll()
			}
			CwtDataType.Enum -> {
				val enumName = configExpression.value ?: return emptyList()
				val name = expression
				//尝试解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val invocationExpression = element.findParentProperty(fromParentBlock = true)
						?.castOrNull<ParadoxScriptProperty>()
						?: return emptyList()
					val invocationExpressionConfig = config.parent
						?.castOrNull<CwtPropertyConfig>()
						?: return emptyList()
					return ParadoxParameterSupport.resolveParameterFromInvocationExpression(name, invocationExpression, invocationExpressionConfig)
						.toSingletonListOrEmpty()
				}
				//尝试解析为简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					return resolvePredefinedEnumValue(element, name, enumName, configGroup).toSingletonListOrEmpty()
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
			CwtDataType.Value, CwtDataType.ValueSet -> {
				//参见：ParadoxValueSetValueExpression
				val name = expression
				val predefinedResolved = resolvePredefinedValueSetValue(name, configExpression, configGroup)
				if(predefinedResolved != null) return predefinedResolved.toSingletonListOrEmpty()
				return ParadoxValueSetValueHandler.resolveValueSetValue(element, name, configExpression, configGroup).toSingletonListOrEmpty()
			}
			CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> {
				//不在这里处理，参见：ParadoxScopeFieldExpression
				return emptyList()
			}
			CwtDataType.ValueField, CwtDataType.IntValueField -> {
				//不在这里处理，参见：ParadoxValueFieldExpression
				return emptyList()
			}
			CwtDataType.VariableField, CwtDataType.IntVariableField -> {
				//不在这里处理，参见：ParadoxVariableFieldExpression
				return emptyList()
			}
			CwtDataType.Modifier -> {
				return resolveModifier(element, expression, configGroup).toSingletonListOrEmpty()
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.SingleAliasRight -> return emptyList()
			CwtDataType.AliasKeysField -> {
				val aliasName = configExpression.value ?: return emptyList()
				val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
				val aliasSubName = getAliasSubName(element, expression, element.text.isLeftQuoted(), aliasName, configGroup)
				val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptyList()
				return multiResolveScriptExpression(element, rangeInElement, alias, alias.expression, configGroup, isKey)
			}
			CwtDataType.AliasName -> {
				val aliasName = configExpression.value ?: return emptyList()
				val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
				val aliasSubName = getAliasSubName(element, expression, element.text.isLeftQuoted(), aliasName, configGroup)
				val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptyList()
				return multiResolveScriptExpression(element, rangeInElement, alias, alias.expression, configGroup, isKey)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.AliasMatchLeft -> return emptyList()
			CwtDataType.TemplateExpression -> {
				//不在这里处理，参见：ParadoxTemplateExpression
				return emptyList()
			}
			CwtDataType.Constant -> {
				return when {
					config == null -> emptyList()
					config is CwtDataConfig<*> -> config.resolved().pointer.element.toSingletonListOrEmpty()
					else -> config.pointer.element.toSingletonListOrEmpty()
				}
			}
			else -> {
				if(ParadoxPathReferenceExpression.get(configExpression) != null) {
					val pathReference = expression.normalizePath()
					val selector = fileSelector().gameType(gameType).preferRootFrom(element)
					return ParadoxFilePathSearch.search(pathReference, project, configExpression, selector = selector).findAll().mapNotNull { it.toPsiFile(project) }
				}
				if(isKey == true && config is CwtPropertyConfig) return config.resolved().pointer.element.toSingletonListOrEmpty()
				return emptyList()
			}
		}
	}
	
	fun resolveModifier(element: ParadoxScriptExpressionElement, name: String, configGroup: CwtConfigGroup): PsiElement? {
		if(element !is ParadoxScriptStringExpressionElement) return null
		return ParadoxModifierHandler.resolveModifier(name, element, configGroup)
	}
	
	fun resolveTemplateExpression(element: ParadoxScriptExpressionElement, text: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
		if(element !is ParadoxScriptStringExpressionElement) return null
		val templateConfigExpression = CwtTemplateExpression.resolve(configExpression.expressionString)
		return templateConfigExpression.resolve(text, element, configGroup)
	}
	
	fun resolvePredefinedScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val systemLink = configGroup.systemLinks[name] ?: return null
		val resolved = systemLink.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, systemLink)
		return resolved
	}
	
	fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val linkConfig = configGroup.linksAsScopeNotData[name] ?: return null
		val resolved = linkConfig.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, linkConfig)
		return resolved
	}
	
	fun resolveValueLinkValue(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val linkConfig = configGroup.linksAsValueNotData[name] ?: return null
		val resolved = linkConfig.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, linkConfig)
		return resolved
	}
	
	fun resolvePredefinedEnumValue(element: ParadoxScriptExpressionElement, name: String, enumName: String, configGroup: CwtConfigGroup): PsiElement? {
		val enumConfig = configGroup.enums[enumName] ?: return null
		val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return null
		val resolved = enumValueConfig.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, enumValueConfig)
		return resolved
	}
	
	fun resolvePredefinedValueSetValue(name: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): PsiElement? {
		val valueSetName = configExpression.value ?: return null
		val read = configExpression.type == CwtDataType.Value
		if(read) {
			//首先尝试解析为预定义的value
			val valueSetConfig = configGroup.values.get(valueSetName)
			val valueSetValueConfig = valueSetConfig?.valueConfigMap?.get(name)
			val predefinedResolved = valueSetValueConfig?.pointer?.element
			if(predefinedResolved != null) {
				predefinedResolved.putUserData(PlsKeys.cwtConfigKey, valueSetValueConfig)
				return predefinedResolved
			}
		}
		return null
	}
	
	fun resolvePredefinedValueSetValue(element: ParadoxScriptExpressionElement, name: String, configExpressions: Iterable<CwtDataExpression>, configGroup: CwtConfigGroup): PsiElement? {
		for(configExpression in configExpressions) {
			val valueSetName = configExpression.value ?: return null
			val read = configExpression.type == CwtDataType.Value
			if(read) {
				//首先尝试解析为预定义的value
				val valueSetConfig = configGroup.values.get(valueSetName)
				val valueSetValueConfig = valueSetConfig?.valueConfigMap?.get(name)
				val predefinedResolved = valueSetValueConfig?.pointer?.element
				if(predefinedResolved != null) {
					predefinedResolved.putUserData(PlsKeys.cwtConfigKey, valueSetValueConfig)
					return predefinedResolved
				}
			}
		}
		return null
	}
	
	fun resolvePredefinedLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val linkConfig = configGroup.localisationLinks[name] ?: return null
		val resolved = linkConfig.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, linkConfig)
		return resolved
	}
	
	fun resolvePredefinedLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val commandConfig = configGroup.localisationCommands[name] ?: return null
		val resolved = commandConfig.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, commandConfig)
		return resolved
	}
	//endregion
}
