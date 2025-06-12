package icu.windea.pls.integrations.lint

import icu.windea.pls.core.collections.optimized
import icu.windea.pls.integrations.lint.tools.*
import icu.windea.pls.model.*

object PlsLintManager {
    fun findTools(gameType: ParadoxGameType?): List<PlsLintToolProvider> {
        return PlsLintToolProvider.EP_NAME.extensions.filter { it.isEnabled() && it.isSupported(gameType) && it.isValid() }.optimized()
    }
}
