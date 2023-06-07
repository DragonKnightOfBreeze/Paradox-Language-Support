package icu.windea.pls.core.hierarchy

import com.intellij.ide.hierarchy.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.actionSystem.ex.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import com.intellij.util.ui.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.search.scope.type.*
import java.awt.*
import javax.swing.*

//com.intellij.ide.hierarchy.HierarchyBrowserBaseEx.ChangeScopeAction

class ChangeScopeTypeAction(
    val provider: HierarchyBrowserBaseEx,
    val settings: ParadoxHierarchyBrowserSettings
) : ComboBoxAction() {
    val project = provider.property<HierarchyBrowserBase>("myProject") as Project
    
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.EDT
    }
    
    override fun update(e: AnActionEvent) {
        val presentation = e.presentation
        val scopeType = settings.scopeType
        presentation.text = ParadoxSearchScopeTypes.get(scopeType).text
    }
    
    override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
        val element = provider.property("hierarchyBase") as PsiElement
        val group = DefaultActionGroup()
        for(scopeType in ParadoxSearchScopeTypes.getScopeTypes(project, element)) {
            group.add(MenuAction(scopeType))
        }
        return group
    }
    
    override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
        val panel = JPanel(GridBagLayout())
        panel.add(
            JLabel(PlsBundle.message("label.scopeType")),
            GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, JBUI.insetsLeft(5), 0, 0)
        )
        panel.add(
            super.createCustomComponent(presentation, place),
            GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0, 0)
        )
        return panel
    }
    
    private inner class MenuAction(val scopeType: ParadoxSearchScopeType) : AnAction(scopeType.text) {
        override fun actionPerformed(e: AnActionEvent) {
            settings.scopeType = scopeType.id
            
            // invokeLater is called to update state of button before long tree building operation
            // scope is kept per type so other builders don't need to be refreshed
            ApplicationManager.getApplication().invokeLater({ provider.function("doRefresh")(true) }) { provider.isDisposed }
        }
    }
}