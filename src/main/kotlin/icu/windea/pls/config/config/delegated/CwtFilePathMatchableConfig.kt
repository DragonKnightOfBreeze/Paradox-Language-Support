package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 可匹配文件路径的规则基类。
 *
 * 概述：
 * - 为若干扩展规则提供统一的“文件路径匹配条件”，以限制其生效范围（目录、扩展名、模式等）。
 *
 * @property paths 允许的目录或路径前缀集合（可多条）。
 * @property pathFile 允许的文件名（单个）。
 * @property pathExtension 允许的文件扩展名（单个）。
 * @property pathStrict 是否严格匹配（默认 false）。
 * @property pathPatterns 允许的路径模式集合（支持通配/ANT/正则的具体能力取决于调用方）。
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
