package icu.windea.pls.config.scopes

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.trace
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.startOffset
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup

@Suppress("unused")
interface CwtConfigResolutionScope : CwtFilePathResolutionScope {
    fun Logger.traceWithPrefix(element: PsiElement?, configGroup: CwtConfigGroup, lazyMessage: () -> String) {
        if (configGroup.project.isDefault) return /// skip for application config groups
        trace { "${getLogPrefix(element, configGroup)} ${lazyMessage()}" }
    }

    fun Logger.traceWithPrefix(config: CwtConfig<*>, lazyMessage: () -> String) {
        val configGroup = config.configGroup
        if (configGroup.project.isDefault) return /// skip for application config groups
        trace { "${getLogPrefix(config)} ${lazyMessage()}" }
    }

    fun Logger.debugWithPrefix(element: PsiElement?, configGroup: CwtConfigGroup, lazyMessage: () -> String) {
        if (configGroup.project.isDefault) return /// skip for application config groups
        debug { "${getLogPrefix(element, configGroup)} ${lazyMessage()}" }
    }

    fun Logger.debugWithPrefix(config: CwtConfig<*>, lazyMessage: () -> String) {
        val configGroup = config.configGroup
        if (configGroup.project.isDefault) return /// skip for application config groups
        debug { "${getLogPrefix(config)} ${lazyMessage()}" }
    }

    fun Logger.infoWithPrefix(element: PsiElement?, configGroup: CwtConfigGroup, message: String) {
        if (configGroup.project.isDefault) return /// skip for application config groups
        info("${getLogPrefix(element, configGroup)} $message")
    }

    fun Logger.infoWithPrefix(config: CwtConfig<*>, message: String) {
        val configGroup = config.configGroup
        if (configGroup.project.isDefault) return /// skip for application config groups
        info("${getLogPrefix(config)} $message")
    }

    fun Logger.warnWithPrefix(element: PsiElement?, configGroup: CwtConfigGroup, message: String) {
        if (configGroup.project.isDefault) return /// skip for application config groups
        warn("${getLogPrefix(element, configGroup)} $message")
    }

    fun Logger.warnWithPrefix(config: CwtConfig<*>, message: String) {
        val configGroup = config.configGroup
        if (configGroup.project.isDefault) return /// skip for application config groups
        warn("${getLogPrefix(config)} $message")
    }

    private fun getLogPrefix(config: CwtConfig<*>): String {
        val configGroup = config.configGroup
        val element = config.pointer.element
        val locationPrefix = getLogPrefix(element, configGroup)
        return locationPrefix
    }

    private fun getLogPrefix(element: PsiElement?, configGroup: CwtConfigGroup): String {
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

    companion object INSTANCE : CwtConfigResolutionScope
}
