package icu.windea.pls.integrations.lints.tools

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.integrations.lints.*
import icu.windea.pls.model.*

/**
 * 提供检查工具。用于提供额外的代码检查。
 */
interface PlsLintToolProvider {
    fun isAvailable(gameType: ParadoxGameType?) = isEnabled() && isSupported(gameType) && isValid()

    fun isEnabled(): Boolean

    fun isSupported(gameType: ParadoxGameType?): Boolean

    fun isValid(): Boolean

    fun validateFile(file: VirtualFile): PlsLintResult?

    fun validateRootDirectory(rootDirectory: VirtualFile): PlsLintResult?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsLintToolProvider>("icu.windea.pls.integrations.lintToolProvider")
    }
}

