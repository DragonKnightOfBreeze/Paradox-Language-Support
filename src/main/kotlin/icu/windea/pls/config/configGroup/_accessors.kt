package icu.windea.pls.config.configGroup

import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKeyWithThis

val CwtConfigGroup.mockConfigModel: CwtConfigGroupMockConfigModel
    by registerKeyWithThis(CwtConfigGroup.Keys) { CwtConfigGroupMockConfigModel(this) }

val CwtConfigGroup.modificationTrackerModel: CwtConfigGroupModificationTrackerModel
    by registerKeyWithThis(CwtConfigGroup.Keys) { CwtConfigGroupModificationTrackerModel(this) }

