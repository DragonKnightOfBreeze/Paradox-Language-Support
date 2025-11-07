package icu.windea.pls.lang.documentation

import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.documentation.DocumentationTarget
import com.intellij.platform.backend.documentation.InlineDocumentation
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.lang.psi.PlsPsiManager

@Suppress("UnstableApiUsage")
class CwtInlineDocumentation(
    private val comments: List<CwtDocComment>,
) : InlineDocumentation {
    private val owner get() = comments.lastOrNull()?.owner

    override fun getDocumentationRange(): TextRange {
        if (comments.isEmpty()) return TextRange.EMPTY_RANGE
        val startOffset = comments.first().textRange.startOffset
        val endOffset = comments.last().textRange.endOffset
        return TextRange.create(startOffset, endOffset)
    }

    override fun getDocumentationOwnerRange(): TextRange? {
        return owner?.textRange
    }

    override fun renderText(): String? {
        return PlsPsiManager.getDocCommentText(comments)
    }

    override fun getOwnerTarget(): DocumentationTarget? {
        val element = owner ?: return null
        val elementWithDocumentation = element.navigationElement ?: element
        return CwtDocumentationTarget(elementWithDocumentation, element)
    }
}
