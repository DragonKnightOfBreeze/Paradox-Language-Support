package icu.windea.pls.core.ui

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class ElementDescriptorContext(
	val project: Project,
	val editor: Editor,
	val descriptors: List<ElementDescriptor>
) {
	val allValues = descriptors.filterIsInstance<ValueDescriptor>().map { it.name }.toTypedArray()
	val allKeys = descriptors.filterIsInstance<PropertyDescriptor>().map { it.name }.toTypedArray()
	val allKeyValuesMap = descriptors.filterIsInstance<PropertyDescriptor>().associateBy({it.name}, {it.constantValueArray})
	
	val resultDescriptors = descriptors.mapTo(mutableListOf()) { it.copyDescriptor() }
}