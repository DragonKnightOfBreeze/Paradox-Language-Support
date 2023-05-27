package icu.windea.pls.lang

import com.google.common.cache.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import com.intellij.util.*
import com.intellij.util.concurrency.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

object ParadoxConfigMatcher {
    object Options {
        /**默认的匹配方式，先尝试通过[Result.ExactMatch]进行匹配，然后再尝试通过其他匹配方式进行匹配。*/
        const val Default = 0x00
        /** 对于[Result.LazyExactMatch]，匹配结果直接返回true。 */
        const val Relax = 0x01
        /** 对于[Result.LazyIndexAwareExactMatch]，匹配结果直接返回true。 */
        const val SkipIndex = 0x02
        /** 对于[Result.LazyScopeAwareExactMatch]，匹配结果直接返回true。 */
        const val SkipScope = 0x04
        /** 对于最终匹配得到的那个结果，不需要再次判断是否精确匹配。 */
        const val Fast = 0x08
    }
    
    sealed class Result {
        abstract fun get(options: Int = Options.Default): Boolean
        
        object NotMatch : Result() {
            override fun get(options: Int) = false
        }
        
        object ExactMatch : Result() {
            override fun get(options: Int) = true
        }
        
        object ParameterizedMatch : Result() {
            override fun get(options: Int) = true
        }
        
        class LazyExactMatch(predicate: () -> Boolean) : Result() {
            private val result by lazy { predicate() }
            override fun get(options: Int) = if(BitUtil.isSet(options, Options.Relax)) true else result
        }
        
        class LazyIndexAwareExactMatch(predicate: () -> Boolean) : Result() {
            private val result by lazy { predicate() }
            override fun get(options: Int) = if(BitUtil.isSet(options, Options.SkipIndex)) true else result
        }
        
        class LazyScopeAwareExactMatch(predicate: () -> Boolean) : Result() {
            private val result by lazy { predicate() }
            override fun get(options: Int) = if(BitUtil.isSet(options, Options.SkipScope)) true else result
        }
    }
    
    data class ResultValue<out T>(val value: T, val result: Result)
    
    inline fun <T : Any> find(collection: Collection<T>?, options: Int = Options.Default, matcher: (T) -> Result?): T? {
        if(collection.isNullOrEmpty()) return null
        val fast = BitUtil.isSet(options, Options.Fast)
        var tempResults: MutableList<ResultValue<T>>? = null
        for(v in collection) {
            val r = matcher(v)
            if(r == null || r == Result.NotMatch) continue
            if(r == Result.ExactMatch && fast) return v
            if(tempResults == null) tempResults = SmartList()
            tempResults.add(ResultValue(v, r))
        }
        if(tempResults.isNullOrEmpty()) return null
        if(tempResults.size == 1 && fast) return tempResults[0].value
        tempResults.forEachFast { (v, r) -> if(r.get(options)) return v }
        return null
    }
    
    inline fun <T : Any> findAll(collection: Collection<T>?, options: Int = Options.Default, matcher: (T) -> Result?): List<T> {
        if(collection.isNullOrEmpty()) return emptyList()
        val fast = BitUtil.isSet(options, Options.Fast)
        var tempResults: MutableList<ResultValue<T>>? = null
        for(v in collection) {
            val r = matcher(v)
            if(r == null || r == Result.NotMatch) continue
            if(r == Result.ExactMatch && fast) return v.toSingletonList()
            if(tempResults == null) tempResults = SmartList()
            tempResults.add(ResultValue(v, r))
        }
        if(tempResults.isNullOrEmpty()) return emptyList()
        if(tempResults.size == 1 && fast) return tempResults[0].value.toSingletonList()
        val result = SmartList<T>()
        tempResults.forEachFast { (v, r) -> if(r.get(options)) result.add(v) }
        return result
    }
    
    //兼容scriptedVariableReference inlineMath parameter
    
