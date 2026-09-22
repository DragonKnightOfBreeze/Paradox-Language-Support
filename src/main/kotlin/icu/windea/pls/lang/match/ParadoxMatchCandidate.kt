package icu.windea.pls.lang.match

import icu.windea.pls.config.config.CwtMemberConfig

/**
 * @see ParadoxMatchCandidateService
 */
data class ParadoxMatchCandidate(
    val value: CwtMemberConfig<*>,
    val result: ParadoxMatchResult,
) {
    var processed: Boolean = false
}
