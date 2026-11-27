@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.psi

import com.intellij.openapi.application.QueryExecutorBase
import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceBase
import com.intellij.psi.search.PsiSearchScopeUtil
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.util.Processor
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import org.intellij.plugins.markdown.lang.MarkdownFileType
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeSpan

/**
 * 使目标 PSI（定义、本地化、封装变量）的查找用法和重命名能够找到 Markdown 内联代码中的引用。
 *
 * 该查询器只返回 [PsiReference]，因此不会影响 Markdown 内联代码自身的解析和导航行为。
 */
class MarkdownInlineCodeReferencesSearcher : QueryExecutorBase<PsiReference, ReferencesSearch.SearchParameters>(true) {
    override fun processQuery(queryParameters: ReferencesSearch.SearchParameters, consumer: Processor<in PsiReference>) {
        val target = queryParameters.elementToSearch
        val expected = getExpectedInlineCode(target) ?: return
        val searchScope = PsiSearchScopeUtil.restrictScopeTo(queryParameters.effectiveSearchScope, MarkdownFileType.INSTANCE)
        val processor = InlineCodeRequestResultProcessor(target, expected)
        queryParameters.optimizer.searchWord(expected.name, searchScope, UsageSearchContext.IN_PLAIN_TEXT, true, target, processor)
    }

    private data class ExpectedInlineCode(
        val prefix: String,
        val name: String,
    )

    private fun getExpectedInlineCode(target: PsiElement): ExpectedInlineCode? {
        return when (target) {
            is ParadoxScriptScriptedVariable -> target.name?.takeIf { it.isNotEmpty() }?.let { ExpectedInlineCode("@", it) }
            is ParadoxLocalisationProperty -> target.name.takeIf { it.isNotEmpty() }?.let { ExpectedInlineCode("", it) }
            is ParadoxDefinitionElement -> target.definitionInfo?.name?.takeIf { it.isNotEmpty() }?.let { ExpectedInlineCode("", it) }
            else -> null
        }
    }

    private class InlineCodeRequestResultProcessor(
        private val target: PsiElement,
        private val expected: ExpectedInlineCode,
    ) : RequestResultProcessor(target) {
        override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
            val codeSpan = element as? MarkdownCodeSpan ?: return true
            if (!target.isValid) return true

            val info = getMarkdownInlineCodeInfo(codeSpan) ?: return true
            if (info.prefix != expected.prefix || info.name != expected.name) return true
            if (!info.rangeInElement.containsOffset(offsetInElement)) return true

            val resolved = resolveInlineCodeTarget(codeSpan, info.prefix, info.name) ?: return true
            if (!target.manager.areElementsEquivalent(resolved, target)) return true

            return consumer.process(MarkdownInlineCodePsiReference(codeSpan, info.rangeInElement, target, info.prefix))
        }
    }
}

/**
 * 仅用于查找用法和重命名的 Markdown 内联代码引用。
 *
 * [rangeInElement] 只覆盖目标名字，不包含封装变量的前缀 `@`，
 * 因此重命名时不会破坏或者误并入前缀。
 */
private class MarkdownInlineCodePsiReference(
    element: MarkdownCodeSpan,
    rangeInElement: TextRange,
    private val target: PsiElement,
    private val prefix: String,
) : PsiReferenceBase<MarkdownCodeSpan>(element, rangeInElement, false) {
    override fun resolve(): PsiElement? {
        return target.takeIf { it.isValid }
    }

    override fun handleElementRename(newElementName: String): PsiElement {
        val codeSpan = element
        val openingLength = codeSpan.firstChild?.textLength ?: 0
        val closingLength = codeSpan.lastChild?.textLength ?: 0
        val contentRange = TextRange(openingLength, codeSpan.textLength - closingLength)
        val newContent = prefix + newElementName.removePrefix("@")
        return ElementManipulators.handleContentChange(codeSpan, contentRange, newContent)
    }
}
