@file:Suppress("UnstableApiUsage")

package icu.windea.pls.extensions.markdown.psi

import com.intellij.model.Symbol
import com.intellij.model.psi.PsiSymbolReference
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.extensions.markdown.MarkdownExtensionManager

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
        val target = MarkdownExtensionManager.resolveTargetFromInlineCode(element, prefix, name) ?: return emptyList()
        return listOf(MarkdownInlineCodeSymbol(target))
    }
}
