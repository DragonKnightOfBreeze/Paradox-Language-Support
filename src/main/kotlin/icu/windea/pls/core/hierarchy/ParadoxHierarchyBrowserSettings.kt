package icu.windea.pls.core.hierarchy

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.*

@State(name = "ParadoxHierarchyBrowserSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ParadoxHierarchyBrowserSettings: PersistentStateComponent<ParadoxHierarchyBrowserSettings> {
    var scopeType: String = "all"
    
    override fun getState() = this
    
    override fun loadState(state: ParadoxHierarchyBrowserSettings) = XmlSerializerUtil.copyBean(state, this)
    
    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<ParadoxHierarchyBrowserSettings>()
    }
}