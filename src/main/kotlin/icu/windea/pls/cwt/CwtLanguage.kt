package icu.windea.pls.cwt

import com.intellij.lang.*

class CwtLanguage : Language("CWT") {
    override fun getDisplayName() = "Cwt"

    companion object {
        @JvmField
        val INSTANCE = CwtLanguage()
    }
}

