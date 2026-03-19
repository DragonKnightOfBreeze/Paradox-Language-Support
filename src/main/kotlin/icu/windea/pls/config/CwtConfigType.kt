package icu.windea.pls.config

import javax.swing.Icon

/**
 * 规则类型。
 *
 * 用于描述规则文件中作为特殊规则的属性或单独的值的类型，例如类型规则、枚举规则、别名规则等。
 *
 * ### 解析逻辑
 *
 * 由[CwtConfigService.resolveConfigType][icu.windea.pls.config.config.CwtConfigService.resolveConfigType]根据CWT元素的规则路径（config path）确定：
 *
 * - 首先排除内部规则文件（internal config files），然后获取元素的规则路径。
 * - 根据路径深度和首段路径分派到不同的规则类型：
 *   - 深度1的属性：`single_alias[*]`、`alias[*]`、`directive[*]`。其中`alias`会进一步区分`modifier`、`trigger`、`effect`。
 *   - 深度2+的属性或值：根据首段路径（如`types`、`enums`、`values`、`links`等）匹配对应的规则类型。
 *   - 扩展规则类型（`Extended*`）：不检查元素类型，用于插件自身扩展的规则声明。
 *
 * ### 属性说明
 *
 * @property id 唯一标识符。
 * @property category 所属分类，对应规则组中的分组键（如`"enums"`、`"values"`）。
 * @property isReference 是否表示一个可引用的规则声明。
 * @property icon 在IDE中展示时使用的图标。
 * @property prefix 用于导航和展示的前缀文本。
 * @property description 用于展示的描述文本。
 *
 * @see CwtConfigTypes
 * @see icu.windea.pls.config.config.CwtConfigService.resolveConfigType
 */
@Suppress("unused")
class CwtConfigType private constructor(
    val id: String,
    val category: String? = null,
    val isReference: Boolean = false,
    val icon: Icon? = null,
    val prefix: String? = null,
    val description: String? = null,
) {
    // NOTE 2.1.1 为了与 `CwtDataType` 保持一致，这里直接使用引用相等
    // override fun equals(other: Any?) = super.equals(other)
    //
    // override fun hashCode() = super.hashCode()

    override fun toString(): String = "CwtConfigType(id=$id, category=$category)"

    class Builder(
        private val id: String,
        private val category: String? = null,
        private var isReference: Boolean = false,
        private var icon: Icon? = null,
        private var prefix: String? = null,
        private var description: String? = null,
    ) {
        fun reference() = apply { isReference = true }
        fun icon(value: Icon) = apply { icon = value }
        fun prefix(value: String) = apply { prefix = value }
        fun description(value: String) = apply { description = value }

        fun build(): CwtConfigType = CwtConfigType(id, category, isReference, icon, prefix, description).also { _entries[id] = it }
    }

    companion object {
        private val _entries = mutableMapOf<String, CwtConfigType>()

        @JvmStatic
        val entries: Map<String, CwtConfigType> get() = _entries

        @JvmStatic
        fun builder(id: String, category: String? = null): Builder = Builder(id, category)
    }
}
