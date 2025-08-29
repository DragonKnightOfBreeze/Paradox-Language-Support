package icu.windea.pls.integrations.lints.tools

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.integrations.lints.PlsLintResult
import icu.windea.pls.model.ParadoxGameType

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

