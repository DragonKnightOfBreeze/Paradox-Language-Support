package icu.windea.pls.script.psi

import com.intellij.psi.PsiElement

/**
 * @see ParadoxScriptParameter
 */
interface ParadoxScriptArgumentAwareElement: PsiElement {
    val argumentElement: ParadoxScriptArgument?
}
