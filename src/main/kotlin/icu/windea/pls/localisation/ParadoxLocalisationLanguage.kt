package icu.windea.pls.localisation

import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.ParadoxBaseLanguage

object ParadoxLocalisationLanguage : ParadoxBaseLanguage("PARADOX_LOCALISATION") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    override fun getDisplayName() = PlsBundle.message("language.name.localisation")
}
