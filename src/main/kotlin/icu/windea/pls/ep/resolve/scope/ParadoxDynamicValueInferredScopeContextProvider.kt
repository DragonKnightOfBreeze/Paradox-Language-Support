package icu.windea.pls.ep.resolve.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.annotations.PlsAnnotationManager
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.util.ParadoxScopeManager
import icu.windea.pls.model.scope.ParadoxScopeContext
import icu.windea.pls.model.scope.ParadoxScopeContextInferenceInfo

/**
 * 用于为动态值提供（基于使用）推断的作用域上下文。
 */
@WithGameTypeEP
interface ParadoxDynamicValueInferredScopeContextProvider {
    fun supports(element: ParadoxDynamicValueElement): Boolean

    fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContextInferenceInfo?

    // 注意：同名的动态值在不同的上下文中完全可能拥有不同的作用域上下文

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<ParadoxDynamicValueInferredScopeContextProvider>("icu.windea.pls.dynamicValueInferredScopeContextProvider")

        fun getScopeContext(dynamicValue: ParadoxDynamicValueElement): ParadoxScopeContext? {
            val gameType = dynamicValue.gameType
            var map: Map<String, String>? = null
            EP_NAME.extensionList.forEach f@{ ep ->
                if (!PlsAnnotationManager.check(ep, gameType)) return@f
                if (!ep.supports(dynamicValue)) return@f
                val info = ep.getScopeContext(dynamicValue) ?: return@f
                if (info.hasConflict) return null // 只要任何推断方式的推断结果存在冲突，就不要继续推断scopeContext
                if (map == null) {
                    map = info.scopeContextMap
                } else {
                    map = ParadoxScopeManager.mergeScopeContextMap(map!!, info.scopeContextMap)
                }
            }
            val resultMap = map ?: return null
            val result = ParadoxScopeContext.get(resultMap)
            return result
        }
    }
}
