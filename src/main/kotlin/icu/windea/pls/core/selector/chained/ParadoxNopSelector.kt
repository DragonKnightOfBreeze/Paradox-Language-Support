package icu.windea.pls.core.selector.chained

class ParadoxNopSelector<T>: ChainedParadoxSelector<T>()

fun <T> nopSelector() = ParadoxNopSelector<T>()