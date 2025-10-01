package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

/**
 * 可以带有传入参数（[ParadoxScriptArgument]）的 PSI 元素。
 */
interface ParadoxScriptArgumentAwareElement: PsiElement {
    val argumentElement: ParadoxScriptArgument?
}
