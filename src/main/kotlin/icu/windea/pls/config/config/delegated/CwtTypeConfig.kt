package icu.windea.pls.config.config.delegated

import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.delegated.impl.CwtTypeConfigResolverImpl
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.util.ReversibleValue

/**
 * 类型规则。
 *
 * 用于描述如何定位、匹配与命名对应类型的定义，以及如何提供相关本地化、相关图片等额外信息。
 * 按照路径模式匹配脚本文件，并在其中进一步匹配定义类型。
 *
 * 路径定位：`types/type[{type}]`，`{type}` 匹配规则名称（定义类型）。
 *
 * CWTools 兼容性：兼容，但存在一定的扩展。
 *
 * 示例：
 * ```cwt
 * types = {
 *     type[civic_or_origin] = {
 *         path = "game/common/governments/civics"
 *         file_extension = .txt
 *         # ...
 *     }
 * }
 * ```
 *
 * @property name 类型名。
 * @property baseType 基类型名，若存在表示继承/复用另一类型的部分语义。
 * @property nameField 名称字段键，用于从属性中抽取“展示名称”。
 * @property typeKeyPrefix 类型键前缀（用于限定/推导 `rootKey`）。
 * @property nameFromFile 是否从文件名推导名称（默认 false）。
 * @property typePerFile 是否“一文件一类型实例”（默认 false）。
 * @property unique 是否唯一（用于冲突检查/导航等）。
 * @property severity 严重级别标签（用于标注告警/错误等展示维度）。
 * @property skipRootKey 允许跳过的根键（支持多组设置，大小写不敏感）。
 * @property typeKeyFilter 类型键过滤器（包含/排除，大小写不敏感）。
 * @property typeKeyRegex 类型键正则过滤器（忽略大小写）。
 * @property startsWith 类型键前缀要求（大小写不敏感）。
 * @property graphRelatedTypes 图相关的关联类型集合（尚未启用的扩展能力）。
 * @property subtypes 子类型规则集合。
 * @property localisation 该类型的本地化展示设置规则。
 * @property images 该类型的图片展示设置规则。
 * @property possibleRootKeys 可能的根键集合。
 * @property typeKeyPrefixConfig 当以值条目形式声明前缀时，对应的原始值规则。
 *
 * @see CwtSubtypeConfig
 * @see CwtDeclarationConfig
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
        /** 由属性规规则解析为类型规则。*/
        fun resolve(config: CwtPropertyConfig): CwtTypeConfig?
    }

    companion object : Resolver by CwtTypeConfigResolverImpl()
}
