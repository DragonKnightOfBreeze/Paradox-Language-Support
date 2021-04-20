package com.windea.plugin.idea.paradox.localisation.psi

import com.intellij.psi.stubs.*
import com.windea.plugin.idea.paradox.*
import com.windea.plugin.idea.paradox.model.*

interface ParadoxLocalisationPropertyStub: StubElement<ParadoxLocalisationProperty> {
	val key:String
	val paradoxLocale:ParadoxLocale? //TODO 目没有必要真正实现
}

