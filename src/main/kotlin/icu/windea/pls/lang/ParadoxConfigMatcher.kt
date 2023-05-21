package icu.windea.pls.lang

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.script.psi.*

object ParadoxConfigMatcher {
    sealed class Result {
        abstract fun get(): Boolean
        
        object ExactMatch : Result() {
            override fun get() = true
        }
        
        object NotMatch : Result() {
            override fun get() = false
        }
        
        object ParameterizedMatch : Result() {
            override fun get() = true
        }
        
        class LazyMatch(val predicate: () -> Boolean) : Result() {
            override fun get() = predicate()
        }
        
        class LazyExactMatch(val predicate: () -> Boolean) : Result() {
            override fun get() = predicate()
        }
        
        class LazyIndexAwareExactMatch(val predicate: () -> Boolean) : Result() {
            override fun get() = predicate()
        }
    }
    
    data class ResultValue<out T>(val value: T, val result: Result)
    
    inline fun <T : Any> matchAndReduce(collection: Iterable<T>, transform: (T) -> Result): T? {
        var tempResults: MutableList<ResultValue<T>>? = null
        for(v in collection) {
            val r = transform(v)
            if(r == Result.ExactMatch) return v
            if(r == Result.NotMatch) continue
            if(tempResults == null) tempResults = SmartList()
            tempResults.add(ResultValue(v, r))
        }
        if(tempResults.isNullOrEmpty()) return null
        if(tempResults.size == 1) return tempResults[0].value
        tempResults.forEachFast { (v, r) -> 
            if(r.get()) return v
        }
        return null
    }
    
    inline fun <T : Any> matchAndReduceAll(collection: Iterable<T>, transform: (T) -> Result): List<T> {
        var tempResults: MutableList<ResultValue<T>>? = null
        for(e in collection) {
            val r = transform(e)
            if(r == Result.ExactMatch) return e.toSingletonList()
            if(r == Result.NotMatch) continue
            if(tempResults == null) tempResults = SmartList()
            tempResults.add(ResultValue(e, r))
        }
        if(tempResults.isNullOrEmpty()) return emptyList()
        if(tempResults.size == 1) return tempResults[0].value.toSingletonList()
        val result = SmartList<T>()
        tempResults.forEachFast { (v, r) ->
            if(r.get()) result.add(v)
        }
        return result
    }
    
    //兼容scriptedVariableReference inlineMath parameter
    
    fun match(
        element: PsiElement,
        expression: ParadoxDataExpression,
        config: CwtConfig<*>?,
        configExpression: CwtDataExpression,
        configGroup: CwtConfigGroup
    ): Result {
        return doMatch(element, expression, config, configExpression, configGroup).toResult()
    }
    
