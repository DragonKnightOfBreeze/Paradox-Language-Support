package com.windea.plugin.idea.pls.localisation.psi

import com.intellij.psi.stubs.*
import com.windea.plugin.idea.pls.model.*

interface ParadoxLocalisationPropertyStub: StubElement<ParadoxLocalisationProperty> {
	val key:String
	val paradoxLocale:ParadoxLocale? //TODO 目没有必要真正实现
}

