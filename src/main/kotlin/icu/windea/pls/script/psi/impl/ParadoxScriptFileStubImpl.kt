package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFileStubImpl(
	file: ParadoxScriptFile?,
	override val name: String? = null,
	override val type: String? = null,
	override val subtypes: List<String>? = null
) : PsiFileStubImpl<ParadoxScriptFile>(file), ParadoxScriptFileStub {
	override fun toString(): String {
		return "ParadoxScriptFileStub(name=$name, type=$type, subtypes=$subtypes)"
	}
}