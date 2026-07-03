package icu.windea.pls.lang.select

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.generateSequenceFromSeeds
import icu.windea.pls.core.match.KeywordMatcher
import icu.windea.pls.core.match.PathMatcher
import icu.windea.pls.core.processParent
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiMatchService
import icu.windea.pls.lang.psi.members
import icu.windea.pls.lang.psi.properties
import icu.windea.pls.lang.psi.values
import icu.windea.pls.model.expressions.ParadoxDefinitionTypeExpression
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptBoolean
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptFloat
import icu.windea.pls.script.psi.ParadoxScriptInt
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptMemberContext
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isDirectValue

class ParadoxPsiSelectScopeImpl : ParadoxPsiSelectScope {
    // region Common

    override fun <T : PsiElement> Sequence<T>.one(): T? = firstOrNull()

    override fun <T : PsiElement> Sequence<T>.all(): List<T> = toList()

    override fun ParadoxScriptMember.walkUp(): Sequence<ParadoxScriptMember> {
        return generateSequence<PsiElement>(this) { if (it is PsiFile) null else it.parent }.filterIsInstance<ParadoxScriptMember>()
    }

    override fun ParadoxScriptMemberContext.walkDown(traversal: TreeTraversal, conditional: Boolean?, inline: Boolean?): Sequence<ParadoxScriptMember> {
        val seeds = members(conditional, inline).asIterable()
        return generateSequenceFromSeeds(traversal, seeds) { it.members(conditional, inline).asIterable() }
    }

    override fun ParadoxScriptMember?.literalValue(): String? {
        if (this == null) return null
        return when (this) {
            is ParadoxScriptProperty -> propertyValue?.literalValue()
            is ParadoxScriptBoolean -> value
            is ParadoxScriptInt -> value
            is ParadoxScriptFloat -> value
            is ParadoxScriptString -> value
            else -> null
        }
    }

    // endregion

    // region Filters

    override fun PsiElement?.asProperty(): ParadoxScriptProperty? {
        return this?.castOrNull()
    }

    override fun Sequence<PsiElement>.asProperty(): Sequence<ParadoxScriptProperty> {
        return filterIsInstance<ParadoxScriptProperty>()
    }

    override fun PsiElement?.asValue(): ParadoxScriptValue? {
        return this?.castOrNull()
    }

    override fun Sequence<PsiElement>.asValue(): Sequence<ParadoxScriptValue> {
        return filterIsInstance<ParadoxScriptValue>()
    }

    override fun PsiElement?.asBlock(): ParadoxScriptBlock? {
        return this?.castOrNull()
    }

    override fun Sequence<PsiElement>.asBlock(): Sequence<ParadoxScriptBlock> {
        return filterIsInstance<ParadoxScriptBlock>()
    }

    override fun PsiElement?.asMember(): ParadoxScriptMember? {
        return this?.castOrNull()
    }

    override fun Sequence<PsiElement>.asMember(): Sequence<ParadoxScriptMember> {
        return filterIsInstance<ParadoxScriptMember>()
    }

    override fun PsiElement?.asMemberContainer(): ParadoxScriptMemberContainer? {
        return this?.castOrNull()
    }

    override fun Sequence<PsiElement>.asMemberContainer(): Sequence<ParadoxScriptMemberContainer> {
        return filterIsInstance<ParadoxScriptMemberContainer>()
    }

    override fun PsiElement?.asMemberContext(): ParadoxScriptMemberContext? {
        return this?.castOrNull()
    }

    override fun Sequence<PsiElement>.asMemberContext(): Sequence<ParadoxScriptMemberContext> {
        return filterIsInstance<ParadoxScriptMemberContext>()
    }

    override fun ParadoxScriptProperty?.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptProperty? {
        if (this == null) return null
        return takeIf { with(it.name) { KeywordMatcher.matches(this, key, ignoreCase, usePattern) } }
    }

    override fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<ParadoxScriptProperty> {
        return filter { with(it.name) { KeywordMatcher.matches(this, key, ignoreCase, usePattern) } }
    }

