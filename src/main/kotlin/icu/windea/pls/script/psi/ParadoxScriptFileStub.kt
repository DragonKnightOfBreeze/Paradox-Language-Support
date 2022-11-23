package icu.windea.pls.script.psi

import icu.windea.pls.core.psi.*

interface ParadoxScriptFileStub : ParadoxDefinitionPropertyStub<ParadoxScriptFile> {
	override val rootKey: String? get() = null
}
