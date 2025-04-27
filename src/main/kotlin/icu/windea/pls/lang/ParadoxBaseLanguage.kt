package icu.windea.pls.lang

import com.intellij.lang.*

open class ParadoxBaseLanguage(ID: String) : Language(ID), DependentLanguage {
    companion object {
        @JvmField
        val INSTANCE = ParadoxBaseLanguage("Paradox")
    }
}
