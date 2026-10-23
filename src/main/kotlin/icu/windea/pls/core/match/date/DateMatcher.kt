package icu.windea.pls.core.match.date

/**
 * 日期匹配器。
 *
 * 负责根据 [pattern] 判断输入文本是否匹配一个日期字段。
 * 具体实现可以针对常见 pattern 增加轻量预检查，但仍应保持与日期格式解析一致的语义。
 */
interface DateMatcher {
    val pattern: String

    fun matches(input: String): Boolean
}
