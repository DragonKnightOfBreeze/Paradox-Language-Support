package icu.windea.pls.ep.resolve.localisation

import icu.windea.pls.base.annotations.ForGameType
import icu.windea.pls.core.addPrefix
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.constants.ParadoxDefinitionTypes

class ParadoxBaseLocalisationIconSupport : ParadoxCompositeLocalisationIconSupport() {
    override fun registerSupports() {
        fromDefinition(ParadoxDefinitionTypes.sprite, { it.addPrefix("GFX_text_") }, { it.removePrefixOrNull("GFX_text_") })
        fromDefinition(ParadoxDefinitionTypes.sprite, { it.addPrefix("GFX_") }, { it.removePrefixOrNull("GFX_") })
        fromImageFile("icon[gfx/interface/icons/]")
    }

    @ForGameType(ParadoxGameType.Stellaris)
    class Stellaris : ParadoxCompositeLocalisationIconSupport() {
        override fun supports(gameType: ParadoxGameType) = gameType == ParadoxGameType.Stellaris

        override fun registerSupports() {
            fromDefinition(ParadoxDefinitionTypes.job, { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition(ParadoxDefinitionTypes.swappedJob, { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition(ParadoxDefinitionTypes.resource)
        }
    }
}
