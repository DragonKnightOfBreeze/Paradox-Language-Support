package icu.windea.pls.integrations.lints

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.model.ParadoxGameType

/**
 * 提供检查工具。用于驱动额外的代码检查。
 */
interface LintToolProvider {
    fun isAvailable(gameType: ParadoxGameType?): Boolean

    suspend fun validateRootDirectory(request: LintRequest): LintResult

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<LintToolProvider>("icu.windea.pls.integrations.lintToolProvider")
    }
}
