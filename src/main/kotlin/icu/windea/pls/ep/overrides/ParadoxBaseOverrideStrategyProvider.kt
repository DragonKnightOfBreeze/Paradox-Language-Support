package icu.windea.pls.ep.overrides

import icu.windea.pls.PlsFacade
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.model.ParadoxGameType

class ParadoxBaseOverrideStrategyProvider : ParadoxFilePathMapBasedOverrideStrategyProvider() {
    override fun getFilePathMap(gameType: ParadoxGameType): Map<String, ParadoxOverrideStrategy> {
        val configGroup = PlsFacade.getConfigGroup(gameType)
        return configGroup.priorities
    }
}
