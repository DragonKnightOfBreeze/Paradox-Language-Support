package icu.windea.pls.extensions.markdown

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import icu.windea.pls.core.containsBlank
import icu.windea.pls.core.removePrefixOrNull

data class MarkdownInlineCodeInfo(
    val prefix: String,
    val name: String,
    val rangeInElement: TextRange,
) {
    companion object {
        /**
         * 从 Markdown 内联代码 PSI 元素中提取目标名字信息。
         */
        fun from(element: PsiElement): MarkdownInlineCodeInfo? {
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
    }
}
