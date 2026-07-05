package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.annotations.FromOptionMember
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.config.optimizedPathExtension
import icu.windea.pls.config.util.CwtConfigResolverScope
import icu.windea.pls.config.util.CwtMemberConfigRecursiveVisitor
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 复杂枚举规则。
 *
 * 用于描述复杂枚举，并基于锚点动态定位可选项，作为动态的枚举值。
 * 按照路径模式匹配脚本文件，并在其中进一步匹配锚点。
 * 复杂枚举的枚举值默认不忽略大小写。
 *
 * 路径定位：
 * - `enums/complex_enum[{name}]`。其中 `{name}` 匹配规则名称。
 *
 * 示例：
 *
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
 * > CWTools 兼容性：部分兼容。插件进行了额外的扩展和改进。
 *
 * @property name 规则名称（即枚举名）。
 * @property startFromRoot 是否从文件顶部（而非顶级属性的下一级）开始查询。
 * @property caseInsensitive （扩展）是否将复杂枚举值标记为忽略大小写。
 * @property perDefinition （扩展）是否将同名同类型的复杂枚举值的等效性限制在定义级别（而非文件级别）。
 * @property searchScopeType 查询作用域类型。目前仅支持 `definition`，或者不指定。
 * @property nameConfig `name` 对应的规则。
 * @property enumNameConfigs 在 [nameConfig] 中作为锚点的 `enum_name` 对应的规则集合。
 */
interface CwtComplexEnumConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty>, CwtFilePathMatchableConfig<CwtProperty> {
    @FromName("complex_enum[$]")
    val name: String
    @FromMember("start_from_root: boolean", defaultValue = "no")
    val startFromRoot: Boolean
    @FromOptionMember("case_insensitive")
    val caseInsensitive: Boolean
    @FromOptionMember("per_definition")
    val perDefinition: Boolean

    val searchScopeType: String?
    val nameConfig: CwtPropertyConfig?
    val enumNameConfigs: List<CwtMemberConfig<*>>

    companion object {
        /** 由属性规则解析为复杂枚举规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig? {
            return CwtComplexEnumConfigResolver.resolve(config)
        }

        /** 由列规则解析为复杂枚举规则。 */
        fun resolveFromColumnConfig(config: CwtPropertyConfig): CwtComplexEnumConfig? {
            return CwtComplexEnumConfigResolver.resolveFromColumnConfig(config)
        }
    }
}

// region Implementations

private object CwtComplexEnumConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtComplexEnumConfig? {
        val name = config.key.removeSurroundingOrNull("complex_enum[", "]")?.orNull() ?: return null
        val propConfigs = config.properties
        if (propConfigs.isNullOrEmpty()) {
            logger.warn("Skipped invalid complex enum config (name: $name): Missing properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propConfigs.groupBy { it.key }
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

    fun resolveFromColumnConfig(config: CwtPropertyConfig): CwtComplexEnumConfig? {
        val name = config.declareComplexEnum?.orNull() ?: return null
        return CwtComplexEnumConfigFromColumnConfig(config, name)
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
    override val searchScopeType: String? = computeSearchScopeType()
    override val enumNameConfigs: List<CwtMemberConfig<*>> by lazy { computeEnumNameConfigs().optimized() }

    private fun computeSearchScopeType(): String? {
        return if (perDefinition) "definition" else null
    }

    private fun computeEnumNameConfigs(): List<CwtMemberConfig<*>> {
        val result = mutableListOf<CwtMemberConfig<*>>()
        nameConfig.accept(object : CwtMemberConfigRecursiveVisitor() {
            override fun visitProperty(config: CwtPropertyConfig): Boolean {
                if (config.key == "enum_name" || config.stringValue == "enum_name") result.add(config)
                return super.visitProperty(config)
            }

            override fun visitValue(config: CwtValueConfig): Boolean {
                if (config.stringValue == "enum_name") result.add(config)
                return super.visitValue(config)
            }
        })
        return result
    }

    override fun toString() = "CwtComplexEnumConfigImpl(name='$name')"
}

private class CwtComplexEnumConfigFromColumnConfig(
    override val config: CwtPropertyConfig,
    override val name: String,
) : UserDataHolderBase(), CwtComplexEnumConfig {
    override val startFromRoot: Boolean get() = false
    override val caseInsensitive: Boolean get() = false
    override val perDefinition: Boolean get() = false
    override val searchScopeType: String? get() = null
    override val nameConfig: CwtPropertyConfig? get() = null
    override val enumNameConfigs: List<CwtMemberConfig<*>> get() = emptyList()

    override fun toString() = "CwtComplexEnumConfigFromColumnConfig(name='$name')"
}

// endregion
