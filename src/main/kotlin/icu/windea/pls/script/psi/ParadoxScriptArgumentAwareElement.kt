package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

/**
 * 表示此 PSI 元素可以带有传入参数（[ParadoxScriptArgument]）。
 *
 * @see ParadoxScriptArgument
 */
interface ParadoxScriptArgumentAwareElement: PsiElement {
    val argumentElement: ParadoxScriptArgument?
}
