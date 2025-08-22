package icu.windea.pls.lang.util.dataFlow

/**
 * @property forward 是否从前往后搜索。
 */
interface BidirectionalDataFlowOptions: DataFlowOptions {
    val forward: Boolean
}
