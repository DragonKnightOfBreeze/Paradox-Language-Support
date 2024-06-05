package icu.windea.pls.ep.priority

import icu.windea.pls.config.configGroup.*
import icu.windea.pls.lang.*
import icu.windea.pls.model.*

class BaseFilePathBasedParadoxPriorityProvider: FilePathBasedParadoxPriorityProvider() {
    override fun getFilePathMap(gameType: ParadoxGameType): Map<String, ParadoxPriority> {
        val configGroup = getConfigGroup(gameType)
        return configGroup.priorities
    }
}
