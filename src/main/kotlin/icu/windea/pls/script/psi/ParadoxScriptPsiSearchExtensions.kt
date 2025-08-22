@file:Suppress("unused", "UNCHECKED_CAST")

package icu.windea.pls.script.psi

import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.expression.*
import icu.windea.pls.lang.util.dataFlow.*
import icu.windea.pls.lang.util.manipulators.*
import icu.windea.pls.model.*
import icu.windea.pls.script.*

fun ParadoxScriptFile.members(): ParadoxMemberSequence {
    return ParadoxScriptManipulator.buildMemberSequence(this)
}

fun ParadoxScriptMemberContainer.members(): ParadoxMemberSequence {
    return ParadoxScriptManipulator.buildMemberSequence(this)
}

fun ParadoxScriptFile.properties(): ParadoxPropertySequence {
    return ParadoxScriptManipulator.buildMemberSequence(this).transform { filterIsInstance<ParadoxScriptProperty>() }
}

fun ParadoxScriptMemberContainer.properties(): ParadoxPropertySequence {
    return ParadoxScriptManipulator.buildMemberSequence(this).transform { filterIsInstance<ParadoxScriptProperty>() }
}

fun ParadoxScriptFile.values(): ParadoxValueSequence {
    return ParadoxScriptManipulator.buildMemberSequence(this).transform { filterIsInstance<ParadoxScriptValue>() }
}

fun ParadoxScriptMemberContainer.values(): ParadoxValueSequence {
    return ParadoxScriptManipulator.buildMemberSequence(this).transform { filterIsInstance<ParadoxScriptValue>() }
}

/**
 * 得到指定名字的属性。
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果为空字符串且自身是脚本属性，则返回自身。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
fun PsiElement.findProperty(
    propertyName: String? = null,
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false
): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    if (propertyName != null && propertyName.isEmpty()) return this as? ParadoxScriptProperty
    val block = when {
        this is ParadoxScriptDefinitionElement -> this.block
        this is ParadoxScriptBlock -> this
        else -> null
    }
    var result: ParadoxScriptProperty? = null
    block?.properties()?.options(conditional, inline)?.process {
        if (propertyName == null || propertyName.equals(it.name, ignoreCase)) {
            result = it
            false
        } else {
            true
        }
    }
    return result
}

/**
 * 得到符合指定条件的属性。可能为null，可能是定义，可能是脚本文件。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
fun PsiElement.findProperty(
    conditional: Boolean = false,
    inline: Boolean = false,
    propertyPredicate: (String) -> Boolean
): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    if (propertyPredicate("")) return this as? ParadoxScriptProperty
    val block = when {
        this is ParadoxScriptDefinitionElement -> this.block
        this is ParadoxScriptBlock -> this
        else -> null
    }
    var result: ParadoxScriptProperty? = null
    block?.properties()?.options(conditional, inline)?.process {
        if (propertyPredicate(it.name)) {
            result = it
            false
        } else {
            true
        }
    }
    return result
}

/**
 * 基于路径向下查找指定的属性或值。如果路径为空，则返回查找到的第一个属性或值。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 * @see ParadoxExpressionPath
 * @see ParadoxScriptMemberElement
 */
fun <T : ParadoxScriptMemberElement> ParadoxScriptMemberElement.findByPath(
    path: String,
    targetType: Class<T>,
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false
): T? {
    if (language !is ParadoxScriptLanguage) return null
    var current: ParadoxScriptMemberElement = this
    if (path.isNotEmpty()) {
        val elementPath = ParadoxExpressionPath.resolve(path)
        for (subPath in elementPath.subPaths) {
            if (subPath == "-") {
                return null //TODO 暂不支持
            } else {
                current = current.findProperty(subPath, ignoreCase, conditional, inline) ?: return null
            }
        }
    } else {
        current = current.findProperty("", ignoreCase, conditional, inline) ?: return null
    }
    when {
        ParadoxScriptProperty::class.java.isAssignableFrom(targetType) -> {
            return current.castOrNull<ParadoxScriptProperty>() as? T
        }
        ParadoxScriptValue::class.java.isAssignableFrom(targetType) -> {
            return current.castOrNull<ParadoxScriptProperty>()?.propertyValue<ParadoxScriptValue>() as? T
        }
    }
    return null
}

