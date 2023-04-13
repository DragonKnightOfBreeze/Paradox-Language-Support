package icu.windea.pls.core.hierarchy.call

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.*
import icu.windea.pls.core.hierarchy.*

@State(name = "ParadoxCallHierarchyBrowserSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ParadoxCallHierarchyBrowserSettings : PersistentStateComponent<ParadoxCallHierarchyBrowserSettings>, ParadoxHierarchyBrowserSettings {
    override var scopeType: String = "all"
    var showScriptedVariables: Boolean = true
    var showDefinitions: Boolean = true
    
    override fun getState() = this
    
    override fun loadState(state: ParadoxCallHierarchyBrowserSettings) = XmlSerializerUtil.copyBean(state, this)
    
    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<ParadoxCallHierarchyBrowserSettings>()
    }
}
