package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.config.optimizedPathExtension
import icu.windea.pls.config.select.*
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.withLocationPrefix
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull

/**
 * 复杂枚举规则。
 *
 * 用于描述需要基于锚点动态定位可选项（即枚举值）的复杂枚举。
 * 按照路径模式匹配脚本文件，并在其中进一步匹配锚点。
 * 其枚举值默认不忽略大小写。
 *
 * 路径定位：`enums/complex_enum[{name}]`，`{name}` 匹配规则名称（枚举名）。
 *
 * CWTools 兼容性：兼容，但存在一些扩展。
 *
 * 示例：
 * ```cwt
 * enums = {
 *     complex_enum[component_tag] = {
 *         path = "game/common/component_tags"
 *         start_from_root = yes
 *         name = {
 *         	   enum_name
 *         }
 *     }
 * }
 * ```
 *
 * @property name 名称（枚举名）。
 * @property startFromRoot 是否从文件顶部（而非顶级属性）开始查询。
 * @property caseInsensitive （PLS 扩展）是否将复杂枚举值标记为忽略大小写。
 * @property perDefinition （PLS 扩展）是否将同名同类型的复杂枚举值的等效性限制在定义级别（而非文件级别）。
 * @property searchScopeType 查询作用域类型。目前仅支持 `definition`，或者不指定。
 * @property nameConfig `name` 对应的规则。
 * @property enumNameConfigs 在 [nameConfig] 中作为锚点的 `enum_name` 对应的规则集合。
 */
interface CwtComplexEnumConfig : CwtFilePathMatchableConfig {
    @FromKey("complex_enum[$]")
    val name: String
    @FromProperty("start_from_root: boolean", defaultValue = "no")
    val startFromRoot: Boolean
    @FromOption("case_insensitive")
    val caseInsensitive: Boolean
    @FromOption("per_definition")
    val perDefinition: Boolean

    val searchScopeType: String?
    val nameConfig: CwtPropertyConfig
    val enumNameConfigs: List<CwtMemberConfig<*>>

    interface Resolver {
        /** 由属性规则解析为复杂枚举规则。 */
        fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig?
    }

    companion object : Resolver by CwtComplexEnumConfigResolverImpl()
}

// region Implementations

private class CwtComplexEnumConfigResolverImpl : CwtComplexEnumConfig.Resolver, CwtConfigResolverScope {
    private val logger = thisLogger()

    override fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig? = doResolve(config)

    private fun doResolve(config: CwtPropertyConfig): CwtComplexEnumConfig? {
        val name = config.key.removeSurroundingOrNull("complex_enum[", "]")?.orNull() ?: return null
        val propElements = config.properties
        if (propElements.isNullOrEmpty()) {
            logger.warn("Skipped invalid complex enum config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propElements.groupBy { it.key }
        val paths = propGroup.getAll("path").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val pathFile = propGroup.getOne("path_file")?.stringValue
        val pathExtension = propGroup.getOne("path_extension")?.stringValue?.optimizedPathExtension()
        val pathStrict = propGroup.getOne("path_strict")?.booleanValue ?: false
        val pathPatterns = propGroup.getAll("path_pattern").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val startFromRoot = propGroup.getOne("start_from_root")?.booleanValue ?: false
        val caseInsensitive = config.optionData.caseInsensitive
        val perDefinition = config.optionData.perDefinition
        val nameConfig = propGroup.getOne("name")

        if (nameConfig == null) {
            logger.warn("Skipped invalid complex enum config (name: $name): Missing name config.".withLocationPrefix(config))
            return null
        }
        logger.debug { "Resolved complex enum config (name: $name).".withLocationPrefix(config) }
        return CwtComplexEnumConfigImpl(
            config, name,
            paths, pathFile, pathExtension, pathStrict, pathPatterns,
            startFromRoot, caseInsensitive, perDefinition, nameConfig
        )
    }
}

private class CwtComplexEnumConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val paths: Set<String>,
    override val pathFile: String?,
    override val pathExtension: String?,
    override val pathStrict: Boolean,
    override val pathPatterns: Set<String>,
    override val startFromRoot: Boolean,
    override val caseInsensitive: Boolean,
    override val perDefinition: Boolean,
    override val nameConfig: CwtPropertyConfig,
) : UserDataHolderBase(), CwtComplexEnumConfig {
    override val searchScopeType: String? = if (perDefinition) "definition" else null
    override val enumNameConfigs: List<CwtMemberConfig<*>> by lazy {
        selectConfigScope {
            nameConfig.walkDown().filter { c ->
                when (c) {
                    is CwtPropertyConfig -> c.key == "enum_name" || c.stringValue == "enum_name"
                    is CwtValueConfig -> c.stringValue == "enum_name"
                }
            }.toList()
        }
    }

    override fun toString() = "CwtComplexEnumConfigImpl(name='$name')"
}

// endregion
