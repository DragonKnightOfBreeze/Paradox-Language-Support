package icu.windea.pls.core.ui

import com.intellij.openapi.editor.*
import com.intellij.openapi.project.*

class ElementsContext(
	val project: Project,
	val editor: Editor,
	val propertyName: String?,
	val descriptorsInfoList: List<ElementsInfo>
) {
	var index: Int = 0
	
	val descriptorsInfo get() = descriptorsInfoList[index]
}
