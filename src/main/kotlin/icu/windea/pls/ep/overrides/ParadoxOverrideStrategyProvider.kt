package icu.windea.pls.ep.overrides

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.search.util.ParadoxSearchParameters
import icu.windea.pls.model.ParadoxGameType
import icu.windea.pls.model.overrides.ParadoxOverrideStrategy

/**
 * 覆盖策略提供者。
 *
 * 用于从目标或查询参数得到覆盖策略。
 *
 * 强制性的提供者会有限给出与规则文件无关的覆盖策略（文件/目录固定 `FIOS`、定义注入固定 `LIOS`、特定定义类型固定 `ORDERED`），
 * 随后才回退到基于规则文件中的覆盖规则的提供者（按目标目录的路径匹配）。
 *
 * @see ParadoxOverrideStrategy
 */
interface ParadoxOverrideStrategyProvider {
    fun supports(gameType: ParadoxGameType): Boolean = true

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
