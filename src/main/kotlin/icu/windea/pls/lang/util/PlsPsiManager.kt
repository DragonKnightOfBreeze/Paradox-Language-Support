package icu.windea.pls.lang.util

import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import com.intellij.psi.TokenType
import com.intellij.psi.impl.source.tree.LeafPsiElement
import com.intellij.psi.util.elementType
import com.intellij.psi.util.endOffset
import com.intellij.psi.util.siblings
import com.intellij.psi.util.startOffset

object PlsPsiManager {
    fun containsBlankLine(element: PsiWhiteSpace): Boolean {
        return StringUtil.getLineBreakCount(element.text) > 1
    }

    /**
     * 得到附加到 [element] 上的所有注释的列表，顺序从后到前。
     *
     * 向前遍历，仅采用注释以及不包含空白行的空白，然后返回其中的所有注释。
     */
    fun getAttachedComments(element: PsiElement): List<PsiComment> {
        if (element is PsiComment || element is PsiWhiteSpace) return emptyList()
        return element.siblings(forward = false, withSelf = false)
            .takeWhile { it is PsiComment || (it is PsiWhiteSpace && !containsBlankLine(it)) }
            .filterIsInstance<PsiComment>()
            .toList()
    }

    /**
     * 得到 [comment] 附加到的元素。
     *
     * 向后遍历，仅采用注释、不包含空白行的空白以及第一个非注释非空白的元素，然后然后此元素。
     */
    fun getAttachingElement(comment: PsiComment): PsiElement? {
        return comment.siblings(forward = true, withSelf = false)
            .dropWhile { it is PsiComment || (it is PsiWhiteSpace && !containsBlankLine(it)) }
            .firstOrNull()
            ?.takeIf { it !is PsiComment && it !is PsiWhiteSpace }
    }

    /**
     * 适用于结构视图。
     */
    fun findAcceptableElementInStructureView(element: PsiElement?, canAttachComments: Boolean = false, predicate: (PsiElement) -> Boolean): Any? {
        var current = element
        while (current != null && current !is PsiFile) {
            if (predicate(current)) return current
            if (canAttachComments && current is PsiComment) {
                val attachingElement = getAttachingElement(current)
                if (attachingElement != null && predicate(attachingElement)) return attachingElement
                return null
            }
            current = current.parent
        }
        return null
    }

    /**
     * 适用于各种包含编辑器文本片段的视图（如，快速定义视图）。
     */
    fun findTextStartOffsetInView(element: PsiElement, canAttachComment: Boolean = false): Int {
        if (canAttachComment) {
            val attachingComments = getAttachedComments(element)
            if (attachingComments.isNotEmpty()) return attachingComments.last().startOffset
        }
        return element.startOffset
    }

    /**
     * 适用于各种包含编辑器文本片段的视图（如，快速定义视图）。
     */
    fun findTextEndOffsetInView(element: PsiElement): Int {
        return element.endOffset
    }

    fun getReferenceElement(originalElement: PsiElement?): PsiElement? {
        val element = when {
            originalElement == null -> return null
            originalElement.elementType == TokenType.WHITE_SPACE -> originalElement.prevSibling ?: return null
            else -> originalElement
        }
        return when {
            element is LeafPsiElement -> element.parent
            else -> element
        }
    }

    fun findSiblingLineComments(element: PsiComment, predicate: (PsiComment) -> Boolean): List<PsiComment> {
        if (!predicate(element)) return emptyList()
        val before = element.siblings(forward = false, withSelf = false)
            .takeWhile { (it is PsiComment && predicate(it)) || (it is PsiWhiteSpace && !containsBlankLine(it)) }
            .filterIsInstance<PsiComment>()
            .toList()
        val after = element.siblings(forward = true, withSelf = false)
            .takeWhile { (it is PsiComment && predicate(it)) || (it is PsiWhiteSpace && !containsBlankLine(it)) }
            .filterIsInstance<PsiComment>()
            .toList()
        if (before.isEmpty() && after.isEmpty()) return emptyList()
        val result = mutableListOf<PsiComment>()
        result.addAll(before.reversed())
        result.add(element)
        result.addAll(after)
        return result
    }

    fun findAllSiblingLineCommentsIn(parentElement: PsiElement, predicate: (PsiComment) -> Boolean): List<List<PsiComment>> {
        var current = parentElement.firstChild
        val result = mutableListOf<List<PsiComment>>()
        while (current != null) {
            if (current is PsiComment) {
                val comments = findSiblingLineComments(current, predicate)
                if (comments.isNotEmpty()) {
                    result.add(comments)
                    current = comments.last()
                }
            }
            current = current.nextSibling
        }
        return result
    }
}
