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

    // NOTE 2.1.1 这里仍然使用值相等，但是为了优化性能，在调用时通常可以直接使用引用相等（如果指针是直接来自原始规则的）
    override fun equals(other: Any?) = delegate == other

    override fun hashCode() = delegate.hashCode()

    override fun toString() = delegate.toString()
}
