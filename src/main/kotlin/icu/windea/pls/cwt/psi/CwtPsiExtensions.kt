@file:Suppress("unused")

package icu.windea.pls.cwt.psi

import com.intellij.psi.util.siblings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.toBooleanYesNo

// region PSI Value Accessors

val CwtBoolean.booleanValue: Boolean get() = this.value.toBooleanYesNo()

val CwtInt.intValue: Int get() = this.value.toIntOrNull() ?: 0

val CwtFloat.floatValue: Float get() = this.value.toFloatOrNull() ?: 0f

val CwtString.stringValue: String get() = this.value

// endregion

// region PSI Accessors

val CwtMember.containingMember: CwtMember get() = castOrNull<CwtValue>()?.parentProperty ?: this

val CwtExpressionElement.parentProperty: CwtProperty? get() = parent?.castOrNull()

val CwtMember.parentBlock: CwtBlock? get() = parent?.castOrNull()

val CwtPropertyKey.propertyValue: CwtValue? get() = siblings(forward = true, withSelf = false).findIsInstance()

val CwtValue.propertyKey: CwtPropertyKey? get() = siblings(forward = false, withSelf = false).findIsInstance()

inline fun <reified T : CwtValue> CwtProperty.propertyValue(): T? = propertyValue?.castOrNull()

// endregion

// region PSI Predicates

/** 是否是直接位于块（文件顶级/子句）中的成员。 */
fun CwtMember.isBlockMember(): Boolean {
    val parent = parent ?: return false
    return parent is CwtRootBlock || (parent is CwtBlock && parent.parent !is CwtOption)
}

/** 是否是位于子句结构（属性&值）中的，用于表示规则数据的表达式元素。 */
fun CwtExpressionElement.isDataExpression(): Boolean {
    return when (this) {
        is CwtPropertyKey -> true
        is CwtValue -> {
            val parent = parent ?: return false
            parent is CwtProperty || parent is CwtRootBlock || (parent is CwtBlock && parent.parent !is CwtOption)
        }
        else -> false
    }
}

fun CwtValue.isPropertyValue(): Boolean {
    return parent is CwtProperty
}

fun CwtValue.isBlockValue(): Boolean {
    val parent = parent ?: return false
    return parent is CwtRootBlock || (parent is CwtBlock && parent.parent !is CwtOption)
}

fun CwtValue.isOptionValue(): Boolean {
    return parent is CwtOption
}

fun CwtValue.isOptionBlockValue(): Boolean {
    val parent = parent ?: return false
    return parent is CwtOptionComment || (parent is CwtBlock && parent.parent is CwtOption)
}

// endregion
