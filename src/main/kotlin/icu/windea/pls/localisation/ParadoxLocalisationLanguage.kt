package icu.windea.pls.localisation

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxLocalisationLanguage : ParadoxLanguage("PARADOX_LOCALISATION") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    override fun getDisplayName() = ChronicleBundle.message("localisation.language.displayName")

    // 3.1.0 from inspection: Serializable object must implement 'readResolve'
    @Suppress("unused")
    private fun readResolve(): Any = ParadoxLocalisationLanguage
}
