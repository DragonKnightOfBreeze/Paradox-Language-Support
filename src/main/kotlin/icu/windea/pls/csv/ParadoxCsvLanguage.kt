package icu.windea.pls.csv

import icu.windea.pls.*
import icu.windea.pls.lang.*

object ParadoxCsvLanguage : ParadoxBaseLanguage("PARADOX_CSV") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    override fun getDisplayName() = PlsBundle.message("language.name.csv")
}
