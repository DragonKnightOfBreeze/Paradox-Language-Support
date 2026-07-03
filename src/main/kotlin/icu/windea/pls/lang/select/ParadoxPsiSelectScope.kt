package icu.windea.pls.lang.select

import com.intellij.psi.PsiElement
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContext
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

@ParadoxPsiSelectDsl
interface ParadoxPsiSelectScope {
    // region Common

    fun <T : PsiElement> Sequence<T>.one(): T?

    fun <T : PsiElement> Sequence<T>.all(): List<T>

    fun ParadoxScriptMember.walkUp(): Sequence<ParadoxScriptMember>

    fun ParadoxScriptMemberContext.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS, conditional: Boolean? = null, inline: Boolean? = null): Sequence<ParadoxScriptMember>

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

    fun PsiElement?.asMemberContainer(): ParadoxScriptMemberContext?

    fun Sequence<PsiElement>.asMemberContainer(): Sequence<ParadoxScriptMemberContext>

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
    fun ParadoxScriptMemberContext?.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see @see ParadoxMemberPath */
    fun Sequence<ParadoxScriptMemberContext>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see @see ParadoxMemberPath */
    fun ParadoxScriptMemberContext?.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** @see @see ParadoxMemberPath */
    fun Sequence<ParadoxScriptMemberContext>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /** 包含自身在内，向上查询父节点，返回第一个 [ParadoxScriptProperty]。 */
    fun PsiElement?.containingProperty(): ParadoxScriptProperty?

    /** 包含自身在内，向上查询父节点，返回第一个 [ParadoxScriptBlock]。 */
    fun PsiElement?.containingBlock(): ParadoxScriptBlock?

    /** 包含自身在内，向上查询父节点，返回第一个 [ParadoxScriptMember]。 */
    fun PsiElement?.containingMember(): ParadoxScriptMember?

    /** 包含自身在内，向上查询父节点，返回第一个 [ParadoxScriptMemberContext]。 */
    fun PsiElement?.containingMemberContainer(): ParadoxScriptMemberContext?

    /** 向上查询父节点，返回第一个 [ParadoxScriptProperty]。 */
    fun PsiElement?.parentProperty(): ParadoxScriptProperty?

    /** 向上查询父节点，返回第一个 [ParadoxScriptBlock]。 */
    fun PsiElement?.parentBlock(): ParadoxScriptBlock?

    /** 向上查询父节点，返回第一个 [ParadoxScriptMember]。 */
    fun PsiElement?.parentMember(): ParadoxScriptMember?

    /** 向上查询父节点，返回第一个 [ParadoxScriptMemberContext]。 */
    fun PsiElement?.parentMemberContainer(): ParadoxScriptMemberContext?

    /**
     * 在当前脚本文件中，包含自身在内，根据指定的 [key] 向上查询并依次匹配属性、块值（位于文件顶级，或者直接位于块中的值）或者文件。
     *
     * 说明：
     * - 如果 [key] 为 `null`，则匹配任意属性、块或文件。
     * - 如果 [key] 为 `*`，则匹配任意属性或文件。
     * - 如果 [key] 为 `-`，则匹配任意块或文件。
     * - 如果 [key] 为其他情况，则仅匹配键名符合条件的属性。
     */
    fun PsiElement?.parentOfKey(key: String? = null, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptMember?

    /**
     * 在当前脚本文件中，包含自身在内，根据指定的 [path] 向上查询并依次匹配属性、块值（位于文件顶级，或者直接位于块中的值）或者文件。
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

    companion object {
        @JvmStatic val INSTANCE = ParadoxPsiSelectScopeImpl()
    }
}
