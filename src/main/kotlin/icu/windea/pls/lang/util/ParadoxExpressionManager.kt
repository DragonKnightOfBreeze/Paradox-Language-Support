package icu.windea.pls.lang.util

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.findFast
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.isIdentifierChar
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.isComplexExpression
import icu.windea.pls.lang.psi.isDefinitionTypeKey
import icu.windea.pls.lang.psi.isResolvableLiteralExpression
import icu.windea.pls.lang.resolve.ParadoxExpressionService
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.model.type.ParadoxExpressionRole
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.isDataExpression

@Optimized
object ParadoxExpressionManager {
    object Keys : KeyRegistry() {
        val cachedReferences by registerKey<CachedValue<Array<out PsiReference>>>(Keys)
        val cachedReferencesDumb by registerKey<CachedValue<Array<out PsiReference>>>(Keys)
        val cachedExpressionReferences by registerKey<CachedValue<Array<out PsiReference>>>(Keys)
        val cachedExpressionReferencesDumb by registerKey<CachedValue<Array<out PsiReference>>>(Keys)
    }

    // region Common Methods

    private val regex1 = """(?<!\\)\$.*?\$""".toRegex()
    private val regex2 = """(?<!\\)\[\[.*?](.*?)]""".toRegex()

    /**
     * 检查 [text] 是否携带参数。
     *
     * 说明：
     * - “携带参数”意味着使用到了其中一种或多种高级插值语法：参数（形如 `a_$PARAM$_b`）和条件块（形如 `a_[[PARAM]b]_c`）。
     * - 快速判断，不检查携带参数后的语法是否合法。
     * - 仅接受长度大于2的字符串。
     */
    fun isParameterized(text: String, conditionBlock: Boolean = true, full: Boolean = false): Boolean {
        // 快速判断，不检测带参数后的语法是否合法
        val length = text.length
        if (length < 2) return false
        if (full) {
            // `$PARAM$` - 仅限：高级插值语法 A
            return text[0] == '$' && text.indexOf('$', 1).let { c -> c == length - 1 && !text.isEscapedCharAt(c) }
        }
        // `a_$PARAM$_b` - 高级插值语法 A
        // `a_[[PARAM]b]_c` - 高级插值语法 B
        for ((i, c) in text.withIndex()) {
            if (c == '$' && !text.isEscapedCharAt(i)) {
                return true
            } else if (conditionBlock && c == '[' && !text.isEscapedCharAt(i)) {
                if (i == length - 1 || text[i + 1] != '[') continue // 仅接受 `[[`
                return true
            }
        }
        return false
    }

    /**
     * 得到 [text] 中携带的参数的一组文本范围。
     *
     * 说明：
     * - “携带参数”意味着使用到了其中一种或多种高级插值语法：参数（形如 `a_$PARAM$_b`）和条件块（形如 `a_[[PARAM]b]_c`）。
     * - 快速判断，不检查携带参数后的语法是否合法。
     * - 仅接受长度大于2的字符串，否则直接返回空列表。
     */
    fun getParameterRanges(text: String, conditionBlock: Boolean = true): List<TextRange> {
        // 优化：仅在必要时创建列表
        if (text.length < 2) return emptyList()
        var parameterRanges: MutableList<TextRange>? = null
        // `a_$PARAM$_b` - 高级插值语法 A - 深度计数
        var depth1 = 0
        // `a_[[PARAM]b]_c` - 高级插值语法 B - 深度计数
        var depth2 = 0
        var startIndex = -1
        var endIndex = -1
        for ((i, c) in text.withIndex()) {
            if (c == '$' && !text.isEscapedCharAt(i)) {
                if (depth2 > 0) continue
                if (depth1 == 0) {
                    startIndex = i
                    endIndex = -1
                    depth1++
                } else {
                    endIndex = i
                    if (parameterRanges == null) parameterRanges = ArrayList()
                    parameterRanges += TextRange.create(startIndex, endIndex + 1)
                    depth1--

                }
            } else if (conditionBlock && c == '[' && !text.isEscapedCharAt(i)) {
                if (depth1 > 0) continue
                if (depth2 == 0) {
                    if (i == text.length - 1 || text[i + 1] != '[') continue // 仅接受 `[[`
                    startIndex = i
                    endIndex = -1
                }
                depth2++
            } else if (conditionBlock && c == ']' && !text.isEscapedCharAt(i)) {
                if (depth1 > 0) continue
                if (depth2 <= 0) continue
                depth2--
                if (depth2 == 0) {
                    endIndex = i
                    if (parameterRanges == null) parameterRanges = ArrayList()
                    parameterRanges += TextRange.create(startIndex, endIndex + 1)
                }
            }
        }
        if (startIndex != -1 && endIndex == -1) {
            if (parameterRanges == null) parameterRanges = ArrayList()
            parameterRanges += TextRange.create(startIndex, text.length)
        }
        return parameterRanges ?: emptyList()
    }

