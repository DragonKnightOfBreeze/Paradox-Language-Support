package icu.windea.pls.lang.select

import com.intellij.psi.PsiElement
import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.model.paths.ParadoxMemberPath
import icu.windea.pls.script.psi.ParadoxDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptBlock
import icu.windea.pls.script.psi.ParadoxScriptExpressionElement
import icu.windea.pls.script.psi.ParadoxScriptMember
import icu.windea.pls.script.psi.ParadoxScriptMemberContainer
import icu.windea.pls.script.psi.ParadoxScriptMemberContext
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptValue

@ParadoxSelectDsl
interface ParadoxSelectScope {
    // region Common

    fun <T : PsiElement> Sequence<T>.one(): T?

    fun <T : PsiElement> Sequence<T>.all(): List<T>

    fun ParadoxScriptMember.walkUp(): Sequence<ParadoxScriptMember>

    fun ParadoxScriptMemberContext.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS, conditional: Boolean? = null, inline: Boolean? = null): Sequence<ParadoxScriptMember>

    fun ParadoxScriptMember?.literalValue(): String?

    // endregion

    // region Filters

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

    fun PsiElement?.asMemberContext(): ParadoxScriptMemberContext?

    fun Sequence<PsiElement>.asMemberContext(): Sequence<ParadoxScriptMemberContext>

    fun ParadoxScriptProperty?.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty?

    fun Sequence<ParadoxScriptProperty>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty>

    fun ParadoxScriptProperty?.ofKeys(vararg keys: String, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty?

    fun Sequence<ParadoxScriptProperty>.ofKeys(vararg keys: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty>

    fun ParadoxScriptProperty?.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): ParadoxScriptProperty?

    fun Sequence<ParadoxScriptProperty>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<ParadoxScriptProperty>

    fun <T : ParadoxScriptMember> T?.ofValue(value: String, ignoreCase: Boolean = true, usePattern: Boolean = true): T?

    fun <T : ParadoxScriptMember> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<T>

    fun <T : ParadoxScriptMember> T?.ofValues(vararg values: String, ignoreCase: Boolean = true, usePattern: Boolean = true): T?

    fun <T : ParadoxScriptMember> Sequence<T>.ofValues(vararg values: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<T>

    fun <T : ParadoxScriptMember> T?.ofValues(values: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): T?

    fun <T : ParadoxScriptMember> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<T>

    // endregion

    // region Queries

    /**
     * 向下查询直接位于成员容器中的所有成员。
     */
    fun ParadoxScriptMemberContext?.query(conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /**
     * 向下查询直接位于成员容器中的所有成员。
     */
    fun Sequence<ParadoxScriptMemberContext>.query(conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /**
     * 根据指定的路径，递归向下查询并匹配直接位于成员容器中的所有成员。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see ParadoxMemberPath
     */
    fun ParadoxScriptMemberContext?.queryBy(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /**
     * 根据指定的路径，递归向下查询并匹配直接位于成员容器中的所有成员。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see ParadoxMemberPath
     */
    fun Sequence<ParadoxScriptMemberContext>.queryBy(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /**
     * 根据指定的一组路径，递归向下查询并匹配直接位于成员容器中的所有成员。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see ParadoxMemberPath
     */
    fun ParadoxScriptMemberContext?.queryBy(vararg paths: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /**
     * 根据指定的一组路径，递归向下查询并匹配直接位于成员容器中的所有成员。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see ParadoxMemberPath
     */
    fun Sequence<ParadoxScriptMemberContext>.queryBy(vararg paths: String, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /**
     * 根据指定的一组路径，递归向下查询并匹配直接位于成员容器中的所有成员。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see ParadoxMemberPath
     */
    fun ParadoxScriptMemberContext?.queryBy(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /**
     * 根据指定的一组路径，递归向下查询并匹配直接位于成员容器中的所有成员。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see ParadoxMemberPath
     */
    fun Sequence<ParadoxScriptMemberContext>.queryBy(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true, conditional: Boolean = false, inline: Boolean = false): Sequence<ParadoxScriptMember>

    /**
     * 向上查询直接位于成员容器中的第一个成员（包括脚本文件）。
     */
    fun PsiElement?.queryParent(withSelf: Boolean = true): ParadoxScriptMember?

    /**
     * 根据指定的路径，递归向上查询并匹配直接位于成员容器中的成员（包括脚本文件）。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see ParadoxMemberPath
     */
    fun PsiElement?.queryParentBy(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true, withSelf: Boolean = true): ParadoxScriptMember?

    // endregion

    // region Semantic Filters

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
        @JvmStatic val INSTANCE = ParadoxSelectScopeImpl()
    }
}
