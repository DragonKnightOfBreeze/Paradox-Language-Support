package icu.windea.pls.lang

import com.intellij.lang.Language

open class ParadoxBaseLanguage(id: String) : Language(id) {
    companion object General: ParadoxBaseLanguage("PARADOX")
}
