package icu.windea.pls.lang.ui

class ElementsInfo(
	val descriptors: List<ElementDescriptor>,
	val hasRemain: Boolean
) {
	val allValues = descriptors.filterIsInstance<ValueDescriptor>().map { it.name }.toTypedArray()
	val allKeys = descriptors.filterIsInstance<PropertyDescriptor>().map { it.name }.toTypedArray()
	val allKeyValuesMap = descriptors.filterIsInstance<PropertyDescriptor>().associateBy({it.name}, {it.constantValueArray})
	
	val resultDescriptors = descriptors.mapTo(mutableListOf()) { it.copyDescriptor() }
}