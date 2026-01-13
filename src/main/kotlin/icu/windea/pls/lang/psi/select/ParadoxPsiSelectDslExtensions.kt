@file:Suppress("unused")

package icu.windea.pls.lang.psi.select

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.generateSequenceFromSeeds
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
import icu.windea.pls.script.psi.parentBlock

// region Old

/**
 * 得到指定名字的属性。可能为 `null`，可能是定义。
 *
 * @param propertyName 要查找到的属性的名字。如果为 `null`，则不指定。如果为空字符串且自身是脚本属性，则返回自身。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.propertyOld(
    propertyName: String? = null,
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false,
): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    if (propertyName != null && propertyName.isEmpty()) return this as? ParadoxScriptProperty
    val block = when {
        this is ParadoxScriptFile -> this.block
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
 * 得到符合指定条件的属性。可能为 `null`，可能是定义。
 *
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.propertyOld(
    conditional: Boolean = false,
    inline: Boolean = false,
    propertyPredicate: (String) -> Boolean,
): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    if (propertyPredicate("")) return this as? ParadoxScriptProperty
    val block = when {
        this is ParadoxScriptFile -> this.block
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
 * 向上得到第一个定义。可能为 `null`，可能为自身。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinitionOld(): ParadoxScriptDefinitionElement? {
    return parents(withSelf = true)
        .takeWhile { it !is PsiDirectory }
        .filterIsInstance<ParadoxScriptDefinitionElement>()
        .find { it.definitionInfo != null }
}

/**
 * 向上得到第一个作为脚本属性的定义。可能为 `null`，可能为自身。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentPropertyDefinitionOld(): ParadoxScriptProperty? {
    return parents(withSelf = true)
        .takeWhile { it !is ParadoxScriptRootBlock }
        .filterIsInstance<ParadoxScriptProperty>()
        .find { it.definitionInfo != null }
}

/**
 * 向上得到第一个定义注入。可能为 `null`，可能为自身。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinitionInjectionOld(): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    return parents(withSelf = true)
        .takeWhile { it !is ParadoxScriptRootBlock }
        .filterIsInstance<ParadoxScriptProperty>()
        .find { it.parent is ParadoxScriptRootBlock && it.definitionInjectionInfo != null }
}

/**
 * 向上得到第一个定义或定义注入。可能为 `null`，可能为自身。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinitionOrInjectionOld(): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    return parents(withSelf = true)
        .takeWhile { it !is PsiDirectory }
        .filterIsInstance<ParadoxScriptDefinitionElement>()
        .find { it.definitionInfo != null || (it is ParadoxScriptProperty && it.parent is ParadoxScriptRootBlock && it.definitionInjectionInfo != null) }
}

/**
 * 向上得到第一个作为脚本属性的定义或定义注入。可能为 `null`，可能为自身。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentPropertyDefinitionOrInjectionOld(): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    return parents(withSelf = true)
        .takeWhile { it !is ParadoxScriptRootBlock }
        .filterIsInstance<ParadoxScriptProperty>()
        .find { it.definitionInfo != null || (it.parent is ParadoxScriptRootBlock && it.definitionInjectionInfo != null) }
}

/**
 * 向上得到第一个属性。可能为 `null`，可能是定义，可能是脚本文件。
 *
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果得到的是脚本文件，则忽略。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentOld(
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
 *
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentOld(
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
 * 基于路径向下查找指定的属性或值。如果路径为空，则返回查找到的第一个属性或值。
 *
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 *
 * @see ParadoxMemberPath
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMember.propertyByPathOld(
    path: String,
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false
): ParadoxScriptProperty? {
    // if (language !is ParadoxScriptLanguage) return null
    var current: ParadoxScriptMember = this
    if (path.isNotEmpty()) {
        val memberPath = ParadoxMemberPath.resolve(path)
        for (subPath in memberPath.subPaths) {
            if (subPath == "-") {
                return null // TODO 暂不支持
            } else {
                current = current.propertyOld(subPath, ignoreCase, conditional, inline) ?: return null
            }
            ProgressManager.checkCanceled()
        }
    } else {
        current = current.propertyOld("", ignoreCase, conditional, inline) ?: return null
    }
    return current.castOrNull()
}

/**
 * 基于路径向上查找指定的属性或值（块）。如果路径为空，则返回查找到的第一个属性或值（块）。
 *
 * @param definitionType 如果不为null则在查找到指定的属性之后再向上查找一层属性，并要求其是定义，如果接着不为空字符串则要求匹配该定义类型表达式。
 *
 * @see ParadoxMemberPath
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMember.parentByPathOld(
    path: String = "",
    ignoreCase: Boolean = true,
    definitionType: String? = null
): PsiElement? {
    // if (language !is ParadoxScriptLanguage) return null
    var current = this
    if (path.isNotEmpty()) {
        val memberPath = ParadoxMemberPath.resolve(path)
        for (subPath in memberPath.subPaths.reversed()) {
            current = when (subPath) {
                "-" -> current.parentBlock ?: return null
                else -> current.parentOld(subPath, ignoreCase) ?: return null
            }
            ProgressManager.checkCanceled()
        }
    }
    if (definitionType != null) {
        val result = current.parentOld(null) ?: return null
        val definitionInfo = result.definitionInfo ?: return null
        if (definitionType.isNotEmpty()) {
            if (!ParadoxDefinitionTypeExpression.resolve(definitionType).matches(definitionInfo)) return null
        }
        return result
    }
    return current
}

// endregion

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.walkUp(): Sequence<PsiElement> {
    return generateSequence(this) { it.parent }
}

private fun ParadoxScriptMemberContainer.rootContainer(): ParadoxScriptMemberContainer? {
    return when (this) {
        is ParadoxScriptFile -> this.block
        is ParadoxScriptProperty -> this.block
        else -> this
    }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMemberContainer.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember> {
    val seeds = members(conditional, inline)
    return generateSequenceFromSeeds(traversal, seeds.asIterable()) { member ->
        (member).members(conditional, inline).asIterable()
    }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.memberContainer(ofSelf: Boolean = true): ParadoxScriptMemberContainer? {
    if (language !is ParadoxScriptLanguage) return null
    if (ofSelf) this.castOrNull<ParadoxScriptMemberContainer>()?.let { return it }
    return parents(withSelf = ofSelf)
        .filterIsInstance<ParadoxScriptMemberContainer>()
        .firstOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentMemberContainer(ofSelf: Boolean = false): ParadoxScriptMemberContainer? {
    return memberContainer(ofSelf = ofSelf)
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.asProperty(): ParadoxScriptProperty? {
    return this.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<*>.asProperty(): Sequence<ParadoxScriptProperty> {
    return filterIsInstance<ParadoxScriptProperty>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.asValue(): ParadoxScriptValue? {
    return this.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<*>.asValue(): Sequence<ParadoxScriptValue> {
    return filterIsInstance<ParadoxScriptValue>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.asBlock(): ParadoxScriptBlock? {
    return this.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<*>.asBlock(): Sequence<ParadoxScriptBlock> {
    return filterIsInstance<ParadoxScriptBlock>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptProperty.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty? {
    return takeIf { icu.windea.pls.core.match.PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty> {
    return filter { icu.windea.pls.core.match.PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptProperty.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty? {
    return takeIf { keys.any { key -> icu.windea.pls.core.match.PathMatcher.matches(it.name, key, ignoreCase, usePattern) } }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty> {
    return filter { property -> keys.any { key -> icu.windea.pls.core.match.PathMatcher.matches(property.name, key, ignoreCase, usePattern) } }
}

private fun PsiElement.selectValue(): String? {
    return when (this) {
        is ParadoxScriptProperty -> this.value
        is ParadoxScriptValue -> this.value
        else -> null
    }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : PsiElement> T.ofValue(value: String, ignoreCase: Boolean = true): T? {
    val current = selectValue() ?: return null
    return takeIf { current.equals(value, ignoreCase) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : PsiElement> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectValue()?.equals(value, ignoreCase) == true }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : PsiElement> T.ofValues(values: Collection<String>, ignoreCase: Boolean = true): T? {
    val current = selectValue() ?: return null
    return takeIf { values.any { value -> current.equals(value, ignoreCase) } }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : PsiElement> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true): Sequence<T> {
    return filter { element ->
        val current = element.selectValue() ?: return@filter false
        values.any { value -> current.equals(value, ignoreCase) }
    }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.property(
    propertyName: String? = null,
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false,
): ParadoxScriptProperty? {
    return propertyOld(propertyName, ignoreCase, conditional, inline)
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.property(
    conditional: Boolean = false,
    inline: Boolean = false,
    propertyPredicate: (String) -> Boolean,
): ParadoxScriptProperty? {
    return propertyOld(conditional, inline, propertyPredicate)
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinition(): ParadoxScriptDefinitionElement? {
    return parentDefinitionOld()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentPropertyDefinition(): ParadoxScriptProperty? {
    return parentPropertyDefinitionOld()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinitionInjection(): ParadoxScriptProperty? {
    return parentDefinitionInjectionOld()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinitionOrInjection(): ParadoxScriptDefinitionElement? {
    return parentDefinitionOrInjectionOld()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentPropertyDefinitionOrInjection(): ParadoxScriptProperty? {
    return parentPropertyDefinitionOrInjectionOld()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentOfKey(
    propertyName: String? = null,
    ignoreCase: Boolean = true,
    fromParentBlock: Boolean = false,
): ParadoxScriptDefinitionElement? {
    return parentOld(propertyName, ignoreCase, fromParentBlock)
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentOfKey(
    fromParentBlock: Boolean = false,
    propertyPredicate: (String) -> Boolean,
): ParadoxScriptDefinitionElement? {
    return parentOld(fromParentBlock, propertyPredicate)
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMemberContainer.ofPath(
    path: String,
    ignoreCase: Boolean = true,
    usePattern: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false,
): Sequence<ParadoxScriptMember> {
    val subPaths = ParadoxMemberPath.resolve(path).subPaths
    if (subPaths.isEmpty()) return emptySequence()

    var current: Sequence<ParadoxScriptMember>? = null
    for (subPath in subPaths) {
        if (current == null) {
            current = when (subPath) {
                "-" -> values(conditional, inline).filterIsInstance<ParadoxScriptBlock>()
                else -> properties(conditional, inline).ofKey(subPath, ignoreCase, usePattern)
            }
        } else {
            current = when (subPath) {
                "-" -> current.flatMap { it.values(conditional, inline).filterIsInstance<ParadoxScriptBlock>() }
                else -> current.flatMap { it.properties(conditional, inline).ofKey(subPath, ignoreCase, usePattern) }
            }
        }
        ProgressManager.checkCanceled()
    }
    return current.orEmpty()
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptMemberContainer>.ofPath(
    path: String,
    ignoreCase: Boolean = true,
    usePattern: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false,
): Sequence<ParadoxScriptMember> {
    return flatMap { it.ofPath(path, ignoreCase, usePattern, conditional, inline) }
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMemberContainer.ofPaths(
    paths: Collection<String>,
    ignoreCase: Boolean = true,
    usePattern: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false,
): Sequence<ParadoxScriptMember> {
    return paths.asSequence().flatMap { path -> ofPath(path, ignoreCase, usePattern, conditional, inline) }
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptMemberContainer>.ofPaths(
    paths: Collection<String>,
    ignoreCase: Boolean = true,
    usePattern: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false,
): Sequence<ParadoxScriptMember> {
    return flatMap { it.ofPaths(paths, ignoreCase, usePattern, conditional, inline) }
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMember.propertyByPath(
    path: String,
    ignoreCase: Boolean = true,
    conditional: Boolean = false,
    inline: Boolean = false,
): ParadoxScriptProperty? {
    return propertyByPathOld(path, ignoreCase, conditional, inline)
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMember.parentByPath(
    path: String = "",
    ignoreCase: Boolean = true,
    definitionType: String? = null,
): PsiElement? {
    return parentByPathOld(path, ignoreCase, definitionType)
}
