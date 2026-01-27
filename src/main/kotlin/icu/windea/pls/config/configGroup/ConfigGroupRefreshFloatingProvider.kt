package icu.windea.pls.config.configGroup

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.application.EDT
import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider
import com.intellij.openapi.editor.toolbar.floating.FloatingToolbarComponent
import com.intellij.openapi.editor.toolbar.floating.isInsideMainEditor
import com.intellij.openapi.project.Project
import com.intellij.util.application
import icu.windea.pls.PlsFacade
import icu.windea.pls.config.listeners.CwtConfigGroupRefreshStatusListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// com.intellij.openapi.externalSystem.autoimport.ProjectRefreshFloatingProvider

class ConfigGroupRefreshFloatingProvider : AbstractFloatingToolbarProvider(ACTION_GROUP) {
    override val autoHideable = false

    override fun isApplicable(dataContext: DataContext): Boolean {
        return isInsideMainEditor(dataContext)
    }

    private fun updateToolbarComponent(project: Project, component: FloatingToolbarComponent) {
        val coroutineScope = PlsFacade.getCoroutineScope(project)
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
        val project = dataContext.getData(CommonDataKeys.PROJECT) ?: return
        application.messageBus.connect(parentDisposable).subscribe(CwtConfigGroupRefreshStatusListener.TOPIC, object : CwtConfigGroupRefreshStatusListener {
            override fun onChange(project: Project) {
                updateToolbarComponent(project, component)
            }
        })
        updateToolbarComponent(project, component)
    }

    companion object {
        private const val ACTION_GROUP = "Pls.ConfigGroupRefreshActionGroup"
    }
}
