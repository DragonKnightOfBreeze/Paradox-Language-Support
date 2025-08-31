package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.cwt.psi.CwtProperty

interface CwtFilePathMatchableConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig> {
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
