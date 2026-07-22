package icu.windea.pls.lang.references.csv

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.expandConfigExpression
import icu.windea.pls.core.createResults
import icu.windea.pls.csv.psi.ParadoxCsvColumn
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.csv.psi.ParadoxCsvPsiService
import icu.windea.pls.lang.psi.ParadoxPsiService
import icu.windea.pls.lang.references.ParadoxConstrainedPsiReference
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint

class ParadoxCsvExpressionPsiReference(
    element: ParadoxCsvExpressionElement,
    rangeInElement: TextRange,
    val columnConfig: CwtPropertyConfig
) : PsiPolyVariantReferenceBase<ParadoxCsvExpressionElement>(element, rangeInElement), ParadoxConstrainedPsiReference {
    private val configGroup get() = columnConfig.configGroup
    private val project get() = configGroup.project

    override fun handleElementRename(newElementName: String): PsiElement {
        return ParadoxPsiService.handleExpressionElementRename(element, rangeInElement, newElementName, resolve())
    }

    // 缓存解析结果以优化性能

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
        if (element is ParadoxCsvColumn && ParadoxCsvPsiService.isHeaderColumn(element)) {
            return columnConfig.pointer.element
        }
        // 根据对应的 expression 进行解析
        val config = columnConfig.valueConfig ?: return null
        val resolved = ParadoxExpressionManager.resolveCsvExpression(element, rangeInElement, config)
        return resolved
    }

    private fun doMultiResolve(): Array<out ResolveResult> {
        val element = element
        if (element is ParadoxCsvColumn && ParadoxCsvPsiService.isHeaderColumn(element)) {
            val resolved = columnConfig.pointer.element
            return resolved.createResults()
        }
        // 根据对应的 expression 进行解析
        val config = columnConfig.valueConfig ?: return ResolveResult.EMPTY_ARRAY
        val resolved = ParadoxExpressionManager.resolveAllCsvExpression(element, rangeInElement, config)
        return resolved.createResults()
    }

    override fun canResolveFor(constraint: ParadoxReferenceConstraint): Boolean {
        // NOTE 3.0.1 expand config expression first since it's necessary for unions and aliases
        val config = columnConfig.valueConfig ?: return false
        return config.expandConfigExpression().any { constraint.test(it.type) }
    }
}
