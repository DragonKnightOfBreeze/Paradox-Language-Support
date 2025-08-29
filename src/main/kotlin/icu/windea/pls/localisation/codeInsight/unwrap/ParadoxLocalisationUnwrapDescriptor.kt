package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapDescriptorBase
import com.intellij.codeInsight.unwrap.Unwrapper

class ParadoxLocalisationUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        ParadoxLocalisationPropertyRemover(),
        ParadoxLocalisationIconRemover(),
        ParadoxLocalisationCommandRemover(),
        ParadoxLocalisationReferenceRemover(),
        ParadoxLocalisationColorfulTextRemover(),
        ParadoxLocalisationColorfulTextUnwrapper(),
    )

    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}

