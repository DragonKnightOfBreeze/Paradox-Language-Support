package icu.windea.pls.ep.overrides

import icu.windea.pls.ChronicleFacade
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy

class ParadoxBaseOverrideStrategyProvider : ParadoxFilePathMapBasedOverrideStrategyProvider() {
    override fun getFilePathMap(gameType: ParadoxGameType): Map<String, ParadoxOverrideStrategy> {
        val configGroup = ChronicleFacade.getConfigGroup(gameType)
        return configGroup.priorities
    }
}
