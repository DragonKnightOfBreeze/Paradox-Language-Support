package icu.windea.pls.lang.search

import com.intellij.openapi.project.Project
import com.intellij.psi.search.GlobalSearchScope
import icu.windea.pls.lang.search.selector.ChainedParadoxSelector

interface ParadoxSearchParameters<T> {
    val selector: ChainedParadoxSelector<T>

    val project: Project get() = selector.project
    val scope: GlobalSearchScope get() = selector.scope
}
