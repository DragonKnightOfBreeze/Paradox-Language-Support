package icu.windea.pls.lang

import com.google.common.cache.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.PlsContext.indexStatus
import icu.windea.pls.core.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.ParadoxConfigMatcher.Result
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.model.*
import icu.windea.pls.script.psi.*
import java.util.logging.*
import java.util.logging.Logger

object ParadoxConfigMatcher {
    object Options {
        /** 默认的匹配方式，先尝试通过[Result.ExactMatch]进行匹配，然后再尝试通过其他匹配方式进行匹配。 */
        const val Default = 0x00
        /** 对于[Result.LazySimpleMatch]和[Result.LazyBlockAwareMatch]，匹配结果直接返回true。 */
        const val Relax = 0x01
        /** 对于[Result.LazyIndexAwareMatch]，匹配结果直接返回true。 */
        const val SkipIndex = 0x02
        /** 对于[Result.LazyScopeAwareMatch]，匹配结果直接返回true。 */
        const val SkipScope = 0x04
        
        /** 对于最终匹配得到的那个结果，不需要再次判断是否精确匹配。 */
        const val Fast = 0x08
        /** 允许匹配定义自身。（当要匹配表达式的是一个键时） */
        const val AcceptDefinition = 0x10
    }
    
    sealed class Result {
        abstract fun get(options: Int = Options.Default): Boolean
        
        object NotMatch : Result() {
            override fun get(options: Int) = false
        }
        
        object ExactMatch : Result() {
            override fun get(options: Int) = true
        }
        
        object FallbackMatch : Result() {
            override fun get(options: Int) = true
        }
        
        object ParameterizedMatch : Result() {
            override fun get(options: Int) = true
        }
        
        sealed class LazyMatch(predicate: () -> Boolean) : Result() {
            //use manual lazy implementation instead of kotlin Lazy to optimize memory
            private var value: Any = predicate
            
            override fun get(options: Int): Boolean {
                if(skip(options)) return true
                if(value is Boolean) return value as Boolean
                return synchronized(this) {
                    if(value is Boolean) return value as Boolean
                    val r = doGetCatching()
                    value = r
                    r
                }
            }
            
            private fun skip(options: Int): Boolean {
                return when {
                    this is LazySimpleMatch -> BitUtil.isSet(options, Options.Relax)
                    this is LazyBlockAwareMatch -> BitUtil.isSet(options, Options.Relax)
                    this is LazyIndexAwareMatch -> BitUtil.isSet(options, Options.SkipIndex) || indexStatus.get() == true
                    this is LazyScopeAwareMatch -> BitUtil.isSet(options, Options.SkipScope) || indexStatus.get() == true
                    else -> false
                }
            }
            
            private fun doGetCatching(): Boolean {
                //it's necessary to suppress outputting error logs and throwing certain exceptions here
                //it's unexpected to throw index related exceptions here (but it's hard to prevent) 
                
                //java.lang.Throwable: Indexing process should not rely on non-indexed file data.
                //java.lang.AssertionError: Reentrant indexing
                //com.intellij.openapi.project.IndexNotReadyException
                
                var error: Throwable? = null
                val globalLogger = Logger.getLogger("") //DO NOT use Logger.getGlobalLogger(), it's incorrect
                val loggerLevel = globalLogger.level
                try {
                    globalLogger.level = Level.OFF
                    @Suppress("UNCHECKED_CAST")
                    return (value as () -> Boolean)()
                } catch(e: Throwable) {
                    if(e is ProcessCanceledException) throw e
                    error = e
                    return true
                } finally {
                    globalLogger.level = loggerLevel
                    if(error != null) thisLogger().info(error)
                }
            }
        }
        
        class LazySimpleMatch(predicate: () -> Boolean) : LazyMatch(predicate)
        
        class LazyBlockAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)
        
