package icu.windea.pls.lang.util.dataFlow

/**
 * 数据流的序列。可以附带自定义的选项 [options]。
 */
class DataFlowSequence<T, O : DataFlowOptions>(
    private val delegate: Sequence<T> = emptySequence(),
    var options: O? = null
) : Sequence<T> by delegate
