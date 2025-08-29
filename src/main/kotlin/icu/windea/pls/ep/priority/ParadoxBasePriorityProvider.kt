package icu.windea.pls.ep.priority

import icu.windea.pls.PlsFacade
import icu.windea.pls.config.configGroup.priorities
import icu.windea.pls.model.ParadoxGameType

class ParadoxBasePriorityProvider : FilePathBasedParadoxPriorityProvider() {
    override fun getFilePathMap(gameType: ParadoxGameType): Map<String, ParadoxPriority> {
        val configGroup = PlsFacade.getConfigGroup(gameType)
        return configGroup.priorities
    }
}
