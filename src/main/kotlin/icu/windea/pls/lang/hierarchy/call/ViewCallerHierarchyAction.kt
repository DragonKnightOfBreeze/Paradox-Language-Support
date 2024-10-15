package icu.windea.pls.lang.hierarchy.call

import com.intellij.icons.*
import com.intellij.ide.hierarchy.*
import icu.windea.pls.*

class ViewCallerHierarchyAction : ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.caller.hierarchy"),
    PlsBundle.message("action.description.view.caller.hierarchy"),
    AllIcons.Hierarchy.Supertypes
) {
    override fun getTypeName(): String {
        return CallHierarchyBrowserBase.getCallerType()
    }
}

