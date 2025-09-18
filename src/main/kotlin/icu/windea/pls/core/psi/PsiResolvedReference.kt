package icu.windea.pls.core.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException

/**
 * 已解析的（单目标）引用。
 *
 * 用于在构造时即给定解析结果 [resolved]，从而在 [resolve] 中直接返回。
 *
 * @property resolved 解析得到的 PSI 目标（可为空）。
 */
open class PsiResolvedReference<T : PsiElement>(
    element: PsiElement,
    rangeInElement: TextRange,
    val resolved: T?
) : PsiReferenceBase<PsiElement>(element, rangeInElement) {
    /** 不支持重命名。*/
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException()
    }

    /** 返回构造时提供的 [resolved]。*/
    override fun resolve() = resolved
}
