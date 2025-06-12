package icu.windea.pls.integrations.lint.tools

import com.intellij.openapi.extensions.*
import com.intellij.openapi.vfs.*
import icu.windea.pls.integrations.lint.*
import icu.windea.pls.model.ParadoxGameType

/**
 * 提供检查工具。用于提供额外的代码检查。
 *
 * 注意：具体的操作方法可能不会再次验证工具是否可用。
 */
interface PlsLintToolProvider {
    fun isEnabled(): Boolean

    fun isSupported(gameType: ParadoxGameType?): Boolean

    fun isValid(): Boolean

    fun validateFile(file: VirtualFile): PlsLintResult?

    fun validateRootDirectory(rootDirectory: VirtualFile): PlsLintResult?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<PlsLintToolProvider>("icu.windea.pls.integrations.lintToolProvider")
    }
}

