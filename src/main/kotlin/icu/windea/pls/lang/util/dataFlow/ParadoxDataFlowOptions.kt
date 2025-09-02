package icu.windea.pls.lang.util.dataFlow

interface ParadoxDataFlowOptions {
    data class Base(
        override var forward: Boolean = true
    ) : BidirectionalDataFlowOptions

    /**
     * @property conditional 如果包含参数条件块，是否需要处理其中的子节点。
     * @property inline 如果包含内联脚本使用，是否需要先进行内联。
     */
    data class Member(
        override var forward: Boolean = true,
        var conditional: Boolean = false,
        var inline: Boolean = false,
    ) : BidirectionalDataFlowOptions

    data class Localisation(
        override var forward: Boolean = true
    ) : BidirectionalDataFlowOptions
}

@JvmName("baseOptions")
fun <T> DataFlowSequence<T, ParadoxDataFlowOptions.Base>.options(
    forward: Boolean = true
): DataFlowSequence<T, ParadoxDataFlowOptions.Base> {
    options.forward = forward
    return this
}

@JvmName("memberOptions")
fun <T> DataFlowSequence<T, ParadoxDataFlowOptions.Member>.options(
    forward: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false,
): DataFlowSequence<T, ParadoxDataFlowOptions.Member> {
    options.forward = forward
    options.conditional = conditional
    options.inline = inline
    return this
}

@JvmName("localisationOptions")
fun <T> DataFlowSequence<T, ParadoxDataFlowOptions.Localisation>.options(
    forward: Boolean = true
): DataFlowSequence<T, ParadoxDataFlowOptions.Localisation> {
    options.forward = forward
    return this
}
