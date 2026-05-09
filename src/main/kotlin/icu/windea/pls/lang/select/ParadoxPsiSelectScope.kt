package icu.windea.pls.lang.select

import com.intellij.psi.PsiElement
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

@ParadoxPsiSelectDsl
interface ParadoxPsiSelectScope {
    // region Common

    fun <T : PsiElement> Sequence<T>.one(): T?

    fun <T : PsiElement> Sequence<T>.all(): List<T>

    // endregion

    // region Walks

    fun PsiElement.walkUp(): Sequence<PsiElement>

    fun ParadoxScriptMemberContainer.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS, conditional: Boolean? = null, inline: Boolean? = null): Sequence<ParadoxScriptMember>

    // endregion

    // region Casts

    fun PsiElement?.asProperty(): ParadoxScriptProperty?

    fun Sequence<PsiElement>.asProperty(): Sequence<ParadoxScriptProperty>

    fun PsiElement?.asValue(): ParadoxScriptValue?

    fun Sequence<PsiElement>.asValue(): Sequence<ParadoxScriptValue>

    fun PsiElement?.asBlock(): ParadoxScriptBlock?

    fun Sequence<PsiElement>.asBlock(): Sequence<ParadoxScriptBlock>

    fun PsiElement?.asMember(): ParadoxScriptMember?

    fun Sequence<PsiElement>.asMember(): Sequence<ParadoxScriptMember>

    fun PsiElement?.asMemberContainer(): ParadoxScriptMemberContainer?

    fun Sequence<PsiElement>.asMemberContainer(): Sequence<ParadoxScriptMemberContainer>

    // endregion

    // region Queries

    fun ParadoxScriptMember.selectLiteralValue(): String?

    fun ParadoxScriptProperty.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty?

    fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty>

    fun ParadoxScriptProperty.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty?

    fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty>

    fun <T : ParadoxScriptMember> T.ofValue(value: String, ignoreCase: Boolean = true): T?

    fun <T : ParadoxScriptMember> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true): Sequence<T>

    fun <T : ParadoxScriptMember> T.ofValues(values: Collection<String>, ignoreCase: Boolean = true): T?

    fun <T : ParadoxScriptMember> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true): Sequence<T>

    /** @see ParadoxMemberPath */
    fun ParadoxScriptMemberContainer.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see ParadoxMemberPath */
    fun Sequence<ParadoxScriptMemberContainer>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see ParadoxMemberPath */
    fun ParadoxScriptMemberContainer.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see ParadoxMemberPath */
    fun Sequence<ParadoxScriptMemberContainer>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    // endregion

    // region Utility

    fun PsiElement.parentMemberContainer(withSelf: Boolean = false): ParadoxScriptMemberContainer?

    fun PsiElement.parentOfKey(propertyName: String? = null, ignoreCase: Boolean = true, fromBlock: Boolean = false): ParadoxScriptMemberContainer?

    /** @see ParadoxMemberPath */
    fun ParadoxScriptMember.parentOfPath(path: String = "", ignoreCase: Boolean = true, definitionType: String? = null): ParadoxScriptMemberContainer?

    // endregion

    // region Semantic Queries

    fun ParadoxDefinitionElement.nameElement(nameField: String?): ParadoxScriptExpressionElement?

    fun ParadoxDefinitionElement.nameFieldElement(nameField: String?): ParadoxScriptValue?

    fun PsiElement.parentDefinitionCandidate(withSelf: Boolean = true): ParadoxDefinitionElement?

    fun PsiElement.parentDefinition(withSelf: Boolean = true): ParadoxDefinitionElement?

    fun PsiElement.parentDefinitionInjection(withSelf: Boolean = true): ParadoxScriptProperty?

    fun PsiElement.parentDefineVariable(withSelf: Boolean = true): ParadoxScriptProperty?

    // endregion
}


