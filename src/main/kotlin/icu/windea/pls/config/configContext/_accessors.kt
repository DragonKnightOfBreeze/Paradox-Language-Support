package icu.windea.pls.config.configContext

import icu.windea.pls.core.util.createKey
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.setValue
import icu.windea.pls.model.ParadoxDefinitionInfo
import icu.windea.pls.model.paths.ParadoxElementPath

// region CwtConfigContext Accessors

var CwtConfigContext.definitionInfo: ParadoxDefinitionInfo? by createKey(CwtConfigContext.Keys)
var CwtConfigContext.elementPathFromRoot: ParadoxElementPath? by createKey(CwtConfigContext.Keys)

// endregion
