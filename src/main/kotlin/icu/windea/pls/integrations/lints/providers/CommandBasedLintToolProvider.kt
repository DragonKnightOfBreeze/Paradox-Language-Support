package icu.windea.pls.integrations.lints.providers

import icu.windea.pls.integrations.lints.LintToolProvider
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path

abstract class CommandBasedLintToolProvider : LintToolProvider {
    final override fun isAvailable(gameType: ParadoxGameType?): Boolean {
        return isEnabled() && isSupported(gameType) && isValid()
    }

    abstract fun isEnabled(): Boolean

    abstract fun isSupported(gameType: ParadoxGameType?): Boolean

    open fun isValid(): Boolean {
        val executablePath = getExecutablePath() ?: return false
        return validateExecutablePath(executablePath)
    }

    abstract fun getExecutablePath(): Path?

    abstract fun validateExecutablePath(path: Path): Boolean
}
