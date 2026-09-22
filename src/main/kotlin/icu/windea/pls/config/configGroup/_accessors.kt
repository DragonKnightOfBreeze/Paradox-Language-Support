package icu.windea.pls.config.configGroup

import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKeyWithThis

val CwtConfigGroup.mockConfigs: CwtConfigGroupMockConfigs
    by registerKeyWithThis(CwtConfigGroup.Keys) { CwtConfigGroupMockConfigs(this) }

val CwtConfigGroup.modificationTrackers: CwtConfigGroupModificationTrackers
    by registerKeyWithThis(CwtConfigGroup.Keys) { CwtConfigGroupModificationTrackers(this) }
