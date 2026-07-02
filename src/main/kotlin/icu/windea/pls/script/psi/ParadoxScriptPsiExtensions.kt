@file:Suppress("unused")

package icu.windea.pls.script.psi

import com.intellij.psi.util.siblings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.toBooleanYesNo
import icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl
import icu.windea.pls.script.psi.impl.ParadoxScriptScriptedVariableImpl
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptScriptedVariableStub
import java.awt.Color

// region PSI Value Accessors

val ParadoxScriptBoolean.booleanValue: Boolean get() = this.value.toBooleanYesNo()

val ParadoxScriptInt.intValue: Int get() = this.value.toIntOrNull() ?: 0

val ParadoxScriptFloat.floatValue: Float get() = this.value.toFloatOrNull() ?: 0f

val ParadoxScriptString.stringValue: String get() = this.value

val ParadoxScriptColor.colorValue: Color? get() = this.color

// endregion

// region PSI Accessors

val ParadoxScriptExpressionElement.parentProperty: ParadoxScriptProperty? get() = parent?.castOrNull()

val ParadoxScriptMember.parentBlock: ParadoxScriptBlock? get() = parent?.castOrNull()

val ParadoxScriptMember.containingProperty: ParadoxScriptProperty? get() = this as? ParadoxScriptProperty ?: this.parent as? ParadoxScriptProperty

val ParadoxScriptPropertyKey.propertyValue: ParadoxScriptValue? get() = siblings(forward = true, withSelf = false).findIsInstance()

val ParadoxScriptValue.propertyKey: ParadoxScriptPropertyKey? get() = siblings(forward = false, withSelf = false).findIsInstance()

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.propertyValue(): T? = propertyValue?.castOrNull()

val ParadoxScriptScriptedVariable.greenStub: ParadoxScriptScriptedVariableStub? get() = this.castOrNull<ParadoxScriptScriptedVariableImpl>()?.greenStub

val ParadoxScriptProperty.greenStub: ParadoxScriptPropertyStub? get() = this.castOrNull<ParadoxScriptPropertyImpl>()?.greenStub

// endregion

// region PSI Predicates

/** 是否是直接位于块（文件顶级/子句）中的成员。 */
fun ParadoxScriptMember.isBlockMember(): Boolean {
    val parent = parent ?: return false
    return parent is ParadoxScriptBlockElement
}

/** 是否是位于子句结构（属性&值）中的，用于表示游戏数据的表达式元素。 */
fun ParadoxScriptExpressionElement.isDataExpression(): Boolean {
    return when (this) {
        is ParadoxScriptPropertyKey -> true
        is ParadoxScriptValue -> {
            val parent = parent ?: return false
            parent is ParadoxScriptProperty || parent is ParadoxScriptBlockElement
        }
        else -> false
    }
}

fun ParadoxScriptValue.isScriptedVariableValue(): Boolean {
    return parent is ParadoxScriptScriptedVariable
}

fun ParadoxScriptValue.isPropertyValue(): Boolean {
    return parent is ParadoxScriptProperty
}

fun ParadoxScriptValue.isBlockValue(): Boolean {
    val parent = parent ?: return false
    return parent is ParadoxScriptBlockElement
}

// endregion
