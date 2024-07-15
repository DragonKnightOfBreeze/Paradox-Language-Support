package icu.windea.pls.model.expression.complex.nodes

import com.intellij.lang.*
import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.ep.inherit.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.*
import icu.windea.pls.localisation.highlighter.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxDatabaseObjectNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val expression: ParadoxDatabaseObjectExpression,
    val index: Int
) : ParadoxComplexExpressionNode.Base() {
    val config = expression.typeNode?.config
    
    override fun getAttributesKey(language: Language): TextAttributesKey {
        return when(language) {
            ParadoxLocalisationLanguage -> ParadoxLocalisationAttributesKeys.DATABASE_OBJECT_KEY
            else -> ParadoxScriptAttributesKeys.DATABASE_OBJECT_KEY
        }
    }
    
    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        if(config == null) return null
        val reference = getReference(element)
        if(reference == null || reference.resolveFirst() != null) return null
        val typeToSearch = if(index == 0) config.type else config.swapType
        return ParadoxComplexExpressionErrors.unresolvedDatabaseObject(rangeInExpression, text, typeToSearch)
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        if(config == null) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, this)
    }
    
    fun checkDatabaseObject(definition: ParadoxScriptDefinitionElement, typeToSearch: String): Boolean {
        val definitionInfo = definition.definitionInfo ?: return false
        if(definitionInfo.type != typeToSearch) return false
        if(index == 0) return true
        
        //filter out mismatched swap definition vs base definition
        val expectedSuperDefinitionName = expression.valueNode?.text ?: return false
        val expectedSuperDefinitionType = config?.type ?: return false
        val superDefinition = ParadoxDefinitionInheritSupport.getSuperDefinition(definition, definitionInfo)?: return false
        val superDefinitionInfo = superDefinition.definitionInfo ?: return false
        return superDefinitionInfo.name == expectedSuperDefinitionName && superDefinitionInfo.type == expectedSuperDefinitionType
    }
    
    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val node: ParadoxDatabaseObjectNode
    ): PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        val expression = node.expression
        val project = expression.configGroup.project
        val config = expression.typeNode?.config
        
        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
        }
        
        //缓存解析结果以优化性能
        
        private object Resolver : ResolveCache.AbstractResolver<Reference, PsiElement>{
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
            if(config == null) return null
            val name = node.text
            val typeToSearch = if(node.index == 0) config.type else config.swapType
            if(typeToSearch == null) return null
            val selector = definitionSelector(project, element).contextSensitive()
            return ParadoxDefinitionSearch.search(name, typeToSearch, selector).find()
                ?.takeIf { node.checkDatabaseObject(it, typeToSearch) }
        }
        
        private fun doMultiResolve(): Array<out ResolveResult> {
            if(config == null) return ResolveResult.EMPTY_ARRAY
            val name = node.text
            val typeToSearch = if(node.index == 0) config.type else config.swapType
            if(typeToSearch == null) return ResolveResult.EMPTY_ARRAY
            val selector = definitionSelector(project, element).contextSensitive()
            return ParadoxDefinitionSearch.search(name, typeToSearch, selector).findAll()
                .filter { node.checkDatabaseObject(it, typeToSearch) }
                .mapToArray { PsiElementResolveResult(it) }
        }
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, expression: ParadoxDatabaseObjectExpression, index: Int): ParadoxDatabaseObjectNode {
            return ParadoxDatabaseObjectNode(text, textRange, expression, index)
        }
    }
}
