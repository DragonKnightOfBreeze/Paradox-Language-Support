package icu.windea.pls.core.hierarchy.call

import com.intellij.icons.*
import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import icu.windea.pls.*

class ViewCalleeHierarchyAction: ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.callee.hierarchy"),
    PlsBundle.message("action.description.view.callee.hierarchy"),
    AllIcons.Hierarchy.Subtypes
) {
    override fun getTypeName(): String {
        return CallHierarchyBrowserBase.getCalleeType()
    }
    
    override fun update(event: AnActionEvent) {
        super.update(event)
        val browser = getHierarchyBrowser(event.dataContext)
        event.presentation.isEnabled = browser != null
    }
}