/**
 * 向上得到第一个定义。
 * 可能为null，可能为自身。
 */
fun PsiElement.findParentDefinition(): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    var current: PsiElement = this
    while (current !is PsiDirectory) {
        ProgressManager.checkCanceled()
        if (current is ParadoxScriptDefinitionElement && current.definitionInfo != null) return current
        current = current.parent ?: break
    }
    return null
}

/**
 * 向上得到第一个属性。可能为null，可能是定义，可能是脚本文件。
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果得到的是脚本文件，则忽略。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
fun PsiElement.findParentProperty(
    propertyName: String? = null,
    ignoreCase: Boolean = true,
    fromParentBlock: Boolean = false
): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    var current: PsiElement = when {
        fromParentBlock -> this.parentOfType<ParadoxScriptBlockElement>() ?: return null
        this is ParadoxScriptProperty -> this.parent
        else -> this
    }
    while (current !is PsiFile) {
        ProgressManager.checkCanceled()
        if (current is ParadoxScriptDefinitionElement) {
            if (propertyName == null || propertyName.equals(current.name, ignoreCase)) return current
        }
        if (current is ParadoxScriptBlock && !current.isPropertyValue()) return null
        current = current.parent ?: break
    }
    if (current is ParadoxScriptFile) return current
    return null
}

/**
 * 向上得到第一个符合指定条件的属性。可能为null，可能是定义，可能是脚本文件。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
fun PsiElement.findParentProperty(
    fromParentBlock: Boolean = false,
    propertyPredicate: (String) -> Boolean
): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    var current: PsiElement = when {
        fromParentBlock -> this.parentOfType<ParadoxScriptBlockElement>() ?: return null
        this is ParadoxScriptProperty -> this.parent
        else -> this
    }
    while (current !is PsiFile) {
        ProgressManager.checkCanceled()
        if (current is ParadoxScriptDefinitionElement) {
            if (propertyPredicate(current.name)) return current
        }
        if (current is ParadoxScriptBlock && !current.isPropertyValue()) return null
        current = current.parent ?: break
    }
    if (current is ParadoxScriptFile) return current
    return null
}

/**
 * 基于路径向上查找指定的属性。如果路径为空，则返回查找到的第一个属性或值。
 * @param definitionType 如果不为null则在查找到指定的属性之后再向上查找一层属性，并要求其是定义，如果接着不为空字符串则要求匹配该定义类型表达式。
 * @see ParadoxExpressionPath
 * @see ParadoxScriptMemberElement
 * @see ParadoxDefinitionTypeExpression
 */
fun ParadoxScriptMemberElement.findParentByPath(
    path: String = "",
    ignoreCase: Boolean = true,
    definitionType: String? = null
): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    var current: ParadoxScriptMemberElement = this
    if (path.isNotEmpty()) {
        val elementPath = ParadoxExpressionPath.resolve(path)
        for (subPath in elementPath.subPaths.reversed()) {
            if (subPath == "-") {
                current = current.parentOfType<ParadoxScriptBlock>() ?: return null
            } else {
                current = current.findParentProperty(subPath, ignoreCase) ?: return null
            }
        }
    }
    if (definitionType != null) {
        val result = current.findParentProperty(null) ?: return null
        val definitionInfo = result.definitionInfo ?: return null
        if (definitionType.isNotEmpty()) {
            if (!ParadoxDefinitionTypeExpression.resolve(definitionType).matches(definitionInfo)) return null
        }
        return result
    }
    return current as? ParadoxScriptDefinitionElement?
}
