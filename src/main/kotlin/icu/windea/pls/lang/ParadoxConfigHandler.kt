@file:Suppress("UnusedReceiverParameter", "UNUSED_PARAMETER")

package icu.windea.pls.lang

import com.intellij.codeInsight.completion.*
import com.intellij.codeInsight.lookup.*
import com.intellij.lang.annotation.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import com.intellij.util.text.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.codeInsight.completion.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.expression.nodes.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.config.*
import icu.windea.pls.lang.data.*
import icu.windea.pls.lang.data.impl.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

/**
 * CWT规则的处理器。
 *
 * 提供基于CWT规则实现的匹配、校验、代码提示、引用解析等功能。
 */
@Suppress("UNCHECKED_CAST")
object ParadoxConfigHandler {
    //region Common Methods
    
    fun isAlias(propertyConfig: CwtPropertyConfig): Boolean {
        return propertyConfig.keyExpression.type == CwtDataType.AliasName
            && propertyConfig.valueExpression.type == CwtDataType.AliasMatchLeft
    }
    
    fun isSingleAlias(propertyConfig: CwtPropertyConfig): Boolean {
        return propertyConfig.valueExpression.type == CwtDataType.SingleAliasRight
    }
    
    fun isComplexEnum(config: CwtDataConfig<*>): Boolean {
        return config.expression.type == CwtDataType.EnumValue
            && config.expression.value?.let { config.info.configGroup.complexEnums[it] } != null
    }
    
    /**
     * 从CWT规则元素推断得到对应的CWT规则组。
     */
    @InferApi
    fun getConfigGroupFromCwt(from: PsiElement, project: Project): CwtConfigGroup? {
        val file = from.containingFile ?: return null
        val virtualFile = file.virtualFile ?: return null
        val path = virtualFile.path
        //这里的key可能是"core"，而这不是gameType
        val key = path.substringAfter("config/cwt/", "").substringBefore("/", "")
        if(key.isEmpty()) return null
        return getCwtConfig(project).get(key)
    }
    
    fun getEntryName(config: CwtConfig<*>): String? {
        return when {
            config is CwtPropertyConfig -> config.key
            config is CwtValueConfig && config.propertyConfig != null -> getEntryName(config.propertyConfig)
            config is CwtValueConfig -> null
            config is CwtAliasConfig -> config.subName
            else -> null
        }
    }
    
    fun getEntryConfigs(config: CwtConfig<*>): List<CwtDataConfig<*>> {
        val configGroup = config.info.configGroup
        return when {
            config is CwtPropertyConfig -> {
                config.inlineableConfig?.let { getEntryConfigs(it) }
                    ?: config.parent?.castOrNull<CwtPropertyConfig>()?.configs?.filter { it is CwtPropertyConfig && it.key == config.key }
                    ?: config.toSingletonList()
            }
            config is CwtValueConfig && config.propertyConfig != null -> {
                getEntryConfigs(config.propertyConfig)
            }
            config is CwtValueConfig -> {
                config.parent?.castOrNull<CwtPropertyConfig>()?.configs?.filter { it is CwtValueConfig }
                    ?: config.toSingletonList()
            }
            config is CwtSingleAliasConfig -> {
                config.config.toSingletonListOrEmpty()
            }
            config is CwtAliasConfig -> {
                configGroup.aliasGroups.get(config.name)?.get(config.subName)?.map { it.config }.orEmpty()
            }
            else -> {
                emptyList()
            }
        }
    }
    
    val inBlockKeysKey = Key.create<Set<String>>("cwt.config.inBlockKeys")
    
