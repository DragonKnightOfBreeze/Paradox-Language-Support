package icu.windea.pls.core.data

/**
 * 去除 JSON5 文本中的注释。
 *
 * 支持行注释与块注释，并会跳过字符串字面量内部的注释标记。
 * 尾随逗号、无引号字段名等其它宽松语法由 [JsonService.json5] 处理。
 */
fun String.stripJson5Comments(): String {
    if (!contains('/')) return this
    val result = StringBuilder(length)
    var i = 0
    var inString = false
    var quote = ' '
    while (i < length) {
        val c = this[i]
        if (inString) {
            result.append(c)
            when {
                c == '\\' && i + 1 < length -> {
                    result.append(this[i + 1])
                    i += 2
                    continue
                }
                c == quote -> inString = false
            }
            i++
            continue
        }
        when {
            c == '"' || c == '\'' -> {
                inString = true
                quote = c
                result.append(c)
                i++
            }
            c == '/' && i + 1 < length && this[i + 1] == '/' -> {
                i += 2
                while (i < length && this[i] != '\n' && this[i] != '\r') i++
            }
            c == '/' && i + 1 < length && this[i + 1] == '*' -> {
                i += 2
                while (i + 1 < length && !(this[i] == '*' && this[i + 1] == '/')) i++
                i += 2
                result.append(' ')
            }
            else -> {
                result.append(c)
                i++
            }
        }
    }
    return result.toString()
}
