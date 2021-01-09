package com.windea.plugin.idea.paradox.localisation.psi

import com.intellij.psi.stubs.*

interface ParadoxLocalisationPropertyStub: StubElement<ParadoxLocalisationProperty> {
	val key:String
}