    fun getInBlockKeys(config: CwtDataConfig<*>): Set<String> {
        return config.getOrPutUserData(inBlockKeysKey) {
            val keys = caseInsensitiveStringSet()
            config.configs?.forEach { if(it is CwtPropertyConfig && isInBlockKey(it)) keys.add(it.key) }
            when(config) {
                is CwtPropertyConfig -> {
                    val propertyConfig = config
                    propertyConfig.parent?.configs?.forEach { c ->
                        if(c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true) && c.pointer != propertyConfig.pointer) {
                            c.configs?.forEach { if(it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                        }
                    }
                }
                is CwtValueConfig -> {
                    val propertyConfig = config.propertyConfig
                    propertyConfig?.parent?.configs?.forEach { c ->
                        if(c is CwtPropertyConfig && c.key.equals(propertyConfig.key, true) && c.pointer != propertyConfig.pointer) {
                            c.configs?.forEach { if(it is CwtPropertyConfig && isInBlockKey(it)) keys.remove(it.key) }
                        }
                    }
                }
            }
            return keys
        }
    }
    
    private fun isInBlockKey(config: CwtPropertyConfig): Boolean {
        return config.keyExpression.type == CwtDataType.Constant && config.cardinality.isRequired()
    }
    //endregion
    
    //region Matches Methods
    //DONE 基于cwt规则文件的匹配方法需要进一步匹配scope
    //DONE 兼容scriptedVariableReference inlineMath parameter
    fun matchesScriptExpression(
        element: PsiElement,
        expression: ParadoxDataExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        matchType: Int = CwtConfigMatchType.DEFAULT
    ): Boolean {
        ProgressManager.checkCanceled()
        val isStatic = BitUtil.isSet(matchType, CwtConfigMatchType.STATIC)
        val isNotExact = BitUtil.isSet(matchType, CwtConfigMatchType.NOT_EXACT)
        
        //匹配空字符串
        if(configExpression.isEmpty()) {
            return expression.isEmpty()
        }
        
        val project = configGroup.project
        val isParameterized = expression.type == ParadoxDataType.StringType && expression.text.isParameterized()
        when(configExpression.type) {
            CwtDataType.Block -> {
                if(expression.isKey != false) return false
                if(expression.type != ParadoxDataType.BlockType) return false
                if(isNotExact) return true //非精确匹配 - 直接使用第一个
                if(config !is CwtDataConfig) return true
                return matchesScriptExpressionInBlock(element, config, configGroup)
            }
            CwtDataType.Bool -> {
                return expression.type.isBooleanType()
            }
            CwtDataType.Int -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) {
                    if(isNotExact) return true
                    val (min, max) = configExpression.extraValue<Tuple2<Int?, Int?>>() ?: return true
                    val value = expression.text.toIntOrNull() ?: return true
                    return (min == null || min <= value) && (max == null || max >= value)
                }
                return false
            }
            CwtDataType.Float -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) {
                    if(isNotExact) return true
                    val (min, max) = configExpression.extraValue<Tuple2<Float?, Float?>>() ?: return true
                    val value = expression.text.toFloatOrNull() ?: return true
                    return (min == null || min <= value) && (max == null || max >= value)
                }
                return false
            }
            CwtDataType.Scalar -> {
                return when {
                    expression.isKey == true -> true //key -> ok
                    expression.type == ParadoxDataType.ParameterType -> true //parameter -> ok
                    expression.type.isBooleanType() -> true //boolean -> sadly, also ok for compatibility
                    expression.type.isIntType() -> true //number -> ok according to vanilla game files
                    expression.type.isFloatType() -> true //number -> ok according to vanilla game files
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
                if(isParameterized) return true
                if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
                    val selector = localisationSelector(project, element)
                    return ParadoxLocalisationSearch.search(expression.text, selector).findFirst() != null
                }
                return true
            }
            CwtDataType.SyncedLocalisation -> {
                if(!expression.type.isStringType()) return false
                if(isStatic) return true
                if(isParameterized) return true
                if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
                    val selector = localisationSelector(project, element)
                    return ParadoxSyncedLocalisationSearch.search(expression.text, selector).findFirst() != null
                }
                return true
            }
            CwtDataType.InlineLocalisation -> {
                if(!expression.type.isStringType()) return false
                if(expression.quoted) return true //"quoted_string" -> any string
                if(isStatic) return true
                if(isParameterized) return true
                if(BitUtil.isSet(matchType, CwtConfigMatchType.LOCALISATION)) {
                    val selector = localisationSelector(project, element)
                    return ParadoxLocalisationSearch.search(expression.text, selector).findFirst() != null
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
                if(isParameterized) return true
                val typeExpression = configExpression.value ?: return false //invalid cwt config
                if(BitUtil.isSet(matchType, CwtConfigMatchType.DEFINITION)) {
                    val selector = definitionSelector(project, element)
                    return ParadoxDefinitionSearch.search(expression.text, typeExpression, selector).findFirst() != null
                }
                return true
            }
            CwtDataType.EnumValue -> {
                if(!isStatic && isParameterized) return true
                val name = expression.text
                val enumName = configExpression.value ?: return false //invalid cwt config
                //匹配简单枚举
                val enumConfig = configGroup.enums[enumName]
                if(enumConfig != null) {
                    return name in enumConfig.values
                }
                //匹配复杂枚举
                if(!expression.type.isStringType()) return false
                if(isStatic) return true
                if(BitUtil.isSet(matchType, CwtConfigMatchType.COMPLEX_ENUM_VALUE)) {
                    val complexEnumConfig = configGroup.complexEnums[enumName]
                    if(complexEnumConfig != null) {
                        val searchScope = complexEnumConfig.searchScopeType
                        val selector = complexEnumValueSelector(project, element)
                            .withSearchScopeType(searchScope)
                        val complexEnumValueInfo = ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst()
                        return complexEnumValueInfo != null
                    }
                }
                //complexEnumValue的值必须合法
                return ParadoxComplexEnumValueHandler.getName(expression.text) != null
            }
            CwtDataType.Value -> {
                if(!expression.type.isStringType()) return false
                if(isStatic) return true
                if(isParameterized) return true
                //valueSetValue的值必须合法
                val name = ParadoxValueSetValueHandler.getName(expression.text)
                if(name == null) return false
                val valueSetName = configExpression.value
                if(valueSetName == null) return false
                return true
            }
            CwtDataType.ValueSet -> {
                if(!expression.type.isStringType()) return false
                if(isStatic) return true
                if(isParameterized) return true
                //valueSetValue的值必须合法
                val name = ParadoxValueSetValueHandler.getName(expression.text)
                if(name == null) return false
                val valueSetName = configExpression.value
                if(valueSetName == null) return false
                return true
            }
            CwtDataType.ScopeField, CwtDataType.Scope, CwtDataType.ScopeGroup -> {
                if(expression.quoted) return false //不允许用引号括起
                if(isStatic) return true
                if(isParameterized) return true
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
                        val scopeContext = ParadoxScopeHandler.getScopeContext(scopeFieldExpression, parentScopeContext)
                        if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return true
                    }
                    CwtDataType.ScopeGroup -> {
                        val expectedScopeGroup = configExpression.value ?: return true
                        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return true
                        val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return true
                        val scopeContext = ParadoxScopeHandler.getScopeContext(scopeFieldExpression, parentScopeContext)
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
                if(isParameterized) return true
                if(expression.quoted) return false //接下来的匹配不允许用引号括起
                val textRange = TextRange.create(0, expression.text.length)
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                return valueFieldExpression != null
            }
            CwtDataType.IntValueField -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
                if(isStatic) return true
                if(isParameterized) return true
                if(expression.quoted) return false //接下来的匹配不允许用引号括起
                val textRange = TextRange.create(0, expression.text.length)
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                return valueFieldExpression != null
            }
            CwtDataType.VariableField -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
                if(isStatic) return true
                if(isParameterized) return true
                if(expression.quoted) return false //接下来的匹配不允许用引号括起
                val textRange = TextRange.create(0, expression.text.length)
                val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                return variableFieldExpression != null
            }
            CwtDataType.IntVariableField -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
                if(isStatic) return true
                if(isParameterized) return true
                if(expression.quoted) return false //接下来的匹配不允许用引号括起
                val textRange = TextRange.create(0, expression.text.length)
                val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                return variableFieldExpression != null
            }
            CwtDataType.Modifier -> {
                if(isStatic) return true
                if(isParameterized) return true
                //匹配预定义的modifier
                return matchesModifier(element, expression.text, configGroup)
            }
            CwtDataType.Parameter -> {
                //匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                return expression.type.isStringLikeType()
            }
            CwtDataType.ParameterValue -> {
                return expression.type != ParadoxDataType.BlockType
            }
            CwtDataType.LocalisationParameter -> {
                //匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                return expression.type.isStringLikeType()
            }
            CwtDataType.ShaderEffect -> {
                //暂时作为一般的字符串处理
                return expression.type.isStringLikeType()
            }
            CwtDataType.SingleAliasRight -> {
                return false //不在这里处理
            }
            CwtDataType.AliasKeysField -> {
                if(!isStatic && isParameterized) return true
                val aliasName = configExpression.value ?: return false
                return matchesAliasName(element, expression, aliasName, configGroup, matchType)
            }
            CwtDataType.AliasName -> {
                if(!isStatic && isParameterized) return true
                val aliasName = configExpression.value ?: return false
                return matchesAliasName(element, expression, aliasName, configGroup, matchType)
            }
            CwtDataType.AliasMatchLeft -> {
                return false //不在这里处理
            }
            CwtDataType.Template -> {
                if(!expression.type.isStringLikeType()) return false
                //允许用引号括起
                if(isStatic) return true
                if(isParameterized) return true
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
            CwtDataType.Any -> {
                return true
            }
            CwtDataType.Other -> {
                return false
            }
            else -> {
                val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression)
                if(pathReferenceExpressionSupport != null) {
                    if(!expression.type.isStringType()) return false
                    if(isStatic) return true
                    if(isParameterized) return true
                    if(BitUtil.isSet(matchType, CwtConfigMatchType.FILE_PATH)) {
                        val pathReference = expression.text.normalizePath()
                        val selector = fileSelector(project, element)
                        return ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null
                    }
                    return true
                }
                return false
            }
        }
    }
    
    private fun matchesScriptExpressionInBlock(element: PsiElement, config: CwtDataConfig<*>, configGroup: CwtConfigGroup): Boolean {
        val block = when {
            element is ParadoxScriptProperty -> element.propertyValue()
            element is ParadoxScriptBlock -> element
            else -> null
        } ?: return false
        //简单判断：如果block中包含configsInBlock声明的必须的任意propertyKey（作为常量字符串，忽略大小写），则认为匹配
        //注意：不同的子句规则可以拥有部分相同的propertyKey
        val keys = getInBlockKeys(config)
        if(keys.isEmpty()) return true
        val actualKeys = mutableSetOf<String>()
        //注意这里需要考虑内联和可选的情况
        block.processData(conditional = true, inline = true) {
            if(it is ParadoxScriptProperty) actualKeys.add(it.name)
            true
        }
        return actualKeys.any { it in keys }
    }
    
    fun matchesAliasName(
        element: PsiElement,
        expression: ParadoxDataExpression,
        aliasName: String,
        configGroup: CwtConfigGroup,
        matchType: Int = CwtConfigMatchType.DEFAULT
    ): Boolean {
        val aliasSubName = getAliasSubName(element, expression.text, expression.quoted, aliasName, configGroup, matchType) ?: return false
        val configExpression = CwtKeyExpression.resolve(aliasSubName)
        return matchesScriptExpression(element, expression, configExpression, null, configGroup, matchType)
    }
    
    fun matchesModifier(element: PsiElement, name: String, configGroup: CwtConfigGroup): Boolean {
        return ParadoxModifierHandler.matchesModifier(name, element, configGroup)
    }
    
    fun matchesTemplateExpression(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.DEFAULT): Boolean {
        val templateConfigExpression = CwtTemplateExpression.resolve(configExpression.expressionString)
        return templateConfigExpression.matches(expression.text, element, configGroup, matchType)
    }
    
    fun getAliasSubName(element: PsiElement, key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.DEFAULT): String? {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
        if(constKey != null) return constKey
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
        val expression = ParadoxDataExpression.resolve(key, quoted, true)
        return keys.find {
            matchesScriptExpression(element, expression, CwtKeyExpression.resolve(it), null, configGroup, matchType)
        }
    }
    
    fun getAliasSubNames(element: PsiElement, key: String, quoted: Boolean, aliasName: String, configGroup: CwtConfigGroup, matchType: Int = CwtConfigMatchType.DEFAULT): List<String> {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) //不区分大小写
        if(constKey != null) return listOf(constKey)
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return emptyList()
        val expression = ParadoxDataExpression.resolve(key, quoted, true)
        return keys.filter {
            matchesScriptExpression(element, expression, CwtKeyExpression.resolve(it), null, configGroup, matchType)
        }
    }
    
    fun requireNotExactMatch(configExpression: CwtDataExpression): Boolean {
        return when {
            configExpression.type == CwtDataType.Block -> true
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
            CwtDataType.Block -> 100
            CwtDataType.Bool -> 100
            CwtDataType.Int -> 90
            CwtDataType.Float -> 90
            CwtDataType.Scalar -> 90
            CwtDataType.ColorField -> 90
            CwtDataType.PercentageField -> 90
            CwtDataType.DateField -> 90
            CwtDataType.Localisation -> 60
            CwtDataType.SyncedLocalisation -> 60
            CwtDataType.InlineLocalisation -> 60
            CwtDataType.StellarisNameFormat -> 60
            CwtDataType.AbsoluteFilePath -> 70
            CwtDataType.Icon -> 70
            CwtDataType.FilePath -> 70
            CwtDataType.FileName -> 70
            CwtDataType.Definition -> 70
            CwtDataType.EnumValue -> {
                val enumName = configExpression.value ?: return 0 //不期望匹配到
                if(configGroup.enums.containsKey(enumName)) return 80
                if(configGroup.complexEnums.containsKey(enumName)) return 45
                return 0 //不期望匹配到，规则有误！
            }
            CwtDataType.Value -> {
                val valueSetName = configExpression.value ?: return 0 //不期望匹配到
                if(configGroup.values.containsKey(valueSetName)) return 80
                return 40
            }
            CwtDataType.ValueSet -> 40
            CwtDataType.ScopeField -> 50
            CwtDataType.Scope -> 50
            CwtDataType.ScopeGroup -> 50
            CwtDataType.ValueField -> 45
            CwtDataType.IntValueField -> 45
            CwtDataType.VariableField -> 45
            CwtDataType.IntVariableField -> 45
            CwtDataType.Modifier -> 75 //higher than Definition
            CwtDataType.Parameter -> 10
            CwtDataType.ParameterValue -> 90 //same to Scalar
            CwtDataType.LocalisationParameter -> 10
            CwtDataType.ShaderEffect -> 85 // (80,90)
            CwtDataType.SingleAliasRight -> 0 //不期望匹配到
            CwtDataType.AliasName -> 0 //不期望匹配到
            CwtDataType.AliasKeysField -> 0 //不期望匹配到
            CwtDataType.AliasMatchLeft -> 0 //不期望匹配到
            CwtDataType.Template -> 65
            CwtDataType.Constant -> 100
            CwtDataType.Any -> 1
            CwtDataType.Other -> 0 //不期望匹配到
        }
    }
    //endregion
    
    //region Annotate Methods
    fun annotateScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, holder: AnnotationHolder) {
        val text = element.text
        val expression = rangeInElement?.substring(text) ?: element.value
        
        ParadoxScriptExpressionSupport.annotate(element, rangeInElement, expression, holder, config)
    }
    
    fun annotateScriptExpression(element: ParadoxScriptExpressionElement, range: TextRange, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        if(element !is ParadoxScriptStringExpressionElement) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
            return
        }
        //进行特殊代码高亮时，可能需要跳过字符串表达式中的参数部分
        val parameterRanges = getParameterRanges(element)
        if(parameterRanges.isEmpty()) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
        } else {
            val finalRanges = TextRangeUtil.excludeRanges(range, parameterRanges)
            finalRanges.forEach { r ->
                if(!r.isEmpty) {
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r).textAttributes(attributesKey).create()
                }
            }
        }
    }
    
    fun setParameterRanges(element: ParadoxScriptStringExpressionElement): List<TextRange> {
        var parameterRanges: SmartList<TextRange>? = null
        element.processChild { parameter ->
            if(parameter is ParadoxParameter) {
                if(parameterRanges == null) parameterRanges = SmartList()
                parameterRanges?.add(parameter.textRange)
            }
            true
        }
        val result = parameterRanges.orEmpty()
        element.putUserData(PlsKeys.parameterRangesKey, result)
        return result
    }
    
    fun getParameterRanges(element: ParadoxScriptStringExpressionElement): List<TextRange> {
        return element.getUserData(PlsKeys.parameterRangesKey).orEmpty()
    }
    
    fun annotateComplexExpression(element: ParadoxScriptExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>) {
        if(element !is ParadoxScriptStringExpressionElement) return
        doAnnotateComplexExpression(element, expression, holder, config)
    }
    
    private fun doAnnotateComplexExpression(element: ParadoxScriptStringExpressionElement, expressionNode: ParadoxExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>) {
        val attributesKey = expressionNode.getAttributesKey()
        val mustUseAttributesKey = attributesKey != ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY && attributesKey != ParadoxScriptAttributesKeys.STRING_KEY
        if(attributesKey != null && mustUseAttributesKey) {
            doAnnotateComplexExpressionByAttributesKey(expressionNode, element, holder, attributesKey)
        } else {
            val attributesKeyConfig = expressionNode.getAttributesKeyConfig(element)
            if(attributesKeyConfig != null) {
                val rangeInElement = expressionNode.rangeInExpression.shiftRight(if(element.text.isLeftQuoted()) 1 else 0)
                annotateScriptExpression(element, rangeInElement, attributesKeyConfig, holder)
                
                //create tooltip
                if(attributesKeyConfig is CwtValueConfig) {
                    val inferredConfigExpression = attributesKeyConfig.expression.expressionString
                    val tooltip = PlsBundle.message("inferred.config.expression", inferredConfigExpression.escapeXml())
                    holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeInElement.shiftRight(element.startOffset)).tooltip(tooltip).create()
                }
            } else if(attributesKey != null) {
                doAnnotateComplexExpressionByAttributesKey(expressionNode, element, holder, attributesKey)
            }
        }
        
        if(expressionNode.nodes.isNotEmpty()) {
            for(node in expressionNode.nodes) {
                doAnnotateComplexExpression(element, node, holder, config)
            }
        }
    }
    
    private fun doAnnotateComplexExpressionByAttributesKey(expressionNode: ParadoxExpressionNode, element: ParadoxScriptStringExpressionElement, holder: AnnotationHolder, attributesKey: TextAttributesKey) {
        val rangeToAnnotate = expressionNode.rangeInExpression.shiftRight(element.textRange.unquote(element.text).startOffset)
        if(expressionNode is ParadoxTokenExpressionNode) {
            //override default highlight by highlighter (property key or string)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeToAnnotate).textAttributes(HighlighterColors.TEXT).create()
        }
        annotateScriptExpression(element, rangeToAnnotate, attributesKey, holder)
    }
    //endregion
    
    //region Complete Methods
    fun addRootKeyCompletions(definitionElement: ParadoxScriptDefinitionElement, context: ProcessingContext, result: CompletionResultSet) {
        val originalFile = context.originalFile
        val project = originalFile.project
        val gameType = selectGameType(originalFile) ?: return
        val configGroup = getCwtConfig(project).get(gameType)
        val elementPath = ParadoxElementPathHandler.getFromFile(definitionElement, PlsConstants.maxDefinitionDepth) ?: return
        
        context.isKey = true
        context.configGroup = configGroup
        
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
        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchType = CwtConfigMatchType.DEFAULT or CwtConfigMatchType.NOT_EXACT
        val parentConfigs = getConfigs(definitionElement, allowDefinition = true, matchType = matchType)
        val configs = SmartList<CwtPropertyConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if(c2 is CwtPropertyConfig) {
                    configs.add(c2)
                }
            }
        }
        if(configs.isEmpty()) return
        val occurrenceMap = getChildOccurrenceMap(definitionElement, parentConfigs)
        
        context.isKey = true
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(definitionElement)
        
        configs.groupBy { it.key }.forEach { (_, configsWithSameKey) ->
            for(config in configsWithSameKey) {
                if(shouldComplete(config, occurrenceMap)) {
                    val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(context.contextElement, config)
                    if(overriddenConfigs.isNotNullOrEmpty()) {
                        for(overriddenConfig in overriddenConfigs) {
                            context.config = overriddenConfig
                            completeScriptExpression(context, result)
                        }
                        continue
                    }
                    context.config = config
                    context.configs = configsWithSameKey
                    completeScriptExpression(context, result)
                }
            }
        }
        
        context.config = null
        context.configs = null
        return
    }
    
    fun addValueCompletions(memberElement: ParadoxScriptMemberElement, context: ProcessingContext, result: CompletionResultSet) {
        val definitionMemberInfo = memberElement.definitionMemberInfo
        if(definitionMemberInfo == null) return
        
        val configGroup = definitionMemberInfo.configGroup
        //这里不要使用合并后的子规则，需要先尝试精确匹配或者合并所有非精确匹配的规则，最后得到子规则列表
        val matchType = CwtConfigMatchType.DEFAULT or CwtConfigMatchType.NOT_EXACT
        val parentConfigs = getConfigs(memberElement, allowDefinition = true, matchType = matchType)
        val configs = SmartList<CwtValueConfig>()
        parentConfigs.forEach { c1 ->
            c1.configs?.forEach { c2 ->
                if(c2 is CwtValueConfig) {
                    configs.add(c2)
                }
            }
        }
        if(configs.isEmpty()) return
        val occurrenceMap = getChildOccurrenceMap(memberElement, parentConfigs)
        
        context.isKey = false
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(memberElement)
        
        for(config in configs) {
            if(shouldComplete(config, occurrenceMap)) {
                val overriddenConfigs = ParadoxOverriddenConfigProvider.getOverriddenConfigs(context.contextElement, config)
                if(overriddenConfigs.isNotNullOrEmpty()) {
                    for(overriddenConfig in overriddenConfigs) {
                        context.config = overriddenConfig
                        completeScriptExpression(context, result)
                    }
                    continue
                }
                context.config = config
                completeScriptExpression(context, result)
            }
        }
        
        context.config = null
        context.configs = null
        return
    }
    
    fun addPropertyValueCompletions(definitionElement: ParadoxScriptDefinitionElement, context: ProcessingContext, result: CompletionResultSet) {
        val definitionMemberInfo = definitionElement.definitionMemberInfo
        if(definitionMemberInfo == null) return
        
        val configGroup = definitionMemberInfo.configGroup
        val configs = definitionMemberInfo.getConfigs()
        if(configs.isEmpty()) return
        
        context.isKey = false
        context.configGroup = configGroup
        context.scopeContext = ParadoxScopeHandler.getScopeContext(definitionElement)
        
        for(config in configs) {
            if(config is CwtPropertyConfig) {
                val valueConfig = config.valueConfig ?: continue
                context.config = valueConfig
                completeScriptExpression(context, result)
            }
        }
        
        context.config = null
        return
    }
    
    private fun shouldComplete(config: CwtPropertyConfig, occurrenceMap: Map<CwtDataExpression, Occurrence>): Boolean {
        val expression = config.keyExpression
        //如果类型是aliasName，则无论cardinality如何定义，都应该提供补全（某些cwt规则文件未正确编写）
        if(expression.type == CwtDataType.AliasName) return true
        val actualCount = occurrenceMap[expression]?.actual ?: 0
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
    
    private fun shouldComplete(config: CwtValueConfig, occurrenceMap: Map<CwtDataExpression, Occurrence>): Boolean {
        val expression = config.valueExpression
        val actualCount = occurrenceMap[expression]?.actual ?: 0
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
        val path = fileInfo.pathToEntry //这里使用pathToEntry
        val infoMap = mutableMapOf<String, MutableList<Tuple2<CwtTypeConfig, CwtSubtypeConfig?>>>()
        for(typeConfig in configGroup.types.values) {
            if(ParadoxDefinitionHandler.matchesTypeWithUnknownDeclaration(typeConfig, path, null, null)) {
                val skipRootKeyConfig = typeConfig.skipRootKey
                if(skipRootKeyConfig == null || skipRootKeyConfig.isEmpty()) {
                    if(elementPath.isEmpty()) {
                        typeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                            infoMap.getOrPut(it) { SmartList() }.add(typeConfig to null)
                        }
                        typeConfig.subtypes.values.forEach { subtypeConfig ->
                            subtypeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                infoMap.getOrPut(it) { SmartList() }.add(typeConfig to subtypeConfig)
                            }
                        }
                    }
                } else {
                    for(skipConfig in skipRootKeyConfig) {
                        val relative = elementPath.relativeTo(skipConfig) ?: continue
                        if(relative.isEmpty()) {
                            typeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
                                infoMap.getOrPut(it) { SmartList() }.add(typeConfig to null)
                            }
                            typeConfig.subtypes.values.forEach { subtypeConfig ->
                                subtypeConfig.typeKeyFilter?.takeIfTrue()?.forEach {
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
            val config = when {
                typeToUse == null -> null
                else -> {
                    val configContext = CwtConfigContext(context.contextElement, null, typeToUse, subtypesToUse, configGroup)
                    configGroup.declarations[typeToUse]?.getMergedConfig(configContext)
                }
            }
            val element = config?.pointer?.element
            val icon = if(config != null) PlsIcons.Definition else PlsIcons.Property
            val tailText = if(tuples.isEmpty()) null
            else tuples.joinToString(", ", " for ") { (typeConfig, subTypeConfig) ->
                if(subTypeConfig != null) "${typeConfig.name}.${subTypeConfig.name}" else typeConfig.name
            }
            val typeFile = config?.pointer?.containingFile
            context.config = config
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
            context.config = null
        }
    }
    
    fun completeScriptExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        val configExpression = config.expression ?: return@with
        val config = config
        val configGroup = configGroup
        
        if(configExpression.isEmpty()) return
        
        //匹配作用域
        if(scopeMatched) {
            val scopeContext = scopeContext
            val supportedScopes = when {
                config is CwtPropertyConfig -> config.supportedScopes
                config is CwtAliasConfig -> config.supportedScopes
                config is CwtLinkConfig -> config.inputScopes
                else -> null
            }
            val scopeMatched = when {
                scopeContext == null -> true
                else -> ParadoxScopeHandler.matchesScope(scopeContext, supportedScopes, configGroup)
            }
            if(!scopeMatched && getSettings().completion.completeOnlyScopeIsMatched) return
            put(PlsCompletionKeys.scopeMatchedKey, scopeMatched)
        }
        
        ParadoxScriptExpressionSupport.complete(context, result)
        
        put(PlsCompletionKeys.scopeContextKey, scopeContext)
        put(PlsCompletionKeys.scopeMatchedKey, null)
    }
    
    fun completeAliasName(aliasName: String, context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        val config = config
        val configs = configs
        
        val aliasGroup = configGroup.aliasGroups[aliasName] ?: return
        for(aliasConfigs in aliasGroup.values) {
            //aliasConfigs的名字是相同的 
            val aliasConfig = aliasConfigs.firstOrNull() ?: continue
            //aliasSubName是一个表达式
            if(isKey == true) {
                context.config = aliasConfig
                context.configs = aliasConfigs
                completeScriptExpression(context, result)
            } else {
                context.config = aliasConfig
                completeScriptExpression(context, result)
            }
            context.config = config
            context.configs = configs
        }
    }
    
    fun completeModifier(context: ProcessingContext, result: CompletionResultSet) = with(context) {
        ProgressManager.checkCanceled()
        ParadoxModifierHandler.completeModifier(context, result)
    }
    
    fun completeTemplateExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        val element = contextElement
        if(element !is ParadoxScriptStringExpressionElement) return
        val configExpression = context.config.expression ?: return
        val template = CwtTemplateExpression.resolve(configExpression.expressionString)
        val scopeMatched = context.scopeMatched
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
        ProgressManager.checkCanceled()
        //基于当前位置的代码补全
        if(quoted) return
        val startOffset = context.startOffset ?: 0
        val textRange = TextRange.create(startOffset, startOffset + keyword.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(keyword, textRange, configGroup, isKey, true) ?: return
        //合法的表达式需要匹配scopeName或者scopeGroupName，来自scope[xxx]或者scope_group[xxx]中的xxx，目前不基于此进行过滤
        scopeFieldExpression.complete(context, result)
    }
    
    fun completeValueFieldExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        //基于当前位置的代码补全
        if(quoted) return
        val startOffset = context.startOffset ?: 0
        val textRange = TextRange.create(startOffset, startOffset + keyword.length)
        val valueFieldExpression = ParadoxValueFieldExpression.resolve(keyword, textRange, configGroup, isKey, true) ?: return
        valueFieldExpression.complete(context, result)
    }
    
    fun completeVariableFieldExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        //基于当前位置的代码补全
        if(quoted) return
        val startOffset = context.startOffset ?: 0
        val textRange = TextRange.create(startOffset, startOffset + keyword.length)
        val variableFieldExpression = ParadoxVariableFieldExpression.resolve(keyword, textRange, configGroup, isKey, true) ?: return
        variableFieldExpression.complete(context, result)
    }
    
    fun completeValueSetValueExpression(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        //基于当前位置的代码补全
        if(quoted) return
        val startOffset = context.startOffset ?: 0
        val textRange = TextRange.create(startOffset, startOffset + keyword.length)
        val valueSetValueExpression = ParadoxValueSetValueExpression.resolve(keyword, textRange, config, configGroup, isKey, true) ?: return
        valueSetValueExpression.complete(context, result)
    }
    
    fun completeSystemScope(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
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
        ProgressManager.checkCanceled()
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
        ProgressManager.checkCanceled()
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
        ProgressManager.checkCanceled()
        val config = config
        val configs = configs
        val scopeContext = scopeContext
        
        val linkConfigs = when {
            prefix == null -> configGroup.linksAsScopeWithoutPrefix.values
            else -> configGroup.linksAsScopeWithPrefix.values.filter { prefix == it.prefix }
        }
        
        if(dataSourceNodeToCheck is ParadoxScopeFieldExpressionNode) {
            completeForScopeExpressionNode(dataSourceNodeToCheck, context, result)
            context.scopeContext = scopeContext
            return@with
        }
        if(dataSourceNodeToCheck is ParadoxValueSetValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return@with
        }
        
        context.configs = linkConfigs
        for(linkConfig in linkConfigs) {
            context.config = linkConfig
            completeScriptExpression(context, result)
        }
        context.config = config
        context.configs = configs
        context.scopeMatched = null
    }
    
    fun completeValueLinkValue(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
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
        ProgressManager.checkCanceled()
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
        ProgressManager.checkCanceled()
        val config = config
        val configs = configs
        
        val linkConfigs = when {
            prefix == null -> configGroup.linksAsValueWithoutPrefix.values
            else -> configGroup.linksAsValueWithPrefix.values.filter { prefix == it.prefix }
        }
        
        if(dataSourceNodeToCheck is ParadoxValueSetValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return@with
        }
        if(dataSourceNodeToCheck is ParadoxScriptValueExpression) {
            dataSourceNodeToCheck.complete(context, result)
            return@with
        }
        
        context.configs = linkConfigs
        for(linkConfig in linkConfigs) {
            context.config = linkConfig
            completeScriptExpression(context, result)
        }
        context.config = config
        context.configs = configs
        context.scopeMatched = null
    }
    
    fun completeValueSetValue(context: ProcessingContext, result: CompletionResultSet): Unit = with(context) {
        ProgressManager.checkCanceled()
        val configs = configs
        if(configs.isNotNullOrEmpty()) {
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
        val keyword = context.keyword
        val project = configGroup.project
        
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
            val selector = valueSetValueSelector(project, contextElement).distinctByName()
            ParadoxValueSetValueSearch.search(valueSetName, selector).processQueryAsync p@{ info ->
                ProgressManager.checkCanceled()
                if(info.name == keyword) return@p true //排除和当前输入的同名的
                val element = ParadoxValueSetValueElement(contextElement, info, project)
                //去除后面的作用域信息
                val icon = PlsIcons.ValueSetValue(valueSetName)
                //不显示typeText
                val builder = ParadoxScriptExpressionLookupElementBuilder.create(element, info.name)
                    .withIcon(icon)
                    .withTailText(tailText)
                result.addScriptExpressionElement(context, builder)
                true
            }
        }
    }
    
    fun completePredefinedValueSetValue(valueSetName: String, result: CompletionResultSet, context: ProcessingContext) = with(context) {
        ProgressManager.checkCanceled()
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
        ProgressManager.checkCanceled()
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
        ProgressManager.checkCanceled()
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
    
    fun completeEventTarget(context: ProcessingContext, result: CompletionResultSet) = with(context) {
        ProgressManager.checkCanceled()
        val contextElement = contextElement
        val keyword = keyword
        val file = originalFile
        val project = file.project
        val eventTargetSelector = valueSetValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxValueSetValueSearch.search("event_target", eventTargetSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxValueSetValueElement(contextElement, info, project)
            val icon = PlsIcons.ValueSetValue
            val tailText = " from value[event_target]"
            val lookupElement = LookupElementBuilder.create(element, info.name)
                .withIcon(icon)
                .withTailText(tailText, true)
                .withCaseSensitivity(false) //忽略大小写
            result.addElement(lookupElement)
            true
        }
        
        val globalEventTargetSelector = valueSetValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxValueSetValueSearch.search("global_event_target", globalEventTargetSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxValueSetValueElement(contextElement, info, project)
            val icon = PlsIcons.ValueSetValue
            val tailText = " from value[global_event_target]"
            val lookupElement = LookupElementBuilder.create(element, info.name)
                .withIcon(icon)
                .withTailText(tailText, true)
                .withCaseSensitivity(false) //忽略大小写
            result.addElement(lookupElement)
            true
        }
    }
    
    fun completeScriptedLoc(context: ProcessingContext, result: CompletionResultSet) = with(context) {
        ProgressManager.checkCanceled()
        val file = originalFile
        val project = file.project
        val scriptedLocSelector = definitionSelector(project, file).contextSensitive().distinctByName()
        ParadoxDefinitionSearch.search("scripted_loc", scriptedLocSelector).processQueryAsync p@{ scriptedLoc ->
            ProgressManager.checkCanceled()
            val name = scriptedLoc.definitionInfo?.name ?: return@p true //不应该为空
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
    
    fun completeVariable(context: ProcessingContext, result: CompletionResultSet) = with(context) {
        ProgressManager.checkCanceled()
        val contextElement = contextElement
        val keyword = keyword
        val file = originalFile
        val project = file.project
        val variableSelector = valueSetValueSelector(project, file).contextSensitive().distinctByName()
        ParadoxValueSetValueSearch.search("variable", variableSelector).processQueryAsync p@{ info ->
            ProgressManager.checkCanceled()
            if(info.name == keyword) return@p true //排除和当前输入的同名的
            val element = ParadoxValueSetValueElement(contextElement, info, project)
            val icon = PlsIcons.Variable
            val tailText = " from variables"
            val lookupElement = LookupElementBuilder.create(element, info.name)
                .withIcon(icon)
                .withTailText(tailText, true)
                .withCaseSensitivity(false) //忽略大小写
            result.addElement(lookupElement)
            true
        }
    }
    
    fun completeConcept(context: ProcessingContext, result: CompletionResultSet) = with(context) {
        ProgressManager.checkCanceled()
        val file = originalFile
        val project = file.project
        val conceptSelector = definitionSelector(project, file).contextSensitive().distinctByName()
        val keysToDistinct = mutableSetOf<String>()
        ParadoxDefinitionSearch.search("game_concept", conceptSelector).processQueryAsync p@{ element ->
            val tailText = " from concepts"
            val icon = PlsIcons.LocalisationConceptName
            run action@{
                val key = element.name
                if(!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(element, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                result.addElement(lookupElement)
            }
            element.getData<StellarisGameConceptDataProvider.Data>()?.alias?.forEach action@{ alias ->
                val key = alias
                if(!keysToDistinct.add(key)) return@action
                val lookupElement = LookupElementBuilder.create(element, key)
                    .withIcon(icon)
                    .withTailText(tailText, true)
                result.addElement(lookupElement)
            }
            true
        }
    }
    
    fun getScriptExpressionTailText(config: CwtConfig<*>?, withExpression: Boolean = true): String? {
        val configExpression = config?.expression ?: return null
        val fileName = config.resolved().pointer.containingFile?.name
        if(withExpression) {
            if(fileName != null) {
                return " by $configExpression in $fileName"
            } else {
                return " by $configExpression"
            }
        } else {
            if(fileName != null) {
                return " in $fileName"
            } else {
                return null
            }
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
        
        val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.text.unquote()
        if(expression.isParameterized()) return null //排除引用文本带参数的情况
        
        if(config != null) {
            val result = ParadoxScriptExpressionSupport.resolve(element, rangeInElement, expression, config, isKey, exact)
            if(result != null) return result
        }
        
        if(config != null && configExpression is CwtKeyExpression && configExpression.type.isKeyReferenceType()) {
            return config.resolved().pointer.element
        }
        return null
    }
    
    fun multiResolveScriptExpression(element: ParadoxScriptExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>?, configExpression: CwtDataExpression?, configGroup: CwtConfigGroup, isKey: Boolean? = null): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        if(configExpression == null) return emptySet()
        
        val expression = rangeInElement?.substring(element.text)?.unquote() ?: element.text.unquote()
        if(expression.isParameterized()) return emptySet() //排除引用文本带参数的情况
        
        if(config != null) {
            val result = ParadoxScriptExpressionSupport.multiResolve(element, rangeInElement, expression, config, isKey)
            if(result.isNotEmpty()) return result
        }
        
        if(config != null && configExpression is CwtKeyExpression && configExpression.type.isKeyReferenceType()) {
            return config.resolved().pointer.element.toSingletonSetOrEmpty()
        }
        return emptySet()
    }
    
    fun resolveModifier(element: ParadoxScriptExpressionElement, name: String, configGroup: CwtConfigGroup): PsiElement? {
        if(element !is ParadoxScriptStringExpressionElement) return null
        ProgressManager.checkCanceled()
        return ParadoxModifierHandler.resolveModifier(name, element, configGroup)
    }
    
    fun resolveTemplateExpression(element: ParadoxScriptExpressionElement, text: String, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): ParadoxTemplateExpressionElement? {
        if(element !is ParadoxScriptStringExpressionElement) return null
        ProgressManager.checkCanceled()
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
    
    //region Get Methods
    fun getConfigs(element: PsiElement, allowDefinition: Boolean = element is ParadoxScriptValue, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.DEFAULT): List<CwtDataConfig<*>> {
        return when {
            element is ParadoxScriptDefinitionElement -> getPropertyConfigs(element, allowDefinition, orDefault, matchType)
            element is ParadoxScriptPropertyKey -> getPropertyConfigs(element, allowDefinition, orDefault, matchType)
            element is ParadoxScriptValue -> getValueConfigs(element, allowDefinition, orDefault, matchType)
            else -> emptyList()
        }
    }
    
    fun getPropertyConfigs(element: PsiElement, allowDefinition: Boolean = false, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.DEFAULT): List<CwtPropertyConfig> {
        return getConfigsFromCache(element, CwtPropertyConfig::class.java, allowDefinition, orDefault, matchType)
    }
    
    fun getValueConfigs(element: PsiElement, allowDefinition: Boolean = true, orDefault: Boolean = true, matchType: Int = CwtConfigMatchType.DEFAULT): List<CwtValueConfig> {
        return getConfigsFromCache(element, CwtValueConfig::class.java, allowDefinition, orDefault, matchType)
    }
    
    private fun <T : CwtDataConfig<*>> getConfigsFromCache(element: PsiElement, configType: Class<T>, allowDefinition: Boolean, orDefault: Boolean, matchType: Int): List<T> {
        val configsMap = getConfigsCacheFromCache(element) ?: return emptyList()
        val cacheKey = buildString {
            when(configType) {
                CwtPropertyConfig::class.java -> append("property")
                CwtValueConfig::class.java -> append("value")
                else -> throw UnsupportedOperationException()
            }
            if(configType == CwtPropertyConfig::class.java) {
                append("#").append(allowDefinition.toIntString())
            }
            append("#").append(orDefault.toIntString())
            append("#").append(matchType)
        }
        return configsMap.getOrPut(cacheKey) { resolveConfigs(element, configType, allowDefinition, orDefault, matchType) } as List<T>
    }
    
    private fun getConfigsCacheFromCache(element: PsiElement): MutableMap<String, List<CwtConfig<*>>>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedConfigsCacheKey) {
            val value = ConcurrentHashMap<String, List<CwtConfig<*>>>()
            //invalidated on file modification or ScriptFileTracker
            val file = element.containingFile
            val tracker = ParadoxPsiModificationTracker.getInstance(file.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, file, tracker)
        }
    }
    
    private fun <T : CwtDataConfig<*>> resolveConfigs(element: PsiElement, configType: Class<T>, allowDefinition: Boolean, orDefault: Boolean, matchType: Int): List<T> {
        //当输入的元素是key或property时，输入的规则类型必须是property
        val result = when(configType) {
            CwtPropertyConfig::class.java -> {
                val memberElement = when {
                    element is ParadoxScriptDefinitionElement -> element
                    element is ParadoxScriptPropertyKey -> element.parent as? ParadoxScriptProperty ?: return emptyList()
                    else -> throw UnsupportedOperationException()
                }
                val expression = when {
                    element is ParadoxScriptProperty -> element.propertyValue?.let { ParadoxDataExpression.resolve(it, matchType) }
                    element is ParadoxScriptFile -> BlockParadoxDataExpression
                    element is ParadoxScriptPropertyKey -> element.propertyValue?.let { ParadoxDataExpression.resolve(it, matchType) }
                    else -> throw UnsupportedOperationException()
                }
                val definitionMemberInfo = memberElement.definitionMemberInfo ?: return emptyList()
                if(!allowDefinition && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
                
                //如果无法匹配value，则取第一个
                val configs = definitionMemberInfo.getConfigs(matchType)
                val configGroup = definitionMemberInfo.configGroup
                val resultConfigs = SmartList<CwtPropertyConfig>()
                run {
                    //不完整的属性 - 不匹配值
                    if(expression == null) {
                        for(config in configs) {
                            if(config !is CwtPropertyConfig) continue
                            resultConfigs.add(config)
                        }
                        return@run
                    }
                    //精确匹配
                    for(config in configs) {
                        if(config !is CwtPropertyConfig) continue
                        ProgressManager.checkCanceled()
                        if(matchesScriptExpression(memberElement, expression, config.valueExpression, config, configGroup, matchType)) {
                            resultConfigs.add(config)
                        }
                    }
                    //精确匹配无结果 - 不精确匹配
                    if(resultConfigs.isEmpty()) {
                        val newMatchType = matchType or CwtConfigMatchType.NOT_EXACT
                        for(config in configs) {
                            if(config !is CwtPropertyConfig) continue
                            ProgressManager.checkCanceled()
                            val configExpression = config.valueExpression
                            if(!requireNotExactMatch(configExpression)) continue
                            if(matchesScriptExpression(memberElement, expression, configExpression, config, configGroup, newMatchType)) {
                                resultConfigs.add(config)
                            }
                        }
                    }
                    //仍然无结果 - 判断是否使用默认值
                    if(orDefault && resultConfigs.isEmpty()) {
                        configs.forEach { it.castOrNull<CwtPropertyConfig>()?.let<CwtPropertyConfig, Unit> { c -> resultConfigs.add(c) } }
                    }
                }
                resultConfigs as List<T>
            }
            CwtValueConfig::class.java -> {
                val valueElement = when {
                    element is ParadoxScriptValue -> element
                    else -> throw UnsupportedOperationException()
                }
                val expression = ParadoxDataExpression.resolve(valueElement, matchType)
                val parent = element.parent
                when(parent) {
                    //如果value是property的value
                    is ParadoxScriptProperty -> {
                        val property = parent
                        val definitionMemberInfo = property.definitionMemberInfo ?: return emptyList()
                        if(!allowDefinition && definitionMemberInfo.elementPath.isEmpty()) return emptyList()
                        
                        ProgressManager.checkCanceled()
                        val configs = definitionMemberInfo.getConfigs(matchType)
                        val configGroup = definitionMemberInfo.configGroup
                        val resultConfigs = SmartList<CwtValueConfig>()
                        run {
                            //精确匹配
                            for(config in configs) {
                                if(config !is CwtPropertyConfig) continue
                                ProgressManager.checkCanceled()
                                val valueConfig = config.valueConfig ?: continue
                                if(matchesScriptExpression(valueElement, expression, valueConfig.expression, config, configGroup, matchType)) {
                                    resultConfigs.add(valueConfig)
                                }
                            }
                            //精确匹配无结果 - 不精确匹配
                            if(resultConfigs.isEmpty()) {
                                val newMatchType = matchType or CwtConfigMatchType.NOT_EXACT
                                for(config in configs) {
                                    if(config !is CwtPropertyConfig) continue
                                    ProgressManager.checkCanceled()
                                    val valueConfig = config.valueConfig ?: continue
                                    val configExpression = valueConfig.expression
                                    if(!requireNotExactMatch(configExpression)) continue
                                    if(matchesScriptExpression(valueElement, expression, configExpression, config, configGroup, newMatchType)) {
                                        resultConfigs.add(valueConfig)
                                    }
                                }
                            }
                            //仍然无结果 - 判断是否使用默认值
                            if(orDefault && resultConfigs.isEmpty()) {
                                configs.forEach { it.castOrNull<CwtPropertyConfig>()?.valueConfig?.let<CwtValueConfig, Unit> { c -> resultConfigs.add(c) } }
                            }
                        }
                        resultConfigs as List<T>
                    }
                    //如果value是blockElement中的value
                    is ParadoxScriptBlockElement -> {
                        val property = parent.parent as? ParadoxScriptDefinitionElement ?: return emptyList()
                        val definitionMemberInfo = property.definitionMemberInfo ?: return emptyList()
                        
                        val childConfigs = definitionMemberInfo.getChildConfigs(matchType)
                        if(childConfigs.isEmpty()) return emptyList()
                        val configGroup = definitionMemberInfo.configGroup
                        val resultConfigs = SmartList<CwtValueConfig>()
                        run {
                            for(childConfig in childConfigs) {
                                if(childConfig !is CwtValueConfig) continue
                                ProgressManager.checkCanceled()
                                //精确匹配
                                if(matchesScriptExpression(valueElement, expression, childConfig.valueExpression, childConfig, configGroup, matchType)) {
                                    resultConfigs.add(childConfig)
                                }
                            }
                            //精确匹配无结果 - 不精确匹配
                            if(resultConfigs.isEmpty()) {
                                val newMatchType = matchType or CwtConfigMatchType.NOT_EXACT
                                for(childConfig in childConfigs) {
                                    if(childConfig !is CwtValueConfig) continue
                                    ProgressManager.checkCanceled()
                                    val configExpression = childConfig.valueExpression
                                    if(!requireNotExactMatch(configExpression)) continue
                                    if(matchesScriptExpression(valueElement, expression, configExpression, childConfig, configGroup, newMatchType)) {
                                        resultConfigs.add(childConfig)
                                    }
                                }
                            }
                            //仍然无结果 - 判断是否使用默认值
                            if(orDefault && resultConfigs.isEmpty()) {
                                childConfigs.singleOrNull { it is CwtValueConfig }?.cast<CwtValueConfig>()?.let { resultConfigs.add(it) }
                            }
                        }
                        resultConfigs as List<T>
                    }
                    else -> return emptyList()
                }
            }
            else -> throw UnsupportedOperationException()
        }
        ////如果得到的规则有多个且全是block规则，按照子句中的规则个数，将多的排在前面
        //if(result.size > 1 && result.all { it.configs != null }) return result.sortedByDescending { it.configs?.size ?: 0 }
        return result
    }
    
    //DONE 兼容需要考虑内联的情况（如内联脚本）
    //DONE 这里需要兼容匹配key的子句规则有多个的情况 - 匹配任意则使用匹配的首个规则，空子句或者都不匹配则使用合并的规则
    
    /**
     * 得到指定的[element]的作为值的子句中的子属性/值的出现次数信息。（先合并子规则）
     */
    fun getChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtDataConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if(configs.isEmpty()) return emptyMap()
        val childConfigs = configs.flatMap { it.configs.orEmpty() }
        if(childConfigs.isEmpty()) return emptyMap()
        
        val childOccurrenceMap = getChildOccurrenceMapCacheFromCache(element) ?: return emptyMap()
        //NOTE cacheKey基于childConfigs即可，key相同而value不同的规则，上面的cardinality应当保证是一样的 
        val cacheKey = childConfigs.joinToString(" ")
        return childOccurrenceMap.getOrPut(cacheKey) { resolveChildOccurrenceMap(element, configs) }
    }
    
    private fun getChildOccurrenceMapCacheFromCache(element: ParadoxScriptMemberElement): MutableMap<String, Map<CwtDataExpression, Occurrence>>? {
        return CachedValuesManager.getCachedValue(element, PlsKeys.cachedChildOccurrenceMapCacheKey) {
            val value = ConcurrentHashMap<String, Map<CwtDataExpression, Occurrence>>()
            //invalidated on file modification or ScriptFileTracker
            val file = element.containingFile
            val tracker = ParadoxPsiModificationTracker.getInstance(file.project).ScriptFileTracker
            CachedValueProvider.Result.create(value, file, tracker)
        }
    }
    
    private fun resolveChildOccurrenceMap(element: ParadoxScriptMemberElement, configs: List<CwtDataConfig<*>>): Map<CwtDataExpression, Occurrence> {
        if(configs.isEmpty()) return emptyMap()
        val childConfigs = configs.flatMap { it.configs.orEmpty() }
        if(childConfigs.isEmpty()) return emptyMap()
        val configGroup = configs.first().info.configGroup
        val project = configGroup.project
        val blockElement = when {
            element is ParadoxScriptDefinitionElement -> element.block
            element is ParadoxScriptBlockElement -> element
            else -> null
        }
        if(blockElement == null) return emptyMap()
        val occurrenceMap = mutableMapOf<CwtDataExpression, Occurrence>()
        for(childConfig in childConfigs) {
            occurrenceMap.put(childConfig.expression, childConfig.toOccurrence(element, project))
        }
        ProgressManager.checkCanceled()
        //注意这里需要考虑内联和可选的情况
        blockElement.processData(conditional = true, inline = true) p@{ data ->
            val expression = when {
                data is ParadoxScriptProperty -> ParadoxDataExpression.resolve(data.propertyKey)
                data is ParadoxScriptValue -> ParadoxDataExpression.resolve(data)
                else -> return@p true
            }
            val isParameterized = expression.type == ParadoxDataType.StringType && expression.text.isParameterized()
            //may contain parameter -> can't and should not get occurrences
            if(isParameterized) {
                occurrenceMap.clear()
                return@p true
            }
            val matched = childConfigs.find { childConfig ->
                if(childConfig is CwtPropertyConfig && data !is ParadoxScriptProperty) return@find false
                if(childConfig is CwtValueConfig && data !is ParadoxScriptValue) return@find false
                matchesScriptExpression(data, expression, childConfig.expression, childConfig, configGroup)
            }
            if(matched == null) return@p true
            val occurrence = occurrenceMap[matched.expression]
            if(occurrence == null) return@p true
            occurrence.actual += 1
            true
        }
        return occurrenceMap
    }
    
    //endregion
}
