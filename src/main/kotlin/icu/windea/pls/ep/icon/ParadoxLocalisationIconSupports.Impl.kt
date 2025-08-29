package icu.windea.pls.ep.icon

import icu.windea.pls.core.addPrefix
import icu.windea.pls.core.annotations.WithGameType
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

class ParadoxBaseLocalisationIconSupport : CompositeParadoxLocalisationIconSupport() {
    init {
        fromDefinition(ParadoxDefinitionTypes.Sprite, { it.addPrefix("GFX_text_") }, { it.removePrefixOrNull("GFX_text_") })
        fromDefinition(ParadoxDefinitionTypes.Sprite, { it.addPrefix("GFX_") }, { it.removePrefixOrNull("GFX_") })
        fromImageFile("icon[gfx/interface/icons/]")
    }

    @WithGameType(ParadoxGameType.Stellaris)
    class Stellaris : CompositeParadoxLocalisationIconSupport() {
        init {
            fromDefinition(ParadoxDefinitionTypes.Job, { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition(ParadoxDefinitionTypes.SwappedJob, { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition(ParadoxDefinitionTypes.Resource)
        }
    }
}
