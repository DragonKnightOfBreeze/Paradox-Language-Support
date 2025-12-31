package icu.windea.pls.script

import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxScriptLanguage : ParadoxLanguage("PARADOX_SCRIPT") {
    override fun getBaseLanguage() = ParadoxLanguage

    override fun getDisplayName() = PlsBundle.message("language.name.script")
}
