package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.createResults
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.values.singletonSet
import icu.windea.pls.core.util.values.to
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

class ParadoxScriptValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val config: CwtConfig<*>
) : ParadoxComplexExpressionNodeBase(), ParadoxIdentifierNode {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return config.to.singletonSet()
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        if (text.isEmpty()) return null
        return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (element !is ParadoxScriptStringExpressionElement) return null // unexpected
        if (nodes.isNotEmpty()) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val reference = getReference(element)
        if (reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrorBuilder.unresolvedScriptValue(rangeInExpression, text)
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (element !is ParadoxScriptStringExpressionElement) return null // unexpected
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, config)
    }

    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        private val config: CwtConfig<*>
    ) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), ParadoxIdentifierNode.Reference {
        private val project get() = config.configGroup.project

        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
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
            val resolved = ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, config, config.configExpression)
            return resolved
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val resolved = ParadoxExpressionManager.multiResolveScriptExpression(element, rangeInElement, config, config.configExpression)
            return resolved.createResults()
        }

        override fun canResolveFor(constraint: ParadoxResolveConstraint): Boolean {
            return when (constraint) {
                ParadoxResolveConstraint.Definition -> true // <script_value>
                else -> false
            }
        }
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueNode {
            return ParadoxScriptValueNode(text, textRange, configGroup, config)
        }
    }

    companion object : Resolver()
}
