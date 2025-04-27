package icu.windea.pls.script

import icu.windea.pls.lang.*

class ParadoxScriptLanguage : ParadoxBaseLanguage("PARADOX_SCRIPT") {
    override fun getDisplayName() = "Paradox Script"

    override fun getBaseLanguage() = ParadoxBaseLanguage.INSTANCE

    companion object {
        @JvmField
        val INSTANCE = ParadoxScriptLanguage()
    }
}
