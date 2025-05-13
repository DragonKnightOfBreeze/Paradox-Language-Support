package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.impl.source.resolve.*
import com.intellij.util.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.complex.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

/**
 * @see ParadoxTemplateExpression
 */
class ParadoxTemplateSnippetNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val configExpression: CwtDataExpression
) : ParadoxComplexExpressionNode.Base() {
    val config = CwtValueConfig.resolve(emptyPointer(), configGroup, configExpression.expressionString)

    override fun getAttributesKeyConfig(element: ParadoxExpressionElement): CwtConfig<*>? {
        if (text.isParameterized()) return null
        return config
    }

    override fun getUnresolvedError(element: ParadoxExpressionElement): ParadoxComplexExpressionError? {
        if (nodes.isNotEmpty()) return null
        if (text.isEmpty()) return null
        if (text.isParameterized()) return null
        //忽略不是引用的情况
        if (!configExpression.type.isReference) return null
        //排除可解析的情况
        val reference = getReference(element)
        if (reference == null || reference.resolveFirst() != null) return null
        return ParadoxComplexExpressionErrors.unresolvedTemplateSnippet(rangeInExpression, text, configExpression.expressionString)
    }

    override fun getReference(element: ParadoxExpressionElement): PsiReference? {
        if (text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, text, config, configGroup)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val name: String,
        val config: CwtValueConfig,
        val configGroup: CwtConfigGroup
    ) : PsiPolyVariantReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        val project = configGroup.project

        override fun handleElementRename(newElementName: String): PsiElement {
            val element = element
            val resolvedElement = when {
                element is ParadoxScriptStringExpressionElement -> element.resolved()
                else -> element
            }
            return when {
                resolvedElement == null -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
                resolvedElement.language is CwtLanguage -> throw IncorrectOperationException() //cannot rename cwt config
                resolvedElement.language is ParadoxBaseLanguage -> element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
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
            if (element !is ParadoxScriptStringExpressionElement) return null
            if (config.expression.type in CwtDataTypeGroups.DynamicValue) {
                val resolved = ParadoxDynamicValueManager.resolveDynamicValue(element, name, config.expression, configGroup)
                return resolved
            }
            val resolved = ParadoxExpressionManager.resolveScriptExpression(element, rangeInElement, config, config.expression)
            return resolved
        }

        private fun doMultiResolve(): Array<out ResolveResult> {
            val element = element
            if (element !is ParadoxScriptStringExpressionElement) return ResolveResult.EMPTY_ARRAY
            if (config.expression.type in CwtDataTypeGroups.DynamicValue) {
                val resolved = ParadoxDynamicValueManager.resolveDynamicValue(element, name, config.expression, configGroup)
                if (resolved != null) return arrayOf(PsiElementResolveResult(resolved))
                return ResolveResult.EMPTY_ARRAY
            }
            val resolved = ParadoxExpressionManager.multiResolveScriptExpression(element, rangeInElement, config, config.expression)
            if (resolved.isNotEmpty()) return resolved.mapToArray { PsiElementResolveResult(it) }
            return ResolveResult.EMPTY_ARRAY
        }
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, configExpression: CwtDataExpression): ParadoxTemplateSnippetNode {
            //text may contain parameters
            return ParadoxTemplateSnippetNode(text, textRange, configGroup, configExpression)
        }
    }
}
