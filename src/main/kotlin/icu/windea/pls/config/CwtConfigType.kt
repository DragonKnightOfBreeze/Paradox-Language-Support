package icu.windea.pls.config

import icu.windea.pls.config.CwtConfigType.Companion.entries
import icu.windea.pls.config.config.CwtConfigService
import javax.swing.Icon

/**
 * 规则类型。
 *
 * 用于描述规则文件中作为特殊规则的属性或单独的值的类型，例如类型规则、枚举规则、别名规则等。
 *
 * ### 解析逻辑
 *
 * 由 [CwtConfigService.resolveConfigType] 根据 CWT 元素的规则路径（config path）确定：
 *
 * - 首先排除内部规则文件（internal config files），然后获取元素的规则路径。
 * - 根据路径深度和首段路径分派到不同的规则类型。
 *
 * ### 备注
 *
 * 此类使用引用相等（identity equality）而非结构相等。所有实例通过 [Builder] 构建并注册到 [entries] 中。
 *
 * @property id 唯一标识符。
 * @property category 所属分类。
 * @property isReference 是否表示一个可引用的规则声明。
 * @property icon 在 IDE 中展示时使用的图标。
 * @property prefix 用于导航和展示的前缀文本。
 * @property description 用于展示的描述文本。
 *
 * @see CwtConfigTypes
 * @see CwtConfigService.resolveConfigType
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
