package icu.windea.pls.core.psi

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import com.intellij.psi.PsiReferenceService
import com.intellij.psi.ReferenceRange
import com.intellij.psi.search.RequestResultProcessor
import com.intellij.util.Processor

/**
 * 可按元素与引用过滤的 `RequestResultProcessor` 基类。
 *
 * 在 `processTextOccurrence` 中先调用钩子方法决定是否应用过滤，
 * 再按需过滤元素/引用并投递给下游消费者。
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

    /** 是否对当前元素应用过滤逻辑。返回 `false` 则不过滤元素与引用。 */
    protected open fun applyFor(element: PsiElement): Boolean = true

    /** 当应用过滤时，是否接受该元素。返回 `false` 则跳过后续引用检查。 */
    protected open fun acceptElement(element: PsiElement): Boolean = true

    /** 当应用过滤时，是否接受该引用。返回 `false` 则丢弃该引用。 */
    protected open fun acceptReference(reference: PsiReference): Boolean = true
}
