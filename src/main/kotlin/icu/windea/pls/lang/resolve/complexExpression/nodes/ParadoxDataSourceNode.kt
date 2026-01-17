package icu.windea.pls.lang.resolve.complexExpression.nodes

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiPolyVariantReferenceBase
import com.intellij.psi.ResolveResult
import com.intellij.psi.impl.source.resolve.ResolveCache
import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.delegated.CwtLinkConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.createResults
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.psi.ParadoxPsiManager
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionError
import icu.windea.pls.lang.resolve.complexExpression.util.ParadoxComplexExpressionErrorBuilder
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

class ParadoxDataSourceNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val linkConfigs: List<CwtLinkConfig>
) : ParadoxComplexExpressionNodeBase(), ParadoxIdentifierNode {
    private val linkConfigsDynamicValue = linkConfigs.filter { it.configExpression?.type in CwtDataTypeSets.DynamicValue }
    private val linkConfigsNotDynamicValue = linkConfigs.filter { it.configExpression?.type !in CwtDataTypeSets.DynamicValue }

    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return linkConfigs
    }

    override fun getAttributesKeyConfig(element: ParadoxExpressionElement): CwtConfig<*>? {
        if (text.isParameterized()) return null
        if (linkConfigs.isEmpty()) return null
        if (linkConfigs.size == 1) return linkConfigs.first()
        run {
            if (linkConfigsNotDynamicValue.isEmpty()) return@run
            val resolved = linkConfigs.find {
                ParadoxExpressionManager.resolveScriptExpression(element, rangeInExpression, it, it.configExpression, exact = false) != null
            }
            if (resolved != null) return resolved
        }
        run {
            if (linkConfigsDynamicValue.isEmpty()) return@run
            return linkConfigsDynamicValue.first()
        }
        return linkConfigsNotDynamicValue.firstOrNull()
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (nodes.isNotEmpty()) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val configExpressions = linkConfigs.mapNotNullTo(mutableSetOf()) { it.configExpression }
        // 忽略不是引用或者是dynamicValue的情况
        if (configExpressions.any { !it.type.isReference || it.type in CwtDataTypeSets.DynamicValue }) return null
        // 排除可解析的情况
        val reference = getReference(element)
        if (reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrorBuilder.unresolvedDataSource(rangeInExpression, text, configExpressions.joinToString())
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (linkConfigs.isEmpty()) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, this)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        private val node: ParadoxDataSourceNode
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement), ParadoxIdentifierNode.Reference {
        private val name get() = node.text
        private val project get() = node.configGroup.project
        private val linkConfigsDynamicValue = node.linkConfigs.filter { it.configExpression?.type in CwtDataTypeSets.DynamicValue }
        private val linkConfigsNotDynamicValue = node.linkConfigs.filter { it.configExpression?.type !in CwtDataTypeSets.DynamicValue }

        override fun handleElementRename(newElementName: String): PsiElement {
            return ParadoxPsiManager.handleElementRename(element, rangeInElement, newElementName)
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
            val element = element
            run {
                if (linkConfigsNotDynamicValue.isEmpty()) return@run
                val resolved = linkConfigsNotDynamicValue.firstNotNullOfOrNull {
                    ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, it, it.configExpression)
                }
                if (resolved != null) return resolved
            }
            run {
                if (linkConfigsDynamicValue.isEmpty()) return@run
                val configExpressions = linkConfigsDynamicValue.mapNotNull { it.configExpression }
                if (configExpressions.isEmpty()) return@run
                val resolved = ParadoxDynamicValueManager.resolveDynamicValue(element, name, configExpressions, node.configGroup)
                return resolved
            }
            return null
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val element = element
            run {
                if (linkConfigsNotDynamicValue.isEmpty()) return@run
                val resolved = linkConfigsNotDynamicValue.flatMap {
                    ParadoxExpressionManager.multiResolveScriptExpression(element, rangeInElement, it, it.configExpression)
                }
                if (resolved.isNotEmpty()) return resolved.createResults()
            }
            run {
                if (linkConfigsDynamicValue.isEmpty()) return@run
                val configExpressions = linkConfigsDynamicValue.mapNotNull { it.configExpression }
                if (configExpressions.isEmpty()) return@run
                val resolved = ParadoxDynamicValueManager.resolveDynamicValue(element, name, configExpressions, node.configGroup)
                if (resolved != null) return resolved.createResults()
            }
            return ResolveResult.EMPTY_ARRAY
        }

        override fun canResolveFor(constraint: ParadoxResolveConstraint): Boolean {
            val dataTypes = node.linkConfigs.mapNotNull { it.configExpression?.type }
            return when (constraint) {
                ParadoxResolveConstraint.Definition -> dataTypes.any { it in CwtDataTypeSets.DefinitionAware || it == CwtDataTypes.AliasKeysField }
                ParadoxResolveConstraint.ComplexEnumValue -> dataTypes.any { it == CwtDataTypes.EnumValue || it == CwtDataTypes.AliasKeysField }
                ParadoxResolveConstraint.DynamicValue -> dataTypes.any { it in CwtDataTypeSets.DynamicValue || it == CwtDataTypes.AliasKeysField }
                else -> false
            }
        }
    }

    open class Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, linkConfigs: List<CwtLinkConfig>): ParadoxDataSourceNode {
            // text may contain parameters
            return ParadoxDataSourceNode(text, textRange, configGroup, linkConfigs)
        }
    }

    companion object : Resolver()
}

