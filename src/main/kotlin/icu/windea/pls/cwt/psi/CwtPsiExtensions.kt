@file:Suppress("unused")

package icu.windea.pls.cwt.psi

import com.intellij.psi.util.siblings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.toBooleanYesNo

// region PSI Accessors

val CwtExpressionElement.parentProperty: CwtProperty?
    get() = parent?.castOrNull()

val CwtMember.parentBlock: CwtBlock?
    get() = parent?.castOrNull()

val CwtPropertyKey.propertyValue: CwtValue?
    get() = siblings(forward = true, withSelf = false).findIsInstance()

val CwtValue.propertyKey: CwtPropertyKey?
    get() = siblings(forward = false, withSelf = false).findIsInstance()

inline fun <reified T : CwtValue> CwtProperty.propertyValue(): T? {
    return propertyValue?.castOrNull<T>()
}

// endregion

// region Predicates

fun CwtValue.isPropertyValue(): Boolean {
    val parent = parent
    return parent is CwtProperty
}

fun CwtValue.isBlockValue(): Boolean {
    val parent = parent
    return parent is CwtRootBlock || (parent is CwtBlock && parent.parent !is CwtOption)
}

fun CwtValue.isOptionValue(): Boolean {
    val parent = parent
    return parent is CwtOption
}

fun CwtValue.isOptionBlockValue(): Boolean {
    val parent = parent
    return parent is CwtOptionComment || (parent is CwtBlock && parent.parent is CwtOption)
}

fun CwtExpressionElement.isExpression(): Boolean {
    return when {
        this is CwtPropertyKey -> true
        this is CwtValue -> parent.let { it is CwtProperty || it is CwtRootBlock || (it is CwtBlock && it.parent !is CwtOption) }
        else -> false
    }
}

// endregion

// region Value Accessors

val CwtBoolean.booleanValue: Boolean get() = this.value.toBooleanYesNo()

val CwtInt.intValue: Int get() = this.value.toIntOrNull() ?: 0

val CwtFloat.floatValue: Float get() = this.value.toFloatOrNull() ?: 0f

val CwtString.stringValue: String get() = this.value

// endregion
