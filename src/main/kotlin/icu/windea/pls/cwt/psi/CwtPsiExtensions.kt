package icu.windea.pls.cwt.psi

import com.intellij.psi.util.*

fun CwtValue.isPropertyValue(): Boolean {
    val parent = parent
    return parent is CwtProperty
}

fun CwtValue.isBlockValue(): Boolean {
    val parent = parent
    return parent is CwtRootBlock || (parent is CwtBlock && parent.parent !is CwtOption)
}

fun CwtValue.isOptionValue(): Boolean {
    val parent = parent
    return parent is CwtOption
}

fun CwtValue.isOptionBlockValue(): Boolean {
    val parent = parent
    return parent.elementType == CwtElementTypes.OPTION_COMMENT_TOKEN || (parent is CwtBlock && parent.parent is CwtOption)
}
