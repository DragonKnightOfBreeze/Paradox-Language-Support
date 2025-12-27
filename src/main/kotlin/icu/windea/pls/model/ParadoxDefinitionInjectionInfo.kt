package icu.windea.pls.model

import icu.windea.pls.config.config.CwtValueConfig

data class ParadoxDefinitionInjectionInfo(
    val mode: String, // must be valid
    val target: String, // can be empty
    val modeConfig: CwtValueConfig
)
