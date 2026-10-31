package icu.windea.pls.integrations.lints.providers

import com.intellij.execution.ExecutionException
import icu.windea.pls.integrations.lints.LintRequest
import icu.windea.pls.integrations.lints.LintResult
import icu.windea.pls.integrations.lints.LintToolProvider
import icu.windea.pls.model.ParadoxGameType
import java.nio.file.Path
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeoutException

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

    @Throws(UnsupportedOperationException::class, ExecutionException::class, CancellationException::class, TimeoutException::class)
    abstract override suspend fun validateRootDirectory(request: LintRequest): LintResult
}
