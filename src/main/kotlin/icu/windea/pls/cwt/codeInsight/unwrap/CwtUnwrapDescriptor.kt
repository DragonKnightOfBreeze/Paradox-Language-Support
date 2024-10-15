package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*

class CwtUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        CwtPropertyRemover(),
        CwtValueRemover(),
        CwtBlockRemover(),
        CwtPropertyUnwrapper(),
        CwtBlockUnwrapper(),
    )

    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}
