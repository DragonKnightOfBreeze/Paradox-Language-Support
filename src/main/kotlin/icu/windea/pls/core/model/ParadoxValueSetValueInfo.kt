package icu.windea.pls.core.model

data class ParadoxValueSetValueInfo(
	val name: String,
	val valueSetName: String,
	val gameType: ParadoxGameType?,
	val read: Boolean = true
	//TODO 保存作用域信息
)
