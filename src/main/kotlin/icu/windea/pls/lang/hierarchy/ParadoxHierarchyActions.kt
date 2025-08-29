package icu.windea.pls.lang.hierarchy

import com.intellij.ide.hierarchy.HierarchyBrowserBaseEx
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ComboBoxAction
import com.intellij.util.application
import com.intellij.util.ui.JBUI
import icu.windea.pls.PlsBundle
import icu.windea.pls.PlsFacade
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.element
import icu.windea.pls.core.function
import icu.windea.pls.core.project
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyBrowser
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeType
import icu.windea.pls.lang.search.scope.type.ParadoxSearchScopeTypes
import icu.windea.pls.lang.settings.PlsStrategies
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import icu.windea.pls.lang.hierarchy.type.ParadoxDefinitionHierarchyType as Type

interface ParadoxHierarchyActions {
    //com.intellij.ide.hierarchy.HierarchyBrowserBaseEx.ChangeScopeAction

    class ChangeScopeTypeAction(
        val browser: HierarchyBrowserBaseEx,
        val settings: ParadoxHierarchyBrowserSettings
    ) : ComboBoxAction() {
        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            val presentation = e.presentation
            val scopeType = settings.scopeType
            presentation.text = ParadoxSearchScopeTypes.get(scopeType).text
        }

        override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
            val group = DefaultActionGroup()
            for (scopeType in ParadoxSearchScopeTypes.getScopeTypes(browser.project, browser.element)) {
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
                application.invokeLater({ browser.function("doRefresh")(true) }) { browser.isDisposed }
            }
        }
    }

    class ChangeGroupingStrategyAction(
        val browser: HierarchyBrowserBaseEx
    ) : ComboBoxAction() {
        override fun getActionUpdateThread() = ActionUpdateThread.EDT

        override fun update(e: AnActionEvent) {
            val type = browser.castOrNull<ParadoxDefinitionHierarchyBrowser>()?.type
            val strategy = when (type) {
                Type.EventTreeInvoker, Type.EventTreeInvoked -> PlsFacade.getSettings().hierarchy.eventTreeGrouping
                Type.TechTreePre, Type.TechTreePost -> PlsFacade.getSettings().hierarchy.techTreeGrouping
                else -> null
            }

            val presentation = e.presentation
            val visible = strategy != null
            presentation.isEnabledAndVisible = visible
            if (strategy != null) presentation.text = strategy.text
        }

        override fun createPopupActionGroup(button: JComponent, dataContext: DataContext): DefaultActionGroup {
            val type = browser.castOrNull<ParadoxDefinitionHierarchyBrowser>()?.type
            val strategies = when (type) {
                Type.EventTreeInvoker, Type.EventTreeInvoked -> PlsStrategies.EventTreeGrouping.entries
                Type.TechTreePre, Type.TechTreePost -> PlsStrategies.TechTreeGrouping.entries
                else -> emptyList()
            }

            val group = DefaultActionGroup()
            for (strategy in strategies) {
                group.add(MenuAction(strategy))
            }
            return group
        }

        override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
            val panel = JPanel(GridBagLayout())
            panel.add(
                JLabel(PlsBundle.message("label.groupingStrategy")),
                GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, JBUI.insetsLeft(5), 0, 0)
            )
            panel.add(
                super.createCustomComponent(presentation, place),
                GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, JBUI.emptyInsets(), 0, 0)
            )
            return panel
        }

        private inner class MenuAction(val strategy: PlsStrategies.Grouping) : AnAction(strategy.text) {
            override fun actionPerformed(e: AnActionEvent) {
                when (strategy) {
                    is PlsStrategies.EventTreeGrouping -> PlsFacade.getSettings().hierarchy.eventTreeGrouping = strategy
                    is PlsStrategies.TechTreeGrouping -> PlsFacade.getSettings().hierarchy.techTreeGrouping = strategy
                }

                // invokeLater is called to update state of button before long tree building operation
                // scope is kept per type so other builders don't need to be refreshed
                application.invokeLater({ browser.function("doRefresh")(true) }) { browser.isDisposed }
            }
        }
    }
}
