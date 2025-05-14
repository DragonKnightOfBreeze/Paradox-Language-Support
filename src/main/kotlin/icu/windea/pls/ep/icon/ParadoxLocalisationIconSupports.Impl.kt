package icu.windea.pls.ep.icon

import icu.windea.pls.core.*

class ParadoxBaseLocalisationIconSupport : CompositeParadoxLocalisationIconSupport() {
    init {
        fromDefinition("sprite", { it.addPrefix("GFX_text_") }, { it.removePrefixOrNull("GFX_text_") })
        fromDefinition("sprite", { it.addPrefix("GFX_") }, { it.removePrefixOrNull("GFX_") })
        fromImageFile("icon[gfx/interface/icons/]")
    }

    class Stellaris : CompositeParadoxLocalisationIconSupport() {
        init {
            fromDefinition("job", { it.removePrefixOrNull("job_") }, { it.addPrefix("job_") })
            fromDefinition("resource")
        }
    }
}
