package icu.windea.pls.lang.modifier.impl

import icu.windea.pls.lang.modifier.*

class ParadoxBaseModifierIconProvider : ParadoxModifierIconProvider {
    
    override fun addModifierIconPath(name: String, registry: MutableSet<String>) {
        //gfx/interface/icons/modifiers/mod_$.dds
        registry += "gfx/interface/icons/modifiers/mod_${name}.dds"
        //gfx/interface/icons/modifiers/mod_$.png
        registry += "gfx/interface/icons/modifiers/mod_${name}.png"
    }
}

