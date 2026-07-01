package icu.windea.pls.localisation

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxLocalisationLanguage : ParadoxLanguage("PARADOX_LOCALISATION") {
    override fun getBaseLanguage() = ParadoxLanguage

    override fun getDisplayName() = ChronicleBundle.message("localisation.language.name")
}
