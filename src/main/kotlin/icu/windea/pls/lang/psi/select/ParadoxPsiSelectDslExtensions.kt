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
import icu.windea.pls.core.collections.context
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.collections.generateSequenceFromSeeds
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
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

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.walkUp(): Sequence<PsiElement> {
    return generateSequence(this) { it.parent }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMemberContainer.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS, conditional: Boolean? = null, inline: Boolean? = null): Sequence<ParadoxScriptMember> {
    val seeds = members(conditional, inline).asIterable()
    return generateSequenceFromSeeds(traversal, seeds) { it.members(conditional, inline).asIterable() }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asProperty(): ParadoxScriptProperty? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<*>.asProperty(): Sequence<ParadoxScriptProperty> {
    return filterIsInstance<ParadoxScriptProperty>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asValue(): ParadoxScriptValue? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<*>.asValue(): Sequence<ParadoxScriptValue> {
    return filterIsInstance<ParadoxScriptValue>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asBlock(): ParadoxScriptBlock? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<*>.asBlock(): Sequence<ParadoxScriptBlock> {
    return filterIsInstance<ParadoxScriptBlock>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asMemberContainer(): ParadoxScriptMemberContainer? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<*>.asMemberContainer(): Sequence<ParadoxScriptMemberContainer> {
    return filterIsInstance<ParadoxScriptMemberContainer>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.memberContainer(orSelf: Boolean = true): ParadoxScriptMemberContainer? {
    if (language !is ParadoxScriptLanguage) return null
    if (orSelf) this.castOrNull<ParadoxScriptMemberContainer>()?.let { return it }
    return parents(withSelf = orSelf).findIsInstance<ParadoxScriptMemberContainer>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptProperty.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty? {
    if (key.isEmpty()) return this
    return takeIf { PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty> {
    if (key.isEmpty()) return this
    return filter { PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptProperty.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty? {
    return takeIf { keys.any { key -> key.isEmpty() || PathMatcher.matches(it.name, key, ignoreCase, usePattern) } }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty> {
    return filter { property -> keys.any { key -> key.isEmpty() || PathMatcher.matches(property.name, key, ignoreCase, usePattern) } }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : ParadoxScriptMember> T.ofValue(value: String, ignoreCase: Boolean = true): T? {
    return takeIf { selectValue().equals(value, ignoreCase) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : ParadoxScriptMember> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectValue().equals(value, ignoreCase) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : ParadoxScriptMember> T.ofValues(values: Collection<String>, ignoreCase: Boolean = true): T? {
    return takeIf { it.selectValue().let { v -> v != null || values.any { value -> v.equals(value, ignoreCase) } } }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : ParadoxScriptMember> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectValue().let { v -> v != null || values.any { value -> v.equals(value, ignoreCase) } } }
}

/**
 * 得到指定名字的属性。可能为 `null`，可能是定义。
 *
 * @param propertyName 要查找到的属性的名字。如果为 `null`，则不指定。如果为空字符串且自身是脚本属性，则返回自身。
 * @param conditional 是否也包括间接作为其中的参数表达式的子节点的属性。
 * @param inline 是否处理需要内联脚本片段（如，内联脚本）的情况。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
@Deprecated("")
fun PsiElement.property(propertyName: String? = null, ignoreCase: Boolean = true, conditional: Boolean = false, inline: Boolean = false): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    if (propertyName != null && propertyName.isEmpty()) return this as? ParadoxScriptProperty
    val block = when {
        this is ParadoxScriptFile -> this.block
        this is ParadoxScriptDefinitionElement -> this.block
        this is ParadoxScriptBlock -> this
        else -> null
    }
    var result: ParadoxScriptProperty? = null
    block?.properties()?.context { conditional(conditional) + inline(inline) }?.process {
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
@Deprecated("")
fun PsiElement.property(conditional: Boolean = false, inline: Boolean = false, propertyPredicate: (String) -> Boolean): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    if (propertyPredicate("")) return this as? ParadoxScriptProperty
    val block = when {
        this is ParadoxScriptFile -> this.block
        this is ParadoxScriptDefinitionElement -> this.block
        this is ParadoxScriptBlock -> this
        else -> null
    }
    var result: ParadoxScriptProperty? = null
    block?.properties()?.context { conditional(conditional) + inline(inline) }?.process {
        if (propertyPredicate(it.name)) {
            result = it
            false
        } else {
            true
        }
    }
    return result
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMemberContainer.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember> {
    return ofPathInternal(path, ignoreCase, usePattern, conditional, inline)
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptMemberContainer>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember> {
    return flatMap { it.ofPathInternal(path, ignoreCase, usePattern, conditional, inline) }
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMemberContainer.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember> {
    return paths.asSequence().flatMap { path -> ofPathInternal(path, ignoreCase, usePattern, conditional, inline) }
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptMemberContainer>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember> {
    return flatMap { paths.asSequence().flatMap { path -> it.ofPathInternal(path, ignoreCase, usePattern, conditional, inline) } }
}

context(scope: ParadoxPsiSelectScope)
private fun ParadoxScriptMemberContainer.ofPathInternal(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
    ProgressManager.checkCanceled()
    if (path.isEmpty()) {
        if (this is ParadoxScriptMember) return sequenceOf(this)
        throw UnsupportedOperationException()
    }
    var current: Sequence<ParadoxScriptMember>? = null
    val expect = ParadoxMemberPath.resolve(path)
    for (subPath in expect) {
        ProgressManager.checkCanceled()
        if (current == null) {
            current = when (subPath) {
                "-" -> values(conditional, inline)
                else -> properties(conditional, inline).ofKey(subPath, ignoreCase, usePattern)
            }
        } else {
            current = when (subPath) {
                "-" -> current.flatMap { it.values(conditional, inline) }
                else -> current.flatMap { it.properties(conditional, inline).ofKey(subPath, ignoreCase, usePattern) }
            }
        }
    }
    return current.orEmpty()
}

/** @see ParadoxMemberPath */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
@Deprecated("")
fun ParadoxScriptMember.propertyByPath(path: String, ignoreCase: Boolean = true, conditional: Boolean = false, inline: Boolean = false): ParadoxScriptProperty? {
    return ofPath(path, ignoreCase, conditional, inline).asProperty().one()
}

/**
 * 向上得到第一个属性。可能为 `null`，可能是定义，可能是脚本文件。
 *
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果得到的是脚本文件，则忽略。
 * @param fromParentBlock 是否先向上得到第一个子句，再继续进行查找。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parent(propertyName: String? = null, ignoreCase: Boolean = true, fromParentBlock: Boolean = false): ParadoxScriptDefinitionElement? {
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
fun PsiElement.parent(fromParentBlock: Boolean = false, propertyPredicate: (String) -> Boolean): ParadoxScriptDefinitionElement? {
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


context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentMemberContainer(ofSelf: Boolean = false): ParadoxScriptMemberContainer? {
    return memberContainer(orSelf = ofSelf)
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentOfKey(propertyName: String? = null, ignoreCase: Boolean = true, fromParentBlock: Boolean = false): ParadoxScriptDefinitionElement? {
    return parent(propertyName, ignoreCase, fromParentBlock)
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentOfKey(fromParentBlock: Boolean = false, propertyPredicate: (String) -> Boolean): ParadoxScriptDefinitionElement? {
    return parent(fromParentBlock, propertyPredicate)
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
fun ParadoxScriptMember.parentOfPath(path: String = "", ignoreCase: Boolean = true, definitionType: String? = null): PsiElement? {
    // if (language !is ParadoxScriptLanguage) return null
    var current = this
    if (path.isNotEmpty()) {
        val memberPath = ParadoxMemberPath.resolve(path)
        for (subPath in memberPath.subPaths.reversed()) {
            current = when (subPath) {
                "-" -> current.parentBlock ?: return null
                else -> current.parent(subPath, ignoreCase) ?: return null
            }
            ProgressManager.checkCanceled()
        }
    }
    if (definitionType != null) {
        val result = current.parent(null) ?: return null
        val definitionInfo = result.definitionInfo ?: return null
        if (definitionType.isNotEmpty()) {
            if (!ParadoxDefinitionTypeExpression.resolve(definitionType).matches(definitionInfo)) return null
        }
        return result
    }
    return current
}

/**
 * 向上查找第一个符合条件的定义。
 *
 * @param orSelf 结果是否可以是自身。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinition(orSelf: Boolean = true): ParadoxScriptDefinitionElement? {
    return parents(withSelf = orSelf)
        .takeWhile { it !is PsiDirectory }
        .filterIsInstance<ParadoxScriptDefinitionElement>()
        .findIsInstance { ParadoxPsiMatcher.isDefinition(it) }
}

/**
 * 向上查找第一个符合条件的定义注入。
 *
 * @param orSelf 结果是否可以是自身。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinitionInjection(orSelf: Boolean = true): ParadoxScriptProperty? {
    if (language !is ParadoxScriptLanguage) return null
    return parents(withSelf = orSelf)
        .takeWhile { it !is ParadoxScriptRootBlock }
        .findIsInstance { ParadoxPsiMatcher.isDefinitionInjection(it) }
}

/**
 * 向上查找第一个符合条件的定义或定义注入。
 *
 * @param orSelf 结果是否可以是自身。
 */
context(_: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinitionOrInjection(orSelf: Boolean = true): ParadoxScriptDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    return parents(withSelf = orSelf)
        .takeWhile { it !is PsiDirectory }
        .findIsInstance { ParadoxPsiMatcher.isDefinition(it) || ParadoxPsiMatcher.isDefinitionInjection(it) }
}
