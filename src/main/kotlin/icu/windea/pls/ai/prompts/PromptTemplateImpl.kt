package icu.windea.pls.ai.prompts

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
        val process = process(text, normalized, variables, 0, ArrayDeque<String>())
        val finalProcessed = process
        return renderPlaceholders(finalProcessed, variables)
    }

    private fun load(normalized: String): String? {
        val text = engine.loader.load(normalized)
        return text?.trim()?.split("\r\n", "\r")?.joinToString("\n") // 去除首尾空白 + 规范化换行符
    }

    // 渲染主流程
    // 1. 按指令切分模版文本成一组片段
    // 2. 按指令的语义重新组织模版片段
    // 3. 按指令的语义渲染模版文本
    // 4. 进行占位符替换（一次性）

    private fun process(
        content: String,
        currentPath: String,
        variables: Map<String, Any?>,
        depth: Int,
        includeStack: ArrayDeque<String>,
    ): String {
        if (depth > engine.maxIncludeDepth) {
            logger.warn("Max include depth exceeded at $currentPath. Possible include cycle or too deep nesting.")
            return ""
        }

        val sb = StringBuilder()
        var i = 0
        val n = content.length

        while (i < n) {
            val start = content.indexOf("<!--", i)
            if (start < 0) {
                // 无更多特殊注释，直接追加剩余文本
                sb.append(content, i, n)
                break
            }

            // 先追加注释前的普通文本
            sb.append(content, i, start)

            val close = content.indexOf("-->", start + 4)
            if (close < 0) {
                // 注释未闭合：当作普通文本处理并告警
                logger.warn("${uid(currentPath, start)} Unterminated HTML comment. Treating as literal text.")
                sb.append(content.substring(start))
                break
            }

            // 提取注释内容并裁剪空白
            val rawComment = content.substring(start + 4, close)
            val trimmed = rawComment.trim()

            // 匹配特殊指令
            when (val directive = PromptTemplateDirectiveRegistry.match(trimmed)) {
                is IncludePromptTemplateDirective -> {
                    val (includePath, hasExtra) = IncludePromptTemplateDirective.parsePathAndExtras(trimmed)
                    if (hasExtra) {
                        logger.warn("${uid(currentPath, start)} Extra arguments for @include ignored: '$rawComment'")
                    }
                    if (includePath.isNullOrBlank()) {
                        logger.warn("${uid(currentPath, start)} Invalid @include directive (missing path). Removing directive.")
                        // 根据指令定义决定是否剥离紧邻换行
                        i = advanceAfterDirective(content, close + 3, directive)
                        continue
                    }

                    val resolved = resolveRelativePath(currentPath, includePath)
                    // 循环检测
                    if (includeStack.contains(resolved)) {
                        val cycle = (includeStack + listOf(resolved)).joinToString(" -> ")
                        logger.warn("${uid(currentPath, start)} Detected include cycle: $cycle. Skipping $resolved.")
                        i = advanceAfterDirective(content, close + 3, directive)
                        continue
                    }

                    includeStack.addLast(resolved)
                    val includedText = load(resolved)
                    if (includedText == null) {
                        logger.warn("${uid(currentPath, start)} Included file not found: $resolved. Removing directive.")
                        includeStack.removeLast()
                        i = advanceAfterDirective(content, close + 3, directive)
                        continue
                    }

                    val includedRendered = process(includedText, resolved, variables, depth + 1, includeStack)
                    includeStack.removeLast()

                    sb.append(includedRendered)
                    // include：按指令定义处理紧邻换行
                    i = advanceAfterDirective(content, close + 3, directive)
                    continue
                }

                is IfPromptTemplateDirective -> {
                    val (cond, hasExtra) = IfPromptTemplateDirective.parseConditionAndExtras(trimmed)
                    if (cond == null) {
                        logger.warn("${uid(currentPath, start)} Invalid @if directive. Removing directive.")
                        // 按指令定义处理紧邻换行
                        i = advanceAfterDirective(content, close + 3, directive)
                        continue
                    }
                    if (hasExtra) {
                        logger.warn("${uid(currentPath, start)} Extra arguments for @if ignored: '$rawComment'")
                    }

                    // @if：按指令定义处理紧邻换行
                    val blockStart = advanceAfterDirective(content, close + 3, directive)

                    // 寻找匹配的 @endif（支持嵌套）
                    val match = findMatchingEndif(content, currentPath, blockStart)
                    if (match == null) {
                        // 未找到匹配的 @endif：语法错误。删除 @if 注释本身并告警；块内容按普通文本继续渲染。
                        logger.warn("${uid(currentPath, start)} Unmatched @if without @endif. Removing @if directive.")
                        i = blockStart
                        continue
                    }

                    val (blockEndStart, endifCloseIdx) = match

                    // 分割 then/elseif/else 分支
                    val branches = splitIfBranches(content, currentPath, blockStart, blockEndStart)
                    // 计算选择的分支
                    var selected: Pair<Int, Int>? = null
                    if (evalCondition(cond, variables)) {
                        selected = branches.firstOrNull { it.type == BranchType.THEN }?.range
                    } else {
                        // 按顺序检查 elseif
                        for (b in branches) {
                            if (b.type == BranchType.ELSEIF) {
                                val c = b.condition
                                if (c != null && evalCondition(c, variables)) {
                                    selected = b.range
                                    break
                                }
                            }
                        }
                        // 若未命中，检查 else
                        if (selected == null) {
                            selected = branches.firstOrNull { it.type == BranchType.ELSE }?.range
                        }
                    }

                    if (selected != null) {
                        val (s1, e1) = selected
                        if (s1 < e1) {
                            val block = content.substring(s1, e1)
                            val renderedBlock = process(block, currentPath, variables, depth, includeStack)
                            sb.append(renderedBlock)
                        }
                    }

                    // 跳过 @endif 注释，并按 @endif 定义处理紧邻换行
                    i = advanceAfterDirective(content, endifCloseIdx + 3, EndIfPromptTemplateDirective)
                    continue
                }

                is EndIfPromptTemplateDirective -> {
                    // 顶层遇到 @endif（未配对）：语法错误，去除注释并剥离紧随换行
                    if (EndIfPromptTemplateDirective.hasExtraArgs(trimmed)) {
                        logger.warn("${uid(currentPath, start)} Extra arguments for @endif ignored: '$rawComment'")
                    }
                    logger.warn("${uid(currentPath, start)} Unmatched @endif. Removing directive.")
                    i = advanceAfterDirective(content, close + 3, EndIfPromptTemplateDirective)
                    continue
                }

                else -> {
                    // 未知指令：若以 @ 开头但未匹配到任何指令，输出警告；仍按普通注释原样输出
                    if (trimmed.startsWith("@")) {
                        logger.warn("${uid(currentPath, start)} Unknown directive '$trimmed'. Treating as plain comment.")
                    }
                    // 普通 HTML 注释，原样输出
                    sb.append("<!--").append(rawComment).append("-->")
                    i = close + 3
                    continue
                }
            }
        }

        return sb.toString()
    }

    /**
     * 在 [from] 位置开始，如果紧接着是换行，则跳过并返回新索引；否则返回原索引。
     */
    private fun skipFollowingNewline(text: String, from: Int): Int {
        if (from >= text.length) return from
        return when {
            text[from] == '\n' -> from + 1
            else -> from
        }
    }

    /** 按指令定义处理注释后的前进位置。*/
    private fun advanceAfterDirective(text: String, afterCommentIndex: Int, directive: PromptTemplateDirective): Int {
        return if (directive.removeFollowingNewline) skipFollowingNewline(text, afterCommentIndex) else afterCommentIndex
    }

    /** 在 [from] 起搜索匹配的 `@endif`，返回 Pair(块结束位置（endif 起始处）, endif 注释结束位置)。*/
    private fun findMatchingEndif(text: String, currentPath: String, from: Int): Pair<Int, Int>? {
        var i = from
        val n = text.length
        var level = 1

        while (i < n) {
            val start = text.indexOf("<!--", i)
            if (start < 0) return null // 未找到
            val close = text.indexOf("-->", start + 4)
            if (close < 0) return null

            val raw = text.substring(start + 4, close)
            val trimmed = raw.trim()
            when (PromptTemplateDirectiveRegistry.match(trimmed)) {
                is IfPromptTemplateDirective -> level++
                is EndIfPromptTemplateDirective -> {
                    if (EndIfPromptTemplateDirective.hasExtraArgs(trimmed)) {
                        logger.warn("${uid(currentPath, start)} Extra arguments for @endif ignored: '$raw'")
                    }
                    level--
                }
                else -> {}
            }
            if (level == 0) {
                // 块内容结束于 endif 起始处
                return start to close
            }
            i = close + 3
        }
        return null
    }

    // 指令解析与条件求值

    private fun evalCondition(cond: IfPromptTemplateDirective.Condition, variables: Map<String, Any?>): Boolean {
        val v = variables[cond.name]
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

    // if/elseif/else 分支处理
    private enum class BranchType { THEN, ELSEIF, ELSE }
    private data class Branch(
        val type: BranchType,
        val range: Pair<Int, Int>,
        val condition: IfPromptTemplateDirective.Condition? = null,
    )

    /**
     * 将 [blockStart, blockEndStart) 区间内的顶层 then/elseif/else 内容切分出来。
     * 每个分支的 range 均不包含其指令注释，且已经根据对应指令的 removeFollowingNewline 行为裁剪了起始换行。
     */
    private fun splitIfBranches(text: String, currentPath: String, blockStart: Int, blockEndStart: Int): List<Branch> {
        val branches = mutableListOf<Branch>()
        var level = 1
        var i = blockStart
        var currentType = BranchType.THEN
        var currentStart = blockStart
        var currentCondition: IfPromptTemplateDirective.Condition? = null

        while (i < blockEndStart) {
            val start = text.indexOf("<!--", i)
            if (start < 0 || start >= blockEndStart) break
            val close = text.indexOf("-->", start + 4)
            if (close < 0 || close > blockEndStart) break
            val raw = text.substring(start + 4, close)
            val trimmed = raw.trim()
            when (PromptTemplateDirectiveRegistry.match(trimmed)) {
                is IfPromptTemplateDirective -> level++
                is EndIfPromptTemplateDirective -> level--
                is ElseIfPromptTemplateDirective -> if (level == 1) {
                    // 关闭上一段
                    if (currentStart < start) {
                        branches.add(Branch(currentType, currentStart to start, if (currentType == BranchType.ELSEIF) currentCondition else null))
                    }
                    // 解析新的 elseif 条件
                    val parsed = ElseIfPromptTemplateDirective.parseConditionAndExtras(trimmed)
                    if (parsed.hasExtra) {
                        logger.warn("${uid(currentPath, start)} Extra arguments for @elseif ignored: '$raw'")
                    }
                    currentType = BranchType.ELSEIF
                    currentCondition = parsed.condition?.let { IfPromptTemplateDirective.Condition(it.name, it.negate) }
                    currentStart = advanceAfterDirective(text, close + 3, ElseIfPromptTemplateDirective)
                }
                is ElsePromptTemplateDirective -> if (level == 1) {
                    // 关闭上一段
                    if (currentStart < start) {
                        branches.add(Branch(currentType, currentStart to start, if (currentType == BranchType.ELSEIF) currentCondition else null))
                    }
                    if (ElsePromptTemplateDirective.hasExtraArgs(trimmed)) {
                        logger.warn("${uid(currentPath, start)} Extra arguments for @else ignored: '$raw'")
                    }
                    currentType = BranchType.ELSE
                    currentCondition = null
                    currentStart = advanceAfterDirective(text, close + 3, ElsePromptTemplateDirective)
                }
                else -> {}
            }
            i = close + 3
        }

        // 收束最后一段
        if (currentStart < blockEndStart) {
            branches.add(Branch(currentType, currentStart to blockEndStart, if (currentType == BranchType.ELSEIF) currentCondition else null))
        }
        // 为 THEN 与 ELSE 分支清空条件
        return branches.map { b -> if (b.type == BranchType.ELSEIF) b else b.copy(condition = null) }
    }

    private fun uid(path: String, index: Int): String = "[$path@$index]"

    // 占位符替换（单次）

    private val placeholderRegex = Regex("""\{\{\s*([A-Za-z_][A-Za-z0-9_.-]*)\s*}}""")

    private fun renderPlaceholders(text: String, variables: Map<String, Any?>): String {
        return placeholderRegex.replace(text) { m ->
            val name = m.groupValues[1]
            val value = variables[name]
            when (value) {
                null -> m.value // 未提供则保留原样，便于调用方发现遗漏
                else -> value.toString()
            }
        }
    }

    // 路径解析

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
    }
}
