@file:Suppress("unused", "UNCHECKED_CAST")

package icu.windea.pls.lang.psi

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.WalkingSequence
import icu.windea.pls.core.collections.process
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isPropertyValue
import icu.windea.pls.script.psi.propertyValue

fun ParadoxScriptMemberContainer.members(): WalkingSequence<ParadoxScriptMember> {
    return ParadoxPsiSequenceBuilder.members(this)
}

fun ParadoxScriptMemberContainer.properties(): WalkingSequence<ParadoxScriptProperty> {
    return ParadoxPsiSequenceBuilder.members(this).transform { filterIsInstance<ParadoxScriptProperty>() }
}

fun ParadoxScriptMemberContainer.values(): WalkingSequence<ParadoxScriptValue> {
    return ParadoxPsiSequenceBuilder.members(this).transform { filterIsInstance<ParadoxScriptValue>() }
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
    block?.properties()?.options { conditional(conditional) + inline(inline) }?.process {
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
 * 得到符合指定条件的属性。可能为 `null`，可能是定义，可能是脚本文件。
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
    block?.properties()?.options { conditional(conditional) + inline(inline) }?.process {
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
 * @see ParadoxMemberPath
 * @see ParadoxScriptMember
 */
fun <T : ParadoxScriptMember> ParadoxScriptMember.findByPath(
    path: String,
    targetType: Class<T>,
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false
): T? {
    if (language !is ParadoxScriptLanguage) return null
    var current: ParadoxScriptMember = this
    if (path.isNotEmpty()) {
        val memberPath = ParadoxMemberPath.resolve(path)
        for (subPath in memberPath.subPaths) {
            if (subPath == "-") {
                return null // TODO 暂不支持
            } else {
                current = current.findProperty(subPath, ignoreCase, conditional, inline) ?: return null
            }
            ProgressManager.checkCanceled()
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
 * 向上得到第一个定义。可能为 `null`，可能为自身。
 */
fun PsiElement.findParentDefinition(): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    return parents(withSelf = true)
        .takeWhile { it !is PsiDirectory }
        .filterIsInstance<ParadoxScriptDefinitionElement>()
        .find { it.definitionInfo != null }
}

/**
 * 向上得到第一个定义注入。可能为 `null`，可能为自身。
 */
fun PsiElement.findParentDefinitionInjection(): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    return parents(withSelf = true)
        .takeWhile { it !is ParadoxScriptRootBlock }
        .filterIsInstance<ParadoxScriptProperty>()
        .find { it.parent is ParadoxScriptRootBlock && it.definitionInjectionInfo != null }
}

/**
 * 向上得到第一个定义或定义注入。可能为 `null`，可能为自身。
 */
fun PsiElement.findParentDefinitionOrInjection(): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    return parents(withSelf = true)
        .takeWhile { it !is PsiDirectory }
        .filterIsInstance<ParadoxScriptDefinitionElement>()
        .find { it.definitionInfo != null || (it is ParadoxScriptProperty && it.parent is ParadoxScriptRootBlock && it.definitionInjectionInfo != null) }
}

/**
 * 向上得到第一个属性。可能为 `null`，可能是定义，可能是脚本文件。
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果得到的是脚本文件，则忽略。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
fun PsiElement.findParentProperty(
    propertyName: String? = null,
    ignoreCase: Boolean = true,
    fromParentBlock: Boolean = false
): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    var current = this
    when {
        fromParentBlock -> current = current.parentOfType<ParadoxScriptBlockElement>() ?: return null
        current is ParadoxScriptMember -> current = current.parent ?: return null
    }
    while (current !is PsiFile) {
        if (current is ParadoxScriptDefinitionElement) {
            if (propertyName == null || propertyName.equals(current.name, ignoreCase)) return current
        }
        if (current is ParadoxScriptBlock && !current.isPropertyValue()) return null
        current = current.parent ?: break
        ProgressManager.checkCanceled()
    }
    if (current is ParadoxScriptFile) return current
    return null
}

/**
 * 向上得到第一个符合指定条件的属性。可能为 `null`，可能是定义，可能是脚本文件。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
fun PsiElement.findParentProperty(
    fromParentBlock: Boolean = false,
    propertyPredicate: (String) -> Boolean
): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    var current = this
    when {
        fromParentBlock -> current = current.parentOfType<ParadoxScriptBlockElement>() ?: return null
        current is ParadoxScriptMember -> current = current.parent ?: return null
    }
    while (current !is PsiFile) {
        if (current is ParadoxScriptDefinitionElement) {
            if (propertyPredicate(current.name)) return current
        }
        if (current is ParadoxScriptBlock && !current.isPropertyValue()) return null
        current = current.parent ?: break
        ProgressManager.checkCanceled()
    }
    if (current is ParadoxScriptFile) return current
    return null
}

/**
 * 基于路径向上查找指定的属性或值（块）。如果路径为空，则返回查找到的第一个属性或值（块）。
 * @param definitionType 如果不为null则在查找到指定的属性之后再向上查找一层属性，并要求其是定义，如果接着不为空字符串则要求匹配该定义类型表达式。
 * @see ParadoxMemberPath
 * @see ParadoxScriptMember
 * @see ParadoxDefinitionTypeExpression
 */
fun ParadoxScriptMember.findParentByPath(
    path: String = "",
    ignoreCase: Boolean = true,
    definitionType: String? = null
): PsiElement? {
    if (language !is ParadoxScriptLanguage) return null
    var current = this
    if (path.isNotEmpty()) {
        val memberPath = ParadoxMemberPath.resolve(path)
        for (subPath in memberPath.subPaths.reversed()) {
            current = when (subPath) {
                "-" -> current.parent?.castOrNull<ParadoxScriptBlock>() ?: return null
                else -> current.findParentProperty(subPath, ignoreCase) ?: return null
            }
            ProgressManager.checkCanceled()
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
    return current
}
