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
    companion object {
        val DEFAULT = ParadoxMatchOptions()
        val DUMB = ParadoxMatchOptions(skipIndex = true, skipScope = true)
    }
}

/**
 * 规范化输入的匹配选项。
 */
fun ParadoxMatchOptions?.normalized(): ParadoxMatchOptions {
    return this ?: ParadoxMatchOptions.DEFAULT
}

/**
 * 转化为用于构建缓存键的哈希字符串。
 *
 * @param forMatched 是否用于获取匹配的规则。用于提高缓存命中率。
 */
fun ParadoxMatchOptions?.toHashString(forMatched: Boolean = true): String {
    val options = this ?: ParadoxMatchOptions.DEFAULT
    var mask: Byte = 0
    if (options.fallback && forMatched) mask = mask or 1
    if (options.acceptDefinition && forMatched) mask = mask or 2
    if (options.relax) mask = mask or 4
    if (options.skipIndex) mask = mask or 8
    if (options.skipScope) mask = mask or 16
    return mask.toString()
}
