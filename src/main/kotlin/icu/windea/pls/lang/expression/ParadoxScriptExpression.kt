package icu.windea.pls.lang.expression

import icu.windea.pls.lang.expression.impl.ParadoxScriptExpressionResolverImpl
import icu.windea.pls.lang.util.ParadoxExpressionMatcher
import icu.windea.pls.model.ParadoxType
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement

/**
 * 脚本表达式，
 *
 * 脚本表达式用来记录脚本中的键与值（`LHS` & `RHS`）的值、类型、是否用引号括起，是否是键等信息。
 *
 * @see ParadoxScriptExpressionElement
 */
interface ParadoxScriptExpression {
    val value: String
    val type: ParadoxType
    val quoted: Boolean
    val isKey: Boolean?

    fun isParameterized(): Boolean
    fun isFullParameterized(): Boolean
    fun matchesConstant(v: String): Boolean

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    interface Resolver {
        fun resolveBlock(): ParadoxScriptExpression
        fun resolve(value: String, quoted: Boolean, isKey: Boolean? = null): ParadoxScriptExpression
        fun resolve(text: String, isKey: Boolean? = null): ParadoxScriptExpression
        fun resolve(element: ParadoxScriptExpressionElement, matchOptions: Int = ParadoxExpressionMatcher.Options.Default): ParadoxScriptExpression
    }

    companion object : Resolver by ParadoxScriptExpressionResolverImpl()
}
