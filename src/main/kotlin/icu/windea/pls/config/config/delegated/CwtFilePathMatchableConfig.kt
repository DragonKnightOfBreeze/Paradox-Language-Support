package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 可按文件路径匹配生效范围的规则。
 *
 * - 常见于 `type[...]`、`row[...]`、`complex_enum[...]` 等需要按目录/文件后缀收窄作用范围的规则。
 * - 当声明了以下任一字段时，PLS 会在解析/索引时根据源文件路径进行过滤，仅在匹配的文件内生效。
 *
 * 字段含义：
 * - `paths`: 指定目录或文件路径（相对 game/mod 根），可多值。
 * - `pathFile`: 指定文件名（含扩展名）。
 * - `pathExtension`: 指定文件扩展名（不含点）。
 * - `pathStrict`: 是否严格匹配（默认否，表示前缀匹配/子路径匹配）。
 * - `pathPatterns`: 使用通配/正则风格的路径模式，可多值。
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
