package icu.windea.pls.localisation

import icu.windea.pls.lang.*

class ParadoxLocalisationLanguage : ParadoxBaseLanguage("PARADOX_LOCALISATION") {
    override fun getDisplayName() = "Paradox Localisation"

    override fun getBaseLanguage() = ParadoxBaseLanguage.INSTANCE

    companion object {
        @JvmField
        val INSTANCE = ParadoxLocalisationLanguage()
    }
}
