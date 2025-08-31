package icu.windea.pls.lang.expression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configExpression.value
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.core.unquote
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.lang.util.ParadoxDynamicValueManager
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.editor.ParadoxLocalisationAttributesKeys
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxDynamicValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val configs: List<CwtConfig<*>>
) : ParadoxComplexExpressionNode.Base() {
    override fun getRelatedConfigs(): Collection<CwtConfig<*>> {
        return configs
    }

    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey? {
        val expression = configs.first().configExpression ?: return null //first is ok
        val dynamicValueType = expression.value ?: return null
        return when (element.language) {
            is ParadoxLocalisationLanguage -> {
                when (dynamicValueType) {
                    "variable" -> ParadoxLocalisationAttributesKeys.VARIABLE_KEY
                    else -> ParadoxLocalisationAttributesKeys.DYNAMIC_VALUE_KEY
                }
            }
            else -> {
                when (dynamicValueType) {
                    "variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
                    else -> ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
                }
            }
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
        val configExpressions = configs.mapNotNull { it.configExpression }

        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
        }

        override fun resolve(): PsiElement? {
            val configExpressions = configs.mapNotNullTo(mutableSetOf()) { it.configExpression }
            return ParadoxDynamicValueManager.resolveDynamicValue(element, name, configExpressions, configGroup)
        }
    }

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, configs: List<CwtConfig<*>>): ParadoxDynamicValueNode? {
            //text may contain parameters
            if (configs.any { c -> c.configExpression?.type !in CwtDataTypeGroups.DynamicValue }) return null
            return ParadoxDynamicValueNode(text, textRange, configGroup, configs)
        }
    }
}
