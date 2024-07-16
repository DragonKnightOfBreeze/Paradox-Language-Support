package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.model.expression.complex.*
import icu.windea.pls.script.psi.*

class ParadoxDataSourceNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKeyConfig(element: ParadoxExpressionElement): CwtConfig<*>? {
        if(element !is ParadoxScriptStringExpressionElement) return null //unexpected
        if(text.isParameterized()) return null
        return linkConfigs.find { linkConfig ->
            ParadoxExpressionHandler.resolveExpression(element, rangeInExpression, linkConfig, linkConfig.expression, exact = false) != null
        } ?: linkConfigs.firstOrNull()
    }
    
    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if(element !is ParadoxScriptStringExpressionElement) return null //unexpected
        if(nodes.isNotEmpty()) return null
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        //忽略是dynamicValue的情况
        if(linkConfigs.any { it.dataSource?.type in CwtDataTypeGroups.DynamicValue }) return null
        val expect = linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
        //排除可解析的情况
        val reference = getReference(element)
        if(reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrors.unresolvedDataSource(rangeInExpression, text, expect)
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if(element !is ParadoxScriptStringExpressionElement) return null //unexpected
        if(linkConfigs.isEmpty()) return null
        if(text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionHandler.getExpressionOffset(element))
        return Reference(element, rangeInElement, linkConfigs)
    }
    
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val linkConfigs: List<CwtLinkConfig>
    ) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), PsiReferencesAware {
        val project = linkConfigs.first().config.configGroup.project
        
        override fun handleElementRename(newElementName: String): PsiElement {
            val resolved = element.resolved()
            return when {
                resolved == null -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
                resolved.language == CwtLanguage -> throw IncorrectOperationException() //cannot rename cwt config
                resolved.language.isParadoxLanguage() -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
                else -> throw IncorrectOperationException()
            }
        }
        
        override fun getReferences(): Array<out PsiReference>? {
            return linkConfigs.firstNotNullOfOrNull { linkConfig ->
                ParadoxExpressionHandler.getReferences(element, rangeInElement, linkConfig, linkConfig.expression).orNull()
            }
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
            val element = element
            return linkConfigs.firstNotNullOfOrNull { linkConfig ->
                ParadoxExpressionHandler.resolveExpression(element, rangeInElement, linkConfig, linkConfig.expression)
            }
        }
        
        private fun doMultiResolve(): Array<out ResolveResult> {
            val element = element
            return linkConfigs.flatMap { linkConfig ->
                ParadoxExpressionHandler.multiResolveExpression(element, rangeInElement, linkConfig, configExpression = linkConfig.expression)
            }.mapToArray { PsiElementResolveResult(it) }
        }
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxDataSourceNode {
            //text may contain parameters
            return ParadoxDataSourceNode(text, textRange, linkConfigs)
        }
    }
}

