package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.lang.cwt.config.*
import icu.windea.pls.lang.cwt.expression.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxValueSetValueExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val configs: List<CwtConfig<*>>,
    val configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
    override fun getAttributesKey(): TextAttributesKey? {
        if(text.isParameterized()) return null
        val expression = configs.first().expression!! //first is ok
        val valueSetName = expression.value ?: return null
        return when(valueSetName) {
            "variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
            else -> ParadoxScriptAttributesKeys.VALUE_SET_VALUE_KEY
        }
    }
    
    override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
        if(text.isParameterized()) return null
        return Reference(element, rangeInExpression, text, configs, configGroup)
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configs: List<CwtConfig<*>>, configGroup: CwtConfigGroup): ParadoxValueSetValueExpressionNode? {
            //text may contain parameters
            if(configs.any { c -> c.expression?.type.let { it != CwtDataType.Value && it != CwtDataType.ValueSet } }) return null
            return ParadoxValueSetValueExpressionNode(text, textRange, configs, configGroup)
        }
    }
    
    class Reference(
        element: ParadoxScriptStringExpressionElement,
        rangeInElement: TextRange,
        val name: String,
        val configs: List<CwtConfig<*>>,
        val configGroup: CwtConfigGroup
    ) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
        val configExpressions = configs.mapNotNull { it.expression }
        
        override fun handleElementRename(newElementName: String): PsiElement {
            return element.setValue(rangeInElement.replace(element.value, newElementName))
        }
        
        override fun resolve(): PsiElement? {
            val predefinedResolved = ParadoxConfigHandler.resolvePredefinedValueSetValue(element, name, configExpressions, configGroup)
            if(predefinedResolved != null) return predefinedResolved
            val configExpressions = configs.mapNotNullTo(mutableSetOf()) { it.expression }
            return ParadoxValueSetValueHandler.resolveValueSetValue(element, name, configExpressions, configGroup)
        }
    }
}
