package icu.windea.pls.localisation.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapDescriptorBase
import com.intellij.codeInsight.unwrap.Unwrapper

// com.intellij.codeInsight.unwrap.JavaUnwrapDescriptor
// org.jetbrains.kotlin.idea.codeInsight.unwrap.KotlinUnwrapDescriptor

class ParadoxLocalisationUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        ParadoxLocalisationPropertyRemover(),
        ParadoxLocalisationIconRemover(),
        ParadoxLocalisationCommandRemover(),
        ParadoxLocalisationParameterRemover(),
        ParadoxLocalisationColorfulTextRemover(),
        ParadoxLocalisationColorfulTextUnwrapper(),
    )

    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}

