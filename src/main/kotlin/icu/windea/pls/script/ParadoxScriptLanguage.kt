package icu.windea.pls.script

import icu.windea.pls.*
import icu.windea.pls.lang.*

object ParadoxScriptLanguage : ParadoxBaseLanguage("PARADOX_SCRIPT") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    override fun getDisplayName() = PlsBundle.message("language.name.script")
}
