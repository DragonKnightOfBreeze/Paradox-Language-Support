package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxLocalisationPropertyStub: StubElement<ParadoxLocalisationProperty> {
	val name:String
	val category:ParadoxLocalisationCategory
}

