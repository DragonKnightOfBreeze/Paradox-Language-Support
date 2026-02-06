@file:Suppress("unused")

package icu.windea.pls.lang.psi.select

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.config.config.delegated.CwtTypeConfig
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.collections.generateSequenceFromSeeds
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.core.processParent
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.selectLiteralValue
import icu.windea.pls.lang.psi.values
import icu.windea.pls.lang.resolve.expression.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isPropertyValue

// region Walks

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.walkUp(): Sequence<PsiElement> {
    return generateSequence(this) { if (it is PsiFile) null else it.parent } // without walking directories
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMemberContainer.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS, conditional: Boolean? = null, inline: Boolean? = null): Sequence<ParadoxScriptMember> {
    val seeds = members(conditional, inline).asIterable()
    return generateSequenceFromSeeds(traversal, seeds) { it.members(conditional, inline).asIterable() }
}

// endregion Walks

// region Casts

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asProperty(): ParadoxScriptProperty? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<PsiElement>.asProperty(): Sequence<ParadoxScriptProperty> {
    return filterIsInstance<ParadoxScriptProperty>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asValue(): ParadoxScriptValue? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<PsiElement>.asValue(): Sequence<ParadoxScriptValue> {
    return filterIsInstance<ParadoxScriptValue>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asBlock(): ParadoxScriptBlock? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<PsiElement>.asBlock(): Sequence<ParadoxScriptBlock> {
    return filterIsInstance<ParadoxScriptBlock>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asMember(): ParadoxScriptMember? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<PsiElement>.asMember(): Sequence<ParadoxScriptMember> {
    return filterIsInstance<ParadoxScriptMember>()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement?.asMemberContainer(): ParadoxScriptMemberContainer? {
    return this?.castOrNull()
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<PsiElement>.asMemberContainer(): Sequence<ParadoxScriptMemberContainer> {
    return filterIsInstance<ParadoxScriptMemberContainer>()
}

// endregion Cass

// region Queries

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptProperty.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty? {
    if (key.isEmpty()) return null
    return takeIf { PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty> {
    if (key.isEmpty()) return emptySequence()
    return filter { PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptProperty.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty? {
    return takeIf { keys.any { key -> PathMatcher.matches(it.name, key, ignoreCase, usePattern) } }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty> {
    return filter { property -> keys.any { key -> PathMatcher.matches(property.name, key, ignoreCase, usePattern) } }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : ParadoxScriptMember> T.ofValue(value: String, ignoreCase: Boolean = true): T? {
    return takeIf { selectLiteralValue().equals(value, ignoreCase) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : ParadoxScriptMember> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectLiteralValue().equals(value, ignoreCase) }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : ParadoxScriptMember> T.ofValues(values: Collection<String>, ignoreCase: Boolean = true): T? {
    return takeIf { it.selectLiteralValue().let { v -> v != null && values.any { value -> v.equals(value, ignoreCase) } } }
}

context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun <T : ParadoxScriptMember> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true): Sequence<T> {
    return filter { it.selectLiteralValue().let { v -> v != null && values.any { value -> v.equals(value, ignoreCase) } } }
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
    if (path.isEmpty()) return emptySequence()
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

// TODO 2.1.1 further refactoring
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentMemberContainer(orSelf: Boolean = false): ParadoxScriptMemberContainer? {
    if (language !is ParadoxScriptLanguage) return null
    if (orSelf) this.castOrNull<ParadoxScriptMemberContainer>()?.let { return it }
    return parents(withSelf = orSelf).findIsInstance<ParadoxScriptMemberContainer>()
}

// TODO 2.1.1 further refactoring
/**
 * 向上得到第一个属性或文件。
 *
 * @param propertyName 要查找到的属性的名字。如果为null，则不指定。如果得到的是脚本文件，则忽略。
 * @param fromBlock 是否先向上得到第一个子句，再继续进行查找。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentOfKey(propertyName: String? = null, ignoreCase: Boolean = true, fromBlock: Boolean = false): ParadoxDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    var current = this
    when {
        fromBlock -> current = current.parentOfType<ParadoxScriptBlockElement>() ?: return null
        current is ParadoxScriptMember -> current = current.parent ?: return null
    }
    while (current !is PsiFile) {
        if (current is ParadoxDefinitionElement) {
            if (propertyName == null || propertyName.equals(current.name, ignoreCase)) return current
        }
        if (current is ParadoxScriptBlock && !current.isPropertyValue()) return null
        current = current.parent ?: break
        ProgressManager.checkCanceled()
    }
    if (current is ParadoxScriptFile) return current
    return null
}

// TODO 2.1.1 further refactoring
/**
 * 基于路径向上查找指定的属性、值或文件。如果路径为空，则返回查找到的第一个属性或值（块）。
 *
 * @param definitionType 如果不为 null 则在查找到指定的属性之后再向上查找一层属性，并要求其是定义，如果接着不为空字符串则要求匹配该定义类型表达式。
 *
 * @see ParadoxMemberPath
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxScriptMember.parentOfPath(path: String = "", ignoreCase: Boolean = true, definitionType: String? = null): ParadoxScriptMemberContainer? {
    // if (language !is ParadoxScriptLanguage) return null
    var current: ParadoxScriptMemberContainer = this
    if (path.isNotEmpty()) {
        val memberPath = ParadoxMemberPath.resolve(path)
        for (subPath in memberPath.subPaths.reversed()) {
            current = when (subPath) {
                "-" -> current.parent?.castOrNull<ParadoxScriptBlock>() ?: return null
                else -> current.parentOfKey(subPath, ignoreCase) ?: return null
            }
            ProgressManager.checkCanceled()
        }
    }
    if (definitionType != null) {
        val result = current.parentOfKey(null) ?: return null
        val definitionInfo = result.definitionInfo ?: return null
        if (definitionType.isNotEmpty()) {
            if (!ParadoxDefinitionTypeExpression.resolve(definitionType).matches(definitionInfo)) return null
        }
        return result
    }
    return current
}

// endregion

// region Semantic Queries

/** @see CwtTypeConfig.nameField */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxDefinitionElement.nameElement(nameField: String?): ParadoxScriptExpressionElement? {
    // no conditional or inline here
    if (nameField == null) return asProperty()?.propertyKey
    return nameFieldElement(nameField)
}

/** @see CwtTypeConfig.nameField */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun ParadoxDefinitionElement.nameFieldElement(nameField: String?): ParadoxScriptValue? {
    // no conditional or inline here
    return when (nameField) {
        null -> null
        "" -> null
        "-" -> asProperty()?.propertyValue
        else -> properties().ofKey(nameField, usePattern = false).one()?.propertyValue
    }
}

/**
 * 向上查找第一个符合条件的定义。
 *
 * @param orSelf 结果是否可以是自身。
 */
context(scope: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinition(orSelf: Boolean = true): ParadoxDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    processParent(withSelf = orSelf) p@{
        if (ParadoxPsiMatcher.isDefinition(it)) return it
        true
    }
    return null
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
    processParent(withSelf = orSelf) p@{
        if (it is ParadoxScriptRootBlock) return@p false
        if (ParadoxPsiMatcher.isDefinitionInjection(it)) return it
        true
    }
    return null
}

/**
 * 向上查找第一个符合条件的定义或定义注入。
 *
 * @param orSelf 结果是否可以是自身。
 */
context(_: ParadoxPsiSelectScope)
@ParadoxPsiSelectDsl
fun PsiElement.parentDefinitionOrInjection(orSelf: Boolean = true): ParadoxDefinitionElement? {
    if (language !is ParadoxScriptLanguage) return null
    processParent(withSelf = orSelf) p@{
        if (ParadoxPsiMatcher.isDefinition(it) || ParadoxPsiMatcher.isDefinitionInjection(it)) return it
        true
    }
    return null
}

// endregion
