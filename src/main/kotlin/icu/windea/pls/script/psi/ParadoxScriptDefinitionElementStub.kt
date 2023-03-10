package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*

interface ParadoxScriptDefinitionElementStub<T : ParadoxScriptDefinitionElement> : StubElement<T> {
	val name: String?
	val type: String?
	//val subtypes: List<String>? //可能需要通过访问索引获取，不能在索引时就获取
	val rootKey: String?
	val elementPath: ParadoxElementPath
	val gameType: ParadoxGameType?
}
