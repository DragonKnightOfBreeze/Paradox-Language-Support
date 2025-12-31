package icu.windea.pls.csv

import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxCsvLanguage : ParadoxLanguage("PARADOX_CSV") {
    override fun getBaseLanguage() = ParadoxLanguage

    override fun getDisplayName() = PlsBundle.message("language.name.csv")
}
