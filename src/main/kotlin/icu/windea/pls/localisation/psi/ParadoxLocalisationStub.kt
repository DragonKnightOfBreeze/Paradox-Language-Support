package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxLocalisationStub: StubElement<ParadoxLocalisationProperty> {
	val name:String?
	val category: ParadoxLocalisationCategory
	val gameType: ParadoxGameType
}

