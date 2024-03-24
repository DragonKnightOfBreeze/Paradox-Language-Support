package icu.windea.pls.script.psi

import com.intellij.psi.stubs.*
import icu.windea.pls.model.*
import icu.windea.pls.model.path.*

interface ParadoxScriptDefinitionElementStub<T : ParadoxScriptDefinitionElement> : StubElement<T> {
	val name: String
	val type: String
	val subtypes: List<String>? //null -> 无法在索引时获取（需要访问定义索引）
	val rootKey: String
	val elementPath: ParadoxElementPath
	val gameType: ParadoxGameType
	
	val isValidDefinition: Boolean
	val nestedTypeRootKeys: Set<String>
}
