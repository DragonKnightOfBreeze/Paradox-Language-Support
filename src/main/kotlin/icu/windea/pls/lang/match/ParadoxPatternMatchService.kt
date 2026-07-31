package icu.windea.pls.lang.match

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configExpression.CwtDataExpressionRole
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.forEachFast
import icu.windea.pls.core.collections.mapFast
import icu.windea.pls.core.util.values.singletonList
import icu.windea.pls.core.util.values.to
import icu.windea.pls.ep.match.expression.ParadoxScriptExpressionMatcher
import icu.windea.pls.model.expressions.ParadoxExpression

@Optimized
object ParadoxPatternMatchService {
    /**
     * 用输入的 [text] 作为通配符来匹配指定的 [key]。
     *
     * @param key 要与通配符进行匹配的键。
     * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配。
     *
     * @see CwtDataTypeSets.PatternAware
     */
    fun matches(
        text: String,
        key: String,
        contextElement: PsiElement,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        fromIndex: Int = 0,
    ): Boolean {
        if (text == key) return true
        if (key.isEmpty()) return text.isEmpty()
        if (fromIndex < 0 || fromIndex >= text.length || fromIndex >= key.length) return false // invalid
        if (fromIndex > 0) {
            val p1 = text.substring(0, fromIndex)
            val p2 = key.substring(0, fromIndex)
            if (p1 != p2) return false
        }
        val pattern0 = text.substring(fromIndex)
        val configExpression = CwtDataExpression.resolve(pattern0, CwtDataExpressionRole.Key)
        if (configExpression.expressionString.isEmpty()) return false
        ProgressManager.checkCanceled()
        val expression = ParadoxExpression.resolve(key)
        val matchContext = ParadoxScriptExpressionMatchContext(contextElement, expression, configExpression, null, configGroup, options)
        val matchers = ParadoxScriptExpressionMatcher.get(matchContext.dataType)
        matchers.forEachFast f@{ matcher ->
            if (!matcher.isPatternAware(matchContext)) return@f
            matcher.match(matchContext)?.let { return it.get(options) }
        }
        return false
    }

    /**
     * 用输入的 [map] 的键作为通配符来匹配指定的 [key]，得到匹配的首个结果。
     *
     * @param key 要与通配符进行匹配的键。
     * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配。
     *
     * @see CwtDataTypeSets.PatternAware
     */
    fun <V> find(
        map: Map<String, V>,
        key: String,
        contextElement: PsiElement,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        fromIndex: Int = 0,
    ): V? {
        val fastResult = map.get(key)
        if (fastResult != null) return fastResult
        return map.entries.find { (k) -> matches(k, key, contextElement, configGroup, options, fromIndex) }?.value
    }

    /**
     * 用输入的 [map] 的键作为通配符来匹配指定的 [key]，得到匹配的首个结果。
     *
     * @param key 要与通配符进行匹配的键。
     * @param fromIndex 从该索引开始匹配，之前的字符串需要相同才会进行进一步的匹配。
     *
     * @see CwtDataTypeSets.PatternAware
     */
    fun <V> filter(
        map: Map<String, V>,
        key: String,
        contextElement: PsiElement,
        configGroup: CwtConfigGroup,
        options: ParadoxMatchOptions? = null,
        fromIndex: Int = 0,
    ): List<V> {
        val fastResult = map.get(key)
        if (fastResult != null) return fastResult.to.singletonList()
        return map.entries.filter { (k) -> matches(k, key, contextElement, configGroup, options, fromIndex) }.mapFast { it.value }
    }
}
