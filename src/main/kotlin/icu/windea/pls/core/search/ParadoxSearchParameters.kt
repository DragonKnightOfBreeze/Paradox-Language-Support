package icu.windea.pls.core.search

import com.intellij.openapi.project.Project
import icu.windea.pls.core.search.selectors.chained.*
import icu.windea.pls.core.selectors.chained.*

interface ParadoxSearchParameters<T> {
    val selector: ChainedParadoxSelector<T>
    
    val project: Project get() = selector.project
}