package icu.windea.pls.config.configGroup

import icu.windea.pls.config.config.*
import icu.windea.pls.config.configContext.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*

@Tags(Tag.Computed)
val CwtConfigGroup.mockVariableConfig: CwtValueConfig
    by createKey(CwtConfigGroup.Keys) {
        CwtValueConfig.resolve(emptyPointer(), this, "value[variable]")
    }

@Tags(Tag.Computed)
val CwtConfigGroup.mockEventTargetConfig: CwtValueConfig
    by createKey(CwtConfigGroup.Keys) {
        CwtValueConfig.resolve(emptyPointer(), this, "value[event_target]")
    }

@Tags(Tag.Computed)
val CwtConfigGroup.mockGlobalEventTargetConfig: CwtValueConfig
    by createKey(CwtConfigGroup.Keys) {
        CwtValueConfig.resolve(emptyPointer(), this, "value[global_event_target]")
    }
