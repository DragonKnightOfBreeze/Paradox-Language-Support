package icu.windea.pls.core.search

import icu.windea.pls.core.selector.chained.*

interface ParadoxSearchParameters<T> {
	val selector: ChainedParadoxSelector<T>
}