package icu.windea.pls.lang.references

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.config.config.CwtConfig

/**
 * 基于规则的单目标的 PSI 引用。不支持重命名。
 *
 * @see CwtConfig
 */
open class CwtConfigBasedPsiReference<T : PsiElement>(
    element: PsiElement,
    rangeInElement: TextRange,
    open val config: CwtConfig<T>?
) : PsiReferenceBase<PsiElement>(element, rangeInElement) {
    override fun handleElementRename(newElementName: String): PsiElement? {
        throw IncorrectOperationException()
    }

    override fun resolve(): T? {
        return config?.pointer?.element
    }
}
