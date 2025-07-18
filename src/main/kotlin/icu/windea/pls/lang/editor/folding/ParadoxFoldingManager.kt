package icu.windea.pls.lang.editor.folding

import com.intellij.lang.*
import com.intellij.lang.folding.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.lang.util.*

object ParadoxFoldingManager {
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
        val descriptor = FoldingDescriptor(node, TextRange(startOffset, endOffset), null, "# ...")
        descriptors.add(descriptor)
    }
}
