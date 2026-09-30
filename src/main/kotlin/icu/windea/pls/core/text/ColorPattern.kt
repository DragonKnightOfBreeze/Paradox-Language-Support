package icu.windea.pls.core.text

import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.anyFast
import icu.windea.pls.core.match.TextMatcher
import java.awt.Color

/**
 * 颜色模式。
 *
 * 用于按照特定的方式处理颜色参数，以及颜色参数与颜色对象之间的转换。
 *
 * 包含相关的元数据，同时也作为对颜色处理逻辑的策略。
 *
 * @param T 颜色参数的类型。
 * @see ColorPatterns
 */
interface ColorPattern<T> {
    /** 颜色模式的名称。 */
    val name: String

    /**
     * 检查 [args] 是否可用。
     *
     * 可用的颜色参数可以用于得到对应的颜色，但不一定严格合法（可能会使用默认值或者进行截断）。
     */
    fun isAvailable(args: T): Boolean

    /** 检查 [args] 是否严格合法。 */
    fun isValid(args: T): Boolean

    /** 根据 [args] 得到对应的颜色。 */
    fun getColor(args: T): Color?

    /** 根据颜色 [color] 以及作为参考的颜色参数 [referenceArgs]，得到对应的颜色参数。 */
    fun getColorArgs(color: Color, referenceArgs: T): T?

    abstract class Base<T>(override val name: String) : ColorPattern<T>

    abstract class Inline(name: String) : Base<String>(name)

    abstract class Block(name: String) : Base<List<String>>(name) {
        @Optimized
        override fun isAvailable(args: List<String>): Boolean {
            // 要求参数个数为 3 或 4，并且均为数字
            val size = args.size
            if (size != 3 && size != 4) return false
            if (args.anyFast { !TextMatcher.matchesFloat(it) }) return false
            return true
        }
    }
}
