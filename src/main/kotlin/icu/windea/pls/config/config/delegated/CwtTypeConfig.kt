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
 * CWTools 兼容性：兼容，但存在一些扩展。
 *
 * 示例：
 * ```cwt
 * types = {
 *     type[civic_or_origin] = {
 *         path = "game/common/governments/civics"
 *         path_extension = .txt
 *         # ...
 *     }
 * }
 * ```
 *
 * @property name 类型名。
 * @property baseType 基类型名，如果存在则表示继承另一类型的部分语义。
 * @property nameField 名称字段键（用于解析定义名）。如果为空字符串，则强制匿名；如果为 `-`，则从属性值解析定义名；否则从对应名字（忽略大小写）的子属性值解析定义名。
 * @property nameFromFile 是否从文件名解析定义名。默认为 `false`。
 * @property typePerFile 是否一个文件对应一个类型实例。默认为 `false`。
 * @property skipRootKey 需要跳过的顶级键（支持多组设置，忽略大小写，兼容通配符）。用于规则匹配。
 * @property typeKeyPrefix 类型键前缀（位于类型键之前的单独的字符串，忽略大小写）。用于规则匹配。
 * @property typeKeyFilter 类型键的过滤器（包含/排除，忽略大小写）。用于规则匹配。
 * @property typeKeyRegex 类型键的正则过滤器（忽略大小写）。用于规则匹配。
 * @property startsWith 类型键的前缀要求（忽略大小写）。从类型键解析定义名时会先被去除。用于规则匹配。
 * @property unique 是否唯一（用于冲突检查/导航等）。目前并未使用。
 * @property severity 严重级别标签（用于标注告警/错误等展示维度）。目前并未使用。
 * @property graphRelatedTypes 图相关的关联类型集合。目前并未使用。
 * @property subtypes 对应的子类型规则的集合。
 * @property localisation 对应的本地化展示规则。
 * @property images 对应的的图片展示规则。
 * @property possibleTypeKeys 可能的类型键的集合。
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
    @FromProperty("name_from_file: boolean", defaultValue = "no")
    val nameFromFile: Boolean
    @FromProperty("type_per_file: boolean", defaultValue = "no")
    val typePerFile: Boolean
    @FromProperty("skip_root_key: string | string[]", multiple = true)
    val skipRootKey: List<List<@CaseInsensitive String>>
    @FromProperty("type_key_prefix: string?")
    val typeKeyPrefix: @CaseInsensitive String?
    @FromOption("type_key_filter: string | string[]")
    val typeKeyFilter: ReversibleValue<Set<@CaseInsensitive String>>?
    @FromOption("type_key_regex: string?")
    val typeKeyRegex: Regex?
    @FromOption("starts_with: string?")
    val startsWith: @CaseInsensitive String?
    @FromProperty("unique: boolean", defaultValue = "no")
    val unique: Boolean
    @FromProperty("severity: string?")
    val severity: String?
    @FromOption("graph_related_types: string[]")
    val graphRelatedTypes: Set<String>?
    @FromProperty("subtype[*]: SubtypeInfo", multiple = true)
    val subtypes: Map<String, CwtSubtypeConfig>
    @FromProperty("localisation: LocalisationInfo")
    val localisation: CwtTypeLocalisationConfig?
    @FromProperty("images: ImagesInfo")
    val images: CwtTypeImagesConfig?

    val possibleTypeKeys: Set<@CaseInsensitive String>
    val typeKeyPrefixConfig: CwtValueConfig? // #123

    interface Resolver {
        /** 由属性规则解析为类型规则。*/
        fun resolve(config: CwtPropertyConfig): CwtTypeConfig?
    }

    companion object : Resolver by CwtTypeConfigResolverImpl()
}
