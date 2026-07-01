package icu.windea.pls.lang.select

import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.parentOfType
import com.intellij.psi.util.parents
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
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
import icu.windea.pls.script.psi.ParadoxScriptBlockElement
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

    override fun ParadoxScriptMember.selectLiteralValue(): String? {
        return when (this) {
            is ParadoxScriptProperty -> propertyValue?.selectLiteralValue()
            is ParadoxScriptBoolean -> value
            is ParadoxScriptInt -> value
            is ParadoxScriptFloat -> value
            is ParadoxScriptString -> value
            else -> null
        }
    }

    override fun ParadoxScriptProperty.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptProperty? {
        if (key.isEmpty()) return null
        return takeIf { PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
    }

    override fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean, usePattern: Boolean): Sequence<ParadoxScriptProperty> {
        if (key.isEmpty()) return emptySequence()
        return filter { PathMatcher.matches(it.name, key, ignoreCase, usePattern) }
    }

    override fun ParadoxScriptProperty.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): ParadoxScriptProperty? {
        return takeIf { keys.any { key -> PathMatcher.matches(it.name, key, ignoreCase, usePattern) } }
    }

    override fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean, usePattern: Boolean): Sequence<ParadoxScriptProperty> {
        return filter { property -> keys.any { key -> PathMatcher.matches(property.name, key, ignoreCase, usePattern) } }
    }

    override fun <T : ParadoxScriptMember> T.ofValue(value: String, ignoreCase: Boolean): T? {
        return takeIf { selectLiteralValue().equals(value, ignoreCase) }
    }

    override fun <T : ParadoxScriptMember> Sequence<T>.ofValue(value: String, ignoreCase: Boolean): Sequence<T> {
        return filter { it.selectLiteralValue().equals(value, ignoreCase) }
    }

    override fun <T : ParadoxScriptMember> T.ofValues(values: Collection<String>, ignoreCase: Boolean): T? {
        return takeIf { it.selectLiteralValue().let { v -> v != null && values.any { value -> v.equals(value, ignoreCase) } } }
    }

    override fun <T : ParadoxScriptMember> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean): Sequence<T> {
        return filter { it.selectLiteralValue().let { v -> v != null && values.any { value -> v.equals(value, ignoreCase) } } }
    }

    override fun ParadoxScriptMemberContainer.ofPath(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return ofPathInternal(path, ignoreCase, usePattern, conditional, inline)
    }

    override fun Sequence<ParadoxScriptMemberContainer>.ofPath(path: String, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return flatMap { it.ofPathInternal(path, ignoreCase, usePattern, conditional, inline) }
    }

    override fun ParadoxScriptMemberContainer.ofPaths(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return paths.asSequence().flatMap { path -> ofPathInternal(path, ignoreCase, usePattern, conditional, inline) }
    }

    override fun Sequence<ParadoxScriptMemberContainer>.ofPaths(paths: Collection<String>, ignoreCase: Boolean, usePattern: Boolean, conditional: Boolean, inline: Boolean): Sequence<ParadoxScriptMember> {
        return flatMap { paths.asSequence().flatMap { path -> it.ofPathInternal(path, ignoreCase, usePattern, conditional, inline) } }
    }

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

    // endregion

    // region Utility

    override fun PsiElement.parentMemberContainer(withSelf: Boolean): ParadoxScriptMemberContainer? {
        if (language !is ParadoxScriptLanguage) return null
        if (withSelf) this.castOrNull<ParadoxScriptMemberContainer>()?.let { return it }
        return parents(withSelf = withSelf).findIsInstance<ParadoxScriptMemberContainer>()
    }

    override fun PsiElement.parentOfKey(propertyName: String?, ignoreCase: Boolean, fromBlock: Boolean): ParadoxScriptMemberContainer? {
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

    override fun ParadoxScriptMember.parentOfPath(path: String, ignoreCase: Boolean, definitionType: String?): ParadoxScriptMemberContainer? {
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
            if (result !is ParadoxDefinitionElement) return null
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
