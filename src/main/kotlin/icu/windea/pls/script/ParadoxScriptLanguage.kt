package icu.windea.pls.script

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxScriptLanguage : ParadoxLanguage("PARADOX_SCRIPT") {
    override fun getBaseLanguage() = ParadoxLanguage

    override fun getDisplayName() = ChronicleBundle.message("script.language.name")
}