    /**
     * 检查 [text] 是否为允许携带参数的有效的标识符（字符串）。
     *
     * 说明：
     * - “携带参数”意味着使用到了其中一种或多种高级插值语法：参数（形如 `a_$PARAM$_b`）和条件块（形如 `a_[[PARAM]b]_c`）。
     * - 快速判断，不检查携带参数后的语法是否合法。
     * - 通过 [extraChars] 指定额外接受的字符。不接受空字符串。
     * - 不接受空字符串。
     */
    fun isParameterAwareIdentifier(text: String, extraChars: String = ""): Boolean {
        // 优化：仅在必要时创建列表
        if (text.isEmpty()) return false
        var parameterRanges: List<TextRange>? = null
        for ((i, c) in text.withIndex()) {
            if (c.isIdentifierChar(extraChars)) continue
            if (parameterRanges == null) parameterRanges = getParameterRanges(text)
            if (parameterRanges.findFast { it.contains(i) } != null) continue
            return false
        }
        return true
    }

    fun isParameterAwareNumber(text: String, parameterRanges: List<TextRange>): Boolean {
        return text.firstOrNull()?.let { it == '+' || it == '-' } == true
            && parameterRanges.singleOrNull()?.let { it.startOffset == 1 && it.endOffset == text.length } == true
    }

    fun toRegex(text: String, conditionBlock: Boolean = true): Regex {
        var s = text
        s = """\Q$s\E"""
        s = s.replace(regex1, """\\E.*\\Q""")
        if (conditionBlock) {
            s = s.replace(regex2) { g ->
                val dv = g.groupValues[1]
                when {
                    dv == """\E.*\Q""" -> """\E.*\Q"""
                    else -> """\E(?:\Q$dv\E)?\Q"""
                }
            }
        }
        s = s.replace("""\Q\E""", "")
        return s.toRegex(RegexOption.IGNORE_CASE)
    }

    fun resolveExpressionText(text: String, contextElement: PsiElement?, project: Project): String? {
        // 非常神秘，但这个方法在某些情况下是必要的（例如：`value:a|b|@c|`）
        run {
            val name = text.removePrefixOrNull("@")?.orNull() ?: return@run
            val selector = ParadoxScriptedVariableSearch.selector(project, contextElement).contextSensitive()
            ParadoxScriptedVariableSearch.searchLocal(name, selector).findAll().lastOrNull()?.let { return it.value }
            ParadoxScriptedVariableSearch.searchGlobal(name, selector).find()?.let { return it.value }
        }
        return text
    }

    // endregion

    // region Annotate Methods

