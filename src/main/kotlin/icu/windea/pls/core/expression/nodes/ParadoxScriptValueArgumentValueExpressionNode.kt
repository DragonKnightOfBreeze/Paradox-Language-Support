package icu.windea.pls.core.expression.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.lang.cwt.*
import icu.windea.pls.model.*
import icu.windea.pls.script.highlighter.*

class ParadoxScriptValueArgumentValueExpressionNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val scriptValueNode: ParadoxScriptValueExpressionNode?,
    val argumentNode: ParadoxScriptValueArgumentExpressionNode?,
    val configGroup: CwtConfigGroup
) : ParadoxExpressionNode {
    //相关高级语言功能（代码高亮、引用解析等）改为使用语言注入实现
    //see: icu.windea.pls.script.injection.ParadoxScriptInjector
    
    //override fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? {
    //    if(!getSettings().inference.parameterConfig) return null
    //    val parameterElement = argumentNode?.getReference(element)?.resolve() ?: return null
    //    return ParadoxParameterHandler.getInferredConfig(parameterElement)
    //}
    
    override fun getAttributesKey(): TextAttributesKey {
        val type = ParadoxType.resolve(text)
        return when {
            type.isBooleanType() -> ParadoxScriptAttributesKeys.KEYWORD_KEY
            type.isFloatType() -> ParadoxScriptAttributesKeys.NUMBER_KEY
            else -> ParadoxScriptAttributesKeys.STRING_KEY
        }
    }
    
    //override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
    //    if(!getSettings().inference.parameterConfig) return null
    //    if(scriptValueNode == null) return null
    //    if(text.isEmpty()) return null
    //    val reference = scriptValueNode.getReference(element)
    //    if(reference?.resolve() == null) return null //skip if script value cannot be resolved
    //    if(argumentNode == null) return null
    //    return Reference(element, rangeInExpression, this)
    //}
    
    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueExpressionNode?, parameterNode: ParadoxScriptValueArgumentExpressionNode?, configGroup: CwtConfigGroup): ParadoxScriptValueArgumentValueExpressionNode {
            return ParadoxScriptValueArgumentValueExpressionNode(text, textRange, scriptValueNode, parameterNode, configGroup)
        }
    }
    
    //class Reference(
    //    element: ParadoxScriptStringExpressionElement,
    //    rangeInElement: TextRange,
    //    val node: ParadoxScriptValueArgumentValueExpressionNode
    //) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
    //    override fun handleElementRename(newElementName: String): PsiElement {
    //        throw IncorrectOperationException()
    //    }
    //    
    //    override fun resolve(): PsiElement? {
    //        return null
    //    }
    //}
}
