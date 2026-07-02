package icu.windea.pls.lang.select

import com.intellij.psi.PsiElement
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxScriptFile

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

    fun ParadoxScriptMember?.literalValue(): String?

    fun ParadoxScriptProperty?.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty?

    fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty>

    fun ParadoxScriptProperty?.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty?

    fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty>

    fun <T : ParadoxScriptMember> T?.ofValue(value: String, ignoreCase: Boolean = true): T?

    fun <T : ParadoxScriptMember> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true): Sequence<T>

    fun <T : ParadoxScriptMember> T?.ofValues(values: Collection<String>, ignoreCase: Boolean = true): T?

    fun <T : ParadoxScriptMember> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true): Sequence<T>

    /** @see @see ParadoxMemberPath */
    fun ParadoxScriptMemberContainer?.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see @see ParadoxMemberPath */
    fun Sequence<ParadoxScriptMemberContainer>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see @see ParadoxMemberPath */
    fun ParadoxScriptMemberContainer?.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see @see ParadoxMemberPath */
    fun Sequence<ParadoxScriptMemberContainer>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** 包含自身在内，向上查询父节点，返回第一个 [ParadoxScriptProperty]。 */
    fun PsiElement?.containingProperty(): ParadoxScriptProperty?

    /** 包含自身在内，向上查询父节点，返回第一个 [ParadoxScriptBlock]。 */
    fun PsiElement?.containingBlock(): ParadoxScriptBlock?

    /** 包含自身在内，向上查询父节点，返回第一个 [ParadoxScriptMember]。 */
    fun PsiElement?.containingMember(): ParadoxScriptMember?

    /** 包含自身在内，向上查询父节点，返回第一个 [ParadoxScriptMemberContainer]。 */
    fun PsiElement?.containingMemberContainer(): ParadoxScriptMemberContainer?

    /** 向上查询父节点，返回第一个 [ParadoxScriptProperty]。 */
    fun PsiElement?.parentProperty(): ParadoxScriptProperty?

    /** 向上查询父节点，返回第一个 [ParadoxScriptBlock]。 */
    fun PsiElement?.parentBlock(): ParadoxScriptBlock?

    /** 向上查询父节点，返回第一个 [ParadoxScriptMember]。 */
    fun PsiElement?.parentMember(): ParadoxScriptMember?

    /** 向上查询父节点，返回第一个 [ParadoxScriptMemberContainer]。 */
    fun PsiElement?.parentMemberContainer(): ParadoxScriptMemberContainer?

    /**
     * 根据指定的 [key]，向上查询匹配的 [ParadoxScriptProperty]、[ParadoxScriptBlock] 或 [ParadoxScriptFile]。
     *
     * 说明：
     * - 如果 [key] 为 `null`，则匹配任意属性、块或文件。
     * - 如果 [key] 为 `*`，则匹配任意属性或文件。
     * - 如果 [key] 为 `-`，则匹配任意块或文件。
     * - 如果 [key] 为其他情况，则仅匹配键名符合条件的属性。
     */
    fun PsiElement?.parentOfKey(key: String? = null, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptMember?

    /**
     * 根据指定的 [path]，向上查询匹配的 [ParadoxScriptProperty]、[ParadoxScriptBlock] 或 [ParadoxScriptFile]。
     *
     * 说明：
     * - 如果 [path] 为空，则直接返回 `null`。
     * - 如果 [path] 为其他情况，则根据子路径从后向前依次进行匹配。
     *
     * @see ParadoxMemberPath
     */
    fun PsiElement?.parentOfPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptMember?

    // endregion

    // region Semantic Casts

    fun PsiElement?.asDefinition(typeExpression: String? = null): ParadoxDefinitionElement?

    fun Sequence<PsiElement>.asDefinition(typeExpression: String?): Sequence<ParadoxDefinitionElement>

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
