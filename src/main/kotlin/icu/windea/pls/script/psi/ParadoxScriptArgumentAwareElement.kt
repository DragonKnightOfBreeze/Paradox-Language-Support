package icu.windea.pls.script.psi

import com.intellij.psi.*

/**
 * @see ParadoxScriptParameter
 */
interface ParadoxScriptArgumentAwareElement: PsiElement {
    val argumentElement: ParadoxScriptArgument?
}
