package icu.windea.pls.lang.hierarchy.type

import com.intellij.ide.hierarchy.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.model.*
import icu.windea.pls.core.util.*
import icu.windea.pls.lang.util.*

class ViewDefinitionHierarchyWithSubtypesAction: ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.definition.hierarchy.with.subtypes"),
    PlsBundle.message("action.description.view.definition.hierarchy.with.subtypes"),
    PlsIcons.Hierarchy.Definition
) {
    override fun getTypeName(): String {
        return ParadoxDefinitionHierarchyBrowser.getDefinitionHierarchyTypeWithSubtypes()
    }
}