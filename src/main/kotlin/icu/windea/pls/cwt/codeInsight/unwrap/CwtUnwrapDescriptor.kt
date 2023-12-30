package icu.windea.pls.cwt.codeInsight.unwrap

import com.intellij.codeInsight.unwrap.*

class CwtUnwrapDescriptor : UnwrapDescriptorBase() {
    private val _unwrappers = arrayOf(
        CwtUnwrappers.CwtPropertyRemover("cwt.remove.property"),
        CwtUnwrappers.CwtValueRemover("cwt.remove.value"),
        CwtUnwrappers.CwtBlockRemover("cwt.remove.block"),
        CwtUnwrappers.CwtPropertyUnwrapper("cwt.unwrap.property"),
        CwtUnwrappers.CwtBlockUnwrapper("cwt.unwrap.block"),
    )
    
    override fun createUnwrappers(): Array<out Unwrapper> {
        return _unwrappers
    }
}