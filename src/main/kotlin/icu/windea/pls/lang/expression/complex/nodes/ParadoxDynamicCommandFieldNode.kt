package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.editor.*
import icu.windea.pls.script.psi.*

class ParadoxDynamicCommandFieldNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandFieldNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        return when (getReference(element).resolve()) {
            is ParadoxScriptDefinitionElement -> ParadoxLocalisationAttributesKeys.SCRIPTED_LOC_KEY
            is ParadoxDynamicValueElement -> ParadoxLocalisationAttributesKeys.VARIABLE_KEY
            else -> null
        }
    }

    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, text, configGroup)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val name: String,
        val configGroup: CwtConfigGroup
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        val project = configGroup.project

        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
        }

        //缓存解析结果以优化性能

        private object Resolver : ResolveCache.AbstractResolver<Reference, PsiElement> {
            override fun resolve(ref: Reference, incompleteCode: Boolean): PsiElement? {
                return ref.doResolve()
            }
        }

        private object MultiResolver : ResolveCache.PolyVariantResolver<Reference> {
            override fun resolve(ref: Reference, incompleteCode: Boolean): Array<out ResolveResult> {
                return ref.doMultiResolve()
            }
        }

        override fun resolve(): PsiElement? {
            return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
        }

        override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
            return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
        }

        private fun doResolve(): PsiElement? {
            run {
                val selector = selector(project, element).definition().contextSensitive()
                ParadoxDefinitionSearch.search(name, "scripted_loc", selector).find()?.let { return it }
            }
            run {
                val configExpression = configGroup.mockVariableConfig.expression
                ParadoxDynamicValueManager.resolveDynamicValue(element, name, configExpression, configGroup)?.let { return it }
            }
            return null
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            run {
                val selector = selector(project, element).definition().contextSensitive()
                ParadoxDefinitionSearch.search(name, "scripted_loc", selector).findAll().orNull()
                    ?.let { return it.mapToArray { e -> PsiElementResolveResult(e) } }
            }
            run {
                val configExpression = configGroup.mockVariableConfig.expression
                ParadoxDynamicValueManager.resolveDynamicValue(element, name, configExpression, configGroup)
                    ?.let { return arrayOf(PsiElementResolveResult(it)) }
            }
            return ResolveResult.EMPTY_ARRAY
        }
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicCommandFieldNode? {
            if (text.isParameterized()) return null
            if (!text.isIdentifier()) return null
            return ParadoxDynamicCommandFieldNode(text, textRange, configGroup)
        }
    }
}
