package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import icu.windea.pls.*

class ViewDefinitionHierarchyWithSubtypesAction : ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.hierarchy.definition.with.subtypes"),
    PlsBundle.message("action.view.hierarchy.definition.with.subtypes.description"),
    PlsIcons.Hierarchy.Definition
) {
    override fun getTypeName(): String {
        return ParadoxDefinitionHierarchyBrowser.getDefinitionHierarchyTypeWithSubtypes()
    }
}
