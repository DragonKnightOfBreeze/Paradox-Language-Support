package icu.windea.pls.lang.codeInsight.documentation

import com.intellij.openapi.util.TextRange
import com.intellij.platform.backend.documentation.InlineDocumentation
import com.intellij.platform.backend.documentation.InlineDocumentationProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiRecursiveElementVisitor
import com.intellij.psi.util.parentOfType
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.optimized
import icu.windea.pls.cwt.psi.CwtDocComment
import icu.windea.pls.cwt.psi.CwtPsiUtil
import icu.windea.pls.lang.psi.PlsPsiManager

@Suppress("UnstableApiUsage")
class CwtInlineDocumentationProvider : InlineDocumentationProvider {
    override fun inlineDocumentationItems(file: PsiFile?): Collection<InlineDocumentation> {
        if (file == null) return emptyList()
        val result = mutableListOf<InlineDocumentation>()
        file.accept(object : PsiRecursiveElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (CwtPsiUtil.canAttachComment(element)) {
                    val ownedComments = PlsPsiManager.getOwnedComments(element) { it is CwtDocComment }
                        .castOrNull<List<CwtDocComment>>().orEmpty()
                    val comments = ownedComments.optimized() // optimized to optimize memory
                    if (comments.isNotEmpty()) {
                        val inlineDocumentation = CwtInlineDocumentation(comments)
                        result.add(inlineDocumentation)
                    }
                }
                if (CwtPsiUtil.isMemberContextElement(element)) super.visitElement(element)
            }
        })
        return result.optimized() // optimized to optimize memory
    }

    override fun findInlineDocumentation(file: PsiFile, textRange: TextRange): InlineDocumentation? {
        fun findCommentAt(offset: Int): CwtDocComment? {
            if (file.textLength == 0) return null
            if (offset < 0) return null
            val safeOffset = offset.coerceIn(0, file.textLength - 1)
            val leaf = file.findElementAt(safeOffset) ?: return null
            return leaf.parentOfType<CwtDocComment>(withSelf = true)
        }

        val start = textRange.startOffset
        val endExclusive = textRange.endOffset

        // Probe around the given range to locate a doc comment quickly
        val comment = findCommentAt(start)
            ?: findCommentAt(endExclusive - 1)
            ?: findCommentAt(start - 1)
            ?: return null

        // Collect contiguous CWT doc comments around the hit (ignore blank-line breaks)
        val siblingComments = PlsPsiManager.findSiblingComments(comment) { it is CwtDocComment }
        if (siblingComments.isEmpty()) return null
        val comments = siblingComments.filterIsInstance<CwtDocComment>().optimized() // optimized to optimize memory
        if (comments.isEmpty()) return null
        // Validate range matches exactly the target range
        val blockRange = TextRange(comments.first().textRange.startOffset, comments.last().textRange.endOffset)
        if (blockRange != textRange) return null

        // Only return inline documentation if these comments actually attach to an owner element
        val owner = comments.last().owner
        if (owner == null || !CwtPsiUtil.canAttachComment(owner)) return null

        return CwtInlineDocumentation(comments)
    }
}
