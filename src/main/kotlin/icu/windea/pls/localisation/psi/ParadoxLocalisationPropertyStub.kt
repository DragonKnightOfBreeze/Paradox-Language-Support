package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.core.*

interface ParadoxLocalisationPropertyStub: StubElement<ParadoxLocalisationProperty> {
	val name:String
	val category: ParadoxLocalisationCategory
}

