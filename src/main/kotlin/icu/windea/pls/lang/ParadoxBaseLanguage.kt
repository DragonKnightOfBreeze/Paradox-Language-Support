package icu.windea.pls.lang

import com.intellij.lang.*
import icu.windea.pls.model.*

open class ParadoxBaseLanguage(ID: String, val gameType: ParadoxGameType? = null) : Language(ID) {
    companion object General : ParadoxBaseLanguage("PARADOX")
}
