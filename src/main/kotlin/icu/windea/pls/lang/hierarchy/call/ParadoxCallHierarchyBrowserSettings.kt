package icu.windea.pls.lang.hierarchy.call

import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.StoragePathMacros
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.util.xmlb.XmlSerializerUtil
import icu.windea.pls.lang.hierarchy.ParadoxHierarchyBrowserSettings

@Service(Service.Level.PROJECT)
@State(name = "ParadoxCallHierarchyBrowserSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ParadoxCallHierarchyBrowserSettings : PersistentStateComponent<ParadoxCallHierarchyBrowserSettings>, ParadoxHierarchyBrowserSettings {
    override var scopeType: String = "all"

    override fun getState() = this

    override fun loadState(state: ParadoxCallHierarchyBrowserSettings) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<ParadoxCallHierarchyBrowserSettings>()
    }
}
