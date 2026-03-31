package icu.windea.pls.lang.resolve.complexExpression.attributes

import com.intellij.util.BitUtil
import icu.windea.pls.lang.resolve.complexExpression.ParadoxComplexExpression

/**
 * 复杂表达式的综合属性。
 *
 * 用于进行更准确的语义解析与匹配。
 *
 * @property dynamicDataInvolved 涉及动态数据。例如，涉及动态值（如 `event_target`）。
 * @property relaxDynamicDataInvolved 涉及动态数据，并且存在另一种更精确的格式。例如，存在另一种带前缀或带参数的格式。
 *
 * @see ParadoxComplexExpression
 * @see ParadoxComplexExpressionAttributesEvaluator
 */
@JvmInline
value class ParadoxComplexExpressionAttributes(private val value: Int) {
    val dynamicDataInvolved: Boolean get() = BitUtil.isSet(value, Flags.DYNAMIC_DATA_INVOLVED)
    val relaxDynamicDataInvolved: Boolean get() = BitUtil.isSet(value, Flags.RELAX_DYNAMIC_DATA_INVOLVED)

    override fun toString(): String {
        return "ParadoxComplexExpressionAttributes(" +
            "dynamicDataInvolved=$dynamicDataInvolved" +
            ", relaxDynamicDataInvolved=$relaxDynamicDataInvolved" +
            ")"
    }

    object Flags {
        const val DYNAMIC_DATA_INVOLVED = 0x1
        const val RELAX_DYNAMIC_DATA_INVOLVED = 0x2
    }
}
