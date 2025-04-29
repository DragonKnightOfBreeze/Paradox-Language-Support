package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import icu.windea.pls.*

class ViewDefinitionHierarchyAction : ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.hierarchy.definition"),
    PlsBundle.message("action.view.hierarchy.definition.description"),
    PlsIcons.Hierarchy.Definition
) {
    override fun getTypeName(): String {
        return ParadoxDefinitionHierarchyBrowser.getDefinitionHierarchyType()
    }
}
