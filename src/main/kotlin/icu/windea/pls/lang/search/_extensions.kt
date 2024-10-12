package icu.windea.pls.lang.search

import com.intellij.util.*

fun <R : Any, P : ParadoxSearchParameters<R>> QueryFactory<R, P>.createParadoxQuery(parameters: P): ParadoxQuery<R, P> {
    return ParadoxQuery(createQuery(parameters), parameters)
}

fun <T> ParadoxQuery<T, *>.processQuery(onlyMostRelevant: Boolean, consumer: Processor<in T>): Boolean {
    if(onlyMostRelevant) {
        find()?.let { consumer.process(it) }
        return true
    }
    return this.forEach(consumer)
}

fun <T> ParadoxQuery<T, *>.processQueryAsync(onlyMostRelevant: Boolean, consumer: Processor<in T>): Boolean {
    if(onlyMostRelevant) {
        find()?.let { consumer.process(it) }
        return true
    }
    return allowParallelProcessing().forEach(consumer)
}
