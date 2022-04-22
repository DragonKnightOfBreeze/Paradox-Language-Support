package icu.windea.pls.localisation.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.config.internal.config.*
import icu.windea.pls.core.*
import icu.windea.pls.localisation.psi.*

class ParadoxLocalisationStubImpl(
	parent: StubElement<*>,
	override val name: String,
	override val category: ParadoxLocalisationCategory
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationStub{
	override fun toString(): String {
		return "ParadoxLocalisationStub(key=$name, category=$category)"
	}
}