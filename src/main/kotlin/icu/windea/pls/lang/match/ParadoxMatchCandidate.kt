package icu.windea.pls.lang.match

import icu.windea.pls.config.config.CwtMemberConfig

/**
 * 匹配候选项。
 */
data class ParadoxMatchCandidate(
    val value: CwtMemberConfig<*>,
    val result: ParadoxMatchResult
)
