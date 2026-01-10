package icu.windea.pls.config.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.config.CwtConfig

interface CwtConfigResolverScope

context(_: CwtConfigResolverScope)
fun String.withLocationPrefix(element: PsiElement? = null): String {
    val location = CwtConfigResolverManager.getLocation() ?: return this
    val file = element?.containingFile
    val lineNumber = file?.fileDocument?.getLineNumber(element.startOffset)
    val lineNumberString = lineNumber?.let { "#L$it" }.orEmpty()
    return "[$location$lineNumberString] $this"
}

context(_: CwtConfigResolverScope)
fun String.withLocationPrefix(config: CwtConfig<*>): String {
    val element = config.pointer.element
    return withLocationPrefix(element)
}