    fun matches(
        element: PsiElement,
        expression: ParadoxDataExpression,
        configExpression: CwtDataExpression,
        config: CwtConfig<*>?,
        configGroup: CwtConfigGroup,
        options: Int = Options.Default
    ): Result {
        val result = doMatch(element, expression, config, configExpression, configGroup, options)
        return when {
            result is Result -> result
            result is Boolean -> if(result) Result.ExactMatch else Result.NotMatch
            else -> throw IllegalStateException()
        }
    }
    
    private fun doMatch(
        element: PsiElement,
        expression: ParadoxDataExpression,
        config: CwtConfig<*>?,
        configExpression: CwtDataExpression,
        configGroup: CwtConfigGroup,
        options: Int
    ): Any {
        val project = configGroup.project
        val dataType = configExpression.type
        when {
            dataType == CwtDataType.Block -> {
                if(expression.isKey != false) return false
                if(expression.type != ParadoxType.Block) return true
                if(config !is CwtMemberConfig) return Result.NotMatch
                return Result.LazyExactMatch {
                    matchesScriptExpressionInBlock(element, config)
                }
            }
            dataType == CwtDataType.Bool -> {
                return expression.type.isBooleanType()
            }
            dataType == CwtDataType.Int -> {
                //quoted number (e.g. "1") -> ok according to vanilla game files
                if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) {
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
                if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) {
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
                    expression.type == ParadoxType.Parameter -> true //parameter -> ok
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
                return ParadoxType.isPercentageField(expression.text)
            }
            dataType == CwtDataType.DateField -> {
                if(!expression.type.isStringType()) return false
                return ParadoxType.isDateField(expression.text)
            }
            dataType == CwtDataType.Localisation -> {
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getLocalisationMatchResult(element, expression, project)
            }
            dataType == CwtDataType.SyncedLocalisation -> {
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getSyncedLocalisationMatchResult(element, expression, project)
            }
            dataType == CwtDataType.InlineLocalisation -> {
                if(!expression.type.isStringType()) return false
                if(expression.quoted) return true //"quoted_string" -> any string
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getSyncedLocalisationMatchResult(element, expression, project)
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
                return getPathReferenceMatchResult(element, expression, configExpression, project)
            }
            dataType == CwtDataType.Definition -> {
                //注意这里可能是一个整数，例如，对于<technology_tier>
                if(!expression.type.isStringType() && expression.type != ParadoxType.Int) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getDefinitionMatchResult(element, expression, configExpression, project)
            }
            dataType == CwtDataType.EnumValue -> {
                if(expression.type.isBlockLikeType()) return false
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
                    return getComplexEnumValueMatchResult(element, name, enumName, complexEnumConfig, project)
                }
                return false
            }
            dataType.isValueSetValueType() -> {
                if(expression.type.isBlockLikeType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                //valueSetValue的值必须合法
                val name = ParadoxValueSetValueHandler.getName(expression.text)
                if(name == null) return false
                val valueSetName = configExpression.value
                if(valueSetName == null) return false
                return getValueSetValueMatchResult(element, name, valueSetName, project)
            }
            dataType.isScopeFieldType() -> {
                if(expression.quoted) return false //不允许用引号括起
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getScopeFieldMatchResult(element, expression, configExpression, configGroup)
            }
            dataType.isValueFieldType() -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(dataType == CwtDataType.ValueField) {
                    if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) return true
                } else if(dataType == CwtDataType.IntValueField) {
                    if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) return true
                }
                if(expression.quoted) return false //不允许用引号括起
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.text.length)
                val valueFieldExpression = ParadoxValueFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                return valueFieldExpression != null
            }
            dataType.isVariableFieldType() -> {
                //也可以是数字，注意：用括号括起的数字（作为scalar）也匹配这个规则
                if(dataType == CwtDataType.VariableField) {
                    if(expression.type.isFloatType() || ParadoxType.resolve(expression.text).isFloatType()) return true
                } else if(dataType == CwtDataType.IntVariableField) {
                    if(expression.type.isIntType() || ParadoxType.resolve(expression.text).isIntType()) return true
                }
                if(expression.quoted) return false //不允许用引号括起
                if(!expression.type.isStringType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val textRange = TextRange.create(0, expression.text.length)
                val variableFieldExpression = ParadoxVariableFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
                return variableFieldExpression != null
            }
            dataType == CwtDataType.Modifier -> {
                if(expression.isParameterized()) return Result.ParameterizedMatch
                return getModifierMatchResult(element, expression, configGroup)
            }
            dataType == CwtDataType.Parameter -> {
                //匹配参数名（即使对应的定义声明中不存在对应名字的参数，也可以匹配）
                return expression.type.isStringLikeType()
            }
            dataType == CwtDataType.ParameterValue -> {
                return expression.type != ParadoxType.Block
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
                val aliasSubName = ParadoxConfigHandler.getAliasSubName(element, expression.text, expression.quoted, aliasName, configGroup, options) ?: return false
                return matches(element, expression, CwtKeyExpression.resolve(aliasSubName), null, configGroup)
            }
            dataType == CwtDataType.AliasName -> {
                if(!expression.type.isStringLikeType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                val aliasName = configExpression.value ?: return false
                val aliasSubName = ParadoxConfigHandler.getAliasSubName(element, expression.text, expression.quoted, aliasName, configGroup, options) ?: return false
                return matches(element, expression, CwtKeyExpression.resolve(aliasSubName), null, configGroup)
            }
            dataType == CwtDataType.AliasMatchLeft -> {
                return false //不在这里处理
            }
            dataType == CwtDataType.Template -> {
                if(!expression.type.isStringLikeType()) return false
                if(expression.isParameterized()) return Result.ParameterizedMatch
                //允许用引号括起
                return getTemplateMatchResult(element, expression, configExpression, configGroup)
            }
            dataType == CwtDataType.Constant -> {
                val value = configExpression.value
                if(configExpression is CwtValueExpression) {
                    //常量的值也可能是yes/no
                    val text = expression.text
                    if((value == "yes" || value == "no") && text.isLeftQuoted()) return false
                }
                //这里也用来匹配空字符串
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
    
    private val configMatchResultCache = CacheBuilder.newBuilder().buildCache<VirtualFile, Cache<String, Result>> { CacheBuilder.newBuilder().buildCache() }
    
    private fun getCachedResult(element: PsiElement, cacheKey: String, predicate: () -> Boolean): Result {
        ProgressManager.checkCanceled()
        val rootFile = selectRootFile(element) ?: return Result.NotMatch //TODO 1.0.3 考虑优化性能
        val cache = configMatchResultCache.get(rootFile)
        return cache.getOrPut(cacheKey) { Result.LazyIndexAwareExactMatch(predicate) }
    }
    
    private fun getLocalisationMatchResult(element: PsiElement, expression: ParadoxDataExpression, project: Project): Result {
        val name = expression.text
        val cacheKey = "l#$name"
        return getCachedResult(element, cacheKey) {
            val selector = localisationSelector(project, element)
            ParadoxLocalisationSearch.search(name, selector).findFirst() != null
        }
    }
    
    private fun getSyncedLocalisationMatchResult(element: PsiElement, expression: ParadoxDataExpression, project: Project): Result {
        val name = expression.text
        val cacheKey = "ls#$name"
        return getCachedResult(element, cacheKey) {
            val selector = localisationSelector(project, element)
            ParadoxSyncedLocalisationSearch.search(name, selector).findFirst() != null
        }
    }
    
    private fun getDefinitionMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, project: Project): Result {
        val name = expression.text
        val typeExpression = configExpression.value ?: return Result.NotMatch //invalid cwt config
        val cacheKey = "d#${typeExpression}#${name}"
        return getCachedResult(element, cacheKey) {
            val selector = definitionSelector(project, element)
            ParadoxDefinitionSearch.search(name, typeExpression, selector).findFirst() != null
        }
    }
    
    private fun getPathReferenceMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, project: Project): Result {
        val pathReference = expression.text.normalizePath()
        val cacheKey = "p#${pathReference}#${configExpression}"
        return getCachedResult(element, cacheKey) {
            val selector = fileSelector(project, element)
            ParadoxFilePathSearch.search(pathReference, configExpression, selector).findFirst() != null
        }
    }
    
    private fun getComplexEnumValueMatchResult(element: PsiElement, name: String, enumName: String, complexEnumConfig: CwtComplexEnumConfig, project: Project): Result {
        val searchScope = complexEnumConfig.searchScopeType
        if(searchScope == null) {
            val cacheKey = "ce#${enumName}#${name}"
            return getCachedResult(element, cacheKey) {
                val selector = complexEnumValueSelector(project, element)
                ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
            }
        }
        return Result.LazyIndexAwareExactMatch {
            val selector = complexEnumValueSelector(project, element).withSearchScopeType(searchScope)
            ParadoxComplexEnumValueSearch.search(name, enumName, selector).findFirst() != null
        }
    }
    
    private val valueSetValueIndexStatusKey = Key.create<Boolean>("paradox.valueSetValue.index.status")
    
    @Suppress("UNUSED_PARAMETER")
    private fun getValueSetValueMatchResult(element: PsiElement, name: String, valueSetName: String, project: Project): Result {
        //总是认为匹配
        val cacheKey = "vsv"
        return getCachedResult(element, cacheKey) {
            //ParadoxValueSetValueIndex - 此索引目前仅用于进行代码补全，因此这里需要确保索引数据已加载
            if(project.getUserData(valueSetValueIndexStatusKey) != true) {
                synchronized(project) {
                    if(project.getUserData(valueSetValueIndexStatusKey) != true) {
                        project.putUserData(valueSetValueIndexStatusKey, true)
                        ReadAction.nonBlocking(Callable {
                            val selector = valueSetValueSelector(project, element)
                            ParadoxValueSetValueSearch.search("", selector).findAll()
                        }).inSmartMode(project).submit(AppExecutorUtil.getAppExecutorService())
                    }
                }
            }
            true
        }
    }
    
    private fun getScopeFieldMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result {
        val textRange = TextRange.create(0, expression.text.length)
        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(expression.text, textRange, configGroup, expression.isKey)
        if(scopeFieldExpression == null) return Result.NotMatch
        when(configExpression.type) {
            CwtDataType.ScopeField -> return Result.ExactMatch
            CwtDataType.Scope -> {
                val expectedScope = configExpression.value ?: return Result.ExactMatch
                return Result.LazyScopeAwareExactMatch p@{
                    val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                    val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@p true
                    val scopeContext = ParadoxScopeHandler.getScopeContext(scopeFieldExpression, parentScopeContext)
                    if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return@p true
                    false
                }
            }
            CwtDataType.ScopeGroup -> {
                val expectedScopeGroup = configExpression.value ?: return Result.ExactMatch
                return Result.LazyScopeAwareExactMatch p@{
                    val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = false) ?: return@p true
                    val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return@p true
                    val scopeContext = ParadoxScopeHandler.getScopeContext(scopeFieldExpression, parentScopeContext)
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
        return getCachedResult(element, cacheKey) {
            ParadoxModifierHandler.matchesModifier(name, element, configGroup)
        }
    }
    
    private fun getTemplateMatchResult(element: PsiElement, expression: ParadoxDataExpression, configExpression: CwtDataExpression, configGroup: CwtConfigGroup): Result {
        val exp = expression.text
        val template = configExpression.expressionString
        val cacheKey = "t#${template}#${exp}"
        return getCachedResult(element, cacheKey) {
            CwtTemplateExpression.resolve(template).matches(exp, element, configGroup)
        }
    }
    
    class Listener : PsiModificationTracker.Listener {
        override fun modificationCountChanged() {
            configMatchResultCache.invalidateAll()
        }
    }
}