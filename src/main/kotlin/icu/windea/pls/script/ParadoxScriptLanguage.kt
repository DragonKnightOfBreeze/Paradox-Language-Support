package icu.windea.pls.script

import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.ParadoxBaseLanguage

object ParadoxScriptLanguage : ParadoxBaseLanguage("PARADOX_SCRIPT") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    override fun getDisplayName() = PlsBundle.message("language.name.script")
}
