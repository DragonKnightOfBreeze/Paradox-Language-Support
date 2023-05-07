package icu.windea.pls.core.references

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxTemplateSnippetExpressionReference(
    element: ParadoxScriptStringExpressionElement,
    rangeInElement: TextRange,
    val name: String,
    val configExpression: CwtDataExpression,
    val configGroup: CwtConfigGroup
) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
    val project by lazy { element.project }
    
    override fun handleElementRename(newElementName: String): ParadoxScriptStringExpressionElement {
       throw IncorrectOperationException() // cannot rename
    }
    
    //缓存解析结果以优化性能
    
    override fun resolve(): PsiElement? {
        return ResolveCache.getInstance(project).resolveWithCaching(this, Resolver, false, false)
    }
    
    private fun doResolve(): PsiElement? {
        val element = element
        return ParadoxConfigHandler.resolveScriptExpression(element, rangeInElement, null, configExpression, configGroup)
    }
    
    override fun multiResolve(incompleteCode: Boolean): Array<out ResolveResult> {
        return ResolveCache.getInstance(project).resolveWithCaching(this, MultiResolver, false, false)
    }
    
    private fun doMultiResolve(): Array<out ResolveResult> {
        val element = element
        return ParadoxConfigHandler.multiResolveScriptExpression(element, rangeInElement, null, configExpression, configGroup)
            .mapToArray { PsiElementResolveResult(it) }
    }
    
    private object Resolver: ResolveCache.AbstractResolver<ParadoxTemplateSnippetExpressionReference, PsiElement> {
        override fun resolve(ref: ParadoxTemplateSnippetExpressionReference, incompleteCode: Boolean): PsiElement? {
            return ref.doResolve()
        }
    }
    
    private object MultiResolver: ResolveCache.PolyVariantResolver<ParadoxTemplateSnippetExpressionReference> {
        override fun resolve(ref: ParadoxTemplateSnippetExpressionReference, incompleteCode: Boolean): Array<out ResolveResult> {
            return ref.doMultiResolve()
        }
    }
}