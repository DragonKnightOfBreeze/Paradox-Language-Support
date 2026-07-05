package icu.windea.pls.cwt

import com.intellij.lang.Language
import icu.windea.pls.ChronicleBundle

object CwtLanguage : Language("CWT") {
    override fun getDisplayName() = ChronicleBundle.message("cwt.language.name")
}
