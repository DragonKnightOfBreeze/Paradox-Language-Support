package icu.windea.pls.lang.select

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.generateSequenceFromSeeds
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
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptRootBlock
import icu.windea.pls.script.psi.ParadoxScriptString
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.script.psi.isPropertyValue

class ParadoxPsiSelectScopeImpl : ParadoxPsiSelectScope {
    // region Common

    override fun <T : PsiElement> Sequence<T>.one(): T? = firstOrNull()

    override fun <T : PsiElement> Sequence<T>.all(): List<T> = toList()

    // endregion

    // region Walks

    override fun PsiElement.walkUp(): Sequence<PsiElement> {
        return generateSequence(this) { if (it is PsiFile) null else it.parent }
    }

    override fun ParadoxScriptMemberContainer.walkDown(traversal: TreeTraversal, conditional: Boolean?, inline: Boolean?): Sequence<ParadoxScriptMember> {
        val seeds = members(conditional, inline).asIterable()
        return generateSequenceFromSeeds(traversal, seeds) { it.members(conditional, inline).asIterable() }
    }

    // endregion

    // region Casts

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

    // endregion

    // region Queries

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

    override fun ParadoxScriptProperty?.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptProperty? {
        if (this == null) return null
        return takeIf { PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
    }

    override fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<ParadoxScriptProperty> {
        return filter { PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
    }

    override fun ParadoxScriptProperty?.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptProperty? {
        if (this == null) return null
        return takeIf { keys.any { key -> PathMatcher.matches(it.name, key, ignoreCase, usePattern) } }
    }

    override fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<ParadoxScriptProperty> {
        return filter { property -> keys.any { key -> PathMatcher.matches(property.name, key, ignoreCase, usePattern) } }
    }

    override fun <T : ParadoxScriptMember> T?.ofValue(value: String, ignoreCase: Boolean): T? {
        if (this == null) return null
        return takeIf { literalValue().equals(value, ignoreCase) }
    }

    override fun <T : ParadoxScriptMember> Sequence<T>.ofValue(value: String, ignoreCase: Boolean): Sequence<T> {
        return filter { it.literalValue().equals(value, ignoreCase) }
    }

    override fun <T : ParadoxScriptMember> T?.ofValues(values: Collection<String>, ignoreCase: Boolean): T? {
        if (this == null) return null
        return takeIf { it.literalValue().let { v -> v != null && values.any { value -> v.equals(value, ignoreCase) } } }
    }

    override fun <T : ParadoxScriptMember> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean): Sequence<T> {
        return filter { it.literalValue().let { v -> v != null && values.any { value -> v.equals(value, ignoreCase) } } }
    }

    override fun ParadoxScriptMemberContainer?.ofPath(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        if (this == null) return emptySequence()
        return ofPathInternal(path, ignoreCase, usePattern, conditional, inline)
    }

    override fun Sequence<ParadoxScriptMemberContainer>.ofPath(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return flatMap { it.ofPathInternal(path, ignoreCase, usePattern, conditional, inline) }
    }

    override fun ParadoxScriptMemberContainer?.ofPaths(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        if (this == null) return emptySequence()
        return paths.asSequence().flatMap { path -> ofPathInternal(path, ignoreCase, usePattern, conditional, inline) }
    }

    override fun Sequence<ParadoxScriptMemberContainer>.ofPaths(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return flatMap { paths.asSequence().flatMap { path -> it.ofPathInternal(path, ignoreCase, usePattern, conditional, inline) } }
    }

    private fun ParadoxScriptMemberContainer.ofPathInternal(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        if (path.isEmpty()) return emptySequence()
        var current: Sequence<ParadoxScriptMember>? = null
        val resolvedPath = ParadoxMemberPath.resolve(path)
        val subPaths = resolvedPath.subPaths
        for (i in 0..subPaths.lastIndex) {
            ProgressManager.checkCanceled()
            val subPath = subPaths[i]
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

    override fun PsiElement?.containingProperty(): ParadoxScriptProperty? {
        if (this == null) return null
        if (this is ParadoxScriptProperty) return this
        if (language !is ParadoxScriptLanguage) return null
        return parentOfType()
    }

    override fun PsiElement?.containingBlock(): ParadoxScriptBlock? {
        if (this == null) return null
        if (this is ParadoxScriptBlock) return this
        if (language !is ParadoxScriptLanguage) return null
        return parentOfType()
    }

    override fun PsiElement?.containingMember(): ParadoxScriptMember? {
        if (this == null) return null
        if (this is ParadoxScriptMember) return this
        if (language !is ParadoxScriptLanguage) return null
        return parentOfType()
    }

    override fun PsiElement?.containingMemberContainer(): ParadoxScriptMemberContainer? {
        if (this == null) return null
        if (this is ParadoxScriptMemberContainer) return this
        if (language !is ParadoxScriptLanguage) return null
        return parentOfType()
    }

    override fun PsiElement?.parentProperty(): ParadoxScriptProperty? {
        if (this == null) return null
        if (language !is ParadoxScriptLanguage) return null
        return parentOfType()
    }

    override fun PsiElement?.parentBlock(): ParadoxScriptBlock? {
        if (this == null) return null
        if (language !is ParadoxScriptLanguage) return null
        return parentOfType()
    }

    override fun PsiElement?.parentMember(): ParadoxScriptMember? {
        if (this == null) return null
        if (language !is ParadoxScriptLanguage) return null
        return parentOfType()
    }

    override fun PsiElement?.parentMemberContainer(): ParadoxScriptMemberContainer? {
        if (this == null) return null
        if (language !is ParadoxScriptLanguage) return null
        return parentOfType()
    }

    override fun PsiElement?.parentOfKey(key: String?, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptMember? {
        if (this == null) return null
        if (language !is ParadoxScriptLanguage) return null
        var current: PsiElement = parent ?: return null
        while (current !is PsiFile) {
            ProgressManager.checkCanceled()
            if (current is ParadoxScriptProperty) {
                if (key == "-") return null
                if (key == null || PathMatcher.matches(current.name, key, ignoreCase, usePattern)) return current
                return null
            } else if (current is ParadoxScriptBlock) {
                if (key == null || key == "-") return current
                if (!current.isPropertyValue()) return null
            }
            current = current.parent ?: break
        }
        if (current is ParadoxScriptFile && (key == null || key == "-" || key == "*")) return current
        return null
    }

    override fun PsiElement?.parentOfPath(path: String, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptMember? {
        if (this == null) return null
        if (language !is ParadoxScriptLanguage) return null
        if (path.isEmpty()) return null
        var current: PsiElement = this
        val resolvedPath = ParadoxMemberPath.resolve(path)
        val subPaths = resolvedPath.subPaths
        for (i in subPaths.lastIndex downTo 0) {
            ProgressManager.checkCanceled()
            val subPath = subPaths[i]
            current = current.parentOfKey(subPath, ignoreCase, usePattern) ?: return null
        }
        return current.castOrNull()
    }

    // endregion

    // region Semantic Casts

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
