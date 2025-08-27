package icu.windea.pls.config.configExpression

import icu.windea.pls.config.configExpression.impl.CwtLocalisationLocationExpressionResolverImpl
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * CWT本地化位置表达式。用于定位定义的相关本地化。
 *
 * 示例：
 *
 * * `"$_desc"` -> 用当前定义的名字替换占位符，解析为本地化的名字。
 * * `"$_desc|$name"` -> 在前者的基础上，改为用指定路径（`name`）的属性的值替换占位符（如果值存在且可以获取），解析为本地化的名字。管道符后的路径可以有多个，逗号分割。
 * * `"$_desc|$name|u"` -> 在前者的基础上，强制解析为大写的本地化的名字。
 * * `"title"` -> 得到当前定义声明中指定路径（`title`）的属性的值，解析为本地化的名字。
 *
 * @property namePaths 用于获取名字文本的一组表达式路径。名字文本用于替换占位符。
 * @property forceUpperCase 本地化的名字是否需要强制大写。
 */
interface CwtLocalisationLocationExpression : CwtLocationExpression {
    val namePaths: Set<String>
    val forceUpperCase: Boolean

    operator fun component3() = namePaths
    operator fun component4() = forceUpperCase

    class ResolveResult(
        val name: String,
        val message: String? = null,
        resolveAction: () -> ParadoxLocalisationProperty? = { null },
        resolveAllAction: () -> Collection<ParadoxLocalisationProperty> = { emptySet() },
    ) {
        val element: ParadoxLocalisationProperty? by lazy { resolveAction() }
        val elements: Collection<ParadoxLocalisationProperty> by lazy { resolveAllAction() }
    }

    interface Resolver {
        fun resolveEmpty(): CwtLocalisationLocationExpression
        fun resolve(expressionString: String): CwtLocalisationLocationExpression
    }

    companion object : Resolver by CwtLocalisationLocationExpressionResolverImpl()
}
