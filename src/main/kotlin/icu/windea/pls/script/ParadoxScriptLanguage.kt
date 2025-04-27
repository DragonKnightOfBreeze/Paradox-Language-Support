package icu.windea.pls.script

import icu.windea.pls.lang.*

class ParadoxScriptLanguage : ParadoxBaseLanguage("PARADOX_SCRIPT") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    companion object {
        @JvmField
        val INSTANCE = ParadoxScriptLanguage()
    }
}
