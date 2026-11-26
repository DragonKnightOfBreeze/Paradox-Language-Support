package icu.windea.pls.script

import icu.windea.pls.ChronicleBundle
import icu.windea.pls.lang.ParadoxBaseLanguage
import icu.windea.pls.lang.ParadoxLanguage

object ParadoxScriptLanguage : ParadoxLanguage("PARADOX_SCRIPT") {
    override fun getBaseLanguage() = ParadoxBaseLanguage

    override fun getDisplayName() = ChronicleBundle.message("script.language.displayName")

    // 3.1.0 from inspection: Serializable object must implement 'readResolve'
    @Suppress("unused")
    private fun readResolve(): Any = ParadoxScriptLanguage
}
