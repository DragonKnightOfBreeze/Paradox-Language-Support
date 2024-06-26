package icu.windea.pls.model.expression.complex.nodes

import com.intellij.openapi.editor.colors.*
import com.intellij.openapi.util.*
import icu.windea.pls.config.configGroup.*
import icu.windea.pls.model.*
import icu.windea.pls.script.highlighter.*

class ParadoxScriptValueArgumentValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    val scriptValueNode: ParadoxScriptValueNode?,
    val argumentNode: ParadoxScriptValueArgumentNode?,
    val configGroup: CwtConfigGroup
) : ParadoxComplexExpressionNode {
    //相关高级语言功能（代码高亮、引用解析等）改为使用语言注入实现
    //see: icu.windea.pls.script.injection.ParadoxScriptLanguageInjector
    
    //override fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? {
    //    if(!getSettings().inference.parameterConfig) return null
    //    val parameterElement = argumentNode?.getReference(element)?.resolve() ?: return null
    //    return ParadoxParameterHandler.getInferredConfig(parameterElement)
    //}
    
    override fun getAttributesKey(): TextAttributesKey {
        //为参数值提供基础代码高亮
        val type = ParadoxType.resolve(text)
        return when {
            type.isBooleanType() -> ParadoxScriptAttributesKeys.KEYWORD_KEY
            type.isFloatType() -> ParadoxScriptAttributesKeys.NUMBER_KEY
            text.startsWith('@') -> ParadoxScriptAttributesKeys.SCRIPTED_VARIABLE_KEY
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
        fun resolve(text: String, textRange: TextRange, scriptValueNode: ParadoxScriptValueNode?, parameterNode: ParadoxScriptValueArgumentNode?, configGroup: CwtConfigGroup): ParadoxScriptValueArgumentValueNode {
            return ParadoxScriptValueArgumentValueNode(text, textRange, scriptValueNode, parameterNode, configGroup)
        }
    }
    
    //class Reference(
    //    element: ParadoxScriptStringExpressionElement,
    //    rangeInElement: TextRange,
    //    val node: ParadoxScriptValueArgumentValueNode
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
