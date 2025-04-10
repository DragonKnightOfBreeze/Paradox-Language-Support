package icu.windea.pls.lang.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.editor.*

class ParadoxDynamicValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val configs: List<CwtConfig<*>>
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        val expression = configs.first().expression!! //first is ok
        val dynamicValueType = expression.value ?: return null
        return when (dynamicValueType) {
            "variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
            else -> ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
        }
    }

    override fun getReference(element: ParadoxExpressionElement): Reference? {
        if (text.isParameterized()) return null
        val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
        return Reference(element, rangeInElement, text, configs, configGroup)
    }

    class Reference(
        element: ParadoxExpressionElement,
        rangeInElement: TextRange,
        val name: String,
        val configs: List<CwtConfig<*>>,
        val configGroup: CwtConfigGroup
    ) : PsiReferenceBase<ParadoxExpressionElement>(element, rangeInElement) {
        val configExpressions = configs.mapNotNull { it.expression }

        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
        }

        override fun resolve(): PsiElement? {
            val configExpressions = configs.mapNotNullTo(mutableSetOf()) { it.expression }
            return ParadoxDynamicValueManager.resolveDynamicValue(element, name, configExpressions, configGroup)
        }
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueNode? {
            //text may contain parameters
            if (configs.any { c -> c.expression?.type !in CwtDataTypeGroups.DynamicValue }) return null
            return ParadoxDynamicValueNode(text, textRange, configGroup, configs)
        }
    }
}
