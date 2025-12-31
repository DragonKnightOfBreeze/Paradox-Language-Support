package icu.windea.pls.lang

import com.intellij.lang.Language

open class ParadoxLanguage(id: String) : Language(id) {
    companion object General: ParadoxLanguage("PARADOX")
}
