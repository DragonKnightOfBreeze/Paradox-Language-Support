package icu.windea.pls.ep.overrides

import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.model.CwtOverrideConfig
import icu.windea.pls.model.ParadoxGameType

class ParadoxBaseOverrideStrategyProvider : ParadoxFilePathMapBasedOverrideStrategyProvider() {
    override fun getFilePathMap(gameType: ParadoxGameType): Map<String, CwtOverrideConfig> {
        val configGroup = ChronicleFacade.getConfigGroup(gameType)
        return configGroup.overrides
    }
}
