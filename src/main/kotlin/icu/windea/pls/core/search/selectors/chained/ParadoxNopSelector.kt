package icu.windea.pls.core.search.selectors.chained

import com.intellij.openapi.project.*

class ParadoxNopSelector<T>(project: Project) : ChainedParadoxSelector<T>(project)

fun <T> nopSelector(project: Project) = ParadoxNopSelector<T>(project)