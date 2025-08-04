package icu.windea.pls.lang.util

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*

object PlsPsiManager {
    inline fun findAcceptableElementIncludeComment(element: PsiElement?, predicate: (PsiElement) -> Boolean): Any? {
        var current: PsiElement? = element ?: return null
        while (current != null && current !is PsiFile) {
            if (predicate(current)) return current
            if (current is PsiComment) return current.siblings().find { predicate(it) }
                ?.takeIf { it.prevSibling.isSpaceOrSingleLineBreak() }
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
        val startElement = PlsPsiManager.findFurthestSiblingOfSameType(element, findAfter = false)
        val endElement = PlsPsiManager.findFurthestSiblingOfSameType(element, findAfter = true)
        if (startElement == endElement) return //受支持的注释都是单行注释，因此这里可以快速判断
        val startOffset = startElement.startOffset
        val endOffset = endElement.endOffset
        if (document.getLineNumber(startOffset) == document.getLineNumber(endOffset)) return
        val descriptor = FoldingDescriptor(node, TextRange(startOffset, endOffset))
        descriptors.add(descriptor)
    }
}
