package icu.windea.pls.config.definition.config

import com.intellij.psi.*
import icu.windea.pls.core.model.*
import icu.windea.pls.script.psi.*

interface ParadoxDefinitionConfig{
	val name: String
	val gameType: ParadoxGameType
	val pointer: SmartPsiElementPointer<out ParadoxDefinitionProperty>
}