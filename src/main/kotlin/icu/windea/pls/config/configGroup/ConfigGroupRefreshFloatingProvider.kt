package icu.windea.pls.config.configGroup

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys.*
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent
import com.intellij.openapi.editor.toolbar.floating.isInsideMainEditor
import com.intellij.openapi.project.Project
import com.intellij.util.application
import icu.windea.pls.ChronicleFacade
import icu.windea.pls.config.listeners.CwtConfigGroupRefreshStatusListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// com.intellij.openapi.externalSystem.autoimport.ProjectRefreshFloatingProvider

class ConfigGroupRefreshFloatingProvider : AbstractFloatingToolbarProvider("Pls.ConfigGroupRefreshActionGroup") {
    override val autoHideable = false

    // NOTE 3.0.0 [compatibility] `FloatingToolbarProvider.isApplicable(DataContext)` is deprecated since IDEA-262
    //  - Use `isApplicableAsync` instead
    override fun isApplicable(dataContext: DataContext): Boolean {
        return isInsideMainEditor(dataContext)
    }

    private fun updateToolbarComponent(project: Project, component: FloatingToolbarComponent) {
        val coroutineScope = ChronicleFacade.getCoroutineScope(project)
        coroutineScope.launch {
            withContext(Dispatchers.EDT) {
                val configGroupService = CwtConfigGroupService.getInstance(project)
                val isChanged = configGroupService.getConfigGroups().values.any { it.changed }
                when (isChanged) {
                    true -> component.scheduleShow()
                    else -> component.scheduleHide()
                }
            }
        }
    }

    override fun register(dataContext: DataContext, component: FloatingToolbarComponent, parentDisposable: Disposable) {
        val currentProject = dataContext.getData(PROJECT) ?: return
        application.messageBus.connect(parentDisposable).subscribe(CwtConfigGroupRefreshStatusListener.TOPIC, object : CwtConfigGroupRefreshStatusListener {
            override fun onChange(project: Project) {
                if (currentProject == project) {
                    updateToolbarComponent(project, component)
                }
            }
        })
        updateToolbarComponent(currentProject, component)
    }
}
