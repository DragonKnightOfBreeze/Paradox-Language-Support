package icu.windea.pls.integrations.lints

import com.intellij.openapi.util.ModificationTracker
import icu.windea.pls.model.ParadoxGameType

interface LintResult {
    val request: LintRequest
    val error: Exception?
    val modificationTracker: ModificationTracker?

    val name: String get() = request.name
    val gameType: ParadoxGameType get() = request.gameType
}
