package icu.windea.pls.localisation

import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxLocalisationLanguage : ParadoxLanguage("PARADOX_LOCALISATION") {
    override fun getBaseLanguage() = ParadoxLanguage

    override fun getDisplayName() = PlsBundle.message("language.name.localisation")
}
