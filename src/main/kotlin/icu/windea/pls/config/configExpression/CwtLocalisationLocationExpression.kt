package icu.windea.pls.config.configExpression

import icu.windea.pls.config.configExpression.impl.CwtLocalisationLocationExpressionResolverImpl
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty

/**
 * CWT本地化位置表达式。用于定位定义的相关本地化。
 *
 * 语法与约定：
 * - 以 `|` 分隔参数：`<location>|<args...>`。
 * - 以 `$` 开头的参数表示从指定路径读取“名称文本”以替换占位符（支持逗号分隔多路径），映射到 [namePaths]。
 * - 参数 `u` 表示将最终名称强制转为大写（[forceUpperCase]）。
 * - 当 [location] 含 `$` 时表示存在占位符，需要在后续步骤以“定义名或属性值”等替换。
 *
 * 示例：
 *
 * * "$_desc" -> 用当前定义的名字替换占位符，解析为本地化的名字。
 * * "$_desc|$name" -> 在前者基础上，改为用指定路径（`name`）的属性值替换占位符（多路径逗号分隔）。
 * * "$_desc|$name|u" -> 在前者基础上，强制解析为大写的本地化的名字。
 * * "title" -> 得到当前定义声明中指定路径（`title`）的属性的值，解析为本地化的名字。
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
