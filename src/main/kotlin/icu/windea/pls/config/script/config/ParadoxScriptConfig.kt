package icu.windea.pls.config.script.config

import com.intellij.psi.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

interface ParadoxScriptConfig{
	val name: String
	val gameType: ParadoxGameType
	val pointer: SmartPsiElementPointer<out ParadoxScriptDefinitionElement>
}
