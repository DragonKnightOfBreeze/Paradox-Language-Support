package icu.windea.pls.lang.model

import icu.windea.pls.config.cwt.config.*

data class ParadoxLocalisationInfo(
	val name: String,
	val category: ParadoxLocalisationCategory,
	val gameType: ParadoxGameType?
)