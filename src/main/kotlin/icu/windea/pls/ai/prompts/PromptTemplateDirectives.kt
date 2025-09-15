package icu.windea.pls.ai.prompts

/**
 * include 指令（内联指令）。
 *
 * 语法：
 * - `<!-- @include path -->`
 *
 * 说明：
 * - 路径相对于当前模板文件
 * - 忽略注释开始与结尾的空白
 * - 默认不移除紧邻换行
 */
object IncludePromptTemplateDirective : PromptTemplateDirective {
    override val name: String = "include"
    override val isBlock: Boolean = false
    override val removeFollowingNewline: Boolean = false

    // 严格匹配：必须为 @include 后跟至少一个空白或直接结束
    private val pattern = Regex("^@include(?:\\s+|$).*")
    override fun matches(trimmedComment: String): Boolean = pattern.matches(trimmedComment)

    data class PathParse(val path: String?, val hasExtra: Boolean)

    /** 解析 include 的路径参数以及是否带有多余参数。*/
    fun parsePathAndExtras(trimmedComment: String): PathParse {
        val i = trimmedComment.indexOfFirst { it.isWhitespace() }
        if (i < 0) return PathParse(null, false)
        val rest = trimmedComment.substring(i + 1).trim()
        if (rest.isEmpty()) return PathParse(null, false)
        if (rest.startsWith('"')) {
            val end = rest.indexOf('"', startIndex = 1)
            if (end <= 0) return PathParse(rest.removeSurrounding("\"", "\""), false)
            val path = rest.substring(0, end + 1).removeSurrounding("\"", "\"")
            val extra = rest.substring(end + 1).trim().isNotEmpty()
            return PathParse(path, extra)
        } else {
            val j = rest.indexOfFirst { it.isWhitespace() }
            return if (j < 0) PathParse(rest, false) else PathParse(rest.substring(0, j), rest.substring(j + 1).trim().isNotEmpty())
        }
    }
}

/**
 * if 指令（块指令）。
 *
 * 语法：
 * - `<!-- @if name --> ... <!-- @endif -->`
 * - `<!-- @if !name --> ... <!-- @endif -->`
 *
 * 说明：
 * - 忽略注释开始与结尾的空白
 * - 默认移除自身与 `@endif` 紧邻的换行
 */
object IfPromptTemplateDirective : PromptTemplateDirective {
    override val name: String = "if"
    override val isBlock: Boolean = true
    override val removeFollowingNewline: Boolean = true

    // 严格匹配：必须为 @if 后跟至少一个空白或直接结束
    private val pattern = Regex("^@if(?:\\s+|$).*")
    override fun matches(trimmedComment: String): Boolean = pattern.matches(trimmedComment)

    data class Condition(val name: String, val negate: Boolean)
    data class ConditionParse(val condition: Condition?, val hasExtra: Boolean)

    /** 解析 if 的条件表达式与是否存在多余参数。*/
    fun parseConditionAndExtras(trimmedComment: String): ConditionParse {
        val i = trimmedComment.indexOfFirst { it.isWhitespace() }
        if (i < 0) return ConditionParse(null, false)
        var rest = trimmedComment.substring(i + 1).trim()
        var negate = false
        if (rest.startsWith("!")) {
            negate = true
            rest = rest.substring(1).trim()
        }
        if (rest.isEmpty()) return ConditionParse(null, false)
        val j = rest.indexOfFirst { it.isWhitespace() }
        val name = if (j < 0) rest else rest.substring(0, j)
        if (!name.matches(Regex("[A-Za-z_][A-Za-z0-9_.-]*"))) return ConditionParse(null, false)
        val extra = if (j < 0) false else rest.substring(j + 1).trim().isNotEmpty()
        return ConditionParse(Condition(name, negate), extra)
    }
}

/**
 * elseif 指令（块指令）。
 *
 * 语法：
 * - `<!-- @elseif name -->`
 * - `<!-- @elseif !name -->`
 *
 * 说明：
 * - 忽略注释开始与结尾的空白
 * - 默认移除自身紧邻的换行
 */
object ElseIfPromptTemplateDirective : PromptTemplateDirective {
    override val name: String = "elseif"
    override val isBlock: Boolean = true
    override val removeFollowingNewline: Boolean = true

    // 严格匹配：必须为 @elseif 后跟至少一个空白或直接结束
    private val pattern = Regex("^@elseif(?:\\s+|$).*")
    override fun matches(trimmedComment: String): Boolean = pattern.matches(trimmedComment)

    data class Condition(val name: String, val negate: Boolean)
    data class ConditionParse(val condition: Condition?, val hasExtra: Boolean)

    /** 解析 elseif 的条件表达式与是否存在多余参数。*/
    fun parseConditionAndExtras(trimmedComment: String): ConditionParse {
        val i = trimmedComment.indexOfFirst { it.isWhitespace() }
        if (i < 0) return ConditionParse(null, false)
        var rest = trimmedComment.substring(i + 1).trim()
        var negate = false
        if (rest.startsWith("!")) {
            negate = true
            rest = rest.substring(1).trim()
        }
        if (rest.isEmpty()) return ConditionParse(null, false)
        val j = rest.indexOfFirst { it.isWhitespace() }
        val name = if (j < 0) rest else rest.substring(0, j)
        if (!name.matches(Regex("[A-Za-z_][A-Za-z0-9_.-]*"))) return ConditionParse(null, false)
        val extra = if (j < 0) false else rest.substring(j + 1).trim().isNotEmpty()
        return ConditionParse(Condition(name, negate), extra)
    }
}

/**
 * else 指令（块指令）。
 *
 * 语法：
 * - `<!-- @else -->`
 *
 * 说明：
 * - 忽略注释开始与结尾的空白
 * - 默认移除自身紧邻的换行
 */
object ElsePromptTemplateDirective : PromptTemplateDirective {
    override val name: String = "else"
    override val isBlock: Boolean = true
    override val removeFollowingNewline: Boolean = true

    // 接受附加参数，但会在解析阶段给出告警
    private val pattern = Regex("^@else(?:\\s+.*|$)")
    override fun matches(trimmedComment: String): Boolean = pattern.matches(trimmedComment)

    fun hasExtraArgs(trimmedComment: String): Boolean {
        val i = trimmedComment.indexOfFirst { it.isWhitespace() }
        if (i < 0) return false
        return trimmedComment.substring(i + 1).trim().isNotEmpty()
    }
}

/**
 * endif 指令（块指令，结束标记）。
 *
 * 语法：
 * - `<!-- @endif -->`
 * - `<!-- @endif; -->`
 * - `<!-- @endif. -->`
 *
 * 说明：
 * - 只能单独使用，不允许携带参数。
 * - 默认移除自身紧邻的换行。
 */
object EndIfPromptTemplateDirective : PromptTemplateDirective {
    override val name: String = "endif"
    override val isBlock: Boolean = true
    override val removeFollowingNewline: Boolean = true

    // 接受附加参数，但会在解析阶段给出告警
    private val pattern = Regex("^@endif(?:\\s+.*|$)")
    override fun matches(trimmedComment: String): Boolean = pattern.matches(trimmedComment)

    fun hasExtraArgs(trimmedComment: String): Boolean {
        val i = trimmedComment.indexOfFirst { it.isWhitespace() }
        if (i < 0) return false
        return trimmedComment.substring(i + 1).trim().isNotEmpty()
    }
}

