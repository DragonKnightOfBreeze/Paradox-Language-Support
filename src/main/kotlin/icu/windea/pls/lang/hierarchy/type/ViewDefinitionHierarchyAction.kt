package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
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
