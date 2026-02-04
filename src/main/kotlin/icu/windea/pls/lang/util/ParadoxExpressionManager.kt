package icu.windea.pls.lang.util

import com.intellij.codeInsight.daemon.impl.HighlightInfoType
import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.startOffset
import com.intellij.util.text.TextRangeUtil
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.isStatic
import icu.windea.pls.config.configExpression.CwtDataExpression
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.config.resolveElementWithConfig
import icu.windea.pls.config.resolved
import icu.windea.pls.core.collectReferences
import icu.windea.pls.core.isEmpty
import icu.windea.pls.core.isEscapedCharAt
import icu.windea.pls.core.isLeftQuoted
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.orNull
import icu.windea.pls.core.processChild
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.core.withDependencyItems
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.ParadoxModificationTrackers
import icu.windea.pls.lang.PlsStates
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.ParadoxMatchService
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.mock.CwtMemberConfigElement
import icu.windea.pls.lang.references.csv.ParadoxCsvExpressionPsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationExpressionPsiReference
import icu.windea.pls.lang.references.script.ParadoxScriptExpressionPsiReference
import icu.windea.pls.lang.resolve.ParadoxCsvExpressionService
import icu.windea.pls.lang.resolve.ParadoxLocalisationExpressionService
import icu.windea.pls.lang.resolve.ParadoxScriptExpressionService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxComplexExpressionNode
import icu.windea.pls.lang.resolve.complexExpression.nodes.ParadoxTokenNode
import icu.windea.pls.lang.resolve.expression.ParadoxScriptExpression
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.isComplexExpression
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxParameter
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptInlineMath
import icu.windea.pls.script.psi.ParadoxScriptInlineParameterCondition
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement
import icu.windea.pls.script.psi.isExpression

object ParadoxExpressionManager {
    object Keys : KeyRegistry() {
        val cachedParameterRanges by registerKey<CachedValue<List<TextRange>>>(Keys)
        val cachedExpressionReferences by registerKey<CachedValue<Array<out PsiReference>>>(Keys)
        val cachedExpressionReferencesForMergedIndex by registerKey<CachedValue<Array<out PsiReference>>>(Keys)
    }

    // region Common Methods

    fun isParameterized(text: String, conditionBlock: Boolean = true, full: Boolean = false): Boolean {
        // 快速判断，不检测带参数后的语法是否合法
        if (text.length < 2) return false
        if (full) {
            // $PARAM$ - 仅限 高级插值语法 A
            if (!text.startsWith('$')) return false
            if (text.indexOf('$', 1).let { c -> c != text.lastIndex || text.isEscapedCharAt(c) }) return false
            return true
        }
        // a_$PARAM$_b - 高级插值语法 A
        if (text.indexOf('$').let { c -> c != -1 && !text.isEscapedCharAt(c) }) return true
        // a_[[PARAM]b]_c - 高级插值语法 B
        if (conditionBlock && text.indexOf("[[").let { c -> c != -1 && !text.isEscapedCharAt(c) }) return true
        return false
    }

    @Suppress("unused")
    fun getParameterName(text: String): String? {
        // $PARAM$ - 仅限 高级插值语法 A
        if (!isParameterized(text, full = true)) return null
        return text.substring(1, text.length - 1).substringBefore('|')
    }

