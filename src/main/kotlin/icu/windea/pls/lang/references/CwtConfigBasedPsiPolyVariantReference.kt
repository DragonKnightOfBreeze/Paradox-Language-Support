package icu.windea.pls.lang.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.core.createResults

/**
 * 基于规则的多目标的 PSI 引用。不支持重命名。
 *
 * @see icu.windea.pls.config.config.CwtConfig
 */
open class CwtConfigBasedPsiPolyVariantReference<T : PsiElement>(
    element: PsiElement,
    rangeInElement: TextRange,
    open val configs: Collection<CwtConfig<T>>
) : PsiPolyVariantReferenceBase<PsiElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement? {
        throw IncorrectOperationException()
    }

    override fun resolve(): PsiElement? {
        return configs.singleOrNull()?.pointer?.element
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        if (configs.isEmpty()) return ResolveResult.EMPTY_ARRAY
        return configs.mapNotNull { it.pointer.element }.createResults()
    }
}
