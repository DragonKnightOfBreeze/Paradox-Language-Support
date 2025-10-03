package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.search.ParadoxLocalisationSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.localisation
import icu.windea.pls.lang.search.selector.preferLocale
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.selectLocale
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxLocaleManager
import icu.windea.pls.lang.util.psi.ParadoxPsiManager
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

/**
 * Stellaris 命名格式中的本地化名节点：`x`。
 */
class StellarisNameFormatLocalisationNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
) : ParadoxComplexExpressionNodeBase() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return when (element.language) {
            is icu.windea.pls.localisation.ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.LOCALISATION_REFERENCE_KEY
            else -> ParadoxScriptAttributesKeys.LOCALISATION_REFERENCE_KEY
        }
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (text.isEmpty()) return null
        val reference = getReference(element)
        if (reference == null || reference.resolve() != null) return null
        return ParadoxComplexExpressionErrorBuilder.unresolvedDataSource(rangeInExpression, text, "localisation")
    }

    override fun getReference(element: ParadoxExpressionElement): PsiPolyVariantReferenceBase<ParadoxExpressionElement>? {
        if (text.isEmpty()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, this)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        private val node: StellarisNameFormatLocalisationNode,
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        private val name get() = node.text
        private val project get() = node.configGroup.project

        override fun handleElementRename(newElementName: String): PsiElement {
            return ParadoxPsiManager.handleElementRename(element, rangeInElement, newElementName)
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
            val selector = selector(project, element).localisation().contextSensitive().preferLocale(preferredLocale)
            return ParadoxLocalisationSearch.search(name, selector).find()
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val preferredLocale = selectLocale(element) ?: ParadoxLocaleManager.getPreferredLocaleConfig()
            val selector = selector(project, element).localisation().contextSensitive().preferLocale(preferredLocale)
            return ParadoxLocalisationSearch.search(name, selector).findAll().mapToArray { PsiElementResolveResult(it) }
        }
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): StellarisNameFormatLocalisationNode {
            return StellarisNameFormatLocalisationNode(text, textRange, configGroup)
        }
    }
    companion object : Resolver()
}
