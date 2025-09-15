package icu.windea.pls.ai.prompts

import icu.windea.pls.core.orNull
import icu.windea.pls.core.removePrefixOrNull
import icu.windea.pls.core.splitByBlank
import icu.windea.pls.core.unquote
import org.slf4j.LoggerFactory
import java.util.*

class PromptTemplateImpl(
    override val path: String,
    private val engine: PromptTemplateEngine
) : PromptTemplate {
    override fun render(variables: Map<String, Any?>): String {
        val normalized = normalizePath(path)
        val text = load(normalized)
        if (text == null) {
            logger.warn("Prompt template not found: $normalized")
            return ""
        }
        val processed = process(text, variables, Context(normalized))
        return processed
    }

    private fun load(normalized: String): String? {
        return engine.loader.load(normalized)
    }

    private fun process(content: String, variables: Map<String, Any?>, context: Context): String {
        // 1. 去除末尾的空白，并规范化换行符
        val contentLines = content.trimEnd().lines()
        // 2. 按指令切分模版文本成一组片段
        val snippets = parseSnippets(contentLines)
        // 3. 按指令的语义渲染模版文本
        val rendered = renderSnippets(snippets, variables, context)
        // 4. 进行占位符替换（一次性）
        return replacePlaceholders(rendered, variables)
    }

    // 按指令切分模版文本成一组片段

    private fun parseSnippets(contentLines: List<String>): MutableList<Snippet> {
        val snippets = mutableListOf<Snippet>()
        val lines = mutableListOf<String>()
        var directiveIndexCounter = 0

        for ((lineIndex, contentLine) in contentLines.withIndex()) {
            val lineNumber = lineIndex + 1
            var i = 0
            val n = contentLine.length
            while (i < n) {
                // 尝试将注释解析为指令

                val start = contentLine.indexOf(COMMENT_PREFIX, i)
                if (start == -1) {
                    lines += contentLine
                    break
                }
                val end = contentLine.indexOf(COMMENT_SUFFIX, start + COMMENT_PREFIX.length)
                if (end == -1) {
                    lines += contentLine
                    break
                }
                val next = end + COMMENT_SUFFIX.length
                val comment = contentLine.substring(start, next)
                val commentText = contentLine.substring(start + COMMENT_PREFIX.length, end)
                val directiveText = commentText.trim().removePrefixOrNull(DIRECTIVE_PREFIX)
                if (directiveText.isNullOrEmpty()) {
                    lines += contentLine
                    break
                }

                if (lines.isNotEmpty()) {
                    snippets += Snippet.Text(lines.joinToString("\n"))
                    lines.clear()
                }

                // 解析指令的名称和参数

                val atLineStart = start == 0
                val atLineEnd = next == n
                val parts = directiveText.splitByBlank()
                val directiveName = parts.first()
                val directiveArgs = parts.drop(1)
                val directive = when {
                    isValidDirectiveName(directiveName) -> PromptTemplateDirectiveRegistry.directives.find { it.name == directiveName }
                    else -> null
                }
                snippets += Snippet.Directive(comment, lineNumber, atLineStart, atLineEnd, directiveName, directiveArgs, directive, index = directiveIndexCounter++)
                i = next
            }
        }

        if (lines.isNotEmpty()) {
            snippets += Snippet.Text(lines.joinToString("\n"))
        }

        return snippets
    }

    // 按指令的语义渲染模版文本

    private fun renderSnippets(snippets: MutableList<Snippet>, variables: Map<String, Any?>, context: Context): String {
        val builder = StringBuilder()
        var i = 0
        while (i < snippets.size) {
            val snippet = snippets[i]
            i++
            when (snippet) {
                is Snippet.Text -> {
                    builder.append(snippet.text)
                }
                is Snippet.Directive -> {
                    if (snippet.atLineStart && builder.isNotEmpty()) {
                        builder.appendLine()
                    }

                    // 匹配特殊指令
                    val directive = PromptTemplateDirectiveRegistry.directives.find { it.name == snippet.directiveName }
                    var nlMode = NLMode.NONE
                    run {
                        when (directive) {
                            IncludePromptTemplateDirective -> {
                                if (context.depth >= engine.maxIncludeDepth) {
                                    logger.warn("${location(snippet, context)} Max include depth exceeded. Skipping include.")
                                    // 与循环/缺参/缺失文件不同：深度超限时不保留紧随其后的换行
                                    return@run
                                }

                                val includePath = resolveIncludeDirectiveArgs(snippet, context)
                                if (includePath == null) {
                                    if (snippet.atLineEnd) nlMode = NLMode.ALWAYS
                                    return@run
                                }

                                val resolved = resolveRelativePath(context.currentPath, includePath)
                                // 循环检测
                                if (context.includeStack.contains(resolved)) {
                                    val cycle = (context.includeStack + listOf(resolved)).joinToString(" -> ")
                                    logger.warn("${location(snippet, context)} Detected include cycle: $cycle. Skipping $resolved.")
                                    if (snippet.atLineEnd) nlMode = NLMode.ALWAYS
                                    return@run
                                }

                                context.includeStack.addLast(resolved)
                                val includedText = load(resolved)
                                if (includedText == null) {
                                    logger.warn("${location(snippet, context)} Included file not found: $resolved. Removing directive.")
                                    context.includeStack.removeLast()
                                    if (snippet.atLineEnd) nlMode = NLMode.ALWAYS
                                    return@run
                                }

                                val includedRendered = process(includedText, variables, context.copy(currentPath = resolved, depth = context.depth + 1))
                                context.includeStack.removeLast()

                                builder.append(includedRendered)
                                // 如果本行只包含该指令（或指令位于行尾），并且下一个片段不是行首指令，则按需补一个换行
                                if (snippet.atLineEnd) nlMode = NLMode.IF_MISSING
                            }
                            IfPromptTemplateDirective -> {
                                val condition = resolveConditionDirectiveArgs(snippet, context)
                                if (condition == null) {
                                    // 删除指令且移除其后的换行（已在 atLineStart 处处理上一行换行，这里不再追加）
                                    return@run
                                }

                                // 寻找匹配的 @endif（支持嵌套）
                                val endIfIndex = findMatchedEndIfDirectiveIndex(snippets, i)
                                if (endIfIndex == null) {
                                    // 未找到匹配的 @endif：语法错误。删除 @if 注释本身并告警；块内容按普通文本继续渲染。
                                    logger.warn("${location(snippet, context)} Unmatched @if without @endif. Removing @if directive.")
                                    return@run
                                }

                                // 解析 then / elseif / else 分支（仅顶层）
                                val branches = collectConditionBranches(snippets, i, endIfIndex)

                                // 为每个 ELSEIF 分支解析其条件（位于该分支起点前一个片段）
                                for (bi in 0 until branches.size) {
                                    val b = branches[bi]
                                    if (b.type == ConditionBranchType.ELSE_IF) {
                                        val elseifIdx = b.start - 1
                                        val elseifSnippet = if (elseifIdx in snippets.indices) snippets[elseifIdx] else null
                                        val elseifCond = if (elseifSnippet is Snippet.Directive) resolveConditionDirectiveArgs(elseifSnippet, context) else null
                                        branches[bi] = b.copy(cond = elseifCond)
                                    }
                                }

                                // 顶层 ELSE 多余参数告警
                                branches.firstOrNull { it.type == ConditionBranchType.ELSE }?.let { b ->
                                    val elseIdx = b.start - 1
                                    val elseSnippet = if (elseIdx in snippets.indices) snippets[elseIdx] else null
                                    if (elseSnippet is Snippet.Directive && elseSnippet.directiveArgs.isNotEmpty()) {
                                        ignoreExtraArgs(elseSnippet, context)
                                    }
                                }

                                // 顶层 ENDIF 多余参数告警
                                val endIfSnippet0 = snippets[endIfIndex]
                                if (endIfSnippet0 is Snippet.Directive && endIfSnippet0.directiveArgs.isNotEmpty()) {
                                    ignoreExtraArgs(endIfSnippet0, context)
                                }

                                // 选择分支
                                var selected: ConditionBranch?
                                if (evalCondition(condition, variables)) {
                                    selected = branches.firstOrNull { it.type == ConditionBranchType.THEN }
                                } else {
                                    selected = branches.firstOrNull { it.type == ConditionBranchType.ELSE_IF && it.cond?.let { c -> evalCondition(c, variables) } == true }
                                        ?: branches.firstOrNull { it.type == ConditionBranchType.ELSE }
                                }

                                // 渲染选中的分支
                                var renderedBranch = false
                                if (selected != null && selected.start < selected.end) {
                                    val sub = snippets.subList(selected.start, selected.end).toMutableList()
                                    val rendered = renderSnippets(sub, variables, context)
                                    builder.append(rendered)
                                    renderedBranch = true
                                }

                                // 等价于处理 @endif：在行首会补一个换行（与通用渲染一致），避免重复换行
                                if (renderedBranch) {
                                    val endIfSnippet = snippets[endIfIndex] as Snippet.Directive
                                    if (endIfSnippet.atLineStart) nlMode = NLMode.IF_MISSING
                                }

                                // 跳过整个 if 块
                                i = endIfIndex + 1
                            }
                            ElseIfPromptTemplateDirective, ElsePromptTemplateDirective, EndIfPromptTemplateDirective -> {
                                // 悬挂的条件判断指令，直接移除
                                logger.warn("${location(snippet, context)} Danging directive '@${snippet.directiveName}'. Removing directive.")
                            }
                            else -> {
                                // 未知指令：若以 @ 开头但未匹配到任何指令，输出警告；仍按普通注释原样输出
                                logger.warn("${location(snippet, context)} Invalid or unknown directive '@${snippet.directiveName}'. Treating as normal comment.")
                                builder.append(snippet.text)
                                nlMode = NLMode.ALWAYS
                            }
                        }
                    }

                    // 统一处理结尾换行
                    val next = if (i < snippets.size) snippets[i] else null
                    if (!(next is Snippet.Directive && next.atLineStart)) {
                        when (nlMode) {
                            NLMode.IF_MISSING -> if (builder.isEmpty() || builder[builder.length - 1] != '\n') builder.appendLine()
                            NLMode.ALWAYS -> builder.appendLine()
                            else -> {}
                        }
                    }
                }
            }
        }
        return builder.toString().trimEnd() // 去除末尾的空白
    }

    private fun findMatchedEndIfDirectiveIndex(snippets: List<Snippet>, startIndex: Int): Int? {
        var i = startIndex
        var level = 0
        while (i < snippets.size) {
            val s = snippets[i]
            if (s is Snippet.Directive) {
                when (s.directive) {
                    IfPromptTemplateDirective -> level++
                    EndIfPromptTemplateDirective -> if (level == 0) return i else level--
                    else -> {}
                }
            }
            i++
        }
        return null
    }

    private fun collectConditionBranches(snippets: List<Snippet>, startIndex: Int, endIndex: Int): MutableList<ConditionBranch> {
        val conditionBranches = mutableListOf<ConditionBranch>()
        var level = 0
        var curType = ConditionBranchType.THEN
        var curStart = startIndex // 从 if 后第一个片段开始
        var idx = startIndex
        while (idx < endIndex) {
            val s = snippets[idx]
            if (s is Snippet.Directive) {
                when (s.directive) {
                    IfPromptTemplateDirective -> level++
                    EndIfPromptTemplateDirective -> level--
                    ElseIfPromptTemplateDirective -> if (level == 0) {
                        // 关闭上一段
                        conditionBranches.add(ConditionBranch(curType, curStart, idx, null))
                        // 新分支
                        curType = ConditionBranchType.ELSE_IF
                        curStart = idx + 1 // 跳过 elseif 指令本身
                    }
                    ElsePromptTemplateDirective -> if (level == 0) {
                        // 关闭上一段
                        conditionBranches.add(ConditionBranch(curType, curStart, idx, null))
                        // 新分支
                        curType = ConditionBranchType.ELSE
                        curStart = idx + 1 // 跳过 else 指令本身
                    }
                    else -> {}
                }
            }
            idx++
        }
        // 收束最后一段（直到 endif 之前）
        if (curStart <= endIndex) {
            conditionBranches.add(ConditionBranch(curType, curStart, endIndex, null))
        }
        return conditionBranches
    }

    private fun resolveIncludeDirectiveArgs(snippet: Snippet.Directive, context: Context): String? {
        if (snippet.directiveArgs.size > 1) {
            ignoreExtraArgs(snippet, context)
        }
        val path = snippet.directiveArgs.firstOrNull()?.unquote()?.orNull()
        if (path == null) {
            logger.warn("${location(snippet, context)} Invalid directive '@${snippet.directiveName}' (missing path). Removing directive.")
            return null
        }
        return path
    }

    private fun resolveConditionDirectiveArgs(snippet: Snippet.Directive, context: Context): Condition? {
        if (snippet.directiveArgs.size > 1) {
            ignoreExtraArgs(snippet, context)
        }
        val expression = snippet.directiveArgs.firstOrNull()
        if (expression == null) {
            logger.warn("${location(snippet, context)} Invalid directive '@${snippet.directiveName}' (missing expression). Removing directive.")
            return null
        }
        val negate = expression.startsWith('!')
        val variableName = if (negate) expression.drop(1) else expression
        if (!isValidVariableName(variableName)) {
            logger.warn("${location(snippet, context)} Invalid directive '@${snippet.directiveName}' (invalid expression). Removing directive.")
        }
        return Condition(variableName, negate)
    }

    private fun ignoreExtraArgs(snippet: Snippet.Directive, context: Context) {
        logger.warn("${location(snippet, context)} Ignore extra args for directive '@${snippet.directiveName}'")
    }

    private fun location(snippet: Snippet.Directive, context: Context) = "[${context.currentPath}@${snippet.index}#L${snippet.lineNumber}]"

    // 进行占位符替换（一次性）

    private fun replacePlaceholders(rendered: String, variables: Map<String, Any?>): String {
        return placeholderRegex.replace(rendered) { m ->
            val name = m.groupValues[1]
            val value = variables[name]
            when (value) {
                null -> m.value // 未提供则保留原样，便于调用方发现遗漏
                else -> value.toString()
            }
        }
    }

    // 通用方法

    private fun isValidDirectiveName(name: String): Boolean {
        return directiveNameRegex.matches(name)
    }

    private fun isValidVariableName(name: String): Boolean {
        return variableNameRegex.matches(name)
    }

    private fun resolveRelativePath(currentPath: String, includePath: String): String {
        val baseDir = currentPath.replace('\\', '/').substringBeforeLast('/', "")
        val raw = if (baseDir.isEmpty()) includePath else "$baseDir/$includePath"
        return normalizePath(raw)
    }

    private fun normalizePath(path: String): String {
        // 统一分隔符并规范化 . 与 ..
        val parts = path.replace('\\', '/').split('/')
        val stack = ArrayDeque<String>()
        for (p in parts) {
            when {
                p.isEmpty() || p == "." -> {}
                p == ".." -> if (stack.isNotEmpty()) stack.removeLast() else logger.warn("Path escapes above root: $path")
                else -> stack.addLast(p)
            }
        }
        return stack.joinToString("/")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(PromptTemplateImpl::class.java)

        private const val COMMENT_PREFIX = "<!--"
        private const val COMMENT_SUFFIX = "-->"
        private const val DIRECTIVE_PREFIX = "@"

        private val placeholderRegex = """\{\{\s*([A-Za-z_][A-Za-z0-9_.-]*)\s*}}""".toRegex()
        private val directiveNameRegex = """[a-z-]+""".toRegex()
        private val variableNameRegex = """[A-Za-z_][A-Za-z0-9_.-]*""".toRegex()
    }

    private sealed class Snippet {
        abstract val text: String

        data class Text(
            override val text: String,
        ) : Snippet()

        data class Directive(
            override val text: String,
            val lineNumber: Int,
            val atLineStart: Boolean,
            val atLineEnd: Boolean,
            val directiveName: String,
            val directiveArgs: List<String>,
            val directive: PromptTemplateDirective?,
            val index: Int,
        ) : Snippet()
    }

    private data class Context(
        val currentPath: String,
        val depth: Int = 0,
        val includeStack: ArrayDeque<String> = ArrayDeque<String>(),
    )

    private data class Condition(
        val variableName: String,
        val negate: Boolean,
    )

    private fun evalCondition(cond: Condition, variables: Map<String, Any?>): Boolean {
        val v = variables[cond.variableName]
        val truthy = when (v) {
            null -> false
            is Boolean -> v
            is Number -> v.toDouble() != 0.0
            is CharSequence -> v.toString().trim().let { it.isNotEmpty() && it != "0" && !it.equals("false", true) }
            is Collection<*> -> v.isNotEmpty()
            is Map<*, *> -> v.isNotEmpty()
            else -> true
        }
        return if (cond.negate) !truthy else truthy
    }

    private data class ConditionBranch(val type: ConditionBranchType, val start: Int, val end: Int, val cond: Condition?)

    private enum class ConditionBranchType {
        THEN, ELSE_IF, ELSE
    }

    private enum class NLMode {
        NONE, IF_MISSING, ALWAYS
    }
}
