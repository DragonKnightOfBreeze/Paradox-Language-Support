package icu.windea.pls.lang.match

import kotlin.experimental.or

/**
 * 匹配选项。
 *
 * @property fallback 如果无法进一步匹配，则会使用回退后的匹配到的规则。默认为 `true`。
 * @property acceptDefinition 允许匹配定义自身，即其声明对应的脚本属性。
 * @property relax 对于 [ParadoxMatchResult.LazySimpleMatch] 和 [ParadoxMatchResult.LazyBlockAwareMatch]，匹配结果直接返回 `true`。
 * @property skipIndex 对于 [ParadoxMatchResult.LazyIndexAwareMatch]，匹配结果直接返回 `true`。
 * @property skipScope 对于 [ParadoxMatchResult.LazyScopeAwareMatch]，匹配结果直接返回 `true`。
 */
data class ParadoxMatchOptions(
    val fallback: Boolean = true,
    val acceptDefinition: Boolean = false,
    val relax: Boolean = false,
    val skipIndex: Boolean = false,
    val skipScope: Boolean = false,
) {
    /**
     * 转化为用于构建缓存键的哈希字符串。
     *
     * @param forMatched 用于提高缓存命中率。
     */
    fun toHashString(forMatched: Boolean = false): String {
        var mask: Byte = 0
        if (fallback && forMatched) mask = mask or 1
        if (acceptDefinition && forMatched) mask = mask or 2
        if (relax) mask = mask or 4
        if (skipIndex) mask = mask or 8
        if (skipScope) mask = mask or 16
        return mask.toString()
    }

    companion object {
        val DEFAULT = ParadoxMatchOptions()
        val DUMB = ParadoxMatchOptions(skipIndex = true, skipScope = true)
    }
}

@Suppress("NOTHING_TO_INLINE")
inline fun ParadoxMatchOptions?.orDefault() = this ?: ParadoxMatchOptions.DEFAULT
