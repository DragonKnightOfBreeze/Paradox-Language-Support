package icu.windea.pls.config.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig

interface CwtConfigResolverMixin {
    fun String.withLocationPrefix(element: PsiElement? = null): String {
        val location = CwtConfigResolverManager.getLocation() ?: return this
        val file = element?.containingFile
        val lineNumber = file?.fileDocument?.getLineNumber(element.startOffset)
        val lineNumberString = lineNumber?.let { "#L$it" }.orEmpty()
        return "[$location$lineNumberString] $this"
    }

    fun String.withLocationPrefix(config: CwtConfig<*>): String {
        val element = config.pointer.element
        return withLocationPrefix(element)
    }
}
