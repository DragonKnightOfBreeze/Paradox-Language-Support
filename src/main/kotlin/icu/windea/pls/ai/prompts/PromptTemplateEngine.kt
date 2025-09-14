package icu.windea.pls.ai.prompts

import com.intellij.openapi.diagnostic.logger
import java.util.*

/**
 * 自定义提示模板引擎（轻量实现）。
 *
 * 支持功能：
 * - 参数占位符：`{{ paramName }}`（忽略参数名与前后缀的空白；单次替换，不支持嵌套再次解析）
 * - 条件语法：`<!-- @if paramName -->...<!-- @endif -->` 与 `<!-- @if !paramName -->...<!-- @endif -->`
 *   - 忽略注释开始与结尾的空白
 *   - 不渲染紧接在 `@if` 与 `@endif` 注释之后的换行符
 * - 导入语法：`<!-- @include filename -->`（相对当前模板文件路径）
 *   - 忽略注释开始与结尾的空白
 *   - 会渲染紧接在 `@include` 注释之后的换行符
 * - 若存在模板语法错误，会输出警告日志，并在渲染结果中去除不合法的特殊注释
 */
class PromptTemplateEngine(
    private val resourceLoader: ResourceLoader = ClasspathResourceLoader(),
    private val maxIncludeDepth: Int = 16,
) {
    /**
     * 从资源路径渲染模板。
     * @param templatePath 资源路径（例如：`prompts/translate-localisation_zh.md`）
     * @param params 参数映射（占位符与条件变量）
     */
    fun render(templatePath: String, params: Map<String, Any?> = emptyMap()): String {
        val normalized = normalizePath(templatePath)
        val text = resourceLoader.load(normalized)
        if (text == null) {
            logger.warn("Prompt template not found: $normalized")
            return ""
        }
        val rendered = process(text, normalized, params, 0, ArrayDeque())
        return renderPlaceholders(rendered, params)
    }

    // region 渲染主流程（包含 include/if 处理，最后再做占位符替换）

    private fun process(
        content: String,
        currentPath: String,
        params: Map<String, Any?>,
        depth: Int,
        includeStack: ArrayDeque<String>,
    ): String {
        if (depth > maxIncludeDepth) {
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
                logger.warn("Unterminated HTML comment in template: $currentPath at index=$start. Treating as literal text.")
                sb.append(content.substring(start))
                break
            }

            // 提取注释内容并裁剪空白
            val rawComment = content.substring(start + 4, close)
            val trimmed = rawComment.trim()

            // 匹配特殊指令
            when {
                isIncludeDirective(trimmed) -> {
                    val includePath = parseIncludePath(trimmed)
                    if (includePath.isNullOrBlank()) {
                        logger.warn("Invalid @include directive (missing path): $currentPath at index=$start. Removing directive.")
                        // 移除不合法特殊注释（不剥离换行，符合 include 规则）
                        i = close + 3
                        continue
                    }

                    val resolved = resolveRelativePath(currentPath, includePath)
                    // 循环检测
                    if (includeStack.contains(resolved)) {
                        val cycle = (includeStack + listOf(resolved)).joinToString(" -> ")
                        logger.warn("Detected include cycle: $cycle. Skipping $resolved.")
                        i = close + 3 // 保留换行（include 规则）
                        continue
                    }

                    includeStack.addLast(resolved)
                    val includedText = resourceLoader.load(resolved)
                    if (includedText == null) {
                        logger.warn("Included file not found: $resolved (from $currentPath). Removing directive.")
                        includeStack.removeLast()
                        i = close + 3 // 保留换行（include 规则）
                        continue
                    }

                    val includedRendered = process(includedText, resolved, params, depth + 1, includeStack)
                    includeStack.removeLast()

                    sb.append(includedRendered)
                    // include 后保留紧接其后的换行（不额外处理，继续扫描）
                    i = close + 3
                    continue
                }

                isIfDirective(trimmed) -> {
                    val cond = parseIfCondition(trimmed)
                    if (cond == null) {
                        logger.warn("Invalid @if directive: $currentPath at index=$start. Removing directive.")
                        // 去除不合法的特殊注释，并且不渲染紧随其后的换行（按条件语法规则处理）
                        i = skipFollowingNewline(content, close + 3)
                        continue
                    }

                    // @if 后不渲染紧接的换行
                    val blockStart = skipFollowingNewline(content, close + 3)

                    // 寻找匹配的 @endif（支持嵌套）
                    val match = findMatchingEndif(content, blockStart)
                    if (match == null) {
                        // 未找到匹配的 @endif：语法错误。删除 @if 注释本身并告警；块内容按普通文本继续渲染。
                        logger.warn("Unmatched @if without @endif in $currentPath at index=$start. Removing @if directive.")
                        i = blockStart
                        continue
                    }

                    val (blockEndStart, endifCloseIdx) = match
                    val block = content.substring(blockStart, blockEndStart)

                    val result = evalCondition(cond, params)
                    if (result) {
                        val renderedBlock = process(block, currentPath, params, depth, includeStack)
                        sb.append(renderedBlock)
                    }

                    // 跳过 @endif 注释，并且不渲染紧接的换行
                    i = skipFollowingNewline(content, endifCloseIdx + 3)
                    continue
                }

                isEndifDirective(trimmed) -> {
                    // 顶层遇到 @endif（未配对）：语法错误，去除注释并剥离紧随换行
                    logger.warn("Unmatched @endif in $currentPath at index=$start. Removing directive.")
                    i = skipFollowingNewline(content, close + 3)
                    continue
                }

                else -> {
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
     * 在 [from] 位置开始，如果紧接着是换行（支持 \r\n、\n、\r），则跳过并返回新索引；否则返回原索引。
     */
    private fun skipFollowingNewline(text: String, from: Int): Int {
        if (from >= text.length) return from
        return when {
            text.startsWith("\r\n", from) -> from + 2
            text[from] == '\n' || text[from] == '\r' -> from + 1
            else -> from
        }
    }

    /** 在 [from] 起搜索匹配的 `@endif`，返回 Pair(块结束位置（endif 起始处）, endif 注释结束位置)。*/
    private fun findMatchingEndif(text: String, from: Int): Pair<Int, Int>? {
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
            when {
                isIfDirective(trimmed) -> level++
                isEndifDirective(trimmed) -> level--
            }
            if (level == 0) {
                // 块内容结束于 endif 起始处
                return start to close
            }
            i = close + 3
        }
        return null
    }

    // endregion

    // region 指令解析与条件求值

    private fun isIncludeDirective(s: String): Boolean = s.startsWith("@include")
    private fun isIfDirective(s: String): Boolean = s.startsWith("@if")
    private fun isEndifDirective(s: String): Boolean = s == "@endif" || s == "@endif;" || s == "@endif." // 容忍轻微误写

    private fun parseIncludePath(s: String): String? {
        // @include <path>
        val i = s.indexOf(' ')
        if (i < 0) return null
        return s.substring(i + 1).trim().removeSurrounding("\"", "\"")
    }

    private data class IfCondition(val name: String, val negate: Boolean)

    private fun parseIfCondition(s: String): IfCondition? {
        // @if <name> | @if !<name>
        val i = s.indexOf(' ')
        if (i < 0) return null
        var expr = s.substring(i + 1).trim()
        var negate = false
        if (expr.startsWith("!")) {
            negate = true
            expr = expr.substring(1).trim()
        }
        if (expr.isEmpty()) return null
        // 允许字母/数字/下划线/点/中划线
        if (!expr.matches(Regex("[A-Za-z_][A-Za-z0-9_.-]*"))) return null
        return IfCondition(expr, negate)
    }

    private fun evalCondition(cond: IfCondition, params: Map<String, Any?>): Boolean {
        val v = params[cond.name]
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

    // endregion

    // region 占位符替换（单次）

    private val placeholderRegex = Regex("""\{\{\s*([A-Za-z_][A-Za-z0-9_.-]*)\s*}}""")

    private fun renderPlaceholders(text: String, params: Map<String, Any?>): String {
        return placeholderRegex.replace(text) { m ->
            val name = m.groupValues[1]
            val value = params[name]
            when (value) {
                null -> m.value // 未提供则保留原样，便于调用方发现遗漏
                else -> value.toString()
            }
        }
    }

    // endregion

    // region 路径解析与资源加载

    interface ResourceLoader {
        fun load(path: String): String?
    }

    class ClasspathResourceLoader(private val classLoader: ClassLoader = PromptTemplateEngine::class.java.classLoader) : ResourceLoader {
        override fun load(path: String): String? {
            val normalized = normalizePath(path)
            return classLoader.getResource(normalized)?.readText()
        }
    }

    companion object {
        private val logger = logger<PromptTemplateEngine>()

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
    }

    // endregion
}
