package icu.windea.pls.csv

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxCsvLanguage : ParadoxLanguage("PARADOX_CSV") {
    override fun getBaseLanguage() = ParadoxLanguage

    override fun getDisplayName() = ChronicleBundle.message("csv.language.name")
}
