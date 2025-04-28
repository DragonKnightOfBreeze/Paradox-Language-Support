package icu.windea.pls.lang

import com.intellij.lang.*

open class ParadoxBaseLanguage(ID: String) : Language(ID) {
    companion object General: ParadoxBaseLanguage("PARADOX")
}
