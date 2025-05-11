package icu.windea.pls.ep.icon

import icu.windea.pls.core.*

class ParadoxBaseLocalisationIconSupport : CompositeParadoxLocalisationIconSupport() {
    init {
        fromDefinition("sprite") { "GFX_text_$it" }
        fromDefinition("sprite") { "GFX_$it" }
        fromImageFile("icon[gfx/interface/icons/]")
    }

    class Stellaris : CompositeParadoxLocalisationIconSupport() {
        init {
            fromDefinition("job") { it.removePrefixOrNull("job_") }
            fromDefinition("resource")
        }
    }
}
