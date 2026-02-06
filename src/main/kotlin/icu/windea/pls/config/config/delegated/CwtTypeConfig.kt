package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.config.tagType
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.config.optimizedPathExtension
import icu.windea.pls.config.resolveElementWithConfig
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.annotations.CaseInsensitive
import icu.windea.pls.core.collections.caseInsensitiveStringSet
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.core.util.values.ReversibleValue
import icu.windea.pls.model.ParadoxTagType

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
        /** 由属性规则解析为类型规则。 */
        fun resolve(config: CwtPropertyConfig): CwtTypeConfig?
    }

    companion object : Resolver by CwtTypeConfigResolverImpl()
}

// region Implementations

private class CwtTypeConfigResolverImpl : CwtTypeConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtTypeConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtTypeConfig? {
        val name = config.key.removeSurroundingOrNull("type[", "]")?.orNull()?.optimized() ?: return null
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid type config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propElements.groupBy { it.key }
        val paths = propGroup.getAll("path").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val pathFile = propGroup.getOne("path_file")?.stringValue
        val pathExtension = propGroup.getOne("path_extension")?.stringValue?.optimizedPathExtension()
        val pathStrict = propGroup.getOne("path_strict")?.booleanValue ?: false
        val pathPatterns = propGroup.getAll("path_pattern").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val baseType = propGroup.getOne("base_type")?.stringValue
        val nameField = propGroup.getOne("name_field")?.stringValue
        val nameFromFile = propGroup.getOne("name_from_file")?.booleanValue ?: false
        val typePerFile = propGroup.getOne("type_per_file")?.booleanValue ?: false
        val skipRootKey = propGroup.getAll("skip_root_key").map { prop ->
            // 出于一点点的性能考虑，这里保留大小写，后面匹配路径时会忽略掉
            prop.stringValue?.let { listOf(it) } ?: prop.values?.mapNotNull { it.stringValue }?.optimized().orEmpty()
        }
        val typeKeyPrefix = propGroup.getOne("type_key_prefix")?.stringValue
        val typeKeyFilter = config.optionData.typeKeyFilter
        val typeKeyRegex = config.optionData.typeKeyRegex
        val startsWith = config.optionData.startsWith
        val unique = propGroup.getOne("unique")?.booleanValue ?: false
        val severity = propGroup.getOne("severity")?.stringValue
        val graphRelatedTypes = config.optionData.graphRelatedTypes
        val subtypes = propElements.mapNotNull { CwtSubtypeConfig.resolve(it) }.associateBy { it.name }.optimized()
        val localisation = propGroup.getOne("localisation")?.let { CwtTypeLocalisationConfig.resolve(it) }
        val images = propGroup.getOne("images")?.let { CwtTypeImagesConfig.resolve(it) }

        logger.debug { "Resolved type config (name: $name).".withLocationPrefix(config) }
        return CwtTypeConfigImpl(
            config, name, baseType,
            paths, pathFile, pathExtension, pathStrict, pathPatterns,
            nameField, nameFromFile, typePerFile,
            skipRootKey, typeKeyPrefix, typeKeyFilter, typeKeyRegex, startsWith,
            unique, severity, graphRelatedTypes,
            subtypes, localisation, images
        )
    }
}

private class CwtTypeConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val baseType: String?,
    override val paths: Set<String>,
    override val pathFile: String?,
    override val pathExtension: String?,
    override val pathStrict: Boolean,
    override val pathPatterns: Set<String>,
    override val nameField: String?,
    override val nameFromFile: Boolean,
    override val typePerFile: Boolean,
    override val skipRootKey: List<List<String>>,
    override val typeKeyPrefix: String?,
    override val typeKeyFilter: ReversibleValue<Set<String>>?,
    override val typeKeyRegex: Regex?,
    override val startsWith: String?,
    override val unique: Boolean,
    override val severity: String?,
    override val graphRelatedTypes: Set<String>?,
    override val subtypes: Map<String, CwtSubtypeConfig>,
    override val localisation: CwtTypeLocalisationConfig?,
    override val images: CwtTypeImagesConfig?,
) : UserDataHolderBase(), CwtTypeConfig {
    override val possibleTypeKeys: Set<String> by lazy {
        caseInsensitiveStringSet().apply {
            typeKeyFilter?.takeWithOperator()?.let { addAll(it) }
            subtypes.values.forEach { subtype -> subtype.typeKeyFilter?.takeWithOperator()?.let { addAll(it) } }
        }.optimized()
    }

    override val typeKeyPrefixConfig: CwtValueConfig? by lazy {
        config.properties?.find { it.key == "type_key_prefix" }?.valueConfig?.also {
            it.tagType = ParadoxTagType.TypeKeyPrefix
            it.resolveElementWithConfig()
        }
    }

    override fun toString() = "CwtTypeConfigImpl(name='$name')"
}

// endregion
