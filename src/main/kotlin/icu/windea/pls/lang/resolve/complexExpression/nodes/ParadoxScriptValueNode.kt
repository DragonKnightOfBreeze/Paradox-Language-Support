package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementResolveResult
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.core.unquote
import icu.windea.pls.core.util.set
import icu.windea.pls.core.util.singleton
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpressionErrorBuilder
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
        return config.singleton.set()
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
        return Reference(element, rangeInElement, text, config, configGroup)
    }

    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val name: String,
        val config: CwtConfig<*>,
        val configGroup: CwtConfigGroup
    ) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), ParadoxIdentifierNode.Reference {
        val project = configGroup.project

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
            return ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, config, config.configExpression)
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            return ParadoxExpressionManager.multiResolveScriptExpression(element, rangeInElement, config, config.configExpression)
                .mapToArray { PsiElementResolveResult(it) }
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
