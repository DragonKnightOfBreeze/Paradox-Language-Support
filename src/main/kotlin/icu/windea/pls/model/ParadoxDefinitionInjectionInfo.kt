package icu.windea.pls.model

import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.CwtTypeConfig

data class ParadoxDefinitionInjectionInfo(
    val mode: String, // must be valid
    val target: String, // can be empty
    val type: String, // can be empty
    val modeConfig: CwtValueConfig,
    val typeConfig: CwtTypeConfig?,
    val gameType: ParadoxGameType,
)
