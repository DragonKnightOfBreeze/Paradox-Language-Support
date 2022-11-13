package icu.windea.pls.core.search

import icu.windea.pls.core.selector.*

interface ParadoxSearchParameters<T> {
	val selector: ChainedParadoxSelector<T>
}