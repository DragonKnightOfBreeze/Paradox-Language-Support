package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.UnwrapDescriptorBase
import com.intellij.codeInsight.unwrap.Unwrapper

// com.intellij.codeInsight.unwrap.JavaUnwrapDescriptor
// org.jetbrains.kotlin.idea.codeInsight.unwrap.KotlinUnwrapDescriptor

class CwtUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        CwtPropertyRemover(),
        CwtValueRemover(),
        CwtPropertyUnwrapper(),
        CwtBlockUnwrapper(),
    )

    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}
