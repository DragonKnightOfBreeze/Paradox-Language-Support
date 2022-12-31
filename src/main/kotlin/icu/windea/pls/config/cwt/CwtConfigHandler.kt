@file:Suppress("UnusedReceiverParameter", "UNUSED_PARAMETER")

package icu.windea.pls.config.cwt

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.util.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.config.cwt.expression.*
import icu.windea.pls.config.script.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.handler.*
import icu.windea.pls.core.model.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.selector.*
import icu.windea.pls.core.selector.chained.*
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
	//region Common Methods
	const val paramsEnumName = "scripted_effect_params"
	
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
	 * 内联规则以便后续的代码提示、引用解析和结构验证。
	 */
	fun inlineConfig(key: String, isQuoted: Boolean, config: CwtPropertyConfig, configGroup: CwtConfigGroup, result: MutableList<CwtDataConfig<*>>, index: Int, matchType: Int): Int {
		//内联类型为`single_alias_right`或`alias_match_left`的规则
		run {
			val valueExpression = config.valueExpression
			when(valueExpression.type) {
				CwtDataType.SingleAliasRight -> {
					val singleAliasName = valueExpression.value ?: return@run
					val singleAliases = configGroup.singleAliases[singleAliasName] ?: return@run
					for(singleAlias in singleAliases) {
						result.add(config.inlineFromSingleAliasConfig(singleAlias))
					}
					return index + 1
				}
				CwtDataType.AliasMatchLeft -> {
					val aliasName = valueExpression.value ?: return@run
					val aliasGroup = configGroup.aliasGroups[aliasName] ?: return@run
					val aliasSubName = getAliasSubName(key, isQuoted, aliasName, configGroup, matchType) ?: return@run
					val aliases = aliasGroup[aliasSubName] ?: return@run
					for(alias in aliases) {
						result.add(config.inlineFromAliasConfig(alias))
					}
					return index + 1
				}
				else -> pass()
			}
		}
		result.add(config)
		return index + 1
	}
	
	/**
	 * 从CWT规则推断得到对应的CWT规则组。
	 */
	@InferMethod
	fun getConfigGroupFromConfig(from: PsiElement, project: Project): CwtConfigGroup? {
		val file = from.containingFile ?: return null
		val virtualFile = file.virtualFile ?: return null
		val path = virtualFile.path
		//这里的key可能是"core"，而这不是gameType
		val key = path.substringAfter("config/cwt/", "").substringBefore("/", "")
		if(key.isEmpty()) return null
		return getCwtConfig(project).get(key)
	}
	//endregion
	
	//region Matches Methods
	//TODO 基于cwt规则文件的匹配方法需要进一步匹配scope
	//DONE 兼容variableReference inlineMath parameter
	fun matchesScriptExpression(expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.ALL): Boolean {
		//匹配block
		if(configExpression == CwtValueExpression.BlockExpression) {
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
			CwtDataType.Any -> {
				return true
			}
			CwtDataType.Bool -> {
				return expression.type.isBooleanType()
			}
			CwtDataType.Int -> {
				//注意：用括号括起的整数（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				//匹配范围
				if(isExact) {
					val (min,max) = configExpression.extraValue<Tuple2<Int, Int?>>() ?: return true
					val value = expression.text.toIntOrNull() ?: return true
					return min <= value && (max == null || max >= value)
				}
				return false
			}
			CwtDataType.Float -> {
				//注意：用括号括起的浮点数（作为scalar）也匹配这个规则
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
				//匹配范围
				if(isExact) {
					val (min,max) = configExpression.extraValue<Tuple2<Float, Float?>>() ?: return true
					val value = expression.text.toFloatOrNull() ?: return true
					return min <= value && (max == null || max >= value)
				}
				return false
			}
			CwtDataType.Scalar -> {
				//unquoted_string, quoted, any key
				return expression.type.isStringType() || (expression.isKey == true)
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
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return findLocalisation(expression.text, project, preferFirst = true, selector = selector) != null
				}
				return true
			}
			CwtDataType.SyncedLocalisation -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
					val selector = localisationSelector().gameType(gameType)
					return findSyncedLocalisation(expression.text, project, preferFirst = true, selector = selector) != null
				}
				return true
			}
			CwtDataType.InlineLocalisation -> {
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
			CwtDataType.StellarisNameFormat -> {
				if(!expression.type.isStringType()) return false
				return true //specific expression
			}
			CwtDataType.AbsoluteFilePath -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				val path = expression.text.toPathOrNull() ?: return false
				return VfsUtil.findFile(path, true) != null
			}
			CwtDataType.FilePath -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.FILE_PATH)) {
					val resolvedPath = CwtPathExpressionType.FilePath.resolve(configExpression.value, expression.text.normalizePath()) ?: return false
					val selector = fileSelector().gameType(gameType)
					return ParadoxFilePathSearch.search(resolvedPath, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataType.Icon -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				if(BitUtil.isSet(matchType, CwtConfigMatchType.FILE_PATH)) {
					val resolvedPath = CwtPathExpressionType.Icon.resolve(configExpression.value, expression.text.normalizePath()) ?: return false
					val selector = fileSelector().gameType(gameType)
					return ParadoxFilePathSearch.search(resolvedPath, project, selector = selector).findFirst() != null
				}
				return true
			}
			CwtDataType.TypeExpression -> {
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
			CwtDataType.TypeExpressionString -> {
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
			CwtDataType.Value -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				return true //任意字符串即可，不需要进一步匹配
			}
			CwtDataType.ValueSet -> {
				if(!expression.type.isStringType()) return false
				if(isStatic) return false
				if(isParameterAware) return true
				return true //任意字符串即可，不需要进一步匹配
			}
			CwtDataType.ScopeField, CwtDataType.Scope -> {
				if(expression.quoted) return false //不允许用引号括起
				if(!isStatic && isParameterAware) return true
				val textRange = TextRange.create(0, expression.text.length)
				return ParadoxScopeFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey) != null
			}
			CwtDataType.ScopeGroup -> {
				if(expression.quoted) return false //不允许用引号括起
				if(!isStatic && isParameterAware) return true
				val textRange = TextRange.create(0, expression.text.length)
				return ParadoxScopeFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey) != null
			}
			CwtDataType.ValueField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
				if(!isStatic && isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				val textRange = TextRange.create(0, expression.text.length)
				return ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey) != null
			}
			CwtDataType.IntValueField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				if(!isStatic && isParameterAware) return true
				if(expression.quoted) return false //接下来的匹配不允许用引号括起
				val textRange = TextRange.create(0, expression.text.length)
				return ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey) != null
			}
			CwtDataType.VariableField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				if(!isStatic && isParameterAware) return true
				return false //TODO
			}
			CwtDataType.IntVariableField -> {
				//也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
				if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
				if(!isStatic && isParameterAware) return true
				return false //TODO
			}
			CwtDataType.Modifier -> {
				if(!isStatic && isParameterAware) return true
				//匹配预定义的modifier
				return matchesModifier(expression.text, configGroup)
			}
			CwtDataType.SingleAliasRight -> {
				return false //不在这里处理
			}
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataType.AliasKeysField -> {
				if(!isStatic && isParameterAware) return true
				val aliasName = configExpression.value ?: return false
				return matchesAliasName(expression, aliasName, configGroup, matchType)
			}
			CwtDataType.AliasName -> {
				if(!isStatic && isParameterAware) return true
				val aliasName = configExpression.value ?: return false
				return matchesAliasName(expression, aliasName, configGroup, matchType)
			}
			CwtDataType.AliasMatchLeft -> {
				return false //不在这里处理
			}
			CwtDataType.ConstantKey -> {
				val value = configExpression.value
				return expression.text.equals(value, true) //忽略大小写
			}
			CwtDataType.Constant -> {
				val text = expression.text
				val value = configExpression.value
				//常量的值也可能是yes/no
				if((value == "yes" || value == "no") && text.isLeftQuoted()) return false
				return expression.text.equals(value, true) //忽略大小写
			}
			CwtDataType.Other -> {
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
			CwtDataType.Any -> 100
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
			CwtDataType.FilePath -> 70
			CwtDataType.Icon -> 70
			CwtDataType.TypeExpression -> 60
			CwtDataType.TypeExpressionString -> 60
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
			CwtDataType.VariableField -> 20
			CwtDataType.IntVariableField -> 20
			CwtDataType.Modifier -> 80
			CwtDataType.SingleAliasRight -> 0 //不期望匹配到
			CwtDataType.AliasName -> 0 //不期望匹配到
			CwtDataType.AliasKeysField -> 0 //不期望匹配到
			CwtDataType.AliasMatchLeft -> 0 //不期望匹配到
			CwtDataType.ConstantKey -> 100
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
		val elementPath = ParadoxElementPathHandler.resolveFromFile(definitionElement, PlsConstants.maxDefinitionDepth) ?: return
		
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
		if(ParadoxScopeConfigHandler.isScopeContextSupported(definitionElement)) {
			context.put(PlsCompletionKeys.scopeContextKey, ParadoxScopeConfigHandler.getScopeContext(definitionElement))
		}
		
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
		//if(ParadoxScopeConfigHandler.isScopeContextSupported(definitionElement)) {
		//	context.put(PlsCompletionKeys.scopeContextKey, ParadoxScopeConfigHandler.getScopeContext(definitionElement))
		//}
		
		for(config in configs) {
			if(config is CwtPropertyConfig) {
				context.put(PlsCompletionKeys.configKey, config.valueConfig)
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
		val cardinality = config.cardinality
		val maxCount = when {
			cardinality == null -> if(expression.type == CwtDataType.ConstantKey) 1 else null
			else -> cardinality.max
		}
		return maxCount == null || actualCount < maxCount
	}
	
	private fun shouldComplete(config: CwtValueConfig, definitionMemberInfo: ParadoxDefinitionMemberInfo): Boolean {
		val expression = config.valueExpression
		val actualCount = definitionMemberInfo.childValueOccurrenceMap[expression]?.actual ?: 0
		//如果写明了cardinality，则为cardinality.max，否则如果类型为常量，则为1，否则为null，null表示没有限制
		val cardinality = config.cardinality
		val maxCount = when {
			cardinality == null -> if(expression.type == CwtDataType.Constant) 1 else null
			else -> cardinality.max
		}
		return maxCount == null || actualCount < maxCount
	}
	
	fun completeRootKey(context: ProcessingContext, result: CompletionResultSet, elementPath: ParadoxElementPath) {
		val fileInfo = context.originalFile.fileInfo ?: return
		val configGroup = context.configGroup
		val path = fileInfo.path
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
		
		if(configExpression == CwtValueExpression.BlockExpression) {
			result.addBlockElement(context)
			return
		}
		
		if(configExpression.isEmpty()) return
		if(keyword.isParameterAwareExpression()) return //排除带参数的情况
		
		//匹配作用域
		val scopeContext = scopeContext
		val scopeMatched = when {
			scopeContext == null -> true
			config is CwtPropertyConfig -> {
				ParadoxScopeConfigHandler.matchesScope(scopeContext, config.supportedScopes)
			}
			config is CwtLinkConfig -> true //TODO
			else -> true
		}
		if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) return
		
		when(configExpression.type) {
			CwtDataType.Bool -> {
				result.addExpressionElement(context, PlsLookupElements.yesLookupElement)
				result.addExpressionElement(context, PlsLookupElements.noLookupElement)
			}
			CwtDataType.Localisation -> {
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = getScriptExpressionTailText(config)
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processLocalisationVariants(keyword, project, selector = selector) { localisation ->
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
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = getScriptExpressionTailText(config)
				val selector = localisationSelector().gameType(gameType).preferRootFrom(contextElement).preferLocale(preferredParadoxLocale())
				processSyncedLocalisationVariants(keyword, project, selector = selector) { syncedLocalisation ->
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
				result.restartCompletionOnAnyPrefixChange() //当前缀变动时需要重新提示
				val tailText = getScriptExpressionTailText(config)
				processLocalisationVariants(keyword, project) { localisation ->
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
			CwtDataType.FilePath -> {
				val expressionType = CwtPathExpressionType.FilePath
				val expressionValue = configExpression.value
				val tailText = getScriptExpressionTailText(config)
				val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement).distinctByFilePath()
				val virtualFileQuery = ParadoxFilePathSearch.search(expressionValue, project, expressionType, selector = selector)
				virtualFileQuery.processQuery { virtualFile ->
					val file = virtualFile.toPsiFile<PsiFile>(project) ?: return@processQuery true
					val filePath = virtualFile.fileInfo?.path?.path ?: return@processQuery true
					val name = expressionType.extract(expressionValue, filePath) ?: return@processQuery true
					//没有图标
					val builder = ParadoxScriptExpressionLookupElementBuilder.create(file, name)
						.withTailText(tailText)
						.withTypeText(file.name)
						.withTypeIcon(file.icon)
					result.addScriptExpressionElement(context, builder)
					true
				}
			}
			CwtDataType.Icon -> {
				val expressionType = CwtPathExpressionType.Icon
				val expressionValue = configExpression.value
				val tailText = getScriptExpressionTailText(config)
				val selector = fileSelector().gameType(gameType).preferRootFrom(contextElement).distinctByFilePath()
				val virtualFileQuery = ParadoxFilePathSearch.search(expressionValue, project, expressionType, selector = selector)
				virtualFileQuery.processQuery { virtualFile ->
					val file = virtualFile.toPsiFile<PsiFile>(project) ?: return@processQuery true
					val filePath = virtualFile.fileInfo?.path?.path ?: return@processQuery true
					val name = expressionType.extract(expressionValue, filePath) ?: return@processQuery true
					//没有图标
					val builder = ParadoxScriptExpressionLookupElementBuilder.create(file, name)
						.withTailText(tailText)
						.withTypeText(file.name)
						.withTypeIcon(file.icon)
					result.addScriptExpressionElement(context, builder)
					true
				}
			}
			CwtDataType.TypeExpression -> {
				val typeExpression = configExpression.value ?: return
				val tailText = getScriptExpressionTailText(config)
				val selector = definitionSelector().gameType(gameType).preferRootFrom(contextElement).distinctByName()
				val definitionQuery = ParadoxDefinitionSearch.search(typeExpression, project, selector = selector)
				definitionQuery.processQuery { definition ->
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
			CwtDataType.TypeExpressionString -> {
				val typeExpression = configExpression.value ?: return
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return
				val tailText = getScriptExpressionTailText(config)
				val selector = definitionSelector().gameType(gameType).preferRootFrom(contextElement).distinctByName()
				val definitionQuery = ParadoxDefinitionSearch.search(typeExpression, project, selector = selector)
				definitionQuery.processQuery { definition ->
					val definitionName = definition.definitionInfo?.name ?: return@processQuery true
					val name = "$prefix$definitionName$suffix"
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
					val definitionElement = contextElement.findParentProperty(fromParentBlock = true)?.castOrNull<ParadoxScriptProperty>() ?: return
					completeParametersForInvocationExpression(definitionElement, config, context, result)
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
					val query = ParadoxComplexEnumValueSearch.searchAll(enumName, project, selector = selector)
					query.processQuery { complexEnum ->
						val name = complexEnum.value
						//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
						val builder = ParadoxScriptExpressionLookupElementBuilder.create(complexEnum, name)
							.withIcon(PlsIcons.ComplexEnumValue)
							.withTailText(tailText)
							.withTypeText(typeFile?.name)
							.withTypeIcon(typeFile?.icon)
							.caseInsensitive()
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
			CwtDataType.VariableField -> pass() //TODO
			CwtDataType.IntVariableField -> pass() //TODO
			CwtDataType.Modifier -> {
				//提示预定义的modifier
				completeModifier(context, result)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.SingleAliasRight -> pass()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
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
			CwtDataType.ConstantKey -> {
				val name = configExpression.value ?: return
				//if(!name.matchesKeyword(keyword)) return //不预先过滤结果
				val element = config.resolved().pointer.element ?: return
				val typeFile = config.resolved().pointer.containingFile
				val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
					.withIcon(PlsIcons.Property)
					.withTypeText(typeFile?.name)
					.withTypeIcon(typeFile?.icon)
					.caseInsensitive()
					.withScopeMatched(scopeMatched)
					.withPriority(PlsCompletionPriorities.constantKeyPriority)
				result.addScriptExpressionElement(context, builder)
			}
			CwtDataType.Constant -> {
				val name = configExpression.value ?: return
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
				//if(!name.matchesKeyword(keyword)) return //不预先过滤结果
				val element = config.resolved().pointer.element ?: return
				val typeFile = config.resolved().pointer.containingFile
				val icon = when {
					config is CwtValueConfig && config.isTagConfig -> PlsIcons.Tag
					else -> PlsIcons.Value
				}
				val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
					.withIcon(icon)
					.withTypeText(typeFile?.name)
					.withTypeIcon(typeFile?.icon)
					.caseInsensitive()
					.withScopeMatched(scopeMatched)
					.withPriority(PlsCompletionPriorities.constantPriority)
				result.addScriptExpressionElement(context, builder)
			}
			else -> pass()
		}
		pass()
	}
	
	private fun getScriptExpressionTailText(config: CwtConfig<*>?): String? {
		if(config?.expression == null) return null
		return " by ${config.expression} in ${config.resolved().pointer.containingFile?.name.orAnonymous()}"
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
	
	fun completeModifier(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val modifiers = configGroup.modifiers
		if(modifiers.isEmpty()) return
		//批量提示
		val lookupElements = mutableSetOf<LookupElement>()
		for(modifierConfig in modifiers.values) {
			//排除不匹配modifier的supported_scopes的情况
			val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, modifierConfig.supportedScopes)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val name = modifierConfig.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
			val element = modifierConfig.pointer.element ?: continue
			val tailText = " from modifiers"
			val typeFile = modifierConfig.pointer.containingFile
			val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.Modifier)
				.withTailText(tailText)
				.withTypeText(typeFile?.name)
				.withTypeIcon(typeFile?.icon)
				.withScopeMatched(scopeMatched)
				.withPriority(PlsCompletionPriorities.modifierPriority)
			result.addScriptExpressionElement(context, builder)
		}
		result.addAllElements(lookupElements)
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
		//总是提示，无论作用域是否匹配
		val systemScopeConfigs = configGroup.systemScopes
		for(systemScopeConfig in systemScopeConfigs.values) {
			val name = systemScopeConfig.id
			val element = systemScopeConfig.pointer.element ?: continue
			val tailText = " from system scopes"
			val typeFile = systemScopeConfig.pointer.containingFile
			val lookupElement = LookupElementBuilder.create(element, name)
				.withIcon(PlsIcons.SystemScope)
				.withTailText(tailText, true)
				.withTypeText(typeFile?.name, typeFile?.icon, true)
				.withCaseSensitivity(false) //忽略大小写
				.withPriority(PlsCompletionPriorities.systemScopePriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = scopeContext
		
		val linkConfigs = configGroup.linksAsScopeNotData
		for(scope in linkConfigs.values) {
			val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, scope.inputScopes)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
			val name = scope.name
			//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
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
			val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, linkConfig.inputScopes)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
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
				.withScopeMatched(scopeMatched)
				.withPriority(PlsCompletionPriorities.scopeLinkPrefixPriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeScopeLinkDataSource(context: ProcessingContext, result: CompletionResultSet, prefix: String?, dataSourceNodeToCheck: ParadoxExpressionNode?): Unit = with(context) {
		val config = config
		val configs = configs
		val scopeContext = scopeContext
		
		val allLinkConfigs = if(prefix == null) configGroup.linksAsScopeWithoutPrefix else configGroup.linksAsScopeWithPrefix
		val linkConfigs = if(prefix == null) allLinkConfigs.values else allLinkConfigs.values.filter { prefix == it.prefix }
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
			//基于前缀进行提示，即使前缀的input_scopes不匹配前一个scope的output_scope
			//如果没有前缀，排除input_scopes不匹配前一个scope的output_scope的情况
			if(prefix == null) {
				val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, linkConfig.inputScopes)
				if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
				context.put(PlsCompletionKeys.scopeMatchedKey, scopeMatched)
			}
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
			val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, linkConfig.inputScopes)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
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
				.withScopeMatched(scopeMatched)
				.withPriority(PlsCompletionPriorities.valueLinkValuePriority)
			result.addElement(lookupElement)
		}
	}
	
	fun completeValueLinkPrefix(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = scopeContext
		
		val linkConfigs = configGroup.linksAsValueWithPrefix
		for(linkConfig in linkConfigs.values) {
			val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, linkConfig.inputScopes)
			if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
			
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
			result.addElement(lookupElement)
		}
	}
	
	fun completeValueLinkDataSource(context: ProcessingContext, result: CompletionResultSet, prefix: String?, dataSourceNodeToCheck: ParadoxExpressionNode?): Unit = with(context) {
		val config = config
		val configs = configs
		val scopeContext = scopeContext
		
		val allLinkConfigs = if(prefix == null) configGroup.linksAsValueWithoutPrefix else configGroup.linksAsValueWithPrefix
		val linkConfigs = if(prefix == null) allLinkConfigs.values else allLinkConfigs.values.filter { prefix == it.prefix }
		
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
			//基于前缀进行提示，即使前缀的input_scopes不匹配前一个scope的output_scope
			//如果没有前缀，排除input_scopes不匹配前一个scope的output_scope的情况
			if(prefix == null) {
				val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, linkConfig.inputScopes)
				if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) continue
				context.put(PlsCompletionKeys.scopeMatchedKey, scopeMatched)
			}
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
		val tailText = " by $configExpression in ${config.resolved().pointer.containingFile?.name.orAnonymous()}"
		//提示预定义的value
		run {
			ProgressManager.checkCanceled()
			if(configExpression.type == CwtDataType.Value) {
				val valueConfig = this.configGroup.values[valueSetName] ?: return@run
				val valueSetValueConfigs = valueConfig.valueConfigMap.values
				if(valueSetValueConfigs.isEmpty()) return@run
				for(valueSetValueConfig in valueSetValueConfigs) {
					//if(!name.matchesKeyword(keyword)) continue //不预先过滤结果
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
		}
		//提示来自脚本文件的value
		run {
			ProgressManager.checkCanceled()
			val contextElement = contextElement
			val selector = valueSetValueSelector().gameType(gameType)
				.notSamePosition(contextElement)
				.distinctByValue()
			val valueSetValueQuery = ParadoxValueSetValueSearch.search(valueSetName, project, selector = selector)
			valueSetValueQuery.processQuery { valueSetValue ->
				//去除后面的作用域信息
				val value = ParadoxValueSetValueHandler.getName(valueSetValue) ?: return@processQuery true
				val icon = PlsIcons.ValueSetValue(valueSetName)
				//不显示typeText
				val builder = ParadoxScriptExpressionLookupElementBuilder.create(valueSetValue, value)
					.withIcon(icon)
					.withTailText(tailText)
				result.addScriptExpressionElement(context, builder)
				true
			}
		}
	}
	
	fun completeLocalisationCommandScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = context.scopeContext
		
		val localisationLinks = configGroup.localisationLinks
		for(localisationScope in localisationLinks.values) {
			val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, localisationScope.inputScopes)
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
	
	fun completeLocalisationCommandField(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
		val scopeContext = context.scopeContext
		
		val localisationCommands = configGroup.localisationCommands
		for(localisationCommand in localisationCommands.values) {
			val scopeMatched = ParadoxScopeConfigHandler.matchesScope(scopeContext, localisationCommand.supportedScopes)
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
	
	fun completeVariable(file: PsiFile, result: CompletionResultSet) {
		val project = file.project
		val variableSelector = valueSetValueSelector().gameTypeFrom(file).preferRootFrom(file).distinctByValue()
		val variableQuery = ParadoxValueSetValueSearch.search("variable", project, selector = variableSelector)
		variableQuery.processQuery { variable ->
			val value = ParadoxValueSetValueHandler.getName(variable) ?: return@processQuery true
			val icon = PlsIcons.Variable
			val tailText = " from value[variable]"
			val lookupElement = LookupElementBuilder.create(variable, value)
				.withIcon(icon)
				.withTailText(tailText, true)
				.withCaseSensitivity(false) //忽略大小写
			result.addElement(lookupElement)
			true
		}
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
			//没有其他地方使用到时，排除当前正在输入的那个
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
			?.takeIf { it.type == CwtDataType.TypeExpression }?.value ?: return //不期望的结果
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
	fun resolveScriptExpression(element: ParadoxScriptStringExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configGroup: CwtConfigGroup, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
		if(element.isParameterAwareExpression()) return null //排除带参数的情况
		
		val project = element.project
		val gameType = configGroup.gameType ?: return null
		val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.value
		val configExpression = config.expression ?: return null
		when(configExpression.type) {
			CwtDataType.Localisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataType.SyncedLocalisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return findSyncedLocalisation(name, project, selector = selector)
			}
			CwtDataType.InlineLocalisation -> {
				if(element.text.isLeftQuoted()) return null //inline string
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element, exact).preferLocale(preferredParadoxLocale(), exact)
				return findLocalisation(name, project, selector = selector)
			}
			CwtDataType.StellarisNameFormat -> {
				if(element.text.isLeftQuoted()) return null //specific expression
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findLocalisation(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.AbsoluteFilePath -> {
				val filePath = expression
				val path = filePath.toPathOrNull() ?: return null
				return VfsUtil.findFile(path, true)?.toPsiFile(project)
			}
			CwtDataType.FilePath -> {
				val expressionType = CwtPathExpressionType.FilePath
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath()) ?: return null
				val selector = fileSelector().gameType(gameType).preferRootFrom(element, exact)
				return ParadoxFilePathSearch.search(filePath, project, selector = selector).find()
					?.toPsiFile(project)
			}
			CwtDataType.Icon -> {
				val expressionType = CwtPathExpressionType.Icon
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath()) ?: return null
				val selector = fileSelector().gameType(gameType).preferRootFrom(element, exact)
				return ParadoxFilePathSearch.search(filePath, project, selector = selector).find()
					?.toPsiFile(project)
			}
			CwtDataType.TypeExpression -> {
				val name = expression
				val typeExpression = configExpression.value ?: return null
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
			}
			CwtDataType.TypeExpressionString -> {
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return null
				val name = expression.removeSurrounding(prefix, suffix)
				val typeExpression = configExpression.value ?: return null
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element, exact)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).find()
			}
			CwtDataType.Enum -> {
				val enumName = configExpression.value ?: return null
				val name = expression
				//尝试解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val definitionElement = element.findParentProperty(fromParentBlock = true) ?: return null
					val definitionName = definitionElement.name
					val definitionType = config.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.subNameExpression
						?.takeIf { it.type == CwtDataType.TypeExpression }?.value ?: return null
					return ParadoxParameterElement(element, name, definitionName, definitionType, project, gameType, false)
				}
				//尝试解析为简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					return resolveEnumValue(element, name, enumName, configGroup)
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
				return resolveValueSetValue(element, name, config, configGroup)
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
				return null //TODO
			}
			CwtDataType.Modifier -> {
				val name = expression
				return resolveModifier(name, configGroup)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.SingleAliasRight -> return null
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataType.AliasKeysField -> {
				val aliasName = configExpression.value ?: return null
				val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
				val aliasSubName = getAliasSubName(expression, element.text.isLeftQuoted(), aliasName, configGroup)
				val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
				return resolveScriptExpression(element, rangeInElement, alias, configGroup, isKey, exact)
			}
			CwtDataType.AliasName -> {
				val aliasName = configExpression.value ?: return null
				val aliasGroup = configGroup.aliasGroups[aliasName] ?: return null
				val aliasSubName = getAliasSubName(expression, element.text.isLeftQuoted(), aliasName, configGroup)
				val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return null
				return resolveScriptExpression(element, rangeInElement, alias, configGroup, isKey, exact)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.AliasMatchLeft -> return null
			CwtDataType.ConstantKey, CwtDataType.Constant -> {
				when {
					config is CwtDataConfig<*> -> return config.resolved().pointer.element
					else -> return config.pointer.element
				}
			}
			else -> {
				if(isKey == true && config is CwtPropertyConfig) return config.resolved().pointer.element
				return null
			}
		}
	}
	
	fun multiResolveScriptExpression(element: ParadoxScriptStringExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configGroup: CwtConfigGroup, isKey: Boolean? = null): Collection<PsiElement> {
		if(element.isParameterAwareExpression()) return emptyList() //排除带参数的情况  
		
		val project = element.project
		val gameType = configGroup.gameType ?: return emptyList()
		val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.value
		val configExpression = config.expression ?: return emptyList()
		when(configExpression.type) {
			CwtDataType.Localisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.SyncedLocalisation -> {
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findSyncedLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.InlineLocalisation -> {
				if(element.text.isLeftQuoted()) return emptyList() //inline string
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.StellarisNameFormat -> {
				if(element.text.isLeftQuoted()) return emptyList() //specific expression
				val name = expression
				val selector = localisationSelector().gameType(gameType).preferRootFrom(element) //不指定偏好的语言区域
				return findLocalisations(name, project, selector = selector) //仅查找用户的语言区域或任意语言区域的
			}
			CwtDataType.AbsoluteFilePath -> {
				val filePath = expression
				val path = filePath.toPathOrNull() ?: return emptyList()
				return VfsUtil.findFile(path, true)?.toPsiFile<PsiFile>(project).toSingletonListOrEmpty()
			}
			CwtDataType.FilePath -> {
				val expressionType = CwtPathExpressionType.FilePath
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath()) ?: return emptyList()
				val selector = fileSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxFilePathSearch.search(filePath, project, selector = selector).findAll().mapNotNull { it.toPsiFile(project) }
			}
			CwtDataType.Icon -> {
				val expressionType = CwtPathExpressionType.Icon
				val filePath = expressionType.resolve(configExpression.value, expression.normalizePath()) ?: return emptyList()
				val selector = fileSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxFilePathSearch.search(filePath, project, selector = selector).findAll().mapNotNull { it.toPsiFile(project) }
			}
			CwtDataType.TypeExpression -> {
				val name = expression
				val typeExpression = configExpression.value ?: return emptyList()
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findAll()
			}
			CwtDataType.TypeExpressionString -> {
				val (prefix, suffix) = configExpression.extraValue?.cast<TypedTuple2<String>>() ?: return emptyList()
				val name = expression.removeSurrounding(prefix, suffix)
				val typeExpression = configExpression.value ?: return emptyList()
				val selector = definitionSelector().gameType(gameType).preferRootFrom(element)
				return ParadoxDefinitionSearch.search(name, typeExpression, project, selector = selector).findAll()
			}
			CwtDataType.Enum -> {
				val enumName = configExpression.value ?: return emptyList()
				val name = expression
				//尝试解析为参数名
				if(isKey == true && enumName == paramsEnumName && config is CwtPropertyConfig) {
					val definitionElement = element.findParentProperty(fromParentBlock = true) ?: return emptyList()
					val definitionName = definitionElement.name
					val definitionType = config.parent?.castOrNull<CwtPropertyConfig>()
						?.inlineableConfig?.castOrNull<CwtAliasConfig>()?.subNameExpression
						?.takeIf { it.type == CwtDataType.TypeExpression }?.value ?: return emptyList()
					return ParadoxParameterElement(element, name, definitionName, definitionType, project, gameType, false).toSingletonList()
				}
				//尝试解析为简单枚举
				val enumConfig = configGroup.enums[enumName]
				if(enumConfig != null) {
					return resolveEnumValue(element, name, enumName, configGroup).toSingletonListOrEmpty()
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
				return resolveValueSetValue(element, name, config, configGroup).toSingletonListOrEmpty()
			}
			CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> {
				//不在这里处理，参见：ParadoxScopeFieldExpression
				return emptyList()
			}
			CwtDataType.ValueField, CwtDataType.IntValueField -> {
				//不在这里处理，参见：ParadoxValueFieldExpression
				return emptyList()
			}
			CwtDataType.Modifier -> {
				val name = expression
				return resolveModifier(name, configGroup).toSingletonListOrEmpty()
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.SingleAliasRight -> return emptyList()
			//TODO 规则alias_keys_field应该等同于规则alias_name，需要进一步确认
			CwtDataType.AliasKeysField -> {
				val aliasName = configExpression.value ?: return emptyList()
				val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
				val aliasSubName = getAliasSubName(expression, element.text.isLeftQuoted(), aliasName, configGroup)
				val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptyList()
				return multiResolveScriptExpression(element, rangeInElement, alias, configGroup, isKey)
			}
			CwtDataType.AliasName -> {
				val aliasName = configExpression.value ?: return emptyList()
				val aliasGroup = configGroup.aliasGroups[aliasName] ?: return emptyList()
				val aliasSubName = getAliasSubName(expression, element.text.isLeftQuoted(), aliasName, configGroup)
				val alias = aliasGroup[aliasSubName]?.firstOrNull() ?: return emptyList()
				return multiResolveScriptExpression(element, rangeInElement, alias, configGroup, isKey)
			}
			//意味着aliasSubName是嵌入值，如modifier的名字
			CwtDataType.AliasMatchLeft -> return emptyList()
			CwtDataType.ConstantKey, CwtDataType.Constant -> {
				when {
					config is CwtDataConfig<*> -> return config.resolved().pointer.element.toSingletonListOrEmpty()
					else -> return config.pointer.element.toSingletonListOrEmpty()
				}
			}
			else -> {
				if(isKey == true && config is CwtPropertyConfig) return config.resolved().pointer.element.toSingletonListOrEmpty()
				return emptyList()
			}
		}
	}
	
	fun resolveSystemScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val systemScope = configGroup.systemScopes[name] ?: return null
		val resolved = systemScope.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, systemScope)
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
	
	fun resolveEnumValue(element: ParadoxScriptStringExpressionElement, name: String, enumName: String, configGroup: CwtConfigGroup): PsiElement? {
		val enumConfig = configGroup.enums[enumName] ?: return null
		val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return null
		val resolved = enumValueConfig.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, enumValueConfig)
		return resolved
	}
	
	private fun resolveValueSetValue(element: ParadoxScriptStringExpressionElement, name: String, config: CwtConfig<*>, configGroup: CwtConfigGroup): PsiElement? {
		val gameType = configGroup.gameType ?: return null
		val configExpression = config.expression ?: return null
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
		return ParadoxValueSetValueElement(element, name, valueSetName, configGroup.project, gameType, read)
	}
	
	fun resolveValueSetValue(element: ParadoxScriptStringExpressionElement, name: String, configs: List<CwtConfig<*>>, configGroup: CwtConfigGroup): PsiElement? {
		val gameType = configGroup.gameType ?: return null
		for(config in configs) {
			val configExpression = config.expression ?: return null
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
		val config = configs.firstOrNull() ?: return null //first is ok
		val configExpression = config.expression ?: return null
		val read = configExpression.type == CwtDataType.Value
		val valueSetNames = configs.mapNotNull { it.expression?.value }
		return ParadoxValueSetValueElement(element, name, valueSetNames, configGroup.project, gameType, read)
	}
	
	fun resolveModifier(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val modifier = configGroup.modifiers[name] ?: return null
		val resolved = modifier.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, modifier)
		return resolved
	}
	
	fun resolveLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val linkConfig = configGroup.localisationLinks[name] ?: return null
		val resolved = linkConfig.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, linkConfig)
		return resolved
	}
	
	fun resolveLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
		val commandConfig = configGroup.localisationCommands[name] ?: return null
		val resolved = commandConfig.pointer.element ?: return null
		resolved.putUserData(PlsKeys.cwtConfigKey, commandConfig)
		return resolved
	}
	//endregion
}