    private fun doMatch(
        element: PsiElement,
        expression: ParadoxDataExpression,
        config: CwtConfig<*>?,
        configExpression: CwtDataExpression,
        configGroup: CwtConfigGroup
    ): Any {
        //匹配空字符串
        if(configExpression.isEmpty()) return expression.isEmpty().toResult()
        
        val project = configGroup.project
        val dataType = configExpression.type
        when {
            dataType == CwtDataType.Block -> {
                if(expression.isKey != false) return false
                if(expression.type != ParadoxDataType.BlockType) return true
                if(config !is CwtDataConfig) return Result.NotMatch
                return Result.LazyExactMatch { matchesScriptExpressionInBlock(element, config) }
            }
            dataType == CwtDataType.Bool -> {
                return expression.type.isBooleanType()
            }
            dataType == CwtDataType.Int -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) {
                    val (min, max) = configExpression.extraValue<Tuple2<Int?, Int?>>() ?: return true
                    return Result.LazyExactMatch p@{
                        val value = expression.text.toIntOrNull() ?: return@p true
                        (min == null || min <= value) && (max == null || max >= value)
                    }
                }
                return false
            }
            dataType == CwtDataType.Float -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) {
                    val (min, max) = configExpression.extraValue<Tuple2<Float?, Float?>>() ?: return true
                    return Result.LazyExactMatch p@{
                        val value = expression.text.toFloatOrNull() ?: return@p true
                        (min == null || min <= value) && (max == null || max >= value)
                    }
                }
                return false
            }
            dataType == CwtDataType.Scalar -> {
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
            dataType == CwtDataType.ColorField -> {
                return expression.type.isColorType() && configExpression.value?.let { expression.text.startsWith(it) } != false
            }
            dataType == CwtDataType.PercentageField -> {
                if(!expression.type.isStringType()) return false
                return ParadoxDataType.isPercentageField(expression.text)
            }
            dataType == CwtDataType.DateField -> {
                if(!expression.type.isStringType()) return false
                return ParadoxDataType.isDateField(expression.text)
            }
            dataType == CwtDataType.Localisation -> {
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return Result.LazyIndexAwareExactMatch {
                    val selector = localisationSelector(project, element)
                    ParadoxLocalisationSearch.search(expression.text, selector).findFirst() != null
                }
            }
            dataType == CwtDataType.SyncedLocalisation -> {
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return Result.LazyIndexAwareExactMatch {
                    val selector = localisationSelector(project, element)
                    ParadoxSyncedLocalisationSearch.search(expression.text, selector).findFirst() != null
                }
            }
            dataType == CwtDataType.InlineLocalisation -> {
                if(!expression.type.isStringType()) return false
                if(expression.quoted) return true //"quoted_string" -> any string
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return Result.LazyIndexAwareExactMatch {
                    val selector = localisationSelector(project, element)
                    ParadoxSyncedLocalisationSearch.search(expression.text, selector).findFirst() != null
                }
            }
            dataType == CwtDataType.StellarisNameFormat -> {
                if(!expression.type.isStringType()) return false
                return true //specific expression
            }
            dataType == CwtDataType.AbsoluteFilePath -> {
                if(!expression.type.isStringType()) return false
                return true //总是认为匹配
            }
            dataType.isPathReferenceType() -> {
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val pathReferenceExpressionSupport = ParadoxPathReferenceExpressionSupport.get(configExpression)
                if(pathReferenceExpressionSupport == null) return false
                return Result.LazyIndexAwareExactMatch {
                    val pathReference = expression.text.normalizePath()
                    val selector = fileSelector(project, element)
                    ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null
                }
            }
            dataType == CwtDataType.Definition -> {
                //注意这里可能是一个整数，例如，对于<technology_tier>
                if(!expression.type.isStringType() && expression.type != ParadoxDataType.IntType) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val typeExpression = configExpression.value ?: return false //invalid cwt config
                return Result.LazyIndexAwareExactMatch {
                    val selector = definitionSelector(project, element)
                    ParadoxDefinitionSearch.search(expression.text, typeExpression, selector).findFirst() != null
                }
            }
            dataType == CwtDataType.EnumValue -> {
                if(!expression.type.isStringLikeType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val name = expression.text
                val enumName = configExpression.value ?: return false //invalid cwt config
                //匹配简单枚举
                val enumConfig = configGroup.enums[enumName]
                if(enumConfig != null) {
                    return name in enumConfig.values
                }
                //匹配复杂枚举
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if(complexEnumConfig != null) {
                    //complexEnumValue的值必须合法
                    if(ParadoxComplexEnumValueHandler.getName(expression.text) == null) return false
                    return Result.LazyIndexAwareExactMatch {
                        val searchScope = complexEnumConfig.searchScopeType
                        val selector = complexEnumValueSelector(project, element)
                            .withSearchScopeType(searchScope)
                        val complexEnumValueInfo = ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst()
                        complexEnumValueInfo != null
                    }
                }
                return false
            }
            dataType.isValueSetValueType() -> {
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                //valueSetValue的值必须合法
                val name = ParadoxValueSetValueHandler.getName(expression.text)
                if(name == null) return false
                val valueSetName = configExpression.value
                if(valueSetName == null) return false
                //总是认为匹配
                return true
            }
            dataType.isScopeFieldType() -> {
                if(expression.quoted) return false //不允许用引号括起
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return Result.LazyExactMatch p@{
                    val textRange = TextRange.create(0, expression.text.length)
                    val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                    if(scopeFieldExpression == null) return@p false
                    when(configExpression.type) {
                        CwtDataType.ScopeField -> {
                            return@p true
                        }
                        CwtDataType.Scope -> {
                            val expectedScope = configExpression.value ?: return@p true
                            val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                            val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@p true
                            val scopeContext = ParadoxScopeHandler.getScopeContext(scopeFieldExpression, parentScopeContext)
                            if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return@p true
                        }
                        CwtDataType.ScopeGroup -> {
                            val expectedScopeGroup = configExpression.value ?: return@p true
                            val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                            val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@p true
                            val scopeContext = ParadoxScopeHandler.getScopeContext(scopeFieldExpression, parentScopeContext)
                            if(ParadoxScopeHandler.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return@p true
                        }
                        else -> {}
                    }
                    false
                }
            }
            dataType.isValueFieldType() -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(dataType == CwtDataType.ValueField) {
                    if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
                } else if(dataType == CwtDataType.IntValueField) {
                    if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
                }
                if(expression.quoted) return false //不允许用引号括起
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return Result.LazyExactMatch {
                    val textRange = TextRange.create(0, expression.text.length)
                    val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                    valueFieldExpression != null
                }
            }
            dataType.isVariableFieldType() -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(dataType == CwtDataType.VariableField) {
                    if(expression.type.isFloatType() || ParadoxDataType.resolve(expression.text).isFloatType()) return true
                } else if(dataType == CwtDataType.IntVariableField) {
                    if(expression.type.isIntType() || ParadoxDataType.resolve(expression.text).isIntType()) return true
                }
                if(expression.quoted) return false //不允许用引号括起
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return Result.LazyExactMatch {
                    val textRange = TextRange.create(0, expression.text.length)
                    val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                    variableFieldExpression != null
                }
            }
            dataType == CwtDataType.Modifier -> {
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return Result.LazyIndexAwareExactMatch {
                    ParadoxConfigHandler.matchesModifier(element, expression.text, configGroup)
                }
            }
            dataType == CwtDataType.Parameter -> {
                //匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                return expression.type.isStringLikeType()
            }
            dataType == CwtDataType.ParameterValue -> {
                return expression.type != ParadoxDataType.BlockType
            }
            dataType == CwtDataType.LocalisationParameter -> {
                //匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                return expression.type.isStringLikeType()
            }
            dataType == CwtDataType.ShaderEffect -> {
                //暂时作为一般的字符串处理
                return expression.type.isStringLikeType()
            }
            dataType == CwtDataType.SingleAliasRight -> {
                return false //不在这里处理
            }
            dataType == CwtDataType.AliasKeysField -> {
                if(!expression.type.isStringLikeType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return false
                return ParadoxConfigHandler.matchesAliasName(element, expression, aliasName, configGroup)
                //TODO
            }
            dataType == CwtDataType.AliasName -> {
                if(!expression.type.isStringLikeType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return false
                return ParadoxConfigHandler.matchesAliasName(element, expression, aliasName, configGroup)
                //TODO
            }
            dataType == CwtDataType.AliasMatchLeft -> {
                return false //不在这里处理
            }
            dataType == CwtDataType.Template -> {
                if(!expression.type.isStringLikeType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                //允许用引号括起
                return Result.LazyIndexAwareExactMatch {
                    ParadoxConfigHandler.matchesTemplateExpression(element, expression, configExpression, configGroup)
                }
            }
            dataType == CwtDataType.Constant -> {
                val value = configExpression.value
                if(configExpression is CwtValueExpression) {
                    //常量的值也可能是yes/no
                    val text = expression.text
                    if((value == "yes" || value == "no") && text.isLeftQuoted()) return false
                }
                return expression.text.equals(value, true) //忽略大小写
            }
            dataType == CwtDataType.Any -> {
                return true
            }
            else -> {
                return false
            }
        }
    }
    
    private fun matchesScriptExpressionInBlock(element: PsiElement, config: CwtDataConfig<*>): Boolean {
        val block = when {
            element is ParadoxScriptProperty -> element.propertyValue()
            element is ParadoxScriptBlock -> element
            else -> null
        } ?: return false
        //简单判断：如果block中包含configsInBlock声明的必须的任意propertyKey（作为常量字符串，忽略大小写），则认为匹配
        //注意：不同的子句规则可以拥有部分相同的propertyKey
        val keys = ParadoxConfigHandler.getInBlockKeys(config)
        if(keys.isEmpty()) return true
        val actualKeys = mutableSetOf<String>()
        //注意这里需要考虑内联和可选的情况
        block.processData(conditional = true, inline = true) {
            if(it is ParadoxScriptProperty) actualKeys.add(it.name)
            true
        }
        return actualKeys.any { it in keys }
    }
    
    private val CwtDataConfig.Keys.inBlockKeysKey by lazy { Key.create<Set<String>>("cwt.config.inBlockKeys") }
    
    private fun getInBlockKeys(config: CwtDataConfig<*>): Set<String> {
        return config.getOrPutUserData(CwtDataConfig.Keys.inBlockKeysKey) {
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
        return config.keyExpression.type == CwtDataType.Constant && config.cardinality?.isRequired() != false
    }
    
    @Suppress("NOTHING_TO_INLINE")
    private inline fun Any.toResult(): Result {
        return when {
            this is Result -> this
            this is Boolean -> if(this) Result.ExactMatch else Result.NotMatch
            else -> throw IllegalStateException()
        }
    }
}