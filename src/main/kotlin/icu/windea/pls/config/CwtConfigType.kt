package icu.windea.pls.config

import javax.swing.Icon

/**
 * 规则类型。
 *
 * 用于描述规则文件中作为特殊规则的属性或单独的值的类型，例如类型规则、枚举规则、别名规则等。
 *
 * @see CwtConfigTypes
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
