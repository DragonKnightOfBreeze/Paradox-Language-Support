package icu.windea.pls.script.psi

/**
 * 插值。作为内联形式的语法宏（[ParadoxScriptMacro]），可以在特定的标识符/字面量的节点中使用。
 *
 * 说明：
 * - 基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以任意组合使用。
 * - 基本上，这些高级插值语法事实上可以传入和接收任何文本，并不要求相关语法在展开前是合法的。
 * - 认为在内联数学的封装变量引用（[ParadoxScriptInlineMathScriptedVariableReference]）中，仅能使用参数（[ParadoxScriptInlineMathParameter]）.
 *
 * @see ParadoxScriptParameter
 * @see ParadoxScriptInlineConditionalBlock
 */
interface ParadoxScriptInterpolation : ParadoxScriptMacro
