package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement
import icu.windea.pls.core.children
import icu.windea.pls.core.select.listBy


/**
 * 插值容器，可以直接包含各类插值（[ParadoxScriptInterpolation]）。这意味着其标识符/字面量可能是参数化的。
 *
 * 说明：
 * - 基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以任意组合使用。
 * - 基本上，这些高级插值语法事实上可以传入和接收任何文本，并不要求相关语法在展开前是合法的。
 * - 认为在内联数学的封装变量引用（[ParadoxScriptInlineMathScriptedVariableReference]）中，仅能使用参数（[ParadoxScriptInlineMathParameter]）.
 *
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 * @see ParadoxScriptScriptedVariableName
 * @see ParadoxScriptScriptedVariableReference
 * @see ParadoxScriptInlineMathScriptedVariableReference
 * @see ParadoxScriptInlineConditionalBlock
 */
@Suppress("unused")
interface ParadoxScriptInterpolationContainer : PsiElement {
    val interpolations: List<ParadoxScriptInterpolation> get() = children().listBy()
    val parameters: List<ParadoxScriptParameter> get() = children().listBy()
    val inlineConditionalBlocks: List<ParadoxScriptInlineConditionalBlock> get() = children().listBy()
}
