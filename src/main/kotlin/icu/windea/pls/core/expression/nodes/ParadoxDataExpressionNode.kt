package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.expression.errors.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class ParadoxDataExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val linkConfigs: List<CwtLinkConfig>
) : ParadoxExpressionNode {
    override fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? {
        if(text.isParameterized()) return null
        return linkConfigs.find { linkConfig ->
            ParadoxConfigHandler.resolveScriptExpression(element, rangeInExpression, linkConfig, linkConfig.expression, linkConfig.info.configGroup, exact = false) != null
        } ?: linkConfigs.firstOrNull()
    }
    
    override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
        if(linkConfigs.isEmpty()) return null
        if(text.isParameterized()) return null
        return Reference(element, rangeInExpression, linkConfigs)
    }
    
    override fun getUnresolvedError(element: ParadoxScriptStringExpressionElement): ParadoxExpressionError? {
        if(nodes.isNotEmpty()) return null
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        //忽略是valueSetValue的情况
        if(linkConfigs.any { it.dataSource?.type == CwtDataType.Value }) return null
        val expect = linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
        //排除可解析的情况
        val reference = getReference(element)
        if(reference == null || reference.resolveFirst() != null) return null
        return ParadoxUnresolvedScopeLinkDataSourceExpressionError(rangeInExpression, PlsBundle.message("script.expression.unresolvedData", text, expect))
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, linkConfigs: List<CwtLinkConfig>): ParadoxDataExpressionNode {
            //text may contain parameters
            return ParadoxDataExpressionNode(text, textRange, linkConfigs)
        }
    }
    
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val linkConfigs: List<CwtLinkConfig>
    ) : PsiPolyVariantReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement), PsiReferencesAware {
        val project = linkConfigs.first().config.info.configGroup.project
        
        override fun handleElementRename(newElementName: String): ParadoxScriptStringExpressionElement {
            return element.setValue(rangeInElement.replace(element.value, newElementName))
        }
        
        override fun getReferences(): Array<out PsiReference>? {
            val element = element
            return linkConfigs.firstNotNullOfOrNull { linkConfig ->
                ParadoxConfigHandler.getReferences(element, rangeInElement, linkConfig, linkConfig.expression, linkConfig.info.configGroup).takeIfNotEmpty()
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
                ParadoxConfigHandler.resolveScriptExpression(element, rangeInElement, linkConfig, linkConfig.expression, linkConfig.info.configGroup)
            }
        }
        
        private fun doMultiResolve(): Array<out ResolveResult> {
            val element = element
            return linkConfigs.flatMap { linkConfig ->
                ParadoxConfigHandler.multiResolveScriptExpression(element, rangeInElement, linkConfig, configExpression = linkConfig.expression, linkConfig.info.configGroup)
            }.mapToArray { PsiElementResolveResult(it) }
        }
    }
}

