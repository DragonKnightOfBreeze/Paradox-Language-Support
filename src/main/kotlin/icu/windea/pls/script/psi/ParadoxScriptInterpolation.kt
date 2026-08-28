package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.psi.PsiPresentableElement

/**
 * 可以作为插值在特定位置使用的 PSI 元素。
 *
 * 说明：
 * - 基本上，这些高级插值语法，以及对应的标识符/字面量的词元，可以任意组合使用。
 * - 实际上，脚本文件中的任何地方都能使用参数（[ParadoxScriptParameter]）。
 *
 * @see ParadoxScriptParameter
 * @see ParadoxScriptInlineConditionalBlock
 */
interface  ParadoxScriptInterpolation : NavigatablePsiElement, PsiPresentableElement
