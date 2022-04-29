package icu.windea.pls.core

/**
 * @property location 即类型为sprite的定义的键（definitionKey），或者相对于游戏或模组根路径的路径（filepath）。
 */
data class ParadoxRelatedPicturesInfo(
	val name: String,
	val location: String,
	val required: Boolean = false,
	val primary: Boolean = false
)