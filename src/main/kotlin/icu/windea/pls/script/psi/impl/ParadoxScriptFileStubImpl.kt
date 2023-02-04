package icu.windea.pls.script.psi.impl

import com.intellij.psi.stubs.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.script.psi.*

class ParadoxScriptFileStubImpl(
	file: ParadoxScriptFile?,
	override val name: String?,
	override val type: String?,
	override val subtypes: List<String>?,
	override val gameType: ParadoxGameType?
) : PsiFileStubImpl<ParadoxScriptFile>(file), ParadoxScriptFileStub {
	override fun toString(): String {
		return "ParadoxScriptFileStub(name=$name, type=$type, subtypes=$subtypes, gameType=$gameType)"
	}
}