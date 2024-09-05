package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class ParadoxDataSourceNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKeyConfig(element: ParadoxExpressionElement): CwtConfig<*>? {
        if(text.isParameterized()) return null
        if(linkConfigs.isEmpty()) return null
        if(linkConfigs.size == 1) return linkConfigs.first()
        if(linkConfigs.all { it.expression?.type in CwtDataTypeGroups.DynamicValue }) return linkConfigs.first()
        if(element !is ParadoxScriptStringExpressionElement) return null
        return linkConfigs.find { linkConfig ->
            ParadoxExpressionManager.resolveExpression(element, rangeInExpression, linkConfig, linkConfig.expression, exact = false) != null
        } ?: linkConfigs.firstOrNull()
    }
    
    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if(nodes.isNotEmpty()) return null
        if(text.isEmpty()) return null
        if(text.isParameterized()) return null
        //忽略是dynamicValue的情况
        if(linkConfigs.any { it.dataSourceExpression?.type in CwtDataTypeGroups.DynamicValue }) return null
        val expect = linkConfigs.mapNotNullTo(mutableSetOf()) { it.expression }.joinToString()
        //排除可解析的情况
        val reference = getReference(element)
        if(reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrors.unresolvedDataSource(rangeInExpression, text, expect)
    }
    
    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if(linkConfigs.isEmpty()) return null
        if(text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, text, linkConfigs, configGroup)
    }
    
    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val name: String,   
        val linkConfigs: List<CwtLinkConfig>,
        val configGroup: CwtConfigGroup
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        val project = linkConfigs.first().config.configGroup.project
        
        override fun handleElementRename(newElementName: String): PsiElement {
            val element = element
            val resolvedElement = when {
                element is ParadoxScriptStringExpressionElement -> element.resolved()
                else -> element
            }
            return when {
                resolvedElement == null -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
                resolvedElement.language == CwtLanguage -> throw IncorrectOperationException() //cannot rename cwt config
                resolvedElement.language.isParadoxLanguage() -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
                else -> throw IncorrectOperationException()
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
            if(linkConfigs.all { it.expression?.type in CwtDataTypeGroups.DynamicValue }) {
                val configExpressions = linkConfigs.mapNotNull { it.expression }
                return ParadoxDynamicValueManager.resolveDynamicValue(element, name, configExpressions, configGroup)
            }
            if(element !is ParadoxScriptStringExpressionElement) return null
            return linkConfigs.firstNotNullOfOrNull { linkConfig ->
                ParadoxExpressionManager.resolveExpression(element, rangeInElement, linkConfig, linkConfig.expression)
            }
        }
        
        private fun doMultiResolve(): Array<out ResolveResult> {
            val element = element
            if(linkConfigs.all { it.expression?.type in CwtDataTypeGroups.DynamicValue }) {
                val configExpressions = linkConfigs.mapNotNull { it.expression }
                return ParadoxDynamicValueManager.resolveDynamicValue(element, name, configExpressions, configGroup)
                    ?.let { arrayOf(PsiElementResolveResult(it)) } ?: ResolveResult.EMPTY_ARRAY
            }
            if(element !is ParadoxScriptStringExpressionElement) return ResolveResult.EMPTY_ARRAY
            return linkConfigs.flatMap { linkConfig ->
                ParadoxExpressionManager.multiResolveExpression(element, rangeInElement, linkConfig, configExpression = linkConfig.expression)
            }.mapToArray { PsiElementResolveResult(it) }
        }
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxDataSourceNode {
            //text may contain parameters
            return ParadoxDataSourceNode(text, textRange, configGroup, linkConfigs)
        }
    }
}

