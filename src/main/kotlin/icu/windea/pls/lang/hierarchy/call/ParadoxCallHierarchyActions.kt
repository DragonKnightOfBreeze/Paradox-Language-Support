package icu.windea.pls.lang.hierarchy.call

import com.intellij.icons.AllIcons
import com.intellij.ide.hierarchy.CallHierarchyBrowserBase
import com.intellij.ide.hierarchy.ChangeHierarchyViewActionBase
import icu.windea.pls.PlsBundle

interface ParadoxCallHierarchyActions {
    class ViewCallerHierarchyAction : ChangeHierarchyViewActionBase(
        PlsBundle.message("action.view.hierarchy.caller"),
        PlsBundle.message("action.view.hierarchy.caller.description"),
        AllIcons.Hierarchy.Supertypes
    ) {
        override fun getTypeName() = CallHierarchyBrowserBase.getCallerType()
    }

    class ViewCalleeHierarchyAction : ChangeHierarchyViewActionBase(
        PlsBundle.message("action.view.hierarchy.callee"),
        PlsBundle.message("action.view.hierarchy.callee.description"),
        AllIcons.Hierarchy.Subtypes
    ) {
        override fun getTypeName() = CallHierarchyBrowserBase.getCalleeType()
    }
}