        class LazyIndexAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)
        
        class LazyScopeAwareMatch(predicate: () -> Boolean) : LazyMatch(predicate)
    }
    
    data class ResultValue<out T>(val value: T, val result: Result)
    
    //兼容scriptedVariableReference inlineMath parameter
    
    fun matches(
        element: PsiElement,
        expression: ParadoxDataExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = Options.Default
    ): Result {
        val project = configGroup.project
        val dataType = configExpression.type
        when {
            dataType == CwtDataType.Block -> {
                if(expression.isKey != false) return Result.NotMatch
                if(expression.type != ParadoxType.Block) return Result.NotMatch
                if(config !is CwtMemberConfig) return Result.NotMatch
                return Result.LazyBlockAwareMatch {
                    matchesScriptExpressionInBlock(element, config)
                }
            }
            dataType == CwtDataType.Bool -> {
                val r = expression.type.isBooleanType()
                return r.toResult()
            }
            dataType == CwtDataType.Int -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) {
                    val (min, max) = configExpression.extraValue<Tuple2<Int?, Int?>>() ?: return Result.ExactMatch
                    return Result.LazySimpleMatch p@{
                        val value = expression.text.toIntOrNull() ?: return@p true
                        (min == null || min <= value) && (max == null || max >= value)
                    }
                }
                return Result.NotMatch
            }
            dataType == CwtDataType.Float -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) {
                    val (min, max) = configExpression.extraValue<Tuple2<Float?, Float?>>() ?: return Result.ExactMatch
                    return Result.LazySimpleMatch p@{
                        val value = expression.text.toFloatOrNull() ?: return@p true
                        (min == null || min <= value) && (max == null || max >= value)
                    }
                }
                return Result.NotMatch
            }
            dataType == CwtDataType.Scalar -> {
                val r = when {
                    expression.isKey == true -> true //key -> ok
                    expression.type == ParadoxType.Parameter -> true //parameter -> ok
                    expression.type.isBooleanType() -> true //boolean -> sadly, also ok for compatibility
                    expression.type.isIntType() -> true //number -> ok according to vanilla game files
                    expression.type.isFloatType() -> true //number -> ok according to vanilla game files
                    expression.type.isStringType() -> true //unquoted/quoted string -> ok
                    else -> false
                }
                return r.toFallbackResult()
            }
            dataType == CwtDataType.ColorField -> {
                val r = expression.type.isColorType() && configExpression.value?.let { expression.text.startsWith(it) } != false
                return r.toResult()
            }
            dataType == CwtDataType.PercentageField -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                val r = ParadoxType.isPercentageField(expression.text)
                return r.toResult()
            }
            dataType == CwtDataType.DateField -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                val r = ParadoxType.isDateField(expression.text)
                return r.toResult()
            }
            dataType == CwtDataType.Localisation -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getLocalisationMatchResult(element, expression, project)
            }
            dataType == CwtDataType.SyncedLocalisation -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getSyncedLocalisationMatchResult(element, expression, project)
            }
            dataType == CwtDataType.InlineLocalisation -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.quoted) return Result.FallbackMatch //"quoted_string" -> any string
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getSyncedLocalisationMatchResult(element, expression, project)
            }
            dataType == CwtDataType.StellarisNameFormat -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                return Result.FallbackMatch //specific expression
            }
            dataType == CwtDataType.AbsoluteFilePath -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                return Result.ExactMatch //总是认为匹配
            }
            dataType.isPathReferenceType() -> {
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getPathReferenceMatchResult(element, expression, configExpression, project)
            }
            dataType == CwtDataType.Definition -> {
                //注意这里可能是一个整数，例如，对于<technology_tier>
                if(!expression.type.isStringType() && expression.type != ParadoxType.Int) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getDefinitionMatchResult(element, expression, configExpression, project)
            }
            dataType == CwtDataType.EnumValue -> {
                if(expression.type.isBlockLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val name = expression.text
                val enumName = configExpression.value ?: return Result.NotMatch //invalid cwt config
                //匹配简单枚举
                val enumConfig = configGroup.enums[enumName]
                if(enumConfig != null) {
                    val r = name in enumConfig.values
                    return r.toResult()
                }
                //匹配复杂枚举
                val complexEnumConfig = configGroup.complexEnums[enumName]
                if(complexEnumConfig != null) {
                    //complexEnumValue的值必须合法
                    if(ParadoxComplexEnumValueHandler.getName(expression.text) == null) return Result.NotMatch
                    return getComplexEnumValueMatchResult(element, name, enumName, complexEnumConfig, project)
                }
                return Result.NotMatch
            }
            dataType.isValueSetValueType() -> {
                if(expression.type.isBlockLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                //valueSetValue的值必须合法
                val name = ParadoxValueSetValueHandler.getName(expression.text)
                if(name == null) return Result.NotMatch
                val valueSetName = configExpression.value
                if(valueSetName == null) return Result.NotMatch
                return getValueSetValueMatchResult(element, name, valueSetName, project)
            }
            dataType.isScopeFieldType() -> {
                if(expression.quoted) return Result.NotMatch //不允许用引号括起
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getScopeFieldMatchResult(element, expression, configExpression, configGroup)
            }
            dataType.isValueFieldType() -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(dataType == CwtDataType.ValueField) {
                    if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) return Result.ExactMatch
                } else if(dataType == CwtDataType.IntValueField) {
                    if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) return Result.ExactMatch
                }
                if(expression.quoted) return Result.NotMatch //不允许用引号括起
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.text.length)
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup)
                val r = valueFieldExpression != null
                return r.toResult()
            }
            dataType.isVariableFieldType() -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(dataType == CwtDataType.VariableField) {
                    if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) return Result.ExactMatch
                } else if(dataType == CwtDataType.IntVariableField) {
                    if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) return Result.ExactMatch
                }
                if(expression.quoted) return Result.NotMatch //不允许用引号括起
                if(!expression.type.isStringType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.text.length)
                val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.text, textRange, configGroup)
                val r = variableFieldExpression != null
                return r.toResult()
            }
            dataType == CwtDataType.Modifier -> {
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getModifierMatchResult(element, expression, configGroup)
            }
            dataType == CwtDataType.Parameter -> {
                //匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                if(expression.type.isStringLikeType()) return Result.ExactMatch
                return Result.NotMatch
            }
            dataType == CwtDataType.ParameterValue -> {
                if(expression.type != ParadoxType.Block) return Result.ExactMatch
                return Result.NotMatch
            }
            dataType == CwtDataType.LocalisationParameter -> {
                //匹配本地化参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                if(expression.type.isStringLikeType()) return Result.ExactMatch
                return Result.NotMatch
            }
            dataType == CwtDataType.ShaderEffect -> {
                //暂时作为一般的字符串处理
                if(expression.type.isStringLikeType()) return Result.ExactMatch
                return Result.NotMatch
            }
            dataType == CwtDataType.SingleAliasRight -> {
                return Result.NotMatch //不在这里处理
            }
            dataType == CwtDataType.AliasKeysField -> {
                if(!expression.type.isStringLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return Result.NotMatch
                val aliasSubName = ParadoxConfigHandler.getAliasSubName(element, expression.text, expression.quoted, aliasName, configGroup, options) ?: return Result.NotMatch
                return matches(element, expression, CwtKeyExpression.resolve(aliasSubName), null, configGroup)
            }
            dataType == CwtDataType.AliasName -> {
                if(!expression.type.isStringLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return Result.NotMatch
                val aliasSubName = ParadoxConfigHandler.getAliasSubName(element, expression.text, expression.quoted, aliasName, configGroup, options) ?: return Result.NotMatch
                return matches(element, expression, CwtKeyExpression.resolve(aliasSubName), null, configGroup)
            }
            dataType == CwtDataType.AliasMatchLeft -> {
                return Result.NotMatch //不在这里处理
            }
            dataType == CwtDataType.Template -> {
                if(!expression.type.isStringLikeType()) return Result.NotMatch
                if(expression.isParameterized()) return Result.ParameterizedMatch
                //允许用引号括起
                return getTemplateMatchResult(element, expression, configExpression, configGroup)
            }
            dataType == CwtDataType.Constant -> {
                val value = configExpression.value
                if(configExpression is CwtValueExpression) {
                    //常量的值也可能是yes/no
                    val text = expression.text
                    if((value == "yes" || value == "no") && text.isLeftQuoted()) return Result.NotMatch
                }
                //这里也用来匹配空字符串
                val r = expression.text.equals(value, true) //忽略大小写
                return r.toResult()
            }
            dataType == CwtDataType.Any -> {
                return Result.FallbackMatch
            }
            else -> {
                return Result.FallbackMatch
            }
        }
    }
    
    private fun matchesScriptExpressionInBlock(element: PsiElement, config: CwtMemberConfig<*>): Boolean {
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
    
    private fun getCachedMatchResult(element: PsiElement, cacheKey: String, predicate: () -> Boolean): Result {
        ProgressManager.checkCanceled()
        if(indexStatus.get() == true) return Result.ExactMatch // indexing -> should not visit indices -> treat as exact match
        val rootFile = selectRootFile(element) ?: return Result.NotMatch
        val cache = rootFile.configMatchResultCache.value
        return cache.getOrPut(cacheKey) { Result.LazyIndexAwareMatch(predicate) }
    }
    
    private fun getLocalisationMatchResult(element: PsiElement, expression: ParadoxDataExpression, project: Project): Result {
        val name = expression.text
        val cacheKey = "l#$name"
        return getCachedMatchResult(element, cacheKey) {
            val selector = localisationSelector(project, element)
            ParadoxLocalisationSearch.search(name, selector).findFirst() != null
        }
    }
    
    private fun getSyncedLocalisationMatchResult(element: PsiElement, expression: ParadoxDataExpression, project: Project): Result {
        val name = expression.text
        val cacheKey = "ls#$name"
        return getCachedMatchResult(element, cacheKey) {
            val selector = localisationSelector(project, element)
            ParadoxSyncedLocalisationSearch.search(name, selector).findFirst() != null
        }
    }
    
    private fun getDefinitionMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, project: Project): Result {
        val name = expression.text
        val typeExpression = configExpression.value ?: return Result.NotMatch //invalid cwt config
        val cacheKey = "d#${typeExpression}#${name}"
        return getCachedMatchResult(element, cacheKey) {
            val selector = definitionSelector(project, element)
            ParadoxDefinitionSearch.search(name, typeExpression, selector).findFirst() != null
        }
    }
    
    private fun getPathReferenceMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, project: Project): Result {
        val pathReference = expression.text.normalizePath()
        val cacheKey = "p#${pathReference}#${configExpression}"
        return getCachedMatchResult(element, cacheKey) {
            val selector = fileSelector(project, element)
            ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null
        }
    }
    
    private fun getComplexEnumValueMatchResult(element: PsiElement, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig, project: Project): Result {
        val searchScope = complexEnumConfig.searchScopeType
        if(searchScope == null) {
            val cacheKey = "ce#${enumName}#${name}"
            return getCachedMatchResult(element, cacheKey) {
                val selector = complexEnumValueSelector(project, element)
                ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
            }
        }
        return Result.LazyIndexAwareMatch {
            val selector = complexEnumValueSelector(project, element).withSearchScopeType(searchScope)
            ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
        }
    }
    
    @Suppress("UNUSED_PARAMETER")
    private fun getValueSetValueMatchResult(element: PsiElement, name: String, valueSetName: String, project: Project): Result {
        //总是认为匹配
        return Result.ExactMatch
    }
    
    private fun getScopeFieldMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result {
        val textRange = TextRange.create(0, expression.text.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression.text, textRange, configGroup)
        if(scopeFieldExpression == null) return Result.NotMatch
        when(configExpression.type) {
            CwtDataType.ScopeField -> return Result.ExactMatch
            CwtDataType.Scope -> {
                val expectedScope = configExpression.value ?: return Result.ExactMatch
                return Result.LazyScopeAwareMatch p@{
                    val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                    val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@p true
                    val scopeContext = ParadoxScopeHandler.getScopeContext(element, scopeFieldExpression, parentScopeContext)
                    if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return@p true
                    false
                }
            }
            CwtDataType.ScopeGroup -> {
                val expectedScopeGroup = configExpression.value ?: return Result.ExactMatch
                return Result.LazyScopeAwareMatch p@{
                    val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                    val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@p true
                    val scopeContext = ParadoxScopeHandler.getScopeContext(element, scopeFieldExpression, parentScopeContext)
                    if(ParadoxScopeHandler.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return@p true
                    false
                }
            }
            else -> return Result.NotMatch
        }
    }
    
    private fun getModifierMatchResult(element: PsiElement, expression: ParadoxDataExpression, configGroup: CwtConfigGroup): Result {
        val name = expression.text
        val cacheKey = "m#${name}"
        return getCachedMatchResult(element, cacheKey) {
            ParadoxModifierHandler.matchesModifier(name, element, configGroup)
        }
    }
    
    private fun getTemplateMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result {
        val exp = expression.text
        val template = configExpression.expressionString
        val cacheKey = "t#${template}#${exp}"
        return getCachedMatchResult(element, cacheKey) {
            CwtTemplateExpression.resolve(template).matches(exp, element, configGroup)
        }
    }
    
    private fun Boolean.toResult() = if(this) Result.ExactMatch else Result.NotMatch
    
    private fun Boolean.toFallbackResult() = if(this) Result.FallbackMatch else Result.NotMatch
}

private val PlsKeys.configMatchResultCache by createCachedValueKey<Cache<String, Result>>("paradox.configMatchResult.cache") {
    CacheBuilder.newBuilder().buildCache<String, Result>().withDependencyItems(PsiModificationTracker.MODIFICATION_COUNT)
}
private val VirtualFile.configMatchResultCache by PlsKeys.configMatchResultCache