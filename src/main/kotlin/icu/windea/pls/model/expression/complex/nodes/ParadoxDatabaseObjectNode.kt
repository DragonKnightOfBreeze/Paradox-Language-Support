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
    val isBase: Boolean
) : ParadoxComplexExpressionNode.Base() {
    val config = expression.typeNode?.config
    
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        return when(element.language) {
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
        val typeToSearch = if(isBase) config.type else config.swapType
        return ParadoxComplexExpressionErrors.unresolvedDatabaseObject(rangeInExpression, text, typeToSearch)
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        if(config == null) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, this)
    }
    
    fun isForcedBase(): Boolean {
        //referencing base database objects repeatedly is supported, to force show non-swapped form in the game
        //(pre-condition: database object type can be swapped & database object names should be the same)
        if(isBase || config?.swapType == null) return false
        val baseName = expression.valueNode?.text?.orNull()
        return baseName != null && baseName == text
    }
    
    fun isPossibleForcedBase(): Boolean {
        if(isBase || config?.swapType == null) return false
        val baseName = expression.valueNode?.text?.orNull()
        return baseName != null
    }
    
    fun isValidDatabaseObject(definition: ParadoxScriptDefinitionElement, typeToSearch: String): Boolean {
        if(isForcedBase()) return true
        
        val definitionInfo = definition.definitionInfo ?: return false
        if(definitionInfo.name.isEmpty()) return false
        if(definitionInfo.type != typeToSearch) return false
        if(isBase) return true
        
        //filter out mismatched swap definition vs base definition
        val expectedSuperDefinitionName = expression.valueNode?.text?.orNull() ?: return false
        val expectedSuperDefinitionType = config?.type ?: return false
        val superDefinition = ParadoxDefinitionInheritSupport.getSuperDefinition(definition, definitionInfo) ?: return false
        val superDefinitionInfo = superDefinition.definitionInfo ?: return false
        if(superDefinitionInfo.name.isEmpty()) return false
        return superDefinitionInfo.name == expectedSuperDefinitionName && superDefinitionInfo.type == expectedSuperDefinitionType
    }
    
    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val node: ParadoxDatabaseObjectNode
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        val expression = node.expression
        val project = expression.configGroup.project
        val config = expression.typeNode?.config
        
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
            if(config == null) return null
            val name = node.text
            val typeToSearch = if(node.isBase || node.isForcedBase()) config.type else config.swapType
            if(typeToSearch == null) return null
            val selector = definitionSelector(project, element).contextSensitive()
            return ParadoxDefinitionSearch.search(name, typeToSearch, selector).find()
                ?.takeIf { node.isValidDatabaseObject(it, typeToSearch) }
        }
        
        private fun doMultiResolve(): Array<out ResolveResult> {
            if(config == null) return ResolveResult.EMPTY_ARRAY
            val name = node.text
            val typeToSearch = if(node.isBase || node.isForcedBase()) config.type else config.swapType
            if(typeToSearch == null) return ResolveResult.EMPTY_ARRAY
            val selector = definitionSelector(project, element).contextSensitive()
            return ParadoxDefinitionSearch.search(name, typeToSearch, selector).findAll()
                .filter { node.isValidDatabaseObject(it, typeToSearch) }
                .mapToArray { PsiElementResolveResult(it) }
        }
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, expression: ParadoxDatabaseObjectExpression, isBase: Boolean): ParadoxDatabaseObjectNode {
            return ParadoxDatabaseObjectNode(text, textRange, expression, isBase)
        }
    }
}
