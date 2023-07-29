package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*

class CwtUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        CwtUnwrappers.CwtPropertyRemover("cwt.remove.property"),
        CwtUnwrappers.CwtValueRemover("cwt.remove.value"),
    )
    
    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}