package icu.windea.pls.lang.references.csv

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.isHeaderColumn
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxPsiManager

class ParadoxCsvExpressionPsiReference(
    element: ParadoxCsvExpressionElement,
    rangeInElement: TextRange,
    val columnConfig: CwtPropertyConfig
) : PsiPolyVariantReferenceBase<ParadoxCsvExpressionElement>(element, rangeInElement) {
    val project by lazy { columnConfig.configGroup.project }

    override fun handleElementRename(newElementName: String): PsiElement {
        return ParadoxPsiManager.handleElementRename(element, rangeInElement, newElementName)
    }

    //缓存解析结果以优化性能

    private object Resolver : ResolveCache.AbstractResolver<ParadoxCsvExpressionPsiReference, PsiElement> {
        override fun resolve(ref: ParadoxCsvExpressionPsiReference, incompleteCode: Boolean) = ref.doResolve()
    }

    private object MultiResolver : ResolveCache.PolyVariantResolver<ParadoxCsvExpressionPsiReference> {
        override fun resolve(ref: ParadoxCsvExpressionPsiReference, incompleteCode: Boolean) = ref.doMultiResolve()
    }

    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }

    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }

    private fun doResolve(): PsiElement? {
        val element = element

        if (element is ParadoxCsvColumn && element.isHeaderColumn()) {
            return columnConfig.pointer.element
        }

        //根据对应的expression进行解析
        val config = columnConfig.valueConfig ?: return null
        return ParadoxExpressionManager.resolveCsvExpression(element, rangeInElement, config)
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        val element = element

        if (element is ParadoxCsvColumn && element.isHeaderColumn()) {
            return columnConfig.pointer.element?.let { arrayOf(PsiElementResolveResult(it)) } ?: ResolveResult.EMPTY_ARRAY
        }

        //根据对应的expression进行解析
        val config = columnConfig.valueConfig ?: return ResolveResult.EMPTY_ARRAY
        return ParadoxExpressionManager.multiResolveCsvExpression(element, rangeInElement, config)
            .mapToArray { PsiElementResolveResult(it) }
    }
}
