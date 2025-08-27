package icu.windea.pls.config.config

import icu.windea.pls.config.config.CwtConfig.Property
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtFilePathMatchableConfig : CwtConfig<CwtProperty> {
    @Property("path: string", multiple = true)
    val paths: Set<String>
    @Property("path_file: string?")
    val pathFile: String?
    @Property("path_extension: string?")
    val pathExtension: String?
    @Property("path_strict: boolean", defaultValue = "false")
    val pathStrict: Boolean
    @Property("path_pattern: string", multiple = true)
    val pathPatterns: Set<String>
}
