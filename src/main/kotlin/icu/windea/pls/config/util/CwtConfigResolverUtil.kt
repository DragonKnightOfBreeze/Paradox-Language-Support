package icu.windea.pls.config.util

import com.intellij.psi.PsiElement
import com.intellij.psi.util.startOffset
import com.jetbrains.rd.util.ThreadLocal
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup

object CwtConfigResolverUtil {
    private val currentLocation = ThreadLocal<String>()

    fun setLocation(filePath: String, configGroup: CwtConfigGroup) {
        currentLocation.set(getLocation(filePath, configGroup))
    }

    fun resetLocation() {
        currentLocation.remove()
    }

    private fun getLocation(filePath: String, configGroup: CwtConfigGroup): String {
        val gameTypeId = configGroup.gameType.id
        return "$gameTypeId@$filePath"
    }

    fun String.withLocationPrefix(element: PsiElement? = null): String {
        val location = currentLocation.get() ?: return this
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
