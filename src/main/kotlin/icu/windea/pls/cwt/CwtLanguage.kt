package icu.windea.pls.cwt

import com.intellij.lang.Language
import icu.windea.pls.ChronicleBundle

object CwtLanguage : Language("CWT") {
    override fun getDisplayName() = ChronicleBundle.message("cwt.language.name")

    // 3.1.0 from inspection: Serializable object must implement 'readResolve'
    @Suppress("unused")
    private fun readResolve(): Any = CwtLanguage
}
