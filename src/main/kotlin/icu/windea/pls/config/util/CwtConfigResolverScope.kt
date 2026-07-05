package icu.windea.pls.config.util

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup

interface CwtConfigResolverScope {
    fun String.withLocationPrefix(config: CwtConfig<*>): String {
        val configGroup = config.configGroup
        val element = config.pointer.element
        return withLocationPrefix(element, configGroup)
    }

    fun String.withLocationPrefix(element: PsiElement?, configGroup: CwtConfigGroup): String {
        val locationPrefix = getLocationPrefix(element, configGroup)
        return "$locationPrefix $this"
    }

    private fun getLocationPrefix(element: PsiElement?, configGroup: CwtConfigGroup): String {
        val gameType = configGroup.gameType
        val gameTypeId = gameType.id
        val file = element?.containingFile
        val fileName = file?.name
        val lineNumber = if (element is PsiFile) null else file?.fileDocument?.getLineNumber(element.startOffset)
        return buildString {
            append("[").append(gameTypeId).append("]")
            if (file != null) {
                append(" [")
                append(fileName)
                if (lineNumber != null) {
                    append("#L").append(lineNumber)
                }
                append("]")
            }
        }
    }
}
