package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*

interface ParadoxScriptDefinitionElementStub<T : ParadoxScriptDefinitionElement> : StubElement<T> {
	val name: String
	val type: String
	//val subtypes: List<String>
	val rootKey: String
	val elementPath: ParadoxElementPath
	val gameType: ParadoxGameType?
	
	fun isValid() = name.isNotEmpty() && type.isNotEmpty()
}
