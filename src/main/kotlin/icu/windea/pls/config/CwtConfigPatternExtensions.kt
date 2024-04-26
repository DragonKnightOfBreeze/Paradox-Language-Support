package icu.windea.pls.config

import com.google.common.cache.*
import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.util.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.*

interface CwtConfigPatternAware

private val patternResolvers by lazy { CwtDataExpressionResolver.EP_NAME.extensionList.filterFast { it is CwtConfigPatternAware } }
private val patternMatchers by lazy { CwtDataExpressionMatcher.EP_NAME.extensionList.filterFast { it is CwtConfigPatternAware } }

private class CwtKeyExpressionFromPattern(
    override val expressionString: String,
    override val type: CwtDataType,
    override val value: String?,
    override val extraValue: Any?
) : CwtKeyExpression {
    override fun equals(other: Any?) = this === other || other is CwtKeyExpression && expressionString == other.expressionString
    override fun hashCode() = expressionString.hashCode()
    override fun toString() = expressionString
}

private fun resolveConfigExpression(expressionString: String): CwtKeyExpression = configExpressionCache.get(expressionString)

private val configExpressionCache = CacheBuilder.newBuilder().buildCache<String, CwtKeyExpression> { doResolveConfigExpression(it) }

private fun doResolveConfigExpression(expressionString: String): CwtKeyExpression {
    val r = patternResolvers.firstNotNullOfOrNull { it.resolve(expressionString) } ?: return CwtKeyExpression.EmptyExpression
    return CwtKeyExpressionFromPattern(r.expressionString, r.type, r.value, r.extraValue)
}

/**
 * 用当前键来匹配指定的[pattern]，得到匹配的首个结果。
 * @param pattern 允许的数类型：[CwtDataTypeGroups.PatternLike]
 * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配
 */
fun String.matchByPattern(
    pattern: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    matchOptions: Int = CwtConfigMatcher.Options.Default,
    fromIndex: Int = 0,
): Boolean {
    if(this == pattern) return true
    if(pattern.isEmpty()) return this.isEmpty()
    if(fromIndex < 0 || fromIndex >= this.length || fromIndex >= pattern.length) return false //invalid
    if(fromIndex > 0 ) {
        val p1 = this.substring(0, fromIndex)
        val p2 = pattern.substring(0, fromIndex)
        if(p1 != p2) return false
    }
    val pattern0 = pattern.substring(fromIndex)
    val configExpression = resolveConfigExpression(pattern0)
    if(configExpression.expressionString.isEmpty()) return false
    val expression = ParadoxDataExpression.resolve(this)
    val matchResult = patternMatchers.firstNotNullOfOrNull { it.matches(contextElement, expression, configExpression, null, configGroup, matchOptions) } ?: return false
    return matchResult.get(matchOptions)
}

/**
 * 用当前映射的键来匹配指定的[pattern]，得到匹配的首个结果。
 * @param pattern 允许的数类型：[CwtDataTypeGroups.PatternLike]
 * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配
 */
fun <V> Map<String, V>.findByPattern(
    pattern: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    matchOptions: Int = CwtConfigMatcher.Options.Default,
    fromIndex: Int = 0,
): V? {
    val fastResult = get(pattern)
    if(fastResult != null) return fastResult
    return entries.find { (k) -> k.matchByPattern(pattern, contextElement, configGroup, matchOptions, fromIndex) }?.value
}

/**
 * 用当前映射的键来匹配指定的[pattern]，得到匹配的首个结果。
 * @param pattern 允许的数类型：[CwtDataTypeGroups.PatternLike]
 * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配
 */
fun <V> Map<String, V>.filterByPattern(
    pattern: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    matchOptions: Int = CwtConfigMatcher.Options.Default,
    fromIndex: Int = 0,
): List<V> {
    val fastResult = get(pattern)
    if(fastResult != null) return fastResult.toSingletonList()
    return entries.filter { (k) -> k.matchByPattern(pattern, contextElement, configGroup, matchOptions, fromIndex) }.map { it.value }
}