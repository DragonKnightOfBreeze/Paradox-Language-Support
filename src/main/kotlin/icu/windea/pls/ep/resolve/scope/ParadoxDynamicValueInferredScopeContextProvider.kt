package icu.windea.pls.ep.resolve.scope

import com.intellij.openapi.extensions.ExtensionPointName
import icu.windea.pls.lang.annotations.WithGameTypeEP
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
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
    }
}
