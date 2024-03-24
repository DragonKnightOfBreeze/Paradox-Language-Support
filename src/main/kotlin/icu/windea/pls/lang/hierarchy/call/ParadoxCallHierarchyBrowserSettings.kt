package icu.windea.pls.lang.hierarchy.call

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.*
import icu.windea.pls.core.hierarchy.*

@Service(Service.Level.PROJECT)
@State(name = "ParadoxCallHierarchyBrowserSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ParadoxCallHierarchyBrowserSettings : PersistentStateComponent<icu.windea.pls.lang.hierarchy.call.ParadoxCallHierarchyBrowserSettings>, icu.windea.pls.lang.hierarchy.ParadoxHierarchyBrowserSettings {
    override var scopeType: String = "all"
    
    override fun getState() = this
    
    override fun loadState(state: icu.windea.pls.lang.hierarchy.call.ParadoxCallHierarchyBrowserSettings) = XmlSerializerUtil.copyBean(state, this)
    
    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<icu.windea.pls.lang.hierarchy.call.ParadoxCallHierarchyBrowserSettings>()
    }
}
