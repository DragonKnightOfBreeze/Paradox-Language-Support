package icu.windea.pls.localisation.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.model.*

class ParadoxLocalisationStubImpl(
	parent: StubElement<*>,
	override val name: String? = null,
	override val category: ParadoxLocalisationCategory = ParadoxLocalisationCategory.Localisation
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationStub{
	override fun toString(): String {
		return "ParadoxLocalisationStub(key=$name, category=$category)"
	}
}