package icu.windea.pls.ep.resolve.scope

import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.model.scope.ParadoxScopeContextInferenceInfo

class ParadoxBaseDynamicValueInferredScopeContextProvider : ParadoxDynamicValueInferredScopeContextProvider {
    override fun supports(element: ParadoxDynamicValueElement): Boolean {
        return true
    }

    override fun getScopeContext(element: ParadoxDynamicValueElement): ParadoxScopeContextInferenceInfo? {
        // TODO 1.1.10+
        return null
    }
}

