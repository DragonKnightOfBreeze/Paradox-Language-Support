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

    // endregion

    // region Walks

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

    /** @see CwtConfigPath */
    fun CwtMemberContainerConfig<*>?.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /** @see CwtConfigPath */
    fun Sequence<CwtMemberContainerConfig<*>>.ofPath(path: String, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /** @see CwtConfigPath */
    fun CwtMemberContainerConfig<*>?.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    /** @see CwtConfigPath */
    fun Sequence<CwtMemberContainerConfig<*>>.ofPaths(paths: Collection<String>, ignoreCase: Boolean = true, usePattern: Boolean = true): Sequence<CwtMemberConfig<*>>

    // endregion
}
