package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.createResults
import icu.windea.pls.core.orNull
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.core.util.values.singletonSetOrEmpty
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.editor.ParadoxSemanticHighlighterColors
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrors
import icu.windea.pls.lang.search.ParadoxDefineNamespaceSearch
import icu.windea.pls.lang.search.util.contextSensitive
import icu.windea.pls.lang.util.ParadoxExpressionManager

class ParadoxDefineNamespaceNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
) : ParadoxComplexExpressionNodeBase(), ParadoxIdentifierNode, ParadoxDynamicDataNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        val namespace = text.orNull() ?: return emptySet()
        return configGroup.defineNamespaces.get(namespace).to.singletonSetOrEmpty()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxSemanticHighlighterColors.defineNamespace()
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val reference = getReference(element)
        if (reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrors.unresolvedDefineNamespace(rangeInExpression, text)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val offset = ParadoxExpressionManager.getExpressionOffset(element)
        return Reference(element, rangeInExpression.shiftRight(offset), this)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        private val node: ParadoxDefineNamespaceNode
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement), ParadoxIdentifierNode.Reference {
        private val project get() = node.configGroup.project
        private val namespace get() = node.text

        override fun handleElementRename(newElementName: String): PsiElement {
            throw IncorrectOperationException()
        }

        // 缓存解析结果以优化性能

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
            val selector = ParadoxDefineNamespaceSearch.selector(project, element).contextSensitive()
            val resolved = ParadoxDefineNamespaceSearch.search(namespace, selector).find()
            return resolved
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val selector = ParadoxDefineNamespaceSearch.selector(project, element).contextSensitive()
            val resolved = ParadoxDefineNamespaceSearch.search(namespace, selector).findAll()
            return resolved.createResults()
        }
    }

    companion object {
        @JvmStatic
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDefineNamespaceNode {
            return ParadoxDefineNamespaceNode(text, textRange, configGroup)
        }
    }
}
