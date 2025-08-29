package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import com.intellij.util.IncorrectOperationException
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.lang.expression.complex.ParadoxComplexExpressionError
import icu.windea.pls.lang.expression.complex.ParadoxDefineReferenceExpression
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.search.ParadoxDefineSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.define
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxDefineManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxDefineVariableNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val expression: ParadoxDefineReferenceExpression
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.DEFINE_VARIABLE_KEY
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val reference = getReference(element)
        if (reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionError.Builder.unresolvedDefineVariable(rangeInExpression, text)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, this)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        node: ParadoxDefineVariableNode
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        val expression = node.expression
        val project = expression.configGroup.project
        val namespace = expression.namespaceNode?.text
        val variableName = node.text

        override fun handleElementRename(newElementName: String): PsiElement {
            throw IncorrectOperationException()
        }

        //缓存解析结果以优化性能

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
            if(namespace == null) return null
            val selector = selector(project, element).define().contextSensitive()
            val defineInfo = ParadoxDefineSearch.search(namespace, variableName, selector).find() ?: return null
            return ParadoxDefineManager.getDefineElement(defineInfo, project)
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            if(namespace == null) return ResolveResult.EMPTY_ARRAY
            val selector = selector(project, element).define().contextSensitive()
            val defineInfos = ParadoxDefineSearch.search(namespace, variableName, selector).findAll()
            return ParadoxDefineManager.getDefineElements(defineInfos, project).mapToArray { PsiElementResolveResult(it) }
        }
    }
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, expression: ParadoxDefineReferenceExpression): ParadoxDefineVariableNode {
            return ParadoxDefineVariableNode(text, textRange, configGroup, expression)
        }
    }
}
