package icu.windea.pls.lang.search

import com.intellij.openapi.project.*
import icu.windea.pls.lang.search.selector.*

interface ParadoxSearchParameters<T> {
    val selector: ChainedParadoxSelector<T>

    val project: Project get() = selector.project
}
