package icu.windea.pls.ep.resolve.scope

import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.model.scope.ParadoxScopeContextInferenceInfo

class ParadoxBaseDynamicValueInferredScopeContextProvider : ParadoxDynamicValueInferredScopeContextProvider {
    override fun supports(element: ParadoxDynamicValueLightElement): Boolean {
        return true
    }

    override fun getScopeContext(element: ParadoxDynamicValueLightElement): ParadoxScopeContextInferenceInfo? {
        // TODO 1.1.10+
        return null
    }
}

