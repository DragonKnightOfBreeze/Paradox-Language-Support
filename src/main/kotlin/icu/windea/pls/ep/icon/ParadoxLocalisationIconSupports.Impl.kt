package icu.windea.pls.ep.icon

import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*

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
            fromDefinition(ParadoxDefinitionTypes.Resource)
        }
    }
}
