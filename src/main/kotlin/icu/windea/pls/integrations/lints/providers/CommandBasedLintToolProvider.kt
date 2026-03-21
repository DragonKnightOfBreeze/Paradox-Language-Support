package icu.windea.pls.integrations.lints.providers

import icu.windea.pls.integrations.lints.LintToolProvider
import icu.windea.pls.model.ParadoxGameType

abstract class CommandBasedLintToolProvider : LintToolProvider {
    final override fun isAvailable(gameType: ParadoxGameType?) = isEnabled() && isSupported(gameType) && isValid()

    abstract fun isEnabled(): Boolean

    abstract fun isSupported(gameType: ParadoxGameType?): Boolean

    abstract fun isValid(): Boolean

    abstract fun isValidExePath(path: String): Boolean
}
