package icu.windea.pls.localisation.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*

interface ParadoxLocalisationPropertyStub: StubElement<ParadoxLocalisationProperty> {
	val key:String
	val paradoxLocale:ParadoxLocale? //TODO 目前没有必要实现
}

