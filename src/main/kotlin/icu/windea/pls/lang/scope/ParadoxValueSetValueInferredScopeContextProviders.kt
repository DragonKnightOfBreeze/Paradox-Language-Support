package icu.windea.pls.lang.scope

import icu.windea.pls.core.psi.*
import icu.windea.pls.model.*

class ParadoxBaseValueSetValueInferredScopeContextProvider: ParadoxValueSetValueInferredScopeContextProvider {
    override fun supports(valueSetValue: ParadoxValueSetValueElement): Boolean {
        return true
    }
    
    override fun getScopeContext(valueSetValue: ParadoxValueSetValueElement): ParadoxScopeContextInferenceInfo? {
        //TODO 1.1.10+
        return null
    }
}
