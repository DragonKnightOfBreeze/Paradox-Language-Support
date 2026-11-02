package icu.windea.pls.integrations.lints

import icu.windea.pls.model.ParadoxGameType

interface LintRequest {
    val name: String
    val gameType: ParadoxGameType
}
