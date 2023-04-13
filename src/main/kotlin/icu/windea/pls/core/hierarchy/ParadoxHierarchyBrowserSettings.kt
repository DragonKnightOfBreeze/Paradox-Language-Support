package icu.windea.pls.core.hierarchy

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import com.intellij.util.xmlb.*
import com.intellij.util.xmlb.annotations.*

@State(name = "ParadoxHierarchyBrowserSettings", storages = [Storage(StoragePathMacros.WORKSPACE_FILE)])
class ParadoxHierarchyBrowserSettings: PersistentStateComponent<ParadoxHierarchyBrowserSettings> {
    @XMap
    var scopeTypes: MutableMap<String, String> = mutableMapOf()
    @XMap
    var nodeTypes: MutableMap<String, String> = mutableMapOf()
    
    override fun getState() = this
    
    override fun loadState(state: ParadoxHierarchyBrowserSettings) = XmlSerializerUtil.copyBean(state, this)
    
    companion object {
        @JvmStatic
        fun getInstance(project: Project) = project.service<ParadoxHierarchyBrowserSettings>()
        
        const val DEFINITION = "DEFINITION"
        const val CALL = "CALL"
    }
}