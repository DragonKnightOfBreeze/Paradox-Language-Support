package icu.windea.pls.config.core.config

import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.expression.*

data class ParadoxModifierInfo(
	val name: String,
	val gameType: ParadoxGameType,
	val modifierConfig: CwtModifierConfig?,
	val generatedModifierConfig: CwtModifierConfig?,
	val templateExpression: ParadoxTemplateExpression?
)