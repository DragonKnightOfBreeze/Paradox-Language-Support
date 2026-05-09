package icu.windea.pls.ep.resolve.localisation

import icu.windea.pls.core.addPrefix
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.lang.annotations.WithGameType
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

class ParadoxBaseLocalisationIconSupport : ParadoxCompositeLocalisationIconSupport() {
    init {
        fromDefinition(ParadoxDefinitionTypes.sprite, { it.addPrefix("GFX_text_") }, { it.removePrefixOrNull("GFX_text_") })
        fromDefinition(ParadoxDefinitionTypes.sprite, { it.addPrefix("GFX_") }, { it.removePrefixOrNull("GFX_") })
        fromImageFile("icon[gfx/interface/icons/]")
    }

    @WithGameType(ParadoxGameType.Stellaris)
    class Stellaris : ParadoxCompositeLocalisationIconSupport() {
        init {
            fromDefinition(ParadoxDefinitionTypes.job, { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition(ParadoxDefinitionTypes.swappedJob, { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition(ParadoxDefinitionTypes.resource)
        }
    }
}
