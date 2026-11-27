@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.psi

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.core.containsBlank
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.extensions.markdown.MarkdownExtensionManager
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.ParadoxScriptedVariableSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.ParadoxNameValidators

/**
 * 用于在 Markdown 文件中，将内联代码解析为匹配的目标引用（定义、本地化等）。
 *
 * @property prefix 目标名字的前缀。封装变量的前缀是 `@`，定义和本地化的前缀为空。
 * @property name 目标名字。不包含 [prefix]。
 */
class MarkdownInlineCodeSymbolReference(
    private val element: PsiElement,
    private val rangeInElement: TextRange? = null,
    val prefix: String,
    val name: String,
) : PsiSymbolReference {
    override fun getElement(): PsiElement {
        return element
    }

    override fun getRangeInElement(): TextRange {
        return rangeInElement ?: TextRange(0, element.textLength)
    }

    override fun resolveReference(): Collection<Symbol> {
        // - 仅解析为单个符号
        // - 如果带有前缀 `@`，则尝试解析为封装变量，否则尝试解析为定义或者本地化
        val target = resolveInlineCodeTarget(element, prefix, name) ?: return emptyList()
        return listOf(MarkdownInlineCodeSymbol(target))
    }
}

internal data class MarkdownInlineCodeInfo(
    val prefix: String,
    val name: String,
    val rangeInElement: TextRange,
)

/**
 * 从 Markdown 内联代码 PSI 元素中提取目标名字信息。
 */
internal fun getMarkdownInlineCodeInfo(element: PsiElement): MarkdownInlineCodeInfo? {
    val identifier = MarkdownExtensionManager.getIdentifierFromInlineCode(element) ?: return null
    val prefix = if (identifier.startsWith('@')) "@" else ""
    val name = if (prefix.isEmpty()) identifier else identifier.removePrefixOrNull(prefix) ?: return null
    if (name.isEmpty() || name.containsBlank()) return null

    val openingLength = element.firstChild?.textLength ?: return null
    val closingLength = element.lastChild?.textLength ?: return null
    val startOffset = openingLength + prefix.length
    val endOffset = element.textLength - closingLength
    if (startOffset > endOffset) return null

    return MarkdownInlineCodeInfo(prefix, name, TextRange(startOffset, endOffset))
}

/**
 * 将内联代码中的目标名字解析为对应的 PSI 元素。
 *
 * @param element Markdown 内联代码 PSI 元素，用于提供解析上下文。
 */
internal fun resolveInlineCodeTarget(element: PsiElement, prefix: String, name: String): PsiElement? {
    return when {
        prefix == "@" -> {
            if (!ParadoxNameValidators.checkScriptedVariableName(name)) return null
            val selector = ParadoxScriptedVariableSearch.selector(element.project, element).contextSensitive()
            ParadoxScriptedVariableSearch.searchGlobal(name, selector).find()
        }
        prefix.isEmpty() -> {
            run {
                val selector = ParadoxDefinitionSearch.selector(element.project, element).contextSensitive()
                val result = ParadoxDefinitionSearch.searchElement(name, null, selector).find()
                if (result != null) return result
            }
            if (!ParadoxNameValidators.checkLocalisationName(name)) return null
            val selector = ParadoxLocalisationSearch.selector(element.project, element).contextSensitive()
                .preferLocale(ParadoxLocaleManager.getPreferredLocaleConfig())
            ParadoxLocalisationSearch.searchNormal(name, selector).find()
        }
        else -> null
    }
}
