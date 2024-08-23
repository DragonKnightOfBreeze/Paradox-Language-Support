package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxScriptValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val config: CwtConfig<*>
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        if(text.isEmpty()) return null
        return ParadoxScriptAttributesKeys.DEFINITION_REFERENCE_KEY
    }
    
    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if(element !is ParadoxScriptStringExpressionElement) return null //unexpected
        if(nodes.isNotEmpty()) return null
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        val reference = getReference(element)
        if(reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrors.unresolvedScriptValue(rangeInExpression, text)
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if(element !is ParadoxScriptStringExpressionElement) return null //unexpected
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, text, config, configGroup)
    }
    
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val name: String,
        val config: CwtConfig<*>,
        val configGroup: CwtConfigGroup
    ) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
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
            return ParadoxExpressionManager.resolveExpression(element, rangeInElement, config, config.expression)
        }
        
        private fun doMultiResolve(): Array<out ResolveResult> {
            return ParadoxExpressionManager.multiResolveExpression(element, rangeInElement, config, config.expression)
                .mapToArray { PsiElementResolveResult(it) }
        }
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): ParadoxScriptValueNode {
            return ParadoxScriptValueNode(text, textRange, configGroup, config)
        }
    }
}
