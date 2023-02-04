package icu.windea.pls.lang.model

data class ParadoxValueSetValueInfo(
	val name: String,
	val valueSetName: String,
	val gameType: ParadoxGameType?,
	val read: Boolean = true
	//TODO 保存作用域信息
)
