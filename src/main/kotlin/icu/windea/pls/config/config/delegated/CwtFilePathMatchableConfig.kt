package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 可匹配文件路径的规则的统一抽象。
 *
 * @property paths 允许的父目录（可多条）。
 * @property pathFile 允许的文件名（单个）。
 * @property pathExtension 允许的文件扩展名（单个）。
 * @property pathStrict 是否严格匹配。严格匹配意味着不匹配子目录中的文件。
 * @property pathPatterns 允许的路径模式（可多条，使用 ANT 表达式）。
 */
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
