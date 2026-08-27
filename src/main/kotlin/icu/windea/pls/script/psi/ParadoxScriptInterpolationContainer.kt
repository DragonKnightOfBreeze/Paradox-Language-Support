package icu.windea.pls.script.psi

import com.intellij.psi.*
import icu.windea.pls.core.*


/**
 * 插值容器，可以直接包含各类插值（[ParadoxScriptInterpolation]）。这意味着其标识符/字面量可能是参数化的。
 *
 * 说明：
 * - 基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以任意组合使用。
 * - 实际上，脚本文件中的任何地方都能使用参数（[ParadoxParameter]）。
 *
 * @see ParadoxScriptPropertyKey
 * @see ParadoxScriptString
 * @see ParadoxScriptScriptedVariableName
 * @see ParadoxScriptScriptedVariableReference
 * @see ParadoxScriptInlineMathScriptedVariableReference
 */
@Suppress("unused")
interface ParadoxScriptInterpolationContainer : NavigatablePsiElement {
    val interpolations: List<ParadoxScriptInterpolation> get() = this.findChildren<_>()
    val parameters: List<ParadoxParameter> get() = this.findChildren<_>()
    val inlineConditionalBlocks: List<ParadoxScriptInlineConditionalBlock> get() = this.findChildren<_>()
}
