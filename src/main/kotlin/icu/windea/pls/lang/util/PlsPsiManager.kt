package icu.windea.pls.lang.util

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.openapi.util.text.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*

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

    fun findAcceptableElementInStructureView(element: PsiElement?, canAttachComment: Boolean = false, predicate: (PsiElement) -> Boolean): Any? {
        var current = element
        while (current != null && current !is PsiFile) {
            if (predicate(current)) return current
            if (canAttachComment && current is PsiComment) {
                val attachingElement = getAttachingElement(current)
                if (attachingElement != null && predicate(attachingElement)) return attachingElement
                return null
            }
            current = current.parent
        }
        return null
    }

    inline fun findTextStartOffsetIncludeComment(element: PsiElement, findUpPredicate: (PsiElement) -> Boolean = { true }): Int {
        //找到直到没有空行为止的最后一个注释，返回它的开始位移，或者输入元素的开始位移
        val target: PsiElement = if (element.prevSibling == null && findUpPredicate(element)) element.parent else element
        var current: PsiElement? = target
        var comment: PsiComment? = null
        while (current != null) {
            current = current.prevSibling ?: break
            when {
                current is PsiWhiteSpace && current.isSpaceOrSingleLineBreak() -> continue
                current is PsiComment -> comment = current
                else -> break
            }
        }
        if (comment != null) return comment.startOffset
        return target.startOffset
    }

    /**
     * 查找最远的相同类型的兄弟节点。可指定是否向后查找，以及是否在空行处中断。
     */
    fun findFurthestSiblingOfSameType(element: PsiElement, findAfter: Boolean, stopOnBlankLine: Boolean = true): PsiElement {
        var node = element.node
        val expectedType = node.elementType
        var lastSeen = node
        while (node != null) {
            val elementType = node.elementType
            when {
                elementType == expectedType -> lastSeen = node
                elementType == TokenType.WHITE_SPACE -> {
                    if (stopOnBlankLine && node.text.containsBlankLine()) break
                }
                else -> break
            }
            node = if (findAfter) node.treeNext else node.treePrev
        }
        return lastSeen.psi
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

    fun addCommentFoldingDescriptor(node: ASTNode, document: Document, descriptors: MutableList<FoldingDescriptor>) {
        //1. 并不会考虑注释前缀长度不同的情况（例如，不考虑以###开头的用作分区的注释）
        //2. 占位文本始终是 注释前缀+省略号

        val element = node.psi
        if (element !is PsiComment) return
        val startElement = findFurthestSiblingOfSameType(element, findAfter = false)
        val endElement = findFurthestSiblingOfSameType(element, findAfter = true)
        if (startElement == endElement) return //受支持的注释都是单行注释，因此这里可以快速判断
        val startOffset = startElement.startOffset
        val endOffset = endElement.endOffset
        if (document.getLineNumber(startOffset) == document.getLineNumber(endOffset)) return
        val descriptor = FoldingDescriptor(node, TextRange(startOffset, endOffset))
        descriptors.add(descriptor)
    }
}
