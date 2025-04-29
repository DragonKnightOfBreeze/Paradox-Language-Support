package icu.windea.pls.lang.hierarchy.call

import com.intellij.icons.*
import com.intellij.ide.hierarchy.*
import icu.windea.pls.*

class ViewCallerHierarchyAction : ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.hierarchy.caller"),
    PlsBundle.message("action.view.hierarchy.caller.description"),
    AllIcons.Hierarchy.Supertypes
) {
    override fun getTypeName(): String {
        return CallHierarchyBrowserBase.getCallerType()
    }
}

