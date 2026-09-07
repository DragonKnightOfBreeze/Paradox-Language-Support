package icu.windea.pls.lang.match

import icu.windea.pls.core.hasState
import icu.windea.pls.lang.index.ParadoxMergedIndexThreadContext
import kotlin.experimental.or

/**
 * 匹配选项。
 *
 * 对于某些特殊场景，需要考虑采用更合适的匹配选项/匹配策略，例如使用宽松匹配，或者直接跳过基于索引的匹配，等等。
 *
 * @property lenient 匹配时，如果无法进一步匹配，则需要采用宽松匹配策略，返回上一步已匹配得到的规则。
 * @property forExpression 仅匹配表达式本身（这意味着匹配属性时，不要求严格匹配属性值）。
 * @property forDeclarationRoot 允许匹配声明（定义、定义注入、定值变量）的根对应的语法树节点（脚本属性）。
 * @property skipBlock 对于 [ParadoxMatchResult.LazyBlockAwareMatch]，直接认为匹配。
 * @property skipIndex 对于 [ParadoxMatchResult.LazyIndexAwareMatch]，直接认为匹配。
 * @property skipScope 对于 [ParadoxMatchResult.LazyScopeAwareMatch]，直接认为匹配。
 *
 * @see ParadoxMatchService
 */
data class ParadoxMatchOptions(
    val lenient: Boolean = true,
    val forExpression: Boolean = false,
    val forDeclarationRoot: Boolean = false,
    val skipBlock: Boolean = false,
    val skipIndex: Boolean = false,
    val skipScope: Boolean = false,
) {
    companion object {
        val DEFAULT = ParadoxMatchOptions()
        val DUMB = ParadoxMatchOptions(skipIndex = true, skipScope = true)

        fun create(): ParadoxMatchOptions {
            return if (isDumb()) DUMB else DUMB
        }

        fun isDumb(): Boolean {
            return ParadoxMergedIndexThreadContext.isProcessing.hasState()
        }
    }
}

/**
 * 规范化输入的匹配选项。
 */
fun ParadoxMatchOptions?.normalized(): ParadoxMatchOptions {
    return this ?: ParadoxMatchOptions.DEFAULT
}

/**
 * 转化为用于构建缓存键的哈希字符串。
 *
 * 参数 [forMatched] 表示当前的匹配选项是否用于获取匹配的规则，而非，例如，仅用于获取作为上下文的规则。
 * 通过将其显式指定为 `false`，可以提高缓存命中率。
 */
fun ParadoxMatchOptions?.toHashString(forMatched: Boolean = true): String {
    val options = this ?: ParadoxMatchOptions.DEFAULT
    var mask: Byte = 0
    if (options.skipBlock) mask = mask or 0x01
    if (options.skipIndex) mask = mask or 0x02
    if (options.skipScope) mask = mask or 0x04
    if (options.lenient && forMatched) mask = mask or 0x08
    if (options.forExpression && forMatched) mask = mask or 0x10
    if (options.forDeclarationRoot && forMatched) mask = mask or 0x20
    return mask.toString()
}
