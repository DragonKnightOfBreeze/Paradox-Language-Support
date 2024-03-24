package icu.windea.pls.ep.configGroup

import com.intellij.openapi.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.components.*
import com.intellij.openapi.editor.toolbar.floating.*
import com.intellij.openapi.project.*
import com.intellij.util.containers.*

//com.intellij.openapi.externalSystem.autoimport.ProjectRefreshFloatingProvider

class ConfigGroupRefreshFloatingProvider : AbstractFloatingToolbarProvider(ACTION_GROUP) {
    override val autoHideable = false
    
    private val toolbarComponents = DisposableWrapperList<Pair<Project, FloatingToolbarComponent>>()
    
    override fun isApplicable(dataContext: DataContext): Boolean {
        return isInsideMainEditor(dataContext)
    }
    
    fun updateToolbarComponents(project: Project) {
        forEachToolbarComponent(project) {
            updateToolbarComponent(project, it)
        }
    }
    
    private fun updateToolbarComponent(project: Project, component: FloatingToolbarComponent) {
        val configGroupService = project.service<CwtConfigGroupService>()
        val isChanged = configGroupService.getConfigGroups().values.any { it.changed.get() }
        when(isChanged) {
            true -> component.scheduleShow()
            else -> component.scheduleHide()
        }
    }
    
    override fun register(dataContext: DataContext, component: FloatingToolbarComponent, parentDisposable: Disposable) {
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return
        toolbarComponents.add(project to component, parentDisposable)
        updateToolbarComponent(project, component)
    }
    
    private fun forEachToolbarComponent(project: Project, consumer: (FloatingToolbarComponent) -> Unit) {
        for((componentProject, component) in toolbarComponents) {
            if(componentProject === project) {
                consumer(component)
            }
        }
    }
    
    companion object {
        private const val ACTION_GROUP = "Pls.ConfigGroupRefreshActionGroup"
    }
}