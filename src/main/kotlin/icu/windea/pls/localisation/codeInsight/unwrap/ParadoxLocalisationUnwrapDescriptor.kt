package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*

class ParadoxLocalisationUnwrapDescriptor : UnwrapDescriptorBase() {
    companion object {
        private val _unwrappers = arrayOf(
            ParadoxLocalisationUnwrappers.ParadoxLocalisationPropertyRemover("localisation.remove.property"),
            ParadoxLocalisationUnwrappers.ParadoxLocalisationIconRemover("localisation.remove.icon"),
            ParadoxLocalisationUnwrappers.ParadoxLocalisationCommandRemover("localisation.remove.command"),
            ParadoxLocalisationUnwrappers.ParadoxLocalisationReferenceRemover("localisation.remove.reference"),
            ParadoxLocalisationUnwrappers.ParadoxLocalisationColorfulTextRemover("localisation.remove.color"),
            ParadoxLocalisationUnwrappers.ParadoxLocalisationColorfulTextUnwrapper("localisation.unwrap.color"),
        )
    }
    
    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}

