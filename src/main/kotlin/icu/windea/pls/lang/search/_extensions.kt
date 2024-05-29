package icu.windea.pls.lang.search

import com.intellij.util.*

fun <R : Any, P : ParadoxSearchParameters<R>> QueryFactory<R, P>.createParadoxQuery(parameters: P): ParadoxQuery<R, P> {
    return ParadoxQuery(createQuery(parameters), parameters)
}