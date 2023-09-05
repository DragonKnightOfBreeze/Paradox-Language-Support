package icu.windea.pls.lang.modifier.impl

import icu.windea.pls.lang.modifier.*

class ParadoxBaseModifierNameDescProvider : ParadoxModifierNameDescProvider {
    override fun addModifierNameKey(name: String, registry: MutableSet<String>) {
        //mod_$, ALL_UPPER_CASE is ok.
        registry += "mod_${name}"
    }
    
    override fun addModifierDescKey(name: String, registry: MutableSet<String>) {
        //mod_$_desc, ALL_UPPER_CASE is ok.
        registry += "mod_${name}_desc"
    }
}