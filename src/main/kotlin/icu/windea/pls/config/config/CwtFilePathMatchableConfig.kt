package icu.windea.pls.config.config

import icu.windea.pls.config.config.delegated.FromProperty
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtFilePathMatchableConfig : CwtConfig<CwtProperty> {
    @FromProperty("path: string", multiple = true)
    val paths: Set<String>
    @FromProperty("path_file: string?")
    val pathFile: String?
    @FromProperty("path_extension: string?")
    val pathExtension: String?
    @FromProperty("path_strict: boolean", defaultValue = "false")
    val pathStrict: Boolean
    @FromProperty("path_pattern: string", multiple = true)
    val pathPatterns: Set<String>
}
