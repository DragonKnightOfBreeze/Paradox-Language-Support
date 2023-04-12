package icu.windea.pls.core.hierarchy.call

import com.intellij.icons.*
import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*

class ViewCallerHierarchyAction: ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.caller.hierarchy"),
    PlsBundle.message("action.description.view.caller.hierarchy"),
    AllIcons.Hierarchy.Supertypes
) {
    override fun getTypeName(): String {
        return CallHierarchyBrowserBase.getCallerType()
    }
    
    override fun update(event: AnActionEvent) {
        super.update(event)
        val browser = getHierarchyBrowser(event.dataContext)
        event.presentation.isEnabled = browser != null
    }
}

