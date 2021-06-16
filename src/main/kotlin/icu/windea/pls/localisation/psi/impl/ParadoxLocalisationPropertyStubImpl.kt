package icu.windea.pls.localisation.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

class ParadoxLocalisationPropertyStubImpl(
	parent: StubElement<*>,
	override val name: String,
	override val category: ParadoxLocalisationCategory
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationPropertyStub{
	override fun toString(): String {
		return "ParadoxLocalisationPropertyStub(key=$name, category=$category)"
	}
}