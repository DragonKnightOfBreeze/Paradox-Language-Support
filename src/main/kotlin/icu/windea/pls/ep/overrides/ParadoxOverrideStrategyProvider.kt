package icu.windea.pls.ep.overrides

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.search.ParadoxSearchParameters
import icu.windea.pls.lang.selectGameType

/**
 * 用于从目标或查询参数得到覆盖方式。
 *
 * @see ParadoxOverrideStrategy
 */
@WithGameTypeEP
interface ParadoxOverrideStrategyProvider {
    /**
     * 得到目标（文件、全局封装变量、定义、本地化等）使用的覆盖方式。
     * 如果返回 `null`，则表示不适用覆盖方式。
     */
    fun get(target: Any): ParadoxOverrideStrategy?

    /**
     * 从查询参数得到目标（文件、全局封装变量、定义、本地化等）使用的覆盖方式。
     * 如果返回 `null`，则表示不适用覆盖方式。
     */
    fun get(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy?

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxOverrideStrategyProvider>("icu.windea.pls.overrideStrategyProvider")

        fun get(target: Any): ParadoxOverrideStrategy? {
            val gameType by lazy { selectGameType(target) }
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (gameType != null && !PlsAnnotationManager.check(ep, gameType)) return@f null
                ep.get(target)
            }
        }

        fun get(searchParameters: ParadoxSearchParameters<*>): ParadoxOverrideStrategy? {
            val gameType = searchParameters.selector.gameType
            return EP_NAME.extensionList.firstNotNullOfOrNull f@{ ep ->
                if (gameType != null && !PlsAnnotationManager.check(ep, gameType)) return@f null
                ep.get(searchParameters)
            }
        }
    }
}
