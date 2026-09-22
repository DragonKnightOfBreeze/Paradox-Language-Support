package icu.windea.pls.script.psi

import com.intellij.psi.NavigatablePsiElement
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement

/**
 * 宏。作为一类特殊的语言构造，意味着一种运行时的预处理逻辑，诸如求值和替换。
 *
 * 说明：
 * - 这里的宏特指“语法宏”，这意味着它们在语法层面就能确认，因此可以提取为一个 PSI 接口。至于更特殊的“语义宏”，则需要首先进行语义分析。
 * - 根据上下文，宏语法可以分为块形式和内联形式，适用于标识符/字面量的内联形式的宏也被称为插值（[ParadoxScriptInterpolation]）。
 *
 * @see ParadoxScriptParameter
 * @see ParadoxScriptConditionalBlock
 * @see ParadoxScriptScriptedVariableReference
 * @see ParadoxScriptInlineMath
 */
interface ParadoxScriptMacro : NavigatablePsiElement, PsiPresentableTextAwareElement
