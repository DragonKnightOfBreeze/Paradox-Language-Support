package icu.windea.pls.localisation

import icu.windea.pls.*
import icu.windea.pls.lang.*

object ParadoxLocalisationLanguage : ParadoxBaseLanguage("PARADOX_LOCALISATION") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    override fun getDisplayName() = PlsBundle.message("language.name.localisation")
}
