package icu.windea.pls.lang.util.dataFlow

inline fun <T, R, O : DataFlowOptions> DataFlowSequence<T, O>.transform(block: Sequence<T>.() -> Sequence<R>): DataFlowSequence<R, O> {
    return DataFlowSequence(block(this), options)
}

inline fun <T, O : DataFlowOptions> DataFlowSequence<T, O>.options(block: O.() -> Unit): DataFlowSequence<T, O> {
    return apply { options?.block() }
}
