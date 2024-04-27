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
 * 用当前键作为通配符来匹配指定的[key]。
 * @param key 要与通配符进行匹配的键。
 * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配。
 * @see CwtDataTypeGroups.PatternLike
 */
fun String.matchFromPattern(
    key: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    matchOptions: Int = CwtConfigMatcher.Options.Default,
    fromIndex: Int = 0,
): Boolean {
    if(this == key) return true
    if(key.isEmpty()) return this.isEmpty()
    if(fromIndex < 0 || fromIndex >= this.length || fromIndex >= key.length) return false //invalid
    if(fromIndex > 0 ) {
        val p1 = this.substring(0, fromIndex)
        val p2 = key.substring(0, fromIndex)
        if(p1 != p2) return false
    }
    val pattern0 = this.substring(fromIndex)
    val configExpression = resolveConfigExpression(pattern0)
    if(configExpression.expressionString.isEmpty()) return false
    val expression = ParadoxDataExpression.resolve(key)
    val matchResult = patternMatchers.firstNotNullOfOrNull { it.matches(contextElement, expression, configExpression, null, configGroup, matchOptions) } ?: return false
    return matchResult.get(matchOptions)
}

/**
 * 用当前映射的键作为通配符来匹配指定的[key]，得到匹配的首个结果。
 * @param key 要与通配符进行匹配的键。
 * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配。
 * @see CwtDataTypeGroups.PatternLike
 */
fun <V> Map<String, V>.findFromPattern(
    key: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    matchOptions: Int = CwtConfigMatcher.Options.Default,
    fromIndex: Int = 0,
): V? {
    val fastResult = get(key)
    if(fastResult != null) return fastResult
    return entries.find { (k) -> k.matchFromPattern(key, contextElement, configGroup, matchOptions, fromIndex) }?.value
}

/**
 * 用当前映射的键作为通配符来匹配指定的[key]，得到匹配的首个结果。
 * @param key 要与通配符进行匹配的键。
 * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配。
 * @see CwtDataTypeGroups.PatternLike
 */
fun <V> Map<String, V>.filterByPattern(
    key: String,
    contextElement: PsiElement,
    configGroup: CwtConfigGroup,
    matchOptions: Int = CwtConfigMatcher.Options.Default,
    fromIndex: Int = 0,
): List<V> {
    val fastResult = get(key)
    if(fastResult != null) return fastResult.toSingletonList()
    return entries.filter { (k) -> k.matchFromPattern(key, contextElement, configGroup, matchOptions, fromIndex) }.map { it.value }
}