package icu.windea.pls.cwt.psi

import com.intellij.psi.util.*

fun CwtValue.isPropertyValue(): Boolean {
	val parent = parent
	return parent is CwtProperty
}

fun CwtValue.isBlockValue(): Boolean {
	val parent = parent
	return parent is CwtBlockElement && parent.parentOfTypes(CwtOption::class, CwtOptionComment::class) == null
}

fun CwtValue.isOptionValue(): Boolean{
	val parent = parent
	return parent is CwtOption
}

fun CwtValue.isOptionBlockValue(): Boolean{
	val parent = parent
	if(parent is CwtOption) return true
	return parent is CwtBlockElement && parent.parentOfTypes(CwtOption::class, CwtOptionComment::class) != null
}