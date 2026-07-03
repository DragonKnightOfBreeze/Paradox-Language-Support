package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

/**
 * 可以带有传入参数（[ParadoxArgument]）的 PSI 元素。
 *
 * @see ParadoxScriptParameter
 * @see ParadoxScriptInlineMathParameter
 */
interface ParadoxArgumentAwareElement: PsiElement {
    val argumentElement: ParadoxArgument?
}