    override fun ParadoxScriptProperty?.ofKeys(vararg keys: String, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptProperty? {
        if (this == null) return null
        return takeIf { with(it.name) { KeywordMatcher.matches(this, keys, ignoreCase, usePattern) } }
    }

    override fun Sequence<ParadoxScriptProperty>.ofKeys(vararg keys: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<ParadoxScriptProperty> {
        return filter { with(it.name) { KeywordMatcher.matches(this, keys, ignoreCase, usePattern) } }
    }

    override fun ParadoxScriptProperty?.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptProperty? {
        if (this == null) return null
        return takeIf { with(it.name) { KeywordMatcher.matches(this, keys, ignoreCase, usePattern) } }
    }

    override fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<ParadoxScriptProperty> {
        return filter { with(it.name) { KeywordMatcher.matches(this, keys, ignoreCase, usePattern) } }
    }

    override fun <T : ParadoxScriptMember> T?.ofValue(value: String, ignoreCase: Boolean, usePattern: Boolean): T? {
        if (this == null) return null
        return takeIf { with(it.literalValue()) { KeywordMatcher.matches(this, value, ignoreCase, usePattern) } }
    }

    override fun <T : ParadoxScriptMember> Sequence<T>.ofValue(value: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<T> {
        return filter { with(it.literalValue()) { KeywordMatcher.matches(this, value, ignoreCase, usePattern) } }
    }

    override fun <T : ParadoxScriptMember> T?.ofValues(vararg values: String, ignoreCase: Boolean, usePattern: Boolean): T? {
        if (this == null) return null
        return takeIf { with(it.literalValue()) { KeywordMatcher.matches(this, values, ignoreCase, usePattern) } }
    }

    override fun <T : ParadoxScriptMember> Sequence<T>.ofValues(vararg values: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<T> {
        return filter { with(it.literalValue()) { KeywordMatcher.matches(this, values, ignoreCase, usePattern) } }
    }

    override fun <T : ParadoxScriptMember> T?.ofValues(values: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): T? {
        if (this == null) return null
        return takeIf { with(it.literalValue()) { KeywordMatcher.matches(this, values, ignoreCase, usePattern) } }
    }

    override fun <T : ParadoxScriptMember> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<T> {
        return filter { with(it.literalValue()) { KeywordMatcher.matches(this, values, ignoreCase, usePattern) } }
    }

    // endregion

    // region Queries

    override fun ParadoxScriptMemberContext?.query(conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        if (this == null) return emptySequence()
        return members(conditional, inline)
    }

    override fun Sequence<ParadoxScriptMemberContext>.query(conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return flatMap { it.members(conditional, inline) }
    }

    override fun ParadoxScriptMemberContext?.queryBy(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        if (this == null) return emptySequence()
        return queryByInternal(path, ignoreCase, usePattern, conditional, inline)
    }

    override fun Sequence<ParadoxScriptMemberContext>.queryBy(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return flatMap { it.queryByInternal(path, ignoreCase, usePattern, conditional, inline) }
    }

    override fun ParadoxScriptMemberContext?.queryBy(vararg paths: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        if (this == null) return emptySequence()
        return paths.asSequence().flatMap { path -> queryByInternal(path, ignoreCase, usePattern, conditional, inline) }
    }

    override fun Sequence<ParadoxScriptMemberContext>.queryBy(vararg paths: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return flatMap { paths.asSequence().flatMap { path -> it.queryByInternal(path, ignoreCase, usePattern, conditional, inline) } }
    }

    override fun ParadoxScriptMemberContext?.queryBy(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        if (this == null) return emptySequence()
        return paths.asSequence().flatMap { path -> queryByInternal(path, ignoreCase, usePattern, conditional, inline) }
    }

    override fun Sequence<ParadoxScriptMemberContext>.queryBy(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return flatMap { paths.asSequence().flatMap { path -> it.queryByInternal(path, ignoreCase, usePattern, conditional, inline) } }
    }

    private fun ParadoxScriptMemberContext.queryByInternal(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        val resolvedPath = ParadoxMemberPath.resolve(path)
        val subPaths = resolvedPath.subPaths
        if (subPaths.isEmpty()) return emptySequence()
        var current: Sequence<ParadoxScriptMember>? = null
        for (i in 0..subPaths.lastIndex) {
            ProgressManager.checkCanceled()
            val subPath = subPaths[i]
            if (current == null) {
                current = when (subPath) {
                    "-" -> values(conditional, inline)
                    else -> properties(conditional, inline).filter { p -> matchesSubPath(p, subPath, ignoreCase, usePattern) }
                }
            } else {
                current = when (subPath) {
                    "-" -> current.flatMap { it.values(conditional, inline) }
                    else -> current.flatMap { it.properties(conditional, inline).filter { p -> matchesSubPath(p, subPath, ignoreCase, usePattern) } }
                }
            }
        }
        return current.orEmpty()
    }

    override fun PsiElement?.queryParent(withSelf: Boolean): ParadoxScriptMember? {
        if (this == null) return null
        if (language !is ParadoxScriptLanguage) return null
        val current = if (withSelf) this else parent ?: return null
        return current.queryParentInternal()
    }

    private fun PsiElement.queryParentInternal(): ParadoxScriptMember? {
        var current = this
        while (current !is PsiFile) {
            if (current is ParadoxScriptProperty) {
                return current
            } else if (current is ParadoxScriptValue) {
                if (current.isDirectValue()) return current
            }
            current = current.parent ?: return null
        }
        if (current is ParadoxScriptFile) return current
        return null
    }

    override fun PsiElement?.queryParentBy(path: String, ignoreCase: Boolean, usePattern: Boolean, withSelf: Boolean): ParadoxScriptMember? {
        if (this == null) return null
        if (language !is ParadoxScriptLanguage) return null
        val current = if (withSelf) this else parent ?: return null
        return current.queryParentByInternal(path, ignoreCase, usePattern)
    }

    private fun PsiElement.queryParentByInternal(path: String, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptMember? {
        val resolvedPath = ParadoxMemberPath.resolve(path)
        val subPaths = resolvedPath.subPaths
        if (subPaths.isEmpty()) return null
        var current = this
        for (i in subPaths.lastIndex downTo 0) {
            ProgressManager.checkCanceled()
            if (i > 0) current = current.parent ?: return null
            current = current.queryParentInternal() ?: return null
            val subPath = subPaths[i]
            current = when (subPath) {
                "-" -> current.asValue()
                else -> current.asProperty()?.takeIf { matchesSubPath(it, subPath, ignoreCase, usePattern) }
            } ?: return null
        }
        if (current is ParadoxScriptMember) return current
        return null
    }

    private fun matchesSubPath(property: ParadoxScriptProperty, subPath: String, ignoreCase: Boolean, usePattern: Boolean): Boolean {
        return PathMatcher.matches(property.name, subPath, ignoreCase, usePattern)
    }

    // endregion

    // region Semantic Filters

    override fun PsiElement?.asDefinition(typeExpression: String?): ParadoxDefinitionElement? {
        if (this !is ParadoxDefinitionElement) return null
        val definitionInfo = definitionInfo ?: return null
        if (typeExpression == null) return this
        if (ParadoxDefinitionTypeExpression.resolve(typeExpression).matches(definitionInfo)) return this
        return null
    }

    override fun Sequence<PsiElement>.asDefinition(typeExpression: String?): Sequence<ParadoxDefinitionElement> {
        return mapNotNull { it.asDefinition(typeExpression) }
    }

    // endregion

    // region Semantic Queries

    override fun ParadoxDefinitionElement.nameElement(nameField: String?): ParadoxScriptExpressionElement? {
        if (nameField == null) return asProperty()?.propertyKey
        return nameFieldElement(nameField)
    }

    override fun ParadoxDefinitionElement.nameFieldElement(nameField: String?): ParadoxScriptValue? {
        return when (nameField) {
            null -> null
            "" -> null
            "-" -> asProperty()?.propertyValue
            else -> properties().ofKey(nameField, usePattern = false).one()?.propertyValue
        }
    }

    override fun PsiElement.parentDefinitionCandidate(withSelf: Boolean): ParadoxDefinitionElement? {
        if (language !is ParadoxScriptLanguage) return null
        processParent(withSelf = withSelf) p@{
            if (ParadoxPsiMatchService.isDefinitionCandidate(it)) return it
            true
        }
        return null
    }

    override fun PsiElement.parentDefinition(withSelf: Boolean): ParadoxDefinitionElement? {
        if (language !is ParadoxScriptLanguage) return null
        processParent(withSelf = withSelf) p@{
            if (ParadoxPsiMatchService.isDefinition(it)) return it
            true
        }
        return null
    }

    override fun PsiElement.parentDefinitionInjection(withSelf: Boolean): ParadoxScriptProperty? {
        if (language !is ParadoxScriptLanguage) return null
        processParent(withSelf = withSelf) p@{
            if (it is ParadoxScriptRootBlock) return@p false
            if (ParadoxPsiMatchService.isDefinitionInjection(it)) return it
            true
        }
        return null
    }

    override fun PsiElement.parentDefineVariable(withSelf: Boolean): ParadoxScriptProperty? {
        if (language !is ParadoxScriptLanguage) return null
        processParent(withSelf = withSelf) p@{
            if (ParadoxPsiMatchService.isDefineVariable(it)) return it
            true
        }
        return null
    }

    // endregion
}
