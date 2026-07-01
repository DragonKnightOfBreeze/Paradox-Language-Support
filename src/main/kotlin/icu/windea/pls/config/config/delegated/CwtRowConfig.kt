package icu.windea.pls.config.config.delegated

import com.intellij.openapi.diagnostic.debug
import com.intellij.openapi.diagnostic.thisLogger
import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.config.annotations.FromMember
import icu.windea.pls.config.annotations.FromName
import icu.windea.pls.config.attributes.CwtRowConfigAttributes
import icu.windea.pls.config.attributes.CwtRowConfigAttributesEvaluator
import icu.windea.pls.config.config.CwtConfigResolverScope
import icu.windea.pls.config.config.CwtDelegatedConfig
import icu.windea.pls.config.config.CwtFilePathMatchableConfig
import icu.windea.pls.config.config.CwtIdMatchableConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtRowType
import icu.windea.pls.config.config.booleanValue
import icu.windea.pls.config.config.stringValue
import icu.windea.pls.config.optimizedPath
import icu.windea.pls.config.optimizedPathExtension
import icu.windea.pls.core.collections.getAll
import icu.windea.pls.core.collections.getOne
import icu.windea.pls.core.optimized
import icu.windea.pls.core.orNull
import icu.windea.pls.core.removeSurroundingOrNull
import icu.windea.pls.cwt.psi.CwtProperty

/**
 * 行规则。
 *
 * 用于描述 CSV 文件中每一行允许的列的列名与可选值，从而提供代码补全、代码检查等功能。
 * 按照路径模式匹配 CSV 文件。
 *
 * 路径定位：
 * - `rows/row[{name}]`。其中 `{name}` 匹配规则名称。
 *
 * 示例：
 *
 * ```cwt
 * rows = {
 *     row[weapon_template] = {
 *         path = "game/common/weapon_templates"
 *         path_extension = .csv
 *         skip_last_column = yes
 *         columns = {
 *             key = <weapon_template>
 *             damage = float
 *             ## declare_complex_enum = weapon_tag
 *             tag = scalar
 *         }
 *     }
 * }
 * ```
 *
 * > CWTools 兼容性：不兼容。插件作为扩展提供。
 *
 * @property name 规则名称。
 * @property type 行类型（`key`/`index`，默认为 `key`）。决定如何匹配其中的每一列。
 * @property skipLastRow 解析与匹配时，是否忽略最后一行。
 * @property skipLastColumn 解析与匹配时，是否忽略最后一列。
 * @property columns 列规则的列表（一组属性规则，键为列名，值为需要匹配的数据表达式）。
 * @property endColumn 若匹配到该列名，视作可省略的最后一列。
 * @property attributes 综合属性。
 */
interface CwtRowConfig : CwtDelegatedConfig<CwtProperty, CwtPropertyConfig>, CwtIdMatchableConfig<CwtProperty>, CwtFilePathMatchableConfig<CwtProperty> {
    @FromName("row[$]")
    val name: String
    @FromMember("type: string?", allowedValues = ["key", "index"], defaultValue = "key")
    val type: CwtRowType
    @FromMember("skip_last_row: boolean", defaultValue = "no")
    val skipLastRow: Boolean
    @FromMember("skip_last_column: boolean", defaultValue = "no")
    val skipLastColumn: Boolean
    @FromMember("columns: ColumnConfigs")
    val columns: List<CwtPropertyConfig>
    @FromMember("end_column: string?")
    val endColumn: String?

    val attributes: CwtRowConfigAttributes

    companion object {
        /** 由属性规则解析为行规则。 */
        @JvmStatic
        fun resolve(config: CwtPropertyConfig): CwtRowConfig? {
            return CwtRowConfigResolver.resolve(config)
        }
    }
}

// region Implementations

private object CwtRowConfigResolver : CwtConfigResolverScope {
    private val logger = thisLogger()

    fun resolve(config: CwtPropertyConfig): CwtRowConfig? {
        val name = config.key.removeSurroundingOrNull("row[", "]")?.orNull() ?: return null
        val propConfigs = config.properties
        if (propConfigs.isNullOrEmpty()) {
            logger.warn("Skipped invalid row config (name: $name): Empty properties.".withLocationPrefix(config))
            return null
        }

        val propGroup = propConfigs.groupBy { it.key }
        val paths = propGroup.getAll("path").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val pathFile = propGroup.getOne("path_file")?.stringValue
        val pathExtension = propGroup.getOne("path_extension")?.stringValue?.optimizedPathExtension()
        val pathStrict = propGroup.getOne("path_strict")?.booleanValue ?: false
        val pathPatterns = propGroup.getAll("path_pattern").mapNotNullTo(sortedSetOf()) { it.stringValue?.optimizedPath() }.optimized()
        val type = propGroup.getOne("type")?.stringValue.let { CwtRowType.resolve(it) }
        val skipLastRow = propGroup.getOne("skip_last_row")?.booleanValue ?: false
        val skipLastColumn = propGroup.getOne("skip_last_column")?.booleanValue ?: false
        val columns = propGroup.getOne("columns")?.properties?.optimized().orEmpty()
        val endColumn = propGroup.getOne("end_column")?.stringValue

        logger.debug { "Resolved row config (name: $name).".withLocationPrefix(config) }
        return CwtRowConfigImpl(
            config, name,
            paths, pathFile, pathExtension, pathStrict, pathPatterns,
            type, skipLastRow, skipLastColumn, columns, endColumn
        )
    }
}

private class CwtRowConfigImpl(
    override val config: CwtPropertyConfig,
    override val name: String,
    override val paths: Set<String>,
    override val pathFile: String?,
    override val pathExtension: String?,
    override val pathStrict: Boolean,
    override val pathPatterns: Set<String>,
    override val type: CwtRowType,
    override val skipLastRow: Boolean,
    override val skipLastColumn: Boolean,
    override val columns: List<CwtPropertyConfig>,
    override val endColumn: String?
) : UserDataHolderBase(), CwtRowConfig {
    override val attributes: CwtRowConfigAttributes by lazy { CwtRowConfigAttributesEvaluator().evaluate(this) }

    override fun toString() = "CwtRowConfigImpl(name='$name')"
}

// endregion
