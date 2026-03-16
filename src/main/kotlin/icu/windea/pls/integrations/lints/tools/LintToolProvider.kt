package icu.windea.pls.integrations.lints.tools

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.vfs.VirtualFile
import icu.windea.pls.integrations.lints.LintResult
import icu.windea.pls.model.ParadoxGameType

/**
 * 提供检查工具。用于驱动额外的代码检查。
 */
interface LintToolProvider {
    fun isAvailable(gameType: ParadoxGameType?) = isEnabled() && isSupported(gameType) && isValid()

    fun isEnabled(): Boolean

    fun isSupported(gameType: ParadoxGameType?): Boolean

    fun isValid(): Boolean

    fun validateFile(file: VirtualFile): LintResult?

    fun validateRootDirectory(rootDirectory: VirtualFile): LintResult?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<LintToolProvider>("icu.windea.pls.integrations.lintToolProvider")
    }
}

