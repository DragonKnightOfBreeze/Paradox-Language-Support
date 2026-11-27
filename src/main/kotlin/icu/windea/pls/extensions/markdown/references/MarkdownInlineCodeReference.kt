package icu.windea.pls.extensions.markdown.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.ElementManipulators
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.intellij.plugins.markdown.lang.psi.impl.MarkdownCodeSpan

/**
 * 用于查找用法和重命名的 Markdown 内联代码引用。
 *
 * [rangeInElement] 只覆盖目标名字，不包含封装变量的前缀 `@`，
 * 因此重命名时不会破坏或者误并入前缀。
 */
class MarkdownInlineCodeReference(
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
