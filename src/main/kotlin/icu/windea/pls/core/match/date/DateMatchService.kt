package icu.windea.pls.core.match.date

import icu.windea.pls.core.cache.CacheBuilder

/**
 * 日期匹配服务。
 *
 * 按 [pattern] 缓存 [DateMatcher]，避免重复创建 `DateTimeFormatter` 和预检查器。
 *
 * @see DateMatcher
 */
object DateMatchService {
    private val dateMatchersCache = CacheBuilder("maximumSize=100").build<String, DateMatcher> { pattern ->
        DefaultDateMatcher(pattern)
    }

    /**
     * 检查 [input] 是否匹配指定 [pattern] 的日期字段。
     *
     * 无效 pattern 或不匹配的输入都返回 `false`，不向调用方抛出解析异常。
     */
    fun matches(input: String, pattern: String): Boolean {
        return try {
            dateMatchersCache.get(pattern).matches(input)
        } catch (_: Exception) {
            false
        }
    }
}
