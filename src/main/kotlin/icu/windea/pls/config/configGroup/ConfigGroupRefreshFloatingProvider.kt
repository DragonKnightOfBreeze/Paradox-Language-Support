package icu.windea.pls.config.configGroup

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent
import com.intellij.openapi.editor.toolbar.floating.isInsideMainEditor
import com.intellij.openapi.project.Project
import com.intellij.util.containers.DisposableWrapperList

//com.intellij.openapi.externalSystem.autoimport.ProjectRefreshFloatingProvider

class ConfigGroupRefreshFloatingProvider : AbstractFloatingToolbarProvider(ACTION_GROUP) {
    override val autoHideable = false

    private val toolbarComponents = DisposableWrapperList<Pair<Project, FloatingToolbarComponent>>()

    override fun isApplicable(dataContext: DataContext): Boolean {
        return isInsideMainEditor(dataContext)
    }

    override fun register(dataContext: DataContext, component: FloatingToolbarComponent, parentDisposable: Disposable) {
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return
        toolbarComponents.add(project to component, parentDisposable)
        updateToolbarComponent(project, component)
    }

    fun updateToolbarComponents(project: Project) {
        forEachToolbarComponent(project) {
            updateToolbarComponent(project, it)
        }
    }

    private fun updateToolbarComponent(project: Project, component: FloatingToolbarComponent) {
        val configGroupService = project.service<CwtConfigGroupService>()
        val isChanged = configGroupService.getConfigGroups().values.any { it.changed.get() }
        when (isChanged) {
            true -> component.scheduleShow()
            else -> component.scheduleHide()
        }
    }

    private fun forEachToolbarComponent(project: Project, consumer: (FloatingToolbarComponent) -> Unit) {
        for ((componentProject, component) in toolbarComponents) {
            if (componentProject === project) {
                consumer(component)
            }
        }
    }

    companion object {
        private const val ACTION_GROUP = "Pls.ConfigGroupRefreshActionGroup"
    }
}
