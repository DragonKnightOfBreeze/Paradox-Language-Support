package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.ep.match.ParadoxPatternMatcher

@Optimized
object ParadoxPatternMatchService {
    /**
     * @see ParadoxPatternMatcher.matches
     */
    fun matches(text: String, ignoreCase: Boolean, context: ParadoxPatternMatchContext): Boolean {
        val matchers = ParadoxPatternMatcher.getAll()
        matchers.forEachFast f@{ matcher ->
            matcher.matches(text, ignoreCase, context).let { if (it) return true }
        }
        return false
    }

    /**
     * 用 [input] 作为通配符来源，匹配指定的 [key]。
     *
     * @param key 要与通配符进行匹配的键。
     * @param element 上下文 PSI 元素。
     * @param configGroup 规则分组。
     * @param startIndex 从该索引开始匹配，之前的字符串需要完全相同才会进行进一步的匹配。
     * @param ignoreCase 显式指定匹配时是否忽略大小写。
     *
     * @see ParadoxPatternMatcher
     */
    fun matches(
        input: String,
        key: String,
        element: PsiElement,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        startIndex: Int = 0,
        ignoreCase: Boolean = false,
    ): Boolean {
        if (input == key) return true
        if (key.isEmpty()) return input.isEmpty()
        if (startIndex < 0 || startIndex >= input.length || startIndex >= key.length) return false // invalid
        if (startIndex > 0) {
            val p1 = input.substring(0, startIndex)
            val p2 = key.substring(0, startIndex)
            if (p1 != p2) return false // always case-sensitive here
        }
        val text = key.substring(startIndex)
        val configExpression = CwtDataExpression.resolve(input.substring(startIndex), CwtDataExpressionRole.Key)
        if (configExpression.expressionString.isEmpty()) return false
        ProgressManager.checkCanceled()
        val context = ParadoxPatternMatchContext(element, configExpression, configGroup, options)
        return matches(text, ignoreCase, context)
    }

    /**
     * 用 [map] 的键作为通配符来源，匹配指定的 [key]，得到匹配的首个结果。
     *
     * @param key 要与通配符进行匹配的键。
     * @param element 上下文 PSI 元素。
     * @param configGroup 规则分组。
     * @param startIndex 从该索引开始匹配，之前的字符串需要完全相同才会进行进一步的匹配。
     * @param ignoreCase 显式指定匹配时是否忽略大小写。
     *
     * @see ParadoxPatternMatcher
     */
    fun <V> find(
        map: Map<String, V>,
        key: String,
        element: PsiElement,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        startIndex: Int = 0,
        ignoreCase: Boolean = false,
    ): V? {
        val fastResult = map.get(key)
        if (fastResult != null) return fastResult
        return map.entries.find { (k) -> matches(k, key, element, configGroup, options, startIndex, ignoreCase) }?.value
    }

    /**
     * 用 [map] 的键作为通配符，匹配指定的 [key]，得到匹配的所有值的集合。
     *
     * @param key 要与通配符进行匹配的键。
     * @param element 上下文 PSI 元素。
     * @param configGroup 规则分组。
     * @param startIndex 从该索引开始匹配，之前的字符串需要完全相同才会进行进一步的匹配。
     * @param ignoreCase 显式指定匹配时是否忽略大小写。
     *
     * @see ParadoxPatternMatcher
     */
    fun <V> filter(
        map: Map<String, V>,
        key: String,
        element: PsiElement,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        startIndex: Int = 0,
        ignoreCase: Boolean = false,
    ): Collection<V> {
        val fastResult = map.get(key)
        if (fastResult != null) return fastResult.to.singletonList()
        return map.filterKeys { k -> matches(k, key, element, configGroup, options, startIndex, ignoreCase) }.values
    }
}
