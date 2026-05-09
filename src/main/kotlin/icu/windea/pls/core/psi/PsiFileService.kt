package icu.windea.pls.core.psi

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiReference
import com.intellij.psi.util.isAncestor
import icu.windea.pls.core.processChild

object PsiFileService {
    /**
     * 查找位于指定偏移处的 PSI 元素，并尝试对其进行转换。
     *
     * @param forward 查找指定偏移之前还是之后的 PSI 元素。默认为 null，表示同时考虑。
     */
    fun <T : PsiElement> findElementAt(file: PsiFile, offset: Int, forward: Boolean? = null, transform: (element: PsiElement) -> T?): T? {
        if (offset < 0) return null
        var current: PsiElement? = null
        if (forward != false) {
            val element = file.findElementAt(offset)
            if (element != null) {
                current = element
                val result = transform(element)
                if (result != null) {
                    return result
                }
            }
        }
        if (forward != true && offset > 0) {
            val leftElement = file.findElementAt(offset - 1)
            if (leftElement != null && leftElement !== current) {
                val leftResult = transform(leftElement)
                if (leftResult != null) {
                    return leftResult
                }
            }
        }
        return null
    }

    /**
     * 查找位于指定的开始偏移和结束偏移之间的 PSI 元素。
     *
     * 通过对开始偏移处和结束偏移处的 PSI 元素进行转换以得到各自的根元素，得到共同的祖先元素并作为最终的根元素，
     * 再从根元素的子元素中遍历所有位于开始偏移和结束偏移之间（涉及即可）的 PSI 元素。
     */
    fun findElementsBetween(file: PsiFile, startOffset: Int, endOffset: Int, rootTransform: (rootElement: PsiElement) -> PsiElement?): Sequence<PsiElement> {
        if (startOffset < 0 || endOffset < 0) return emptySequence()
        if (startOffset >= endOffset) return emptySequence()
        val startRootElement = findElementAt(file, startOffset, true, rootTransform) ?: return emptySequence()
        val endRootElement = findElementAt(file, endOffset, true, rootTransform) ?: return emptySequence()
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

    /**
     * @param forward 查找偏移之前还是之后的 PSI 引用。默认为 `null`，表示同时考虑。
     */
    fun findReferenceAt(file: PsiFile, offset: Int, forward: Boolean? = null, predicate: (reference: PsiReference) -> Boolean): PsiReference? {
        if (offset < 0) return null
        if (forward != false) {
            val reference = file.findReferenceAt(offset)
            if (reference != null && predicate(reference)) {
                return reference
            }
        }
        if (forward != true && offset > 0) {
            val reference = file.findReferenceAt(offset - 1)
            if (reference != null && predicate(reference)) {
                return reference
            }
        }
        return null
    }
}
