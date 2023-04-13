package icu.windea.pls.core.util

data class Entry<out K, out V>(
	val key: K,
	val value: V
)