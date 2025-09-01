package icu.windea.pls.core.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.collections.mapToArray

/**
 * 已解析的多目标 `PsiReference` 基类。
 *
 * 通过构造参数传入解析结果列表 [resolved]，`multiResolve()` 将据此返回结果数组。
 */
open class PsiResolvedPolyVariantReference<T : PsiElement>(
    element: PsiElement,
    rangeInElement: TextRange,
    val resolved: List<T>
) : PsiPolyVariantReferenceBase<PsiElement>(element, rangeInElement) {
    /** 不支持重命名操作。 */
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException()
    }

    /** 根据传入的解析结果构造并返回 `ResolveResult` 数组。 */
    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return resolved.mapToArray { PsiElementResolveResult(it) }
    }
}
