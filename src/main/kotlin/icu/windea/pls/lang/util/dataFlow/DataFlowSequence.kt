package icu.windea.pls.lang.util.dataFlow

/**
 * 数据流的序列。可以附带自定义的选项 [options]。
 */
open class DataFlowSequence<T, O : DataFlowOptions>(
    private val delegate: Sequence<T> = emptySequence(),
    val options: O
) : Sequence<T> by delegate
