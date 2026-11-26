package icu.windea.pls.csv

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxCsvLanguage : ParadoxLanguage("PARADOX_CSV") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    override fun getDisplayName() = ChronicleBundle.message("csv.language.displayName")

    // 3.1.0 from inspection: Serializable object must implement 'readResolve'
    @Suppress("unused")
    private fun readResolve(): Any = ParadoxCsvLanguage
}
