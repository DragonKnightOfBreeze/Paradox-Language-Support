package icu.windea.pls.lang.references.csv

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.csv.psi.*
import icu.windea.pls.lang.util.*

class ParadoxCsvColumnExpressionPsiReference(
    element: ParadoxCsvColumn,
    rangeInElement: TextRange
) : PsiPolyVariantReferenceBase<ParadoxCsvColumn>(element, rangeInElement) {
    val project by lazy { element.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return ParadoxPsiManager.handleElementRename(element, rangeInElement, newElementName)
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxCsvColumnExpressionPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxCsvColumnExpressionPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxCsvColumnExpressionPsiReference> {
        override fun resolve(ref: ParadoxCsvColumnExpressionPsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    private fun doResolve(): PsiElement? {
        //根据对应的expression进行解析
        return ParadoxExpressionManager.resolveCsvExpression(element, rangeInElement)
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        //根据对应的expression进行解析
        return ParadoxExpressionManager.multiResolveCsvExpression(element, rangeInElement)
            .mapToArray { PsiElementResolveResult(it) }
    }
}
