package icu.windea.pls.core.psi

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.core.collections.mapToArray

/**
 * 已解析的（多目标）引用。
 *
 * 用于在构造时即给定解析结果列表 [resolved]，从而在 [multiResolve] 中直接返回。
 *
 * @property resolved 解析得到的 PSI 目标列表。
 */
open class PsiResolvedPolyVariantReference<T : PsiElement>(
    element: PsiElement,
    rangeInElement: TextRange,
    val resolved: List<T>
) : PsiPolyVariantReferenceBase<PsiElement>(element, rangeInElement) {
    /** 不支持重命名。*/
    override fun handleElementRename(newElementName: String): PsiElement {
        throw IncorrectOperationException()
    }

    /** 返回构造时提供的 [resolved] 目标集合。*/
    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return resolved.mapToArray { PsiElementResolveResult(it) }
    }
}
