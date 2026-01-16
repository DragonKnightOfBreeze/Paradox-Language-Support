@file:Suppress("unused")

package icu.windea.pls.script.psi

import com.intellij.psi.util.siblings
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.toBooleanYesNo
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.match.ParadoxMatchOptions
import icu.windea.pls.lang.match.orDefault
import icu.windea.pls.lang.util.ParadoxConfigManager
import icu.windea.pls.script.psi.impl.ParadoxScriptPropertyImpl
import icu.windea.pls.script.psi.impl.ParadoxScriptScriptedVariableImpl
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub
import icu.windea.pls.script.psi.stubs.ParadoxScriptScriptedVariableStub
import java.awt.Color

// region PSI Accessors

val ParadoxScriptExpressionElement.parentProperty: ParadoxScriptProperty?
    get() = parent?.castOrNull()

val ParadoxScriptMember.parentBlock: ParadoxScriptBlock?
    get() = parent?.castOrNull()

val ParadoxScriptPropertyKey.propertyValue: ParadoxScriptValue?
    get() = siblings(forward = true, withSelf = false).findIsInstance()

val ParadoxScriptValue.propertyKey: ParadoxScriptPropertyKey?
    get() = siblings(forward = false, withSelf = false).findIsInstance()

inline fun <reified T : ParadoxScriptValue> ParadoxScriptProperty.propertyValue(): T? {
    return propertyValue?.castOrNull<T>()
}

val ParadoxScriptScriptedVariable.greenStub: ParadoxScriptScriptedVariableStub?
    get() = this.castOrNull<ParadoxScriptScriptedVariableImpl>()?.greenStub

val ParadoxScriptProperty.greenStub: ParadoxScriptPropertyStub?
    get() = this.castOrNull<ParadoxScriptPropertyImpl>()?.greenStub

// endregion

// region Predicates

fun ParadoxScriptMember.isBlockMember(): Boolean {
    return parent.let { it is ParadoxScriptBlockElement || it is ParadoxScriptParameterCondition }
}

fun ParadoxScriptValue.isScriptedVariableValue(): Boolean {
    return parent is ParadoxScriptScriptedVariable
}

fun ParadoxScriptValue.isPropertyValue(): Boolean {
    return parent is ParadoxScriptProperty
}

fun ParadoxScriptExpressionElement.isExpression(): Boolean {
    return when {
        this is ParadoxScriptPropertyKey -> true
        this is ParadoxScriptValue -> parent.let { it is ParadoxScriptProperty || it is ParadoxScriptBlockElement || it is ParadoxScriptParameterCondition }
        else -> false
    }
}

/**
 * 判断当前字符串表达式是否在顶层或者子句中或者作为属性的值，并且拥有唯一匹配的规则。
 */
fun ParadoxScriptExpressionElement.isValidExpression(matchOptions: ParadoxMatchOptions? = null): Boolean {
    return ParadoxConfigManager.getConfigs(this, matchOptions.orDefault().copy(fallback = false)).size == 1
}

fun ParadoxScriptExpressionElement.isResolvableExpression(): Boolean {
    return this is ParadoxScriptStringExpressionElement || this is ParadoxScriptInt || this is ParadoxScriptFloat
}

fun ParadoxScriptExpressionElement.isDefinitionTypeKeyOrName(): Boolean {
    return when {
        this is ParadoxScriptPropertyKey -> isDefinitionTypeKey()
        this is ParadoxScriptValue -> isDefinitionName()
        else -> false
    }
}

fun ParadoxScriptPropertyKey.isDefinitionTypeKey(): Boolean {
    val definition = parentProperty ?: return false
    if (definition.definitionInfo != null) return true
    return false
}

fun ParadoxScriptValue.isDefinitionName(): Boolean {
    // #131
    if (!this.isResolvableExpression()) return false

    val nameProperty = parentProperty ?: return false
    // def = def_name
    if (nameProperty.definitionInfo.let { it != null && it.typeConfig.nameField == "" }) return true
    val block = nameProperty.parentBlock ?: return false
    val definition = block.parentProperty ?: return false
    // def = { name_prop = def_name }
    if (definition.definitionInfo.let { it != null && it.typeConfig.nameField == nameProperty.name }) return true
    return false
}

// endregion

// region Value Accessors

val ParadoxScriptBoolean.booleanValue: Boolean get() = this.value.toBooleanYesNo()

val ParadoxScriptInt.intValue: Int get() = this.value.toIntOrNull() ?: 0

val ParadoxScriptFloat.floatValue: Float get() = this.value.toFloatOrNull() ?: 0f

val ParadoxScriptString.stringValue: String get() = this.value

val ParadoxScriptColor.colorValue: Color? get() = this.color

// endregion
