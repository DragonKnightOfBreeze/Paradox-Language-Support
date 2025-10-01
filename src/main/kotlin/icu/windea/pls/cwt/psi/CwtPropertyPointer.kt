package icu.windea.pls.cwt.psi

import com.intellij.psi.SmartPsiElementPointer
import icu.windea.pls.core.createPointer

/**
 * [CwtProperty] 的智能指针封装。保留对属性值（[CwtValue]）的指针，便于跨线程/缓存安全地访问属性及其值。
 */
class CwtPropertyPointer(
    private val delegate: SmartPsiElementPointer<CwtProperty>
) : SmartPsiElementPointer<CwtProperty> by delegate {
    val valuePointer: SmartPsiElementPointer<CwtValue>? = delegate.element?.propertyValue?.createPointer()
}
