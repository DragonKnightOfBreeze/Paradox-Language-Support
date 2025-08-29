package icu.windea.pls.lang.ui.clause

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

class ElementsContext(
    val project: Project,
    val editor: Editor,
    val propertyName: String?,
    val descriptorsInfoList: List<ElementsInfo>
) {
    var index: Int = 0

    val descriptorsInfo get() = descriptorsInfoList[index]
}
