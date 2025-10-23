package icu.windea.pls.lang.match

object ParadoxMatchOptions {
    /** 默认的匹配方式，先尝试通过 [ParadoxMatchResult.ExactMatch] 进行匹配，然后再尝试通过其他匹配方式进行匹配。 */
    const val Default = 0x00
    /** 对于 [ParadoxMatchResult.LazySimpleMatch] 和 [ParadoxMatchResult.LazyBlockAwareMatch]，匹配结果直接返回 `true`。 */
    const val Relax = 0x01
    /** 对于 [ParadoxMatchResult.LazyIndexAwareMatch]，匹配结果直接返回 `true`。 */
    const val SkipIndex = 0x02
    /** 对于 [ParadoxMatchResult.LazyScopeAwareMatch]，匹配结果直接返回 `true`。 */
    const val SkipScope = 0x04

    // /** 对于最终匹配得到的那个结果，不需要再次判断是否精确匹配。 */
    // const val Fast = 0x08
    /** 允许匹配定义自身（当要匹配表达式的是一个键时）。 */
    const val AcceptDefinition = 0x10
}
