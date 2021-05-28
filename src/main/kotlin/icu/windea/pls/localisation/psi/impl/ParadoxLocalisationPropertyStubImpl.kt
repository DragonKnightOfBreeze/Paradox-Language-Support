package icu.windea.pls.localisation.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationPropertyStubImpl(
	parent: StubElement<*>,
	override val key: String,
	override val paradoxLocale: ParadoxLocale? = null
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationPropertyStub{
	override fun toString(): String {
		return "ParadoxLocalisationPropertyStub: (key=$key,paradoxLocale=$paradoxLocale)"
	}
}

