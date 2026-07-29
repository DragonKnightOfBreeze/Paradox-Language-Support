package icu.windea.pls.config.attributes

import icu.windea.pls.config.CwtDataTypeSets
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.configExpression.CwtDataExpression
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet

/**
 * 规则分组的综合属性。
 */
interface CwtConfigGroupAttributes {
    /** 是否可能到了使用基于 `## predicate` 的简单结构匹配。 */
    val usePredicateBasedMatch: Boolean

    /** 是否支持内联脚本（作为一种特殊的宏）。 */
    val supportInlineScript: Boolean
    /** 是否支持定义注入（作为一种特殊的宏）。 */
    val supportDefinitionInjection: Boolean

    /** 涉及到的数据类型为路径引用（[CwtDataTypeSets.PathReference]]）的数据表达式。 */
    val filePathExpressions: Set<CwtDataExpression>
    /** 涉及到的数据类型为参数（[CwtDataTypes.Parameter]）的成员规则。 */
    val parameterConfigs: Set<CwtMemberConfig<*>>

    companion object {
        val EMPTY = CwtConfigGroupAttributesBase()
    }
}

class CwtConfigGroupAttributesBase : CwtConfigGroupAttributes {
    override var usePredicateBasedMatch: Boolean = false
    override var supportInlineScript: Boolean = false
    override var supportDefinitionInjection: Boolean = false
    override val parameterConfigs: ObjectOpenHashSet<CwtMemberConfig<*>> = ObjectOpenHashSet()
    override val filePathExpressions: ObjectOpenHashSet<CwtDataExpression> = ObjectOpenHashSet()

    fun trim() {
        parameterConfigs.trim()
        filePathExpressions.trim()
    }
}
