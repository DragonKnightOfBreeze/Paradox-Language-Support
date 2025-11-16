package icu.windea.pls.lang.util

import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationParameterPsiReference
import icu.windea.pls.localisation.psi.ParadoxLocalisationParameter
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

object ParadoxLocaclisationParameterManager {
    fun resolveLocalisation(element: ParadoxLocalisationParameter): ParadoxLocalisationProperty? {
        return element.reference?.castOrNull<ParadoxLocalisationParameterPsiReference>()?.resolveLocalisation()
    }

    fun resolveScriptedVariable(element: ParadoxLocalisationParameter): ParadoxScriptScriptedVariable? {
        return element.scriptedVariableReference?.reference?.castOrNull<ParadoxScriptedVariablePsiReference>()?.resolve()
    }
}
