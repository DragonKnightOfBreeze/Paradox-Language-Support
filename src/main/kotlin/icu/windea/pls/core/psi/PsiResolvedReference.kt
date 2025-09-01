package icu.windea.pls.core.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException

/**
 * 已解析的单目标 `PsiReference` 基类。
 *
 * 通过构造参数传入解析结果 [resolved]，`resolve()` 将直接返回该结果。
 */
open class PsiResolvedReference<T : PsiElement>(
    element: PsiElement,
    rangeInElement: TextRange,
    val resolved: T?
) : PsiReferenceBase<PsiElement>(element, rangeInElement) {
    /** 不支持重命名操作。 */
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException()
    }

    /** 直接返回传入的解析结果。 */
    override fun resolve() = resolved
}