    fun getParameterRanges(text: String, conditionBlock: Boolean = true): List<TextRange> {
        // 比较复杂的实现逻辑
        val ranges = mutableListOf<TextRange>()
        // a_$PARAM$_b - 高级插值语法 A - 深度计数
        var depth1 = 0
        // a_[[PARAM]b]_c - 高级插值语法 B - 深度计数
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
                    ranges += TextRange.create(startIndex, endIndex + 1)
                    depth1--

                }
            } else if (conditionBlock && c == '[' && !text.isEscapedCharAt(i)) {
                if (depth1 > 0) continue
                if (depth2 == 0) {
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
                    ranges += TextRange.create(startIndex, endIndex + 1)
                }
            }
        }
        if (startIndex != -1 && endIndex == -1) {
            ranges += TextRange.create(startIndex, text.length)
        }
        return ranges
    }

    private val regex1 = """(?<!\\)\$.*?\$""".toRegex()
    private val regex2 = """(?<!\\)\[\[.*?](.*?)]""".toRegex()

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

    fun getExpressionText(element: ParadoxExpressionElement, rangeInElement: TextRange? = null): String {
        return when {
            element is ParadoxScriptBlock -> "" // should not be used
            element is ParadoxScriptInlineMath -> "" // should not be used
            rangeInElement != null -> rangeInElement.substring(element.text)
            element is ParadoxScriptStringExpressionElement -> element.text.unquote()
            element is ParadoxCsvColumn -> element.text.unquote()
            else -> element.text
        }
    }

    fun getExpressionTextRange(element: ParadoxExpressionElement): TextRange {
        return when (element) {
            is ParadoxScriptBlock -> TextRange.create(0, 1) // "{"
            is ParadoxScriptInlineMath -> element.firstChild.textRangeInParent // "@[" or "@\["
            is ParadoxScriptStringExpressionElement -> TextRange.create(0, element.textLength).unquote(element.text)
            is ParadoxCsvColumn -> TextRange.create(0, element.textLength).unquote(element.text)
            else -> TextRange.create(0, element.textLength)
        }
    }

    fun getExpressionOffset(element: ParadoxExpressionElement): Int {
        return when {
            element is ParadoxScriptStringExpressionElement && element.text.isLeftQuoted() -> 1
            else -> 0
        }
    }

    fun getParameterRangesInExpression(element: ParadoxExpressionElement): List<TextRange> {
        return doGetParameterRangesInExpressionFromCache(element)
    }

    private fun doGetParameterRangesInExpressionFromCache(element: ParadoxExpressionElement): List<TextRange> {
        return CachedValuesManager.getCachedValue(element, Keys.cachedParameterRanges) {
            val value = doGetParameterRangesInExpression(element)
            value.withDependencyItems(element)
        }
    }

    private fun doGetParameterRangesInExpression(element: ParadoxExpressionElement): List<TextRange> {
        var parameterRanges: MutableList<TextRange>? = null
        element.processChild { e ->
            if (isParameterElementInExpression(e)) {
                if (parameterRanges == null) parameterRanges = mutableListOf()
                parameterRanges.add(e.textRange)
            }
            true
        }
        return parameterRanges.orEmpty()
    }

    fun isParameterElementInExpression(element: PsiElement): Boolean {
        return element is ParadoxParameter || element is ParadoxScriptInlineParameterCondition || element is ParadoxLocalisationParameter
    }

    fun isUnaryOperatorAwareParameter(text: String, parameterRanges: List<TextRange>): Boolean {
        return text.firstOrNull()?.let { it == '+' || it == '-' } == true && parameterRanges.singleOrNull()?.let { it.startOffset == 1 && it.endOffset == text.length } == true
    }

    fun resolve(text: String, contextElement: PsiElement?, project: Project): String? {
        // 非常神秘，但这个方法在某些情况下是必要的（例如：`value:a|b|@c|`）
        run {
            val name = text.removePrefixOrNull("@")?.orNull() ?: return@run
            val selector = selector(project, contextElement).scriptedVariable().contextSensitive()
            ParadoxScriptedVariableSearch.searchLocal(name, selector).findAll().lastOrNull()?.let { return it.value }
            ParadoxScriptedVariableSearch.searchGlobal(name, selector).find()?.let { return it.value }
        }
        return text
    }

    // endregion

    // region Annotate Methods

    fun annotateScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder, config: CwtConfig<*>) {
        val expressionText = getExpressionText(element, rangeInElement)
        ParadoxScriptExpressionService.annotate(element, rangeInElement, expressionText, holder, config)
    }

    fun annotateLocalisationExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder) {
        val expressionText = getExpressionText(element, rangeInElement)
        ParadoxLocalisationExpressionService.annotate(element, rangeInElement, expressionText, holder)
    }

    fun annotateCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, holder: AnnotationHolder, config: CwtValueConfig) {
        if (element is ParadoxCsvColumn && element.isHeaderColumn()) return
        val expressionText = getExpressionText(element, rangeInElement)
        ParadoxCsvExpressionService.annotate(element, rangeInElement, expressionText, holder, config)
    }

    fun annotateExpressionByAttributesKey(element: ParadoxExpressionElement, range: TextRange, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        if (range.isEmpty) return
        // skip parameter ranges
        val parameterRanges = getParameterRangesInExpression(element)
        if (parameterRanges.isEmpty()) {
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(range).textAttributes(attributesKey).create()
            return
        }
        val finalRanges = TextRangeUtil.excludeRanges(range, parameterRanges)
        for (r in finalRanges) {
            if (r.isEmpty) continue
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(r).textAttributes(attributesKey).create()
        }
    }

    fun annotateExpressionAsHighlightedReference(range: TextRange, holder: AnnotationHolder) {
        holder.newSilentAnnotation(HighlightInfoType.HIGHLIGHTED_REFERENCE_SEVERITY).range(range).textAttributes(DefaultLanguageHighlighterColors.HIGHLIGHTED_REFERENCE).create()
    }

    fun annotateComplexExpression(element: ParadoxExpressionElement, expression: ParadoxComplexExpression, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        annotateComplexExpressionNode(element, expression, holder, config)
    }

    private fun annotateComplexExpressionNode(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, holder: AnnotationHolder, config: CwtConfig<*>? = null) {
        val attributesKey = node.getAttributesKey(element)

        run {
            val mustUseAttributesKey = attributesKey != ParadoxScriptAttributesKeys.PROPERTY_KEY_KEY && attributesKey != ParadoxScriptAttributesKeys.STRING_KEY
            if (attributesKey != null && mustUseAttributesKey) {
                annotateNodeByAttributesKey(element, node, attributesKey, holder)
                return@run
            }
            val attributesKeyConfig = node.getAttributesKeyConfig(element)
            if (attributesKeyConfig != null) {
                val rangeInElement = node.rangeInExpression.shiftRight(if (element.text.isLeftQuoted()) 1 else 0)
                annotateScriptExpression(element, rangeInElement, holder, attributesKeyConfig)
                return@run
            }
            if (attributesKey != null) {
                annotateNodeByAttributesKey(element, node, attributesKey, holder)
            }
        }

        if (node.nodes.isNotEmpty()) {
            for (node in node.nodes) {
                annotateComplexExpressionNode(element, node, holder, config)
            }
        }
    }

    private fun annotateNodeByAttributesKey(element: ParadoxExpressionElement, node: ParadoxComplexExpressionNode, attributesKey: TextAttributesKey, holder: AnnotationHolder) {
        val startOffest = element.startOffset + getExpressionOffset(element)
        val rangeToAnnotate = node.rangeInExpression.shiftRight(startOffest)

        // merge text attributes from HighlighterColors.TEXT and attributesKey for token nodes (in case foreground is not set)
        if (node is ParadoxTokenNode) {
            val editorColorsManager = EditorColorsManager.getInstance()
            val schema = editorColorsManager.activeVisibleScheme ?: editorColorsManager.schemeForCurrentUITheme
            val textAttributes1 = schema.getAttributes(HighlighterColors.TEXT)
            val textAttributes2 = schema.getAttributes(attributesKey)
            val textAttributes = TextAttributes.merge(textAttributes1, textAttributes2)
            holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(rangeToAnnotate).enforcedTextAttributes(textAttributes).create()
            return
        }

        annotateExpressionByAttributesKey(element, rangeToAnnotate, attributesKey, holder)
    }

    // endregion

    // region Reference Methods

    fun getExpressionReferences(element: ParadoxExpressionElement): Array<out PsiReference> {
        ProgressManager.checkCanceled()
        // 尝试兼容可能包含参数的情况
        // if(element.text.isParameterized()) return PsiReference.EMPTY_ARRAY
        return when (element) {
            is ParadoxScriptExpressionElement -> {
                if (!element.isExpression()) return PsiReference.EMPTY_ARRAY
                doGetExpressionReferencesFromCache(element)
            }
            is ParadoxLocalisationExpressionElement -> {
                if (!element.isComplexExpression()) return PsiReference.EMPTY_ARRAY
                doGetExpressionReferencesFromCache(element)
            }
            is ParadoxCsvExpressionElement -> {
                doGetExpressionReferencesFromCache(element)
            }
            else -> PsiReference.EMPTY_ARRAY
        }
    }

    private fun doGetExpressionReferencesFromCache(element: ParadoxExpressionElement): Array<out PsiReference> {
        val cacheKey = doGetExpressionReferencesCacheKey()
        return CachedValuesManager.getCachedValue(element, cacheKey) {
            val value = doGetExpressionReferences(element)
            value.withDependencyItems(element, ParadoxModificationTrackers.Resolve)
        }
    }

    private fun doGetExpressionReferencesCacheKey(): Key<CachedValue<Array<out PsiReference>>> {
        val processMergedIndex = PlsStates.processMergedIndex.get() == true
        val key = if (processMergedIndex) Keys.cachedExpressionReferencesForMergedIndex else Keys.cachedExpressionReferences
        return key
    }

    private fun doGetExpressionReferences(element: ParadoxExpressionElement): Array<out PsiReference> {
        return when (element) {
            is ParadoxScriptExpressionElement -> doGetExpressionReferences(element)
            is ParadoxLocalisationExpressionElement -> doGetExpressionReferences(element)
            is ParadoxCsvExpressionElement -> doGetExpressionReferences(element)
            else -> PsiReference.EMPTY_ARRAY
        }
    }

    private fun doGetExpressionReferences(element: ParadoxScriptExpressionElement): Array<out PsiReference> {
        // 尝试基于规则进行解析
        val isKey = element is ParadoxScriptPropertyKey
        val processMergedIndex = PlsStates.processMergedIndex.get() == true
        val options = if (processMergedIndex) ParadoxMatchOptions.DUMB else ParadoxMatchOptions.DEFAULT
        val configs = ParadoxConfigManager.getConfigs(element, options.copy(fallback = isKey))
        val config = configs.firstOrNull() ?: return PsiReference.EMPTY_ARRAY
        val textRange = getExpressionTextRange(element) // unquoted text
        val reference = ParadoxScriptExpressionPsiReference(element, textRange, config, isKey)
        return reference.collectReferences()
    }

    private fun doGetExpressionReferences(element: ParadoxLocalisationExpressionElement): Array<out PsiReference> {
        // 尝试解析为复杂表达式
        val value = element.value
        val textRange = TextRange.create(0, value.length)
        val reference = ParadoxLocalisationExpressionPsiReference(element, textRange)
        return reference.collectReferences()
    }

    private fun doGetExpressionReferences(element: ParadoxCsvExpressionElement): Array<out PsiReference> {
        val columnConfig = when (element) {
            is ParadoxCsvColumn -> ParadoxCsvManager.getColumnConfig(element)
            else -> null
        }
        if (columnConfig == null) return PsiReference.EMPTY_ARRAY
        val textRange = getExpressionTextRange(element) // unquoted text
        val reference = ParadoxCsvExpressionPsiReference(element, textRange, columnConfig)
        return arrayOf(reference)
    }

    fun cleanUpExpressionReferencesCache(element: ParadoxExpressionElement) {
        val cacheKey = doGetExpressionReferencesCacheKey()
        element.putUserData(cacheKey, null)
    }

    // endregion

    // region Resolve Methods

    fun resolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null, exact: Boolean = true): PsiElement? {
        ProgressManager.checkCanceled()
        if (configExpression == null) return null
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return null // 排除引用文本带参数的情况

        val result = ParadoxScriptExpressionService.resolve(element, rangeInElement, expressionText, config, isKey, exact)
        if (result != null) return result

        if (configExpression.isKey) return getResolvedConfigElement(element, config, config.configGroup)

        return null
    }

    fun multiResolveScriptExpression(element: ParadoxExpressionElement, rangeInElement: TextRange?, config: CwtConfig<*>, configExpression: CwtDataExpression?, isKey: Boolean? = null): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        if (configExpression == null) return emptySet()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return emptySet() // 排除引用文本带参数的情况

        val result = ParadoxScriptExpressionService.multiResolve(element, rangeInElement, expressionText, config, isKey)
        if (result.isNotNullOrEmpty()) return result

        if (configExpression.isKey) return getResolvedConfigElement(element, config, config.configGroup).to.singletonSetOrEmpty()

        return emptySet()
    }

    private fun getResolvedConfigElement(element: ParadoxExpressionElement, config: CwtConfig<*>, configGroup: CwtConfigGroup): PsiElement? {
        val resolvedConfig = config.resolved()
        if (resolvedConfig is CwtMemberConfig<*> && resolvedConfig.pointer.isEmpty()) {
            // 特殊处理合成的规则
            val gameType = configGroup.gameType
            val project = configGroup.project
            return CwtMemberConfigElement(element, resolvedConfig, gameType, project)
        }

        return resolvedConfig.pointer.element
    }

    fun resolveLocalisationExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): PsiElement? {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return null // 排除引用文本带参数的情况

        val result = ParadoxLocalisationExpressionService.resolve(element, rangeInElement, expressionText)
        return result
    }

    fun multiResolveLocalisationExpression(element: ParadoxLocalisationExpressionElement, rangeInElement: TextRange?): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        val expressionText = getExpressionText(element, rangeInElement)
        if (expressionText.isParameterized()) return emptySet() // 排除引用文本带参数的情况

        val result = ParadoxLocalisationExpressionService.multiResolve(element, rangeInElement, expressionText)
        return result
    }

    fun resolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, config: CwtValueConfig): PsiElement? {
        ProgressManager.checkCanceled()
        if (element is ParadoxCsvColumn && element.isHeaderColumn()) return null
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxCsvExpressionService.resolve(element, rangeInElement, expressionText, config)
        return result
    }

    fun multiResolveCsvExpression(element: ParadoxCsvExpressionElement, rangeInElement: TextRange?, config: CwtValueConfig): Collection<PsiElement> {
        ProgressManager.checkCanceled()
        if (element is ParadoxCsvColumn && element.isHeaderColumn()) return emptySet()
        val expressionText = getExpressionText(element, rangeInElement)

        val result = ParadoxCsvExpressionService.multiResolve(element, rangeInElement, expressionText, config)
        return result
    }

    fun resolveModifier(element: ParadoxExpressionElement, name: String, configGroup: CwtConfigGroup): PsiElement? {
        if (element !is ParadoxScriptStringExpressionElement) return null // NOTE 1.4.0 - unnecessary to support yet
        return ParadoxModifierManager.resolveModifier(name, element, configGroup)
    }

    @Suppress("unused")
    fun resolveSystemScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val systemScopeConfig = configGroup.systemScopes[name] ?: return null
        val resolved = systemScopeConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolveScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.links[name]?.takeIf { it.type.forScope() && it.isStatic } ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolveValueField(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.links[name]?.takeIf { it.type.forValue() && it.isStatic } ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    fun resolvePredefinedEnumValue(name: String, enumName: String, configGroup: CwtConfigGroup): PsiElement? {
        val enumConfig = configGroup.enums[enumName] ?: return null
        val enumValueConfig = enumConfig.valueConfigMap.get(name) ?: return null
        val resolved = enumValueConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolvePredefinedLocalisationScope(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val linkConfig = configGroup.localisationLinks[name] ?: return null
        val resolved = linkConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    @Suppress("unused")
    fun resolvePredefinedLocalisationCommand(name: String, configGroup: CwtConfigGroup): PsiElement? {
        val commandConfig = configGroup.localisationCommands[name] ?: return null
        val resolved = commandConfig.resolveElementWithConfig() ?: return null
        return resolved
    }

    // endregion

    // region Misc Methods

    fun getMatchedAliasKey(element: PsiElement, configGroup: CwtConfigGroup, aliasName: String, key: String, quoted: Boolean, options: ParadoxMatchOptions? = null): String? {
        val constKey = configGroup.aliasKeysGroupConst[aliasName]?.get(key) // 不区分大小写
        if (constKey != null) return constKey
        val keys = configGroup.aliasKeysGroupNoConst[aliasName] ?: return null
        val expression = ParadoxScriptExpression.resolve(key, quoted, true)
        return keys.find { ParadoxMatchService.matchScriptExpression(element, expression, CwtDataExpression.resolve(it, true), null, configGroup, options).get(options) }
    }

    // endregion
}
