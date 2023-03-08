package icu.windea.pls.core.search

import com.intellij.openapi.project.*
import icu.windea.pls.core.search.selectors.chained.*

interface ParadoxSearchParameters<T> {
    val selector: ChainedParadoxSelector<T>
    
    val project: Project get() = selector.project
}