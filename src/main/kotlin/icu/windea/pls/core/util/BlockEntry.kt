package icu.windea.pls.core.util

data class BlockEntry<out K, out V>(
	val key: K,
	val value: V
)