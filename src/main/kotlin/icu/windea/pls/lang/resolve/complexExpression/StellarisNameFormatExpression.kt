package icu.windea.pls.lang.resolve.complexExpression

import com.intellij.openapi.util.TextRange
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.config.config.CwtConfig
import icu.windea.pls.config.configGroup.CwtConfigGroup
import icu.windea.pls.lang.resolve.complexExpression.impl.StellarisNameFormatExpressionResolverImpl

/**
 * Stellaris 命名格式表达式。
 *
 * 说明：
 * - 用于解析 Stellaris 中以花括号包裹的“命名格式模板”，内部可混合“定义占位”“命令表达式”“本地化标识符”与嵌套参数块。
 * - 对应的规则数据类型为 [CwtDataTypes.StellarisNameFormat]。
 *
 * 示例：
 * ```
 * {<eater_adj> {<patron_noun>}}
 * {AofB{<imperial_mil> [This.GetCapitalSystemNameOrRandom]}}
 * {alpha}
 * ```
 */
interface StellarisNameFormatExpression : ParadoxComplexExpression {
    val config: CwtConfig<*>

    interface Resolver {
        fun resolve(text: String, range: TextRange, configGroup: CwtConfigGroup, config: CwtConfig<*>): StellarisNameFormatExpression?
    }

    companion object : Resolver by StellarisNameFormatExpressionResolverImpl()
}
