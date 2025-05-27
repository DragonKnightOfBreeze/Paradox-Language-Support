package icu.windea.pls.config.config

import icu.windea.pls.cwt.psi.*

interface CwtFilePathMatchableConfig: CwtConfig<CwtProperty> {
    val paths: Set<String>
    val pathFile: String?
    val pathExtension: String?
    val pathStrict: Boolean
    val pathPatterns: Set<String>
}
