package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*

interface ParadoxLocalisationPropertyStub : StubElement<ParadoxLocalisationProperty> {
    val name: String
    val text: String?
    val category: ParadoxLocalisationCategory
    val locale: String?
    val gameType: ParadoxGameType?
    
    fun isValid() = name.isNotEmpty()
}

