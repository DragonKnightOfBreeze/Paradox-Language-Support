package icu.windea.pls.core.util

data class TextPattern(
    /** 前缀 */
    val prefix: String = "",
    /** 后缀 */
    val suffix: String = "",
    /** 分隔符（用于拼接多段文本） */
    val separator: String = "",
)
