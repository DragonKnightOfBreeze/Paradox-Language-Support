package icu.windea.pls.lang.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.isAncestor
import icu.windea.pls.core.findElementAt
import icu.windea.pls.core.processChild

object PlsPsiFileManager {
    /**
     * 查找位于指定的开始偏移和结束偏移之间的 PSI 元素。
     *
     * 通过对开始偏移处和结束偏移处的 PSI 元素进行转换以得到各自的根元素，得到共同的祖先元素并作为最终的根元素，
     * 再从根元素的子元素中遍历所有位于开始偏移和结束偏移之间（涉及即可）的 PSI 元素。
     */
    fun findElementsBetween(file: PsiFile, startOffset: Int, endOffset: Int, rootTransform: (rootElement: PsiElement) -> PsiElement?): Sequence<PsiElement> {
        if (startOffset < 0 || endOffset < 0) return emptySequence()
        if (startOffset >= endOffset) return emptySequence()
        val startRootElement = file.findElementAt(startOffset, true, rootTransform) ?: return emptySequence()
        val endRootElement = file.findElementAt(endOffset, true, rootTransform) ?: return emptySequence()
        val root = if (startRootElement.isAncestor(endRootElement)) startRootElement else endRootElement
        return sequence {
            root.processChild { element ->
                val textRange = element.textRange
                if (textRange.endOffset > startOffset && textRange.startOffset < endOffset) {
                    val isLast = textRange.endOffset >= endOffset
                    yield(element)
                    !isLast
                } else {
                    true
                }
            }
        }
    }
}
