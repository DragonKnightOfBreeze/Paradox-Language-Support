package icu.windea.pls.core.hierarchy.type

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import icons.*
import icu.windea.pls.*

class ViewDefinitionHierarchyAction: ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.definition.hierarchy"),
    PlsBundle.message("action.description.view.definition.hierarchy"),
    PlsIcons.Hierarchy.Definition
) {
    override fun getTypeName(): String {
        return ParadoxDefinitionHierarchyBrowser.getDefinitionHierarchyType()
    }
}
