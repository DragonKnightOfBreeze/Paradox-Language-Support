package icu.windea.pls.lang.ui.clause

class ElementsInfo(
    val descriptors: List<ElementDescriptor>,
    val hasRemain: Boolean
) {
    val allValues = descriptors.filterIsInstance<ElementDescriptors.Value>().map { it.name }.toTypedArray()
    val allKeys = descriptors.filterIsInstance<ElementDescriptors.Property>().map { it.name }.toTypedArray()
    val allKeyValuesMap = descriptors.filterIsInstance<ElementDescriptors.Property>().associateBy({ it.name }, { it.constantValueArray })

    val resultDescriptors = descriptors.mapTo(mutableListOf()) { it.copyDescriptor() }
}