    /**
     * @see ParadoxExpressionService.annotateScriptExpression
     */
    fun annotateScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, holder: AnnotationHolder) {
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)
        ParadoxExpressionService.annotateScriptExpression(element, rangeInElement, expressionText, config, holder)
    }

    /**
     * @see ParadoxExpressionService.annotateLocalisationExpression
     */
    fun annotateLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder) {
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)
        ParadoxExpressionService.annotateLocalisationExpression(element, rangeInElement, expressionText, holder)
    }

    /**
     * @see ParadoxExpressionService.annotateCsvExpression
     */
    fun annotateCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, config: CwtValueConfig, holder: AnnotationHolder) {
        if (element is ParadoxCsvColumn && ParadoxCsvPsiService.isHeaderColumn(element)) return
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)
        ParadoxExpressionService.annotateCsvExpression(element, rangeInElement, expressionText, config, holder)
    }

    // endregion

    // region Resolve Methods

    /**
     * @see ParadoxExpressionService.resolveScriptExpression
     */
    fun resolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, role: ParadoxExpressionRole): PsiElement? {
        if (config.configExpression == null) return null
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return null // 排除文本带参数的情况

        ProgressManager.checkCanceled()
        return ParadoxExpressionService.resolveScriptExpression(element, rangeInElement, expressionText, config, role)
    }

    /**
     * @see ParadoxExpressionService.resolveAllScriptExpression
     */
    fun resolveAllScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, role: ParadoxExpressionRole): List<PsiElement> {
        if (config.configExpression == null) return emptyList()
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return emptyList() // 排除文本带参数的情况

        ProgressManager.checkCanceled()
        return ParadoxExpressionService.resolveAllScriptExpression(element, rangeInElement, expressionText, config, role)
    }

    /**
     * @see ParadoxExpressionService.resolveLocalisationExpression
     */
    fun resolveLocalisationExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): PsiElement? {
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return null // 排除文本带参数的情况

        ProgressManager.checkCanceled()
        return ParadoxExpressionService.resolveLocalisationExpression(element, rangeInElement, expressionText)
    }

    /**
     * @see ParadoxExpressionService.resolveAllLocalisationExpression
     */
    fun resolveAllLocalisationExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): List<PsiElement> {
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return emptyList() // 排除文本带参数的情况

        ProgressManager.checkCanceled()
        return ParadoxExpressionService.resolveAllLocalisationExpression(element, rangeInElement, expressionText)
    }

    /**
     * @see ParadoxExpressionService.resolveCsvExpression
     */
    fun resolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, config: CwtValueConfig): PsiElement? {
        if (element is ParadoxCsvColumn && ParadoxCsvPsiService.isHeaderColumn(element)) return null
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)

        ProgressManager.checkCanceled()
        return ParadoxExpressionService.resolveCsvExpression(element, rangeInElement, expressionText, config)
    }

    /**
     * @see ParadoxExpressionService.resolveAllCsvExpression
     */
    fun resolveAllCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, config: CwtValueConfig): List<PsiElement> {
        if (element is ParadoxCsvColumn && ParadoxCsvPsiService.isHeaderColumn(element)) return emptyList()
        val expressionText = ParadoxExpressionService.getExpressionText(element, rangeInElement)

        ProgressManager.checkCanceled()
        return ParadoxExpressionService.resolveAllCsvExpression(element, rangeInElement, expressionText, config)
    }

    // endregion

    // region PSI Reference Methods

    private val EXPRESSION_HINTS = PsiReferenceService.Hints()

    fun getReferences(element: ParadoxExpressionElement): Array<out PsiReference> {
        // NOTE 2.1.7 DO NOT just call `ReferenceProvidersRegistry.getReferencesFromProviders()` directly to avoid non-idempotent computation problem
        ProgressManager.checkCanceled()
        return getReferencesFromCache(element)
    }

    private fun getReferencesFromCache(element: ParadoxExpressionElement): Array<out PsiReference> {
        val isDumb = ParadoxMatchService.isDumb()
        val cacheKey = if (isDumb) Keys.cachedReferencesDumb else Keys.cachedReferences
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            ProgressManager.checkCanceled()
            val value = resolveReferences(element)
            val tracker = ParadoxModificationTrackers.expression(element)
            value.withDependencyItems(element, PsiModificationTracker.MODIFICATION_COUNT, tracker)
        }
    }

    private fun resolveReferences(element: ParadoxExpressionElement): Array<out PsiReference> {
        return ReferenceProvidersRegistry.getReferencesFromProviders(element, EXPRESSION_HINTS)
    }

    fun getExpressionReferences(element: ParadoxExpressionElement): Array<out PsiReference> {
        ProgressManager.checkCanceled()
        if (!checkForExpressionReferences(element)) return PsiReference.EMPTY_ARRAY
        return getExpressionReferencesFromCache(element)
    }

    private fun checkForExpressionReferences(element: ParadoxExpressionElement): Boolean {
        return when (element) {
            is ParadoxScriptExpressionElement -> {
                if (!element.isResolvableLiteralExpression() && element !is ParadoxScriptBlock) return false // #131
                if (!element.isDataExpression()) return false // fast return
                // skip for definition type keys (and definition injection expressions)
                if (element is ParadoxScriptPropertyKey && element.isDefinitionTypeKey()) return false
                true
            }
            is ParadoxLocalisationExpressionElement -> {
                if (!element.isComplexExpression()) return false
                true
            }
            is ParadoxCsvExpressionElement -> {
                if (element !is ParadoxCsvColumn) return false
                true
            }
            else -> false
        }
    }

    private fun getExpressionReferencesFromCache(element: ParadoxExpressionElement): Array<out PsiReference> {
        val isDumb = ParadoxMatchService.isDumb()
        val cacheKey = if (isDumb) Keys.cachedExpressionReferencesDumb else Keys.cachedExpressionReferences
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            ProgressManager.checkCanceled()
            val value = resolveExpressionReferences(element)
            val tracker = ParadoxModificationTrackers.expression(element)
            value.withDependencyItems(element, tracker)
        }
    }

    private fun resolveExpressionReferences(element: ParadoxExpressionElement): Array<out PsiReference> {
        return when (element) {
            is ParadoxScriptExpressionElement -> ParadoxExpressionService.resolveScriptExpressionReferences(element)
            is ParadoxLocalisationExpressionElement -> ParadoxExpressionService.resolveLocalisationExpressionReferences(element)
            is ParadoxCsvExpressionElement -> ParadoxExpressionService.resolveCsvExpressionReferences(element)
            else -> PsiReference.EMPTY_ARRAY
        }
    }

    // endregion
}
