package icu.windea.pls.ep.overrides

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.base.annotations.WithGameTypeEP
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.search.util.ParadoxSearchParameters

/**
 * 用于从目标或查询参数得到覆盖策略。
 *
 * @see ParadoxOverrideStrategy
 */
@WithGameTypeEP
interface ParadoxOverrideStrategyProvider {
    /**
     * 得到目标（文件、全局封装变量、定义、本地化等）使用的覆盖策略。
     * 如果返回 `null`，则表示不适用覆盖策略。
     */
    fun get(target: Any): ParadoxOverrideStrategy?

    /**
     * 从查询参数得到目标（文件、全局封装变量、定义、本地化等）使用的覆盖策略。
     * 如果返回 `null`，则表示不适用覆盖策略。
     */
    fun get(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy?

    companion object INSTANCE {
        @JvmField val EP_NAME = ExtensionPointName<ParadoxOverrideStrategyProvider>("icu.windea.pls.overrideStrategyProvider")
    }
}
