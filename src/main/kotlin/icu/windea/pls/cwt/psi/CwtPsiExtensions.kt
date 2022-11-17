package icu.windea.pls.cwt.psi

fun CwtValue.isPropertyValue(): Boolean {
	return parent is CwtProperty
}