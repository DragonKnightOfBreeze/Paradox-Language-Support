package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.createResults
import icu.windea.pls.lang.editor.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxPsiService
import icu.windea.pls.lang.resolve.complexExpression.ParadoxNameFormatExpression
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrors
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.search.util.preferLocale
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

/**
 * 命名格式表达式（[ParadoxNameFormatExpression]）中的本地化节点。即 `{x}` 中的 `x`。
 */
class ParadoxNameFormatLocalisationNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
) : ParadoxComplexExpressionNodeBase(), ParadoxIdentifierNode, ParadoxDynamicDataNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxSemanticHighlighterColors.localisationReference(element.language)
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (text.isEmpty()) return null
        val reference = getReference(element)
        if (reference == null || reference.resolve() != null) return null
        return ParadoxComplexExpressionErrors.unresolvedNameFormatLocalisation(rangeInExpression, text)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (text.isEmpty()) return null
        val offset = ParadoxExpressionManager.getExpressionOffset(element)
        return Reference(element, rangeInExpression.shiftRight(offset), this)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        private val node: ParadoxNameFormatLocalisationNode,
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement), ParadoxIdentifierNode.Reference {
        private val name get() = node.text
        private val project get() = node.configGroup.project

        override fun handleElementRename(newElementName: String): PsiElement {
            return ParadoxPsiService.handleExpressionElementRename(element, rangeInElement, newElementName, resolve())
        }

        private object Resolver : ResolveCache.AbstractResolver<Reference, PsiElement> {
            override fun resolve(ref: Reference, incompleteCode: Boolean) = ref.doResolve()
        }

        private object MultiResolver : ResolveCache.PolyVariantResolver<Reference> {
            override fun resolve(ref: Reference, incompleteCode: Boolean) = ref.doMultiResolve()
        }

        override fun resolve(): PsiElement? {
            return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
        }

        override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
            return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
        }

        private fun doResolve(): PsiElement? {
            val preferredLocale = selectLocale(element) ?: ParadoxLocaleManager.getPreferredLocaleConfig()
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(preferredLocale)
            val resolved = ParadoxLocalisationSearch.searchNormal(name, selector).find()
            return resolved
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val preferredLocale = selectLocale(element) ?: ParadoxLocaleManager.getPreferredLocaleConfig()
            val selector = ParadoxLocalisationSearch.selector(project, element).contextSensitive().preferLocale(preferredLocale)
            val resolved = ParadoxLocalisationSearch.searchNormal(name, selector).findAll()
            return resolved.createResults()
        }

        override fun canResolveFor(constraint: ParadoxResolveConstraint): Boolean {
            return when (constraint) {
                ParadoxResolveConstraint.Localisation -> true
                else -> false
            }
        }
    }

    companion object {
        @JvmStatic
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxNameFormatLocalisationNode {
            return ParadoxNameFormatLocalisationNode(text, textRange, configGroup)
        }
    }
}
