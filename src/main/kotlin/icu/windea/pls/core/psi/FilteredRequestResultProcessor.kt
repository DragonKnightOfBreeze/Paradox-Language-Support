package icu.windea.pls.core.psi

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.ReferenceRange
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.util.Processor

/**
 * 基于过滤的引用查找处理器。
 *
 * 包装 [RequestResultProcessor]，在 [processTextOccurrence] 内对元素与引用进行二级过滤：
 * - 通过 [applyFor] 决定是否对某 [element] 启用过滤；
 * - 当启用时，分别调用 [acceptElement] 与 [acceptReference] 进行判断；
 * - 仅当引用区间包含偏移且 `ref.isReferenceTo(target)` 成立时才传递给 [consumer]。
 *
 * @property target 目标 PSI 元素。
 */
abstract class FilteredRequestResultProcessor(private val target: PsiElement) : RequestResultProcessor(target) {
    override fun processTextOccurrence(element: PsiElement, offsetInElement: Int, consumer: Processor<in PsiReference>): Boolean {
        val apply = applyFor(element)
        if (apply && !acceptElement(element)) return true
        if (!target.isValid) return false
        val references = PsiReferenceService.getService().getReferences(element, PsiReferenceService.Hints(target, offsetInElement))
        for (i in references.indices) {
            val ref = references[i]
            if (apply && !acceptReference(ref)) continue
            ProgressManager.checkCanceled()
            if (ReferenceRange.containsOffsetInElement(ref, offsetInElement) && ref.isReferenceTo(target) && !consumer.process(ref)) {
                return false
            }
        }
        return true
    }

    /** 是否对指定 [element] 启用过滤，默认返回 `true`。*/
    protected open fun applyFor(element: PsiElement): Boolean = true

    /** 当启用过滤时，是否接受该 PSI 元素 [element]，默认返回 `true`。*/
    protected open fun acceptElement(element: PsiElement): Boolean = true

    /** 当启用过滤时，是否接受该 PSI 引用 [reference]，默认返回 `true`。*/
    protected open fun acceptReference(reference: PsiReference): Boolean = true
}
