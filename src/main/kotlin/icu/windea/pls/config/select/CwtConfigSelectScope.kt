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

    // endregion

    // region Casts

    fun CwtMemberConfig<*>?.asProperty(): CwtPropertyConfig?

    fun Sequence<CwtMemberConfig<*>>.asProperty(): Sequence<CwtPropertyConfig>

    fun CwtMemberConfig<*>?.asValue(): CwtValueConfig?

    fun Sequence<CwtMemberConfig<*>>.asValue(): Sequence<CwtValueConfig>

    fun CwtMemberConfig<*>?.asBlock(): CwtValueConfig?

    fun Sequence<CwtMemberConfig<*>>.asBlock(): Sequence<CwtValueConfig>

    // endregion

    // region Queries

    fun CwtMemberConfig<*>?.selectLiteralValue(): String?

    fun CwtPropertyConfig?.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig?

    fun Sequence<CwtPropertyConfig>.ofKey(key: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig>

    fun CwtPropertyConfig?.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): CwtPropertyConfig?

    fun Sequence<CwtPropertyConfig>.ofKeys(keys: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtPropertyConfig>

    fun <T : CwtMemberConfig<*>> T?.ofValue(value: String, ignoreCase: Boolean = true): T?

    fun <T : CwtMemberConfig<*>> Sequence<T>.ofValue(value: String, ignoreCase: Boolean = true): Sequence<T>

    fun <T : CwtMemberConfig<*>> T?.ofValues(values: Collection<String>, ignoreCase: Boolean = true): T?

    fun <T : CwtMemberConfig<*>> Sequence<T>.ofValues(values: Collection<String>, ignoreCase: Boolean = true): Sequence<T>
    /**
     * 根据指定的路径，向下查询并依次匹配位于子句结构（属性&值）中的，可能出现在文件顶部 除了属性值以外的属性或值。
     *
     * 说明：
     * - 如果路径为空，则返回空序列。
     * - 如果子路径为 `-`，则匹配当前深度的任意块。
     * - 如果子路径为 `*`，则匹配当前深度的任意块值。
     *
     * - 如果 [key] 为 `null`，则匹配任意属性、块或文件。
     * - 如果 [key] 为 `*`，则匹配任意属性或文件。
     * - 如果 [key] 为 `-`，则匹配任意块或文件。
     * - 如果 [key] 为其他情况，则仅匹配键名符合条件的属性。
     *
     * @see CwtConfigPath
     */
    fun CwtMemberContainerConfig<*>?.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /** @see CwtConfigPath */
    fun Sequence<CwtMemberContainerConfig<*>>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /** @see CwtConfigPath */
    fun CwtMemberContainerConfig<*>?.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /** @see CwtConfigPath */
    fun Sequence<CwtMemberContainerConfig<*>>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    // endregion

    companion object {
        @JvmStatic val INSTANCE = CwtConfigSelectScopeImpl()
    }
}
