package icu.windea.pls.config

import com.intellij.psi.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.expression.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.*

interface CwtConfigPatternAware

private val patternResolvers by lazy { CwtDataExpressionResolver.EP_NAME.extensionList.filter { it is CwtConfigPatternAware } }
private val patternMatchers by lazy { CwtDataExpressionMatcher.EP_NAME.extensionList.filter { it is CwtConfigPatternAware } }

/**
 * 用当前键作为通配符来匹配指定的[key]。
 * @param key 要与通配符进行匹配的键。
 * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配。
 * @see CwtDataTypeGroups.PatternAware
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
    val configExpression = CwtDataExpression.resolve(pattern0, true)
    if(configExpression.expressionString.isEmpty()) return false
    val expression = ParadoxDataExpression.resolve(key)
    val matchResult = patternMatchers.firstNotNullOfOrNull { it.matches(contextElement, expression, configExpression, null, configGroup, matchOptions) } ?: return false
    return matchResult.get(matchOptions)
}

/**
 * 用当前映射的键作为通配符来匹配指定的[key]，得到匹配的首个结果。
 * @param key 要与通配符进行匹配的键。
 * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配。
 * @see CwtDataTypeGroups.PatternAware
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
 * @see CwtDataTypeGroups.PatternAware
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
