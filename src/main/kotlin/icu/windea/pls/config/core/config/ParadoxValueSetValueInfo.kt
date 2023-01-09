package icu.windea.pls.config.core.config

data class ParadoxValueSetValueInfo(
	val name: String,
	val valueSetName: String,
	val gameType: ParadoxGameType?,
	val read: Boolean = true
	//TODO 保存作用域信息
)
