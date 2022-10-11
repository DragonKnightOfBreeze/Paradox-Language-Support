package icu.windea.pls.core.model

import java.util.*

data class ParadoxLocalisationInfo(
	val name:String,
	val category: ParadoxLocalisationCategory,
	val gameType: ParadoxGameType?
){
	override fun equals(other: Any?): Boolean {
		return this === other || other is ParadoxLocalisationInfo
			&& name == other.name && category == other.category && gameType == other.gameType
	}
	
	override fun hashCode(): Int {
		return Objects.hash(name, category, gameType)
	}
}