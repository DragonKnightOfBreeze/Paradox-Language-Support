package com.windea.plugin.idea.paradox.localisation.psi.impl

import com.intellij.psi.stubs.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.localisation.psi.*

class ParadoxLocalisationPropertyStubImpl(
	parent: StubElement<*>,
	override val key: String
) : StubBase<ParadoxLocalisationProperty>(parent, ParadoxLocalisationStubElementTypes.PROPERTY), ParadoxLocalisationPropertyStub{
	override fun toString(): String {
		return "ParadoxLocalisationPropertyStub: $key"
	}
}

