package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.ReversibleValue

/**
 * 类型规则：`type[<name>] = { ... }`。
 *
 * - 用于定义“定义（definition）”的结构与分布范围，是 CWT 中最核心的规则之一。
 * - 可配合路径匹配字段（见 `CwtFilePathMatchableConfig`）限制到特定目录/文件或扩展名。
 * - 支持从文件名、字段或键前缀推导定义名，并可细分子类型（`subtype[...]`）。
 *
 * 字段语义（常用）：
 * - `baseType`: 基类型名。用于类型继承/复用（可选）。
 * - `paths`/`pathFile`/`pathExtension`/`pathStrict`/`pathPatterns`: 按文件路径限定生效范围。
 * - `nameField`: 指定用于作为定义名的字段键名（如 `key`）。
 * - `typeKeyPrefix`: 指定定义键的前缀（例如 `tech_`），其对应规则可由 [typeKeyPrefixConfig] 提供提示与导航。
 * - `nameFromFile`: 名称是否从文件名推导。
 * - `typePerFile`: 是否一个文件仅包含一个定义。
 * - `unique`: 名称是否在全项目唯一。
 * - `severity`: 用于问题校验的默认严重级别（可选）。
 * - `skipRootKey`: 解析时应跳过的根键序列（支持多个候选序列）。
 * - `typeKeyFilter`/`typeKeyRegex`/`startsWith`: 限定可接受的类型键集合、匹配正则或前缀（忽略大小写）。
 * - `graphRelatedTypes`: 与该类型有关联的其它类型表达式（用于图关系/引用等）。
 * - `subtypes`: 子类型配置，键为子类型名，值为 `CwtSubtypeConfig`。
 * - `localisation`/`images`: 类型的本地化与图像位置配置。
 *
 * 扩展属性：
 * - `possibleRootKeys`: 可能作为根键出现的集合，综合自 `typeKeyFilter` 及各 `subtypes` 的过滤设置。
 * - `typeKeyPrefixConfig`: `type_key_prefix` 对应的值规则（扩展规则），用于提供 IDE 级别的补全与校验。
 */
interface CwtTypeConfig : CwtFilePathMatchableConfig {
    @FromKey("type[$]")
    val name: String
    @FromProperty("base_type: string?")
    val baseType: String?
    @FromProperty("name_field: string?")
    val nameField: String?
    @FromProperty("type_key_prefix: string?")
    val typeKeyPrefix: String?
    @FromProperty("name_from_file: boolean", defaultValue = "false")
    val nameFromFile: Boolean
    @FromProperty("type_per_file: boolean", defaultValue = "false")
    val typePerFile: Boolean
    @FromProperty("unique: boolean", defaultValue = "false")
    val unique: Boolean
    @FromProperty("severity: string?")
    val severity: String?
    @FromProperty("skip_root_key: string | string[]", multiple = true)
    val skipRootKey: List<List<@CaseInsensitive String>>?
    @FromOption("type_key_filter: string | string[]")
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    @FromOption("type_key_regex: string?")
    val typeKeyRegex: Regex?
    @FromOption("starts_with: string?")
    val startsWith: @CaseInsensitive String?
    @FromOption("graph_related_types: string[]")
    val graphRelatedTypes: Set<String>?
    @FromProperty("subtype[*]: SubtypeInfo", multiple = true)
    val subtypes: Map<String, CwtSubtypeConfig>
    @FromProperty("localisation: LocalisationInfo")
    val localisation: CwtTypeLocalisationConfig?
    @FromProperty("images: ImagesInfo")
    val images: CwtTypeImagesConfig?

    val possibleRootKeys: Set<String>
    val typeKeyPrefixConfig: CwtValueConfig? // #123

    interface Resolver {
        fun resolve(config: CwtPropertyConfig): CwtTypeConfig?
    }

    companion object : Resolver by CwtTypeConfigResolverImpl()
}
