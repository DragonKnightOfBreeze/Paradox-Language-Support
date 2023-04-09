package icu.windea.pls.core.hierarchy

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import icons.*
import icu.windea.pls.*
import icu.windea.pls.core.hierarchy.*

class ViewDefinitionHierarchy2Action: ChangeHierarchyViewActionBase(
    PlsBundle.message("action.view.definition.hierarchy.2"),
    PlsBundle.message("action.description.view.definition.hierarchy.2"),
    PlsIcons.Hierarchy.Definition
) {
    override fun getTypeName(): String {
        return ParadoxDefinitionHierarchyBrowser.getDefinitionHierarchyType2()
    }
    
    override fun update(event: AnActionEvent) {
        super.update(event)
        val browser = getHierarchyBrowser(event.dataContext)
        event.presentation.isEnabled = browser != null
    }
}