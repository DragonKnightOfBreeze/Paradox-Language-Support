package icu.windea.pls.lang.expression.nodes

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.codeInsight.ParadoxTypeResolver
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.script.editor.ParadoxScriptAttributesKeys

class ParadoxScriptValueArgumentValueNode(
    override val text: String,
    override val rangeInExpression: TextRange,
    override val configGroup: CwtConfigGroup,
    val valueNode: ParadoxScriptValueNode?,
    val argumentNode: ParadoxScriptValueArgumentNode?
) : ParadoxComplexExpressionNode.Base() {
    override fun getAttributesKey(element: ParadoxExpressionElement): TextAttributesKey {
        //为参数值提供基础代码高亮
        val type = ParadoxTypeResolver.resolve(text)
        return when {
            type.isBooleanType() -> ParadoxScriptAttributesKeys.KEYWORD_KEY
            type.isFloatType() -> ParadoxScriptAttributesKeys.NUMBER_KEY
            text.startsWith('@') -> ParadoxScriptAttributesKeys.SCRIPTED_VARIABLE_KEY
            else -> ParadoxScriptAttributesKeys.STRING_KEY
        }
    }

    //相关高级语言功能（代码高亮、引用解析等）改为使用语言注入实现
    //see: icu.windea.pls.lang.injection.ParadoxScriptLanguageInjector

    //region
    //override fun getAttributesKeyConfig(element: ParadoxScriptStringExpressionElement): CwtConfig<*>? {
    //    if(!getSettings().inference.parameterConfig) return null
    //    val parameterElement = argumentNode?.getReference(element)?.resolve() ?: return null
    //    return ParadoxParameterManager.getInferredConfig(parameterElement)
    //}
    //
    //override fun getReference(element: ParadoxScriptStringExpressionElement): Reference? {
    //    if(!getSettings().inference.parameterConfig) return null
    //    if(valueNode == null) return null
    //    if(text.isEmpty()) return null
    //    val reference = valueNode.getReference(element)
    //    if (reference == null) return null
    //    val rangeInElement = rangeInExpression.shiftRight(ParadoxExpressionManager.getExpressionOffset(element))
    //    return Reference(element, rangeInElement, this)
    //}
    //
    //class Reference(
    //    element: ParadoxScriptStringExpressionElement,
    //    rangeInElement: TextRange,
    //    val node: ArgumentValueNode
    //) : PsiReferenceBase<ParadoxScriptStringExpressionElement>(element, rangeInElement) {
    //    override fun handleElementRename(newElementName: String): PsiElement {
    //        throw IncorrectOperationException()
    //    }
    //
    //    override fun resolve(): PsiElement? {
    //        return null
    //    }
    //}
    //endregion

    companion object Resolver {
        fun resolve(text: String, textRange: TextRange, configGroup: CwtConfigGroup, valueNode: ParadoxScriptValueNode?, argumentNode: ParadoxScriptValueArgumentNode?): ParadoxScriptValueArgumentValueNode {
            return ParadoxScriptValueArgumentValueNode(text, textRange, configGroup, valueNode, argumentNode)
        }
    }
}
