package icu.windea.pls.config.select

import com.intellij.util.containers.TreeTraversal
import icu.windea.pls.config.config.CwtMemberConfig
import icu.windea.pls.config.config.CwtMemberContainerConfig
import icu.windea.pls.config.config.CwtPropertyConfig
import icu.windea.pls.config.config.CwtValueConfig
import icu.windea.pls.model.paths.CwtConfigPath

@CwtConfigSelectDsl
interface CwtConfigSelectScope {
    // region Common

    fun <T : CwtMemberConfig<*>> Sequence<T>.one(): T?

    fun <T : CwtMemberConfig<*>> Sequence<T>.all(): List<T>

    fun CwtMemberConfig<*>.walkUp(): Sequence<CwtMemberConfig<*>>

    fun CwtMemberContainerConfig<*>.walkDown(traversal: TreeTraversal = TreeTraversal.PRE_ORDER_DFS): Sequence<CwtMemberConfig<*>>

    fun CwtMemberConfig<*>?.literalValue(): String?

    // endregion

    // region Filters

    fun CwtMemberConfig<*>?.asProperty(): CwtPropertyConfig?

    fun Sequence<CwtMemberConfig<*>>.asProperty(): Sequence<CwtPropertyConfig>

    fun CwtMemberConfig<*>?.asValue(): CwtValueConfig?

    fun Sequence<CwtMemberConfig<*>>.asValue(): Sequence<CwtValueConfig>

    fun CwtMemberConfig<*>?.asBlock(): CwtValueConfig?

    fun Sequence<CwtMemberConfig<*>>.asBlock(): Sequence<CwtValueConfig>

    fun CwtPropertyConfig?.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig?

    fun Sequence<CwtPropertyConfig>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig>

    fun CwtPropertyConfig?.ofKeys(vararg keys: String, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig?

    fun Sequence<CwtPropertyConfig>.ofKeys(vararg keys: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig>

    fun CwtPropertyConfig?.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig?

    fun Sequence<CwtPropertyConfig>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig>

    fun <T : CwtMemberConfig<*>> T?.ofValue(value: String, ignoreCase: Boolean = true, usePattern: Boolean = true): T?

    fun <T : CwtMemberConfig<*>> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<T>

    fun <T : CwtMemberConfig<*>> T?.ofValues(vararg values: String, ignoreCase: Boolean = true, usePattern: Boolean = true): T?

    fun <T : CwtMemberConfig<*>> Sequence<T>.ofValues(vararg values: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<T>

    fun <T : CwtMemberConfig<*>> T?.ofValues(values: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): T?

    fun <T : CwtMemberConfig<*>> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<T>

    // endregion

    // region Queries

    /**
     * 向下查询直接位于成员容器中的所有成员对应的成员规则。
     */
    fun CwtMemberContainerConfig<*>?.query(): Sequence<CwtMemberConfig<*>>

    /**
     * 向下查询直接位于成员容器中的所有成员对应的成员规则。
     */
    fun Sequence<CwtMemberContainerConfig<*>>.query(): Sequence<CwtMemberConfig<*>>

    /**
     * 根据指定的路径，递归向下查询并匹配直接位于成员容器中的所有成员对应的成员规则。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see CwtConfigPath
     */
    fun CwtMemberContainerConfig<*>?.queryBy(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /**
     * 根据指定的路径，递归向下查询并匹配直接位于成员容器中的所有成员对应的成员规则。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see CwtConfigPath
     */
    fun Sequence<CwtMemberContainerConfig<*>>.queryBy(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /**
     * 根据指定的一组路径，递归向下查询并匹配直接位于成员容器中的所有成员对应的成员规则。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see CwtConfigPath
     */
    fun CwtMemberContainerConfig<*>?.queryBy(vararg paths: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /**
     * 根据指定的一组路径，递归向下查询并匹配直接位于成员容器中的所有成员对应的成员规则。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see CwtConfigPath
     */
    fun Sequence<CwtMemberContainerConfig<*>>.queryBy(vararg paths: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /**
     * 根据指定的一组路径，递归向下查询并匹配直接位于成员容器中的所有成员对应的成员规则。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see CwtConfigPath
     */
    fun CwtMemberContainerConfig<*>?.queryBy(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /**
     * 根据指定的一组路径，递归向下查询并匹配直接位于成员容器中的所有成员对应的成员规则。如果路径为空或者无法匹配，则直接返回空序列。
     *
     * @see CwtConfigPath
     */
    fun Sequence<CwtMemberContainerConfig<*>>.queryBy(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    // endregion

    companion object {
        @JvmStatic val INSTANCE = CwtConfigSelectScopeImpl()
    }
}
