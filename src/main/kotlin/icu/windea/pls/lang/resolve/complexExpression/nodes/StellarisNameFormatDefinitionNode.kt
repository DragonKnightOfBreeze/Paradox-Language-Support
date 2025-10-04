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
import icu.windea.pls.lang.resolve.complexExpression.StellarisNameFormatExpression
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.contextSensitive
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.psi.ParadoxPsiManager
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

/**
 * [StellarisNameFormatExpression] 中的定义节点。即 `{<x>}` 中的 `x`。
 *
 * 其中 `x` 的定义类型由表达式所属规则的 `formatName` 推导为 `${format}_name_parts_list`。
 */
class StellarisNameFormatDefinitionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val definitionType: String?,
) : ParadoxComplexExpressionNodeBase(), ParadoxIdentifierNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (text.isEmpty()) return null
        if (definitionType.isNullOrEmpty()) return null
        val reference = getReference(element)
        if (reference == null || reference.resolve() != null) return null
        return ParadoxComplexExpressionErrorBuilder.unresolvedStellarisNamePartsList(rangeInExpression, text, definitionType)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (text.isEmpty()) return null
        val typeToSearch = definitionType ?: return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, this, typeToSearch)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        private val node: StellarisNameFormatDefinitionNode,
        private val typeToSearch: String,
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement), ParadoxIdentifierNode.Reference {
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
            val selector = selector(project, element).definition().contextSensitive()
            return ParadoxDefinitionSearch.search(name, typeToSearch, selector).find()
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val selector = selector(project, element).definition().contextSensitive()
            return ParadoxDefinitionSearch.search(name, typeToSearch, selector).findAll().mapToArray { PsiElementResolveResult(it) }
        }

        override fun canResolveFor(constraint: ParadoxResolveConstraint): Boolean {
            return when (constraint) {
                ParadoxResolveConstraint.Definition -> true
                else -> false
            }
        }
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, definitionType: String?): StellarisNameFormatDefinitionNode {
            return StellarisNameFormatDefinitionNode(text, textRange, configGroup, definitionType)
        }
    }

    companion object : Resolver()
}
