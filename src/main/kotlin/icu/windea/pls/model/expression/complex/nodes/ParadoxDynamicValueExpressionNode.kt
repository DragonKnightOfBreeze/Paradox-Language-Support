package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.config.*
import icu.windea.pls.config.config.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.ep.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.highlighter.*
import icu.windea.pls.script.psi.*

class ParadoxDynamicValueExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val configs: List<CwtConfig<*>>,
    val configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
    override fun getAttributesKey(): TextAttributesKey? {
        val expression = configs.first().expression!! //first is ok
        val dynamicValueType = expression.value ?: return null
        return when(dynamicValueType) {
            "variable" -> ParadoxScriptAttributesKeys.VARIABLE_KEY
            else -> ParadoxScriptAttributesKeys.DYNAMIC_VALUE_KEY
        }
    }
    
    override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
        if(text.isParameterized()) return null
        return Reference(element, rangeInExpression, text, configs, configGroup)
    }
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configs: List<CwtConfig<*>>, configGroup: CwtConfigGroup): ParadoxDynamicValueExpressionNode? {
            //text may contain parameters
            if(configs.any { c -> c.expression?.type !in CwtDataTypeGroups.DynamicValue }) return null
            return ParadoxDynamicValueExpressionNode(text, textRange, configs, configGroup)
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
            return element.setValue(rangeInElement.replace(element.text, newElementName).unquote())
        }
        
        override fun resolve(): PsiElement? {
            val configExpressions = configs.mapNotNullTo(mutableSetOf()) { it.expression }
            return ParadoxDynamicValueHandler.resolveDynamicValue(element, name, configExpressions, configGroup)
        }
    }
}
