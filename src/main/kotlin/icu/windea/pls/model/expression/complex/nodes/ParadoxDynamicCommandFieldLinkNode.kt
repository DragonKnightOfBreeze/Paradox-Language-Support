package icu.windea.pls.model.expression.complex.nodes

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
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxDynamicCommandFieldLinkNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode.Base(), ParadoxCommandFieldLinkNode {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        return when(getReference(element).resolve()) {
            is ParadoxScriptDefinitionElement -> ParadoxLocalisationAttributesKeys.SCRIPTED_LOC_KEY
            is ParadoxDynamicValueElement -> ParadoxLocalisationAttributesKeys.VARIABLE_KEY
            else -> null
        }
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference {
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
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
                val selector = definitionSelector(project, element).contextSensitive()
                ParadoxDefinitionSearch.search(name, "scripted_loc", selector).find()?.let { return it }
            }
            run {
                val configExpression = configGroup.mockVariableConfig.expression
                ParadoxDynamicValueHandler.resolveDynamicValue(element, name, configExpression, configGroup)?.let { return it }
            }
            return null
        }
        
        private fun doMultiResolve(): Array<out ResolveResult> {
            run {
                val selector = definitionSelector(project, element).contextSensitive()
                ParadoxDefinitionSearch.search(name, "scripted_loc", selector).findAll().orNull()
                    ?.mapToArray { PsiElementResolveResult(it) }
            }
            run {
                val configExpression = configGroup.mockVariableConfig.expression
                ParadoxDynamicValueHandler.resolveDynamicValue(element, name, configExpression, configGroup)
                    ?.let { return arrayOf(PsiElementResolveResult(it)) }
            }
            return ResolveResult.EMPTY_ARRAY
        }
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup): ParadoxDynamicCommandFieldLinkNode? {
            if(!text.isIdentifier()) return null
            return ParadoxDynamicCommandFieldLinkNode(text, textRange, configGroup)
        }
    }
}
