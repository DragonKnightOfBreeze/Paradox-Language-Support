package icu.windea.pls.lang.scope.impl

import icu.windea.pls.core.psi.*
import icu.windea.pls.lang.scope.*
import icu.windea.pls.model.*

private val VALUE_SETS = arrayOf("event_target", "global_event_target")

class ParadoxBaseValueSetValueInferredScopeContextProvider: ParadoxValueSetValueInferredScopeContextProvider {
    override fun supports(valueSetValue: ParadoxValueSetValueElement): Boolean {
        return valueSetValue.valueSetNames.any { it in VALUE_SETS }
    }
    
    override fun getScopeContext(valueSetValue: ParadoxValueSetValueElement): ParadoxScopeContextInferenceInfo? {
        //TODO 1.1.10+
        return null
    }
}