package icu.windea.pls.core.collections

data class BlockEntry<out K, out V>(
	val key: K,
	val value: V
